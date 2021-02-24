package com.foxpoint.chattery;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.socket.client.IO;
import io.socket.client.Socket;

public class ServerUtils extends AsyncTask<JSONObject, Void, JSONObject>
{
    public static boolean SessionExists(JSONObject... obj)
    {
        try
        {
            String sessionID = obj[0].getString("sessionID");
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Constants.MAIN_ROUTER_IP + "session_exists?obj={'sessionID' : '"+sessionID+"' }")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            String resString = response.body().string();
            Log.i("MyLog",resString);

            if(resString.equals("OK")) return true;
            return false;
        }
        catch (Exception e)
        {
            Log.e("MyLog", Log.getStackTraceString(e));
            e.printStackTrace();
        }
        return false;
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