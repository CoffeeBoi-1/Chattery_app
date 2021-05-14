package com.foxpoint.chattery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsMessageAction;
import com.github.badoualy.telegram.tl.api.TLAbsPeer;
import com.github.badoualy.telegram.tl.api.TLAbsUser;
import com.github.badoualy.telegram.tl.api.TLChannel;
import com.github.badoualy.telegram.tl.api.TLChannelForbidden;
import com.github.badoualy.telegram.tl.api.TLChat;
import com.github.badoualy.telegram.tl.api.TLChatEmpty;
import com.github.badoualy.telegram.tl.api.TLChatForbidden;
import com.github.badoualy.telegram.tl.api.TLDialog;
import com.github.badoualy.telegram.tl.api.TLInputPeerChannel;
import com.github.badoualy.telegram.tl.api.TLInputPeerChat;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLInputPeerUser;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLMessageService;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.core.TLObject;
import com.github.badoualy.telegram.tl.core.TLVector;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Random;

public class GameUtils extends AsyncTask<JSONObject, Void, JSONObject>
{
    public static JSONArray GetTelegramMessages(JSONObject... obj)
    {
        JSONArray resJson = null;
        try {
            resJson = new JSONArray().put("error");
            Activity activity = (Activity) obj[0].get("activity");
            TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
            TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));
            TLAbsDialogs dialogs = client.messagesGetDialogs(false, 0, 0, new TLInputPeerEmpty(), 100);
            TLAbsInputPeer inputPeer;

            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);

            while (true)
            {
                int randKey = new Random().nextInt(dialogs.getDialogs().size());
                TLAbsInputPeer inputPeer_ = getInputPeer(dialogs, randKey);
                if(inputPeer_.getClass() == TLInputPeerUser.class && !gameSettings.TELEGRAM_BLACKLIST.containsKey(((TLInputPeerUser) inputPeer_).getUserId()))
                {
                    inputPeer = inputPeer_;
                    break;
                }
                dialogs.getDialogs().remove(randKey);
            }

            TLAbsMessages messages = client.messagesGetHistory(inputPeer, 0, 0, 0, 5000, 0, 0);
            int messagesAmount = 0;
            int messagesID = 0;
            JSONArray dialog = new JSONArray();
            if(messages.getMessages().size() < Constants.MAX_MESSAGES_AMOUNT)
            {
                messagesAmount = messages.getMessages().size() - 1;
                messagesID = messages.getMessages().size() - 1;
            }
            if(messages.getMessages().size() >= Constants.MAX_MESSAGES_AMOUNT)
            {
                messagesAmount = Constants.MAX_MESSAGES_AMOUNT - 1;
                messagesID = new Random().nextInt(messages.getMessages().size() - 14) + 14;
            }
            if(messages.getMessages().size() == 0) return resJson;

            for(int i = messagesAmount; i >= 0; i--)
            {
                if (messages.getMessages().get(messagesID) instanceof TLMessage)
                {
                    String messageSender = "me";
                    if(((TLMessage) messages.getMessages().get(messagesID)).getFromId() != gameSettings.TELEGRAM_CLIENT_ID) messageSender = "partner";

                    if(!((TLMessage) messages.getMessages().get(messagesID)).getMessage().isEmpty())
                    {
                        dialog.put(new JSONObject().put("text", ((TLMessage) messages.getMessages().get(messagesID)).getMessage()).put("from", messageSender));
                    }
                }
                messagesID--;
            }
            return dialog;
        } catch (JSONException | RpcErrorException | IOException e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return resJson;
    }

    public static HashMap<Integer, String> GetTelegramDialogs(JSONObject... obj)
    {
        HashMap<Integer, String> res = new HashMap<>();
        try {
            Activity activity = (Activity) obj[0].get("activity");
            TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
            TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));
            TLAbsDialogs dialogs = client.messagesGetDialogs(false, 0, 0, new TLInputPeerEmpty(), 100);

            return createNameMap(dialogs);
        } catch (JSONException | RpcErrorException | IOException e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return res;
    }

    public static HashMap<Integer, String> createNameMap(TLAbsDialogs tlAbsDialogs) {
        HashMap<Integer, String> nameMapUsers = new HashMap<>();
        HashMap<Integer, String> nameMapDialogs = new HashMap<>();

        tlAbsDialogs.getUsers().stream()
                .map(TLAbsUser::getAsUser)
                .forEach(user -> nameMapUsers.put(user.getId(),
                        user.getFirstName() + (user.getLastName()==null? "":" "+user.getLastName())));

        for(int i = 0; i < tlAbsDialogs.getDialogs().size(); i++)
        {
            TLAbsInputPeer inputPeer = getInputPeer(tlAbsDialogs, i);
            if(inputPeer.getClass() == TLInputPeerUser.class)
            {
                Integer id = ((TLInputPeerUser) inputPeer).getUserId();
                if(nameMapUsers.containsKey(id)) nameMapDialogs.put(id, nameMapUsers.get(id));
            }
        }
        return nameMapDialogs;
    }

    public static int getId(TLAbsPeer peer) {
        if (peer instanceof TLPeerUser)
            return ((TLPeerUser) peer).getUserId();
        if (peer instanceof TLPeerChat)
            return ((TLPeerChat) peer).getChatId();

        return ((TLPeerChannel) peer).getChannelId();
    }

    public static TLAbsInputPeer getInputPeer(TLAbsDialogs tlAbsDialogs, int ID) {
        TLAbsPeer tlAbsPeer = tlAbsDialogs.getDialogs().get(ID).getPeer();
        int peerId = getId(tlAbsPeer);
        TLObject peer = tlAbsPeer instanceof TLPeerUser ?
                tlAbsDialogs.getUsers().stream().filter(user -> user.getId() == peerId).findFirst().get()
                : tlAbsDialogs.getChats().stream().filter(chat -> chat.getId() == peerId).findFirst().get();

        if (peer instanceof TLChannel)
        {
            return new TLInputPeerChannel(((TLChannel) peer).getId(), ((TLChannel) peer).getAccessHash());
        }
        if (peer instanceof TLChat)
        {
            return new TLInputPeerChat(((TLChat) peer).getId());
        }
        if (peer instanceof TLUser)
        {
            return new TLInputPeerUser(((TLUser) peer).getId(), ((TLUser) peer).getAccessHash());
        }

        return new TLInputPeerEmpty();
    }

    @Override
    protected JSONObject doInBackground(JSONObject... obj)
    {
        JSONObject resJson = null;
        try {
            String functionName = obj[0].get("function").toString();
            Object res = getClass().getMethod(functionName, obj.getClass()).invoke(new Object(), (Object)obj);
            resJson = new JSONObject().put("res", res);
        } catch (JSONException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return resJson;
    }

    @Override
    protected void onPostExecute(JSONObject obj)
    {
        super.onPostExecute(obj);
    }
}
