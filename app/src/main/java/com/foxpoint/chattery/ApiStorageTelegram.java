package com.foxpoint.chattery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApiStorage;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.mtproto.auth.AuthKey;
import com.github.badoualy.telegram.mtproto.model.DataCenter;
import com.github.badoualy.telegram.mtproto.model.MTSession;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import java.io.IOException;

public class ApiStorageTelegram implements TelegramApiStorage
{
    Activity activity;
    public ApiStorageTelegram(Activity activity)
    {
        this.activity = activity;
    }

    @Override
    public void deleteAuthKey() {
        try
        {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) != null)
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                gameSettings.TELEGRAM_ACCESS_TOKEN = new byte[0];
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }

    @Override
    public void deleteDc() {
        try
        {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) != null)
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                gameSettings.TELEGRAM_DATA_CENTER = "";
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }

    @Nullable
    @Override
    public AuthKey loadAuthKey() {
        try
        {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) == null) return null;
            else
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                return new AuthKey(gameSettings.TELEGRAM_ACCESS_TOKEN);
            }
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    @Override
    public DataCenter loadDc() {
        try
        {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) == null) return null;
            else
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                String[] info = gameSettings.TELEGRAM_DATA_CENTER.split(":");
                return new DataCenter(info[0], Integer.parseInt(info[1]));
            }
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    @Override
    public MTSession loadSession() {
        return null;
    }

    @Override
    public void saveAuthKey(@NotNull AuthKey authKey) {
        try {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) == null)
            {
                GameSettings gameSettings = new GameSettings();
                gameSettings.TELEGRAM_ACCESS_TOKEN = authKey.getKey();
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
            else
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                gameSettings.TELEGRAM_ACCESS_TOKEN = authKey.getKey();
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
        } catch (Exception e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }

    @Override
    public void saveDc(@NotNull DataCenter dataCenter) {
        try {
            SharedPreferences pref = activity.getSharedPreferences("DATA", Context.MODE_PRIVATE);
            if(pref.getString("GAME_SETTINGS", null) == null)
            {
                GameSettings gameSettings = new GameSettings();
                gameSettings.TELEGRAM_DATA_CENTER = dataCenter.toString();
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
            else
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                gameSettings.TELEGRAM_DATA_CENTER = dataCenter.toString();
                pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
            }
        } catch (Exception e) {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
    }

    @Override
    public void saveSession(@Nullable MTSession mtSession) {

    }
}
