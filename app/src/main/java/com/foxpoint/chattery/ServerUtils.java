package com.foxpoint.chattery;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.socket.client.IO;
import io.socket.client.Socket;

public class ServerUtils extends AsyncTask<JSONObject, Void, JSONObject>
{
    public static JSONObject GetSessionInfo(JSONObject... obj)
    {
        JSONObject resJson = null;
        try
        {
            String sessionID = obj[0].getString("sessionID");
            String password = obj[0].getString("password");
            String nickname = obj[0].getString("nickname");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(15000, TimeUnit.MILLISECONDS)
                    .connectTimeout(15000, TimeUnit.MILLISECONDS)
                    .writeTimeout(15000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.MAIN_ROUTER_IP + "session_info?obj={'sessionID' : '"+sessionID+"', 'password':'"+password+"', 'nickname':'"+nickname+"'}")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            resJson = new JSONObject(response.body().string());
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return resJson;
    }

    public static JSONObject CreateSession(JSONObject... obj)
    {
        JSONObject resJson = null;
        try
        {
            int playersAmount = obj[0].getInt("playersAmount");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .readTimeout(15000, TimeUnit.MILLISECONDS)
                    .connectTimeout(15000, TimeUnit.MILLISECONDS)
                    .writeTimeout(15000, TimeUnit.MILLISECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.MAIN_ROUTER_IP + "create_session?obj={'playersAmount':'"+playersAmount+"'}")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            Log.i("MyLog", String.valueOf(response.code()));
            if(response.code() != 200) return new JSONObject().put("error","error");
            resJson = new JSONObject(response.body().string());
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
        }
        return resJson;
    }

    public static Socket GetSessionSocket(JSONObject... obj)
    {
        Socket mSocket;
        try
        {
            mSocket = IO.socket(Constants.MAIN_ROUTER_IP);
        }
        catch (URISyntaxException e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
            return null;
        }
        return mSocket;
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
