package com.foxpoint.chattery;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.HashMap;

public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final Context context;
    private final String names[];
    private final Integer ids[];
    private final String social;

    public SettingsRecyclerViewAdapter(Context context, String social, String[] names, Integer[] ids)
    {
        this.context = context;
        this.names = names;
        this.ids = ids;
        this.social = social;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.settings_fragment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SharedPreferences pref = context.getSharedPreferences("DATA", Context.MODE_PRIVATE);
        Button btn = ((Button)holder.itemView.findViewById(R.id.main_layout));
        if(social == "telegram")
        {
            btn.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_check, context.getTheme()), null, context.getResources().getDrawable(R.drawable.ic_telegram_logo, context.getTheme()), null);
            if(pref.getString("GAME_SETTINGS", null) != null)
            {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                if(gameSettings.TELEGRAM_BLACKLIST != null && gameSettings.TELEGRAM_BLACKLIST.containsKey(ids[position])) btn.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_clear, context.getTheme()), null, context.getResources().getDrawable(R.drawable.ic_telegram_logo, context.getTheme()), null);
            }
            btn.setText(names[position]);
            btn.setOnClickListener(v -> {
                GameSettings gameSettings = new Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings.class);
                if(gameSettings.TELEGRAM_BLACKLIST == null) gameSettings.TELEGRAM_BLACKLIST = new HashMap<Integer, String>();

                if(!gameSettings.TELEGRAM_BLACKLIST.containsKey(ids[position]))
                {
                    gameSettings.TELEGRAM_BLACKLIST.put(ids[position], "");
                    pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
                    btn.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_clear, context.getTheme()), null, context.getResources().getDrawable(R.drawable.ic_telegram_logo, context.getTheme()), null);
                }
                else
                {
                    gameSettings.TELEGRAM_BLACKLIST.remove(ids[position]);
                    pref.edit().putString("GAME_SETTINGS", new Gson().toJson(gameSettings)).apply();
                    btn.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.ic_check, context.getTheme()), null, context.getResources().getDrawable(R.drawable.ic_telegram_logo, context.getTheme()), null);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        Button main_layout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            main_layout = itemView.findViewById(R.id.main_layout);
        }
    }
}
