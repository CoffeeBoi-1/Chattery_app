package com.foxpoint.chattery;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import io.socket.client.Socket;

public class SessionObject {
    Socket socket;
    String sessionID;
    String password;
    public SessionObject(Socket socket, String sessionID, String password)
    {
        this.socket = socket;
        this.sessionID = sessionID;
        this.password = password;

        OnCreate();
    }

    private void OnCreate()
    {
        try
        {
            socket.connect();
            socket.emit("join_session", new JSONObject().put("sessionID",sessionID).put("password",password));
        }
        catch (JSONException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }
}
