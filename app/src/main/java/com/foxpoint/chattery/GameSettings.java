package com.foxpoint.chattery;

import java.util.HashMap;

public class GameSettings
{
    byte[] TELEGRAM_ACCESS_TOKEN;
    String TELEGRAM_DATA_CENTER;
    HashMap<Integer, String> TELEGRAM_BLACKLIST = new HashMap<Integer, String>();
    int TELEGRAM_CLIENT_ID;
    String VK_ACCESS_TOKEN;

    public GameSettings() { }
}
