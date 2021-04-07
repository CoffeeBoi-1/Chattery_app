package com.foxpoint.chattery

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.gson.Gson
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.main_layout
import kotlinx.android.synthetic.main.leave_game_dialog.*
import kotlinx.android.synthetic.main.session_waiting_players.*
import kotlinx.android.synthetic.main.session_waiting_players.view.*
import net.glxn.qrgen.android.QRCode
import org.json.JSONObject
import java.lang.StringBuilder


class Session : AppCompatActivity() {
    lateinit var SESSION_OBJECT : SessionObject
    lateinit var mSocket : Socket
    var dialogShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        val decorView=window.decorView;
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        //---------------------------------------------------------
        mSocket = ServerUtils().execute(JSONObject().put("function", "GetSessionSocket")).get().get("res") as Socket
        var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
        val gameSettings : GameSettings = Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings::class.java)
        SESSION_OBJECT = SessionObject(mSocket, gameSettings, intent.getStringExtra("sessionID"), intent.getStringExtra("password"), intent.getStringExtra("nickname"),
            { args -> run {
                //joinSessionResponse
                if(!SESSION_OBJECT.isWaitingStage) return@run
                this.waiting_players_panel.players_amount.text = StringBuilder().append(JSONObject(args?.get(0).toString()).get("currentPlayersAmount"))
                    .append("/")
                    .append(JSONObject(args?.get(0).toString()).get("playersAmount")) }},

            { args -> run {
                //playerConnected
                if(!SESSION_OBJECT.isWaitingStage) return@run
                this.waiting_players_panel.players_amount.text = StringBuilder().append(JSONObject(args?.get(0).toString()).get("currentPlayersAmount"))
                    .append("/")
                    .append(JSONObject(args?.get(0).toString()).get("playersAmount"))

                SESSION_OBJECT.nicknames.add(JSONObject(args?.get(0).toString()).get("nickname") as String)
            }},

            { args -> runOnUiThread {
                //qrCode
                if(!SESSION_OBJECT.isWaitingStage) return@runOnUiThread
                var qrCode = QRCode.from(args?.get(0).toString()).withSize(1000, 1000).withColor(Color.BLACK, Color.TRANSPARENT).bitmap()
                this.waiting_players_panel.qr_code_progress_bar.visibility = View.GONE
                this.waiting_players_panel.qr_code.setImageBitmap(qrCode) }},

            { args -> run {
                //playerDisconnected
                if(!SESSION_OBJECT.isWaitingStage) return@run
                this.waiting_players_panel.players_amount.text = StringBuilder().append(JSONObject(args?.get(0).toString()).get("currentPlayersAmount"))
                    .append("/")
                    .append(JSONObject(args?.get(0).toString()).get("playersAmount"))
            }},

            { args -> run {
                //sessionClose
                val intent = Intent(this, Main_menu::class.java)
                startActivity(intent)
                Animatoo.animateFade(this)
                finish()
            }},

            { args -> runOnUiThread {
                //gameReady
                if(!SESSION_OBJECT.isWaitingStage) return@runOnUiThread
                SESSION_OBJECT.isWaitingStage = false
                waiting_players_panel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_dialog_close))
                this.main_layout.removeView(waiting_players_panel)

                var gamePanel = layoutInflater.inflate(R.layout.session_game_stage, this.main_layout, false)
                gamePanel.animation = AnimationUtils.loadAnimation(this, R.anim.anim_dialog_open)
                gamePanel.setBackgroundColor(Color.TRANSPARENT)
                this.main_layout.addView(gamePanel)
            }},

            { args -> runOnUiThread {
                //newDialog
            }})

        var waitingPlayersPanel = layoutInflater.inflate(R.layout.session_waiting_players, this.main_layout, false)
        this.main_layout.addView(waitingPlayersPanel)
    }

    override fun onBackPressed() {
        ShowDialog(R.layout.leave_game_dialog)
    }

    private fun ShowDialog(layout: Int)
    {
        if (dialogShowed) return
        dialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {dialogShowed = false}
        alertDialog.show()
        alertDialog.yes_btn.setOnClickListener {
            mSocket.disconnect()
            val intent = Intent(this, Main_menu::class.java)
            startActivity(intent)
            Animatoo.animateFade(this)
            finish()
        }
        alertDialog.no_btn.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        val decorView=window.decorView;
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val decorView=window.decorView;
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }
}