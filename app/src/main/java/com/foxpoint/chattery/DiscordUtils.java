package com.foxpoint.chattery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mb3364.http.AsyncHttpClient;
import com.mb3364.http.HttpClient;
import com.mb3364.http.RequestParams;
import com.mb3364.http.StringHttpResponseHandler;
import java.util.List;
import java.util.Map;

public class DiscordUtils
{
    public static void GetLoginCode(Context context)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/api/oauth2/authorize?client_id=774368080271900713&redirect_uri=http%3A%2F%2Ffoxpoint.chattery%3FauthMethod%3Ddiscord%26&response_type=code&scope=identify")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    public static void GetAndSaveAccessToken(final Activity activity, final String code)
    {
        RequestParams params = new RequestParams();
        params.put("client_id","774368080271900713");
        params.put("client_secret","iW3VsJ2qU4K11Rmzmpt7knGvdaiB5X7F");
        params.put("grant_type", "authorization_code");
        params.put("code",code);
        params.put("redirect_uri","http://foxpoint.chattery?authMethod=discord&");
        params.put("scope","identify");

        HttpClient client = new AsyncHttpClient();
        client.setHeader("Content-Type","application/x-www-form-urlencoded");

        final Gson g = new Gson();
        SharedPreferences pref = activity.getApplicationContext().getSharedPreferences("DATA", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

        client.post("https://discordapp.com/api/oauth2/token", params, new StringHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Map<String, List<String>> headers, String content) {
                JsonObject contentJson = g.fromJson(content, JsonObject.class);
                editor.putString("discordAccessToken",contentJson.get("access_token").getAsString() );
                editor.apply();
            }

            @Override
            public void onFailure(int statusCode, Map<String, List<String>> headers, String content) {
                Intent intent = new Intent(activity.getApplicationContext(),Reg_menu.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(intent);
                Animatoo.animateFade(activity);
                activity.finish();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Intent intent = new Intent(activity.getApplicationContext(),Reg_menu.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.getApplicationContext().startActivity(intent);
                Animatoo.animateFade(activity);
                activity.finish();
            }
        });
    }
}
