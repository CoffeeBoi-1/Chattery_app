package com.foxpoint.chattery;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SessionObject {
    Socket socket;
    String sessionID;
    String password;
    String nickname;
    GameSettings gameSettings;
    List<String> nicknames = new ArrayList<String>();
    Emitter.Listener playerConnected;
    Emitter.Listener joinSessionResponse;
    Emitter.Listener qrCode;
    Emitter.Listener playerDisconnected;
    Emitter.Listener sessionClose;
    Emitter.Listener gameReady;
    Emitter.Listener newDialog;
    boolean isWaitingStage = true;

    public SessionObject(Socket socket, GameSettings gameSettings, String sessionID, String password, String nickname, Emitter.Listener joinSessionResponse,
                         Emitter.Listener playerConnected, Emitter.Listener qrCode, Emitter.Listener playerDisconnected, Emitter.Listener sessionClose, Emitter.Listener gameReady, Emitter.Listener newDialog)
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
        this.nickname = nickname;
        this.gameSettings = gameSettings;

        OnCreate();
    }

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
            socket.emit("join_session", new JSONObject().put("sessionID",sessionID).put("password",password).put("nickname", nickname));
        }
        catch (JSONException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }
}
