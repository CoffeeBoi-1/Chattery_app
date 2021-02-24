package com.foxpoint.chattery;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SessionObject {
    Socket socket;
    String sessionID;
    public SessionObject(Socket socket, String sessionID)
    {
        this.socket = socket;
        this.sessionID = sessionID;

        OnCreate();
    }

    private void OnCreate()
    {
        socket.connect();
    }
}
