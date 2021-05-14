package com.foxpoint.chattery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLInputPeerUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.auth.TLAuthorization;
import com.github.badoualy.telegram.tl.api.auth.TLSentCode;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Random;

import static com.foxpoint.chattery.GameUtils.getInputPeer;

public class LoginUtils extends AsyncTask<JSONObject, Void, JSONObject>
{
    public static JSONObject RequestTelegramCode(JSONObject... obj)
    {
        JSONObject resJson = null;
        try {
            resJson = new JSONObject().put("error", "error");
            Activity activity = (Activity) obj[0].get("activity");
            String number = obj[0].getString("number");
            TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
            TelegramClient client = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));

            TLSentCode sentCode = client.authSendCode(false, number, true);
            resJson.remove("error");
            return resJson.put("sentCode", sentCode);
        } catch (JSONException | RpcErrorException | IOException e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return resJson;
    }

    public static JSONObject TelegramAuth(JSONObject... obj)
    {
        TelegramClient client = null;
        String password = "";
        JSONObject resJson = new JSONObject();
        try {
            resJson = new JSONObject().put("error", "error");
            Activity activity = (Activity) obj[0].get("activity");
            String number = obj[0].getString("number");
            String code = obj[0].getString("code");
            password = obj[0].getString("password");
            TLSentCode sentCode = (TLSentCode) obj[0].get("sentCode");

            TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
            client = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));

            int clientID = client.authSignIn(number, sentCode.getPhoneCodeHash(), code).getUser().getId();

            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) == null)
            {
                GameSettings gameSettings = new GameSettings();
                gameSettings.TELEGRAM_CLIENT_ID = clientID;
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
            else
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                gameSettings.TELEGRAM_CLIENT_ID = clientID;
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }

            resJson.remove("error");
        } catch (JSONException | RpcErrorException | IOException e) {
            if(e.getMessage().equals("401: SESSION_PASSWORD_NEEDED"))
            {
                try
                {
                    TLUser self = Objects.requireNonNull(client).authCheckPassword(password).getUser().getAsUser();
                    Activity activity = (Activity) obj[0].get("activity");

                    SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
                    if(pref.getString("GAME_SETTINGS", null) == null)
                    {
                        GameSettings gameSettings = new GameSettings();
                        gameSettings.TELEGRAM_CLIENT_ID = self.getId();
                        pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
                    }
                    else
                    {
                        GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                        gameSettings.TELEGRAM_CLIENT_ID = self.getId();
                        pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
                    }

                    resJson.remove("error");
                    Log.i("MyLog", "You are now signed in as " + self.getFirstName() + " " + self.getLastName() + " @" + self.getUsername());
                    return resJson.put("ok","ok");
                }
                catch (JSONException | RpcErrorException | IOException rpcErrorException)
                {
                    Log.e("MyLog", Log.getStackTraceString(e));
                }
            }
            else
            {
                Log.e("MyLog", Log.getStackTraceString(e));
                return resJson;
            }
        }
        return resJson;
    }

    public static JSONObject CheckAccounts(JSONObject... obj)
    {
        JSONObject resJson = null;
        try
        {
            resJson = new JSONObject().put("telegram",true).put("vk",true);
            Activity activity = (Activity) obj[0].get("activity");

            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);

            if(gameSettings == null) return resJson;

            if(gameSettings.TELEGRAM_ACCESS_TOKEN != null)
            {
                TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
                TelegramClient telegramClient = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));;

                telegramClient.accountGetAuthorizations();
                resJson.put("telegram",false);
            }
            else { resJson.put("telegram",false); }

            resJson.put("vk", false);
        }
        catch (JSONException | RpcErrorException | IOException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));;
        }
        return resJson;
    }

    public static Integer GetTelegramDialogsAmount(JSONObject... obj)
    {
        try
        {
            Activity activity = (Activity) obj[0].get("activity");
            Integer dialogsAmount = 0;

            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);

            if(gameSettings == null) return 0;

            if(gameSettings.TELEGRAM_ACCESS_TOKEN != null)
            {
                TelegramApp application = new TelegramApp(Constants.TELEGRAM_API_ID, Constants.TELEGRAM_API_HASH, "MODEL", "SYSTEM_VERSION", "1", "en");
                TelegramClient telegramClient = Kotlogram.getDefaultClient(application, new ApiStorageTelegram(activity));

                TLAbsDialogs dialogs = telegramClient.messagesGetDialogs(false, 0, 0, new TLInputPeerEmpty(), 100);

                for(int i = 0; i < dialogs.getDialogs().size(); i++)
                {
                    TLAbsInputPeer inputPeer_ = getInputPeer(dialogs, i);
                    if(inputPeer_.getClass() == TLInputPeerUser.class) dialogsAmount++;
                }
            }
            else return 0;

            return dialogsAmount;
        }
        catch (JSONException | RpcErrorException | IOException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));;
        }
        return 0;
    }

    public static void LoginVk(JSONObject... obj)
    {
        try {
            Activity activity = (Activity) obj[0].get("activity");

        } catch (JSONException e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
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
