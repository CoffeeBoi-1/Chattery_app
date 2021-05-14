package com.foxpoint.chattery;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SessionObject implements Parcelable{
    Socket socket;
    String sessionID;
    String password;
    String nickname;
    JSONArray nicknames;
    String[] accounts;
    GameSettings gameSettings;
    Emitter.Listener playerConnected;
    Emitter.Listener joinSessionResponse;
    Emitter.Listener qrCode;
    Emitter.Listener playerDisconnected;
    Emitter.Listener sessionClose;
    Emitter.Listener gameReady;
    Emitter.Listener newDialog;
    Emitter.Listener getDialog;
    Emitter.Listener requestForRound;
    boolean isWaitingStage = true;

    public SessionObject(Socket socket, GameSettings gameSettings, String[] accounts,String sessionID, String password, String nickname, Emitter.Listener joinSessionResponse,
                         Emitter.Listener playerConnected, Emitter.Listener qrCode, Emitter.Listener playerDisconnected, Emitter.Listener sessionClose, Emitter.Listener gameReady, Emitter.Listener newDialog, Emitter.Listener getDialog,
                         Emitter.Listener requestForRound)
    {
        this.socket = socket;
        this.sessionID = sessionID;
        this.password = password;
        this.joinSessionResponse = joinSessionResponse;
        this.playerConnected = playerConnected;
        this.playerDisconnected = playerDisconnected;
        this.qrCode = qrCode;
        this.sessionClose = sessionClose;
        this.gameReady = gameReady;
        this.newDialog = newDialog;
        this.getDialog = getDialog;
        this.requestForRound = requestForRound;
        this.nickname = nickname;
        this.accounts = accounts;
        this.gameSettings = gameSettings;

        OnCreate();
    }

    protected SessionObject(Parcel in) {
        sessionID = in.readString();
        password = in.readString();
        nickname = in.readString();
        accounts = in.createStringArray();
        isWaitingStage = in.readByte() != 0;
    }

    public static final Creator<SessionObject> CREATOR = new Creator<SessionObject>() {
        @Override
        public SessionObject createFromParcel(Parcel in) {
            return new SessionObject(in);
        }

        @Override
        public SessionObject[] newArray(int size) {
            return new SessionObject[size];
        }
    };

    private void OnCreate()
    {
        try
        {
            socket.connect();
            socket.on("join_session_response", joinSessionResponse);
            socket.on("player_connected", playerConnected);
            socket.once("qr_code_data", qrCode);
            socket.on("player_disconnected", playerDisconnected);
            socket.on("session_close", sessionClose);
            socket.on("game_ready", gameReady);
            socket.on("new_dialog", newDialog);
            socket.on("get_dialog", getDialog);
            socket.on("request_for_round", requestForRound);
            socket.emit("join_session", new JSONObject().put("sessionID",sessionID).put("password",password).put("nickname", nickname));
        }
        catch (JSONException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { dest.writeInt(flags); }
}
