package com.foxpoint.chattery

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.activity_main_menu.join_game_btn
import kotlinx.android.synthetic.main.game_info_panel.*
import kotlinx.android.synthetic.main.game_info_panel.view.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import java.lang.StringBuilder


class Main_menu : AppCompatActivity() {

    var joinDialogShowed = false
    var hostDialogShowed = false
    var createGameSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val decorView=window.decorView
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        RequirePermission()
        logo_img.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_up_slide)
        enter_name_edittext.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_left_slide)
        host_game_btn.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_right_slide)
        join_game_btn.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_left_slide)
        settings_btn.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_down_slide)
        //---------------------------------------------------------
        settings_btn.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
            Animatoo.animateFade(this)
        }
        host_game_btn.setOnClickListener {
            var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
            val gameSettings : GameSettings = Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings::class.java)
            if(enter_name_edittext.text.length < Constants.MIN_NAME_LENGTH)
            {
                enter_name_edittext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_shake_wrong))
                return@setOnClickListener
            }

            val resJsonAcc = LoginUtils().execute(JSONObject().put("function", "CheckAccounts").put("activity", this)).get().get("res") as JSONObject
            val resJsonDialogsTG = LoginUtils().execute(JSONObject().put("function", "GetTelegramDialogsAmount").put("activity", this)).get().get("res") as Int
            if(resJsonAcc.getBoolean("telegram"))
            {
                Toast.makeText(this, resources.getString(R.string.telegram_token_not_working), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(!resJsonAcc.getBoolean("telegram") && resJsonDialogsTG - gameSettings.TELEGRAM_BLACKLIST.size < Constants.MIN_DIALOGS_AMOUNT)
            {
                Toast.makeText(this, resources.getString(R.string.not_enough_dialogs_tg), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            ShowHostDialog(R.layout.host_game_dialog)
        }
        join_game_btn.setOnClickListener {
            var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
            val gameSettings : GameSettings = Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings::class.java)
            if(enter_name_edittext.text.length < Constants.MIN_NAME_LENGTH)
            {
                enter_name_edittext.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_shake_wrong))
                return@setOnClickListener
            }

            val resJsonAcc = LoginUtils().execute(JSONObject().put("function", "CheckAccounts").put("activity", this)).get().get("res") as JSONObject
            val resJsonDialogsTG = LoginUtils().execute(JSONObject().put("function", "GetTelegramDialogsAmount").put("activity", this)).get().get("res") as Int
            if(resJsonAcc.getBoolean("telegram"))
            {
                Toast.makeText(this, resources.getString(R.string.telegram_token_not_working), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(!resJsonAcc.getBoolean("telegram") && resJsonDialogsTG - gameSettings.TELEGRAM_BLACKLIST.size < Constants.MIN_DIALOGS_AMOUNT)
            {
                Toast.makeText(this, resources.getString(R.string.not_enough_dialogs_tg), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            ShowJoinDialog(R.layout.join_existing_game_dialog)
        }
    }

    private fun RequirePermission()
    {
        if(applicationContext.checkCallingOrSelfPermission(android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
            applicationContext.checkCallingOrSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, Array<String>(2){android.Manifest.permission.INTERNET; android.Manifest.permission.CAMERA},1)
        }
    }

    private fun ShowHostDialog(layout: Int)
    {
        try {
            if (hostDialogShowed ) return
            hostDialogShowed  = true
            var dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setView(layout)
            var alertDialog = dialogBuilder.create()
            alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            alertDialog.setOnDismissListener {hostDialogShowed  = false}
            alertDialog.show()

            val playersCounterTextView = alertDialog.window?.findViewById<TextView>(R.id.players_counter)
            val seekBarPlayers = alertDialog.window?.findViewById<SeekBar>(R.id.seek_bar_players)
            var playersCounter:Int? = seekBarPlayers?.min
            seekBarPlayers?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    playersCounterTextView?.text = progress.toString()
                    playersCounter = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {    }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {     }
            })
            alertDialog.window?.findViewById<Button>(R.id.host_game_btn)?.setOnClickListener {
                if (createGameSent) return@setOnClickListener
                createGameSent = true
                var resJson : JSONObject = ServerUtils().execute(JSONObject().put("function", "CreateSession")
                    .put("playersAmount", playersCounter)).get().get("res") as JSONObject
                if(resJson.has("error"))
                {
                    Toast.makeText(this, R.string.host_game_error, Toast.LENGTH_LONG).show()
                    createGameSent = false
                    return@setOnClickListener
                }
                val intent = Intent(this, Session::class.java)
                intent.putExtra("sessionID", resJson.getString("sessionID"))
                intent.putExtra("password", resJson.getString("password"))
                intent.putExtra("nickname", enter_name_edittext.text.toString())
                startActivity(intent)
                Animatoo.animateFade(this)
                finish()
            }
        }
        catch (e: Exception)
        {
            Log.e("MyLog", Log.getStackTraceString(e))
        }
    }

    private fun ShowJoinDialog(layout: Int)
    {
        if (joinDialogShowed) return
        joinDialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {joinDialogShowed = false}
        alertDialog.show()

        StartScanner(alertDialog)
    }

    private fun StartScanner(alertDialog: AlertDialog)
    {
        var scannerView = alertDialog.scannerView
        var mScannerView = ZXingScannerView(alertDialog.context)
        if(scannerView.findViewWithTag<ZXingScannerView>("mScannerView") != null) scannerView.removeView(
            scannerView.findViewWithTag<ZXingScannerView>(
                "mScannerView"
            )
        )
        mScannerView.tag = "mScannerView"
        scannerView.addView(mScannerView)
        mScannerView.startCamera()
        mScannerView.setResultHandler { rawResult ->
            var text = rawResult.text
            try {
                if(text.length<4)
                {
                    Toast.makeText(this, R.string.incorrect_qr_code, Toast.LENGTH_SHORT).show()
                    val handler = Handler()
                    handler.postDelayed({ StartScanner(alertDialog) }, 2000)
                    return@setResultHandler
                }

                if(text.slice(0..3) != "CHR$")
                {
                    Toast.makeText(this, R.string.incorrect_qr_code, Toast.LENGTH_SHORT).show()
                    val handler = Handler()
                    handler.postDelayed({ StartScanner(alertDialog) }, 2000)
                    return@setResultHandler
                }

                var sessionID : String = JSONObject(text.substring(4)).get("sessionID") as String
                var password : String = JSONObject(text.substring(4)).get("password") as String

                var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
                val gameSettings : GameSettings = Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings::class.java)

                var resJsonSessionInfo : JSONObject = ServerUtils().execute(JSONObject()
                    .put("function", "GetSessionInfo")
                    .put("sessionID", sessionID).put("password", password)
                    .put("nickname", enter_name_edittext.text.toString())).get().get("res") as JSONObject
                if(resJsonSessionInfo.has("error"))
                {
                    when(resJsonSessionInfo.getString("error"))
                    {
                        "invalid_session"->{
                            Toast.makeText(this, R.string.session_not_exists, Toast.LENGTH_LONG).show()
                            val handler = Handler()
                            handler.postDelayed({ StartScanner(alertDialog) }, 2000)
                            return@setResultHandler
                        }
                        "invalid_nickname"->{
                            Toast.makeText(this, R.string.nickname_unavailable, Toast.LENGTH_LONG).show()
                            val handler = Handler()
                            handler.postDelayed({ StartScanner(alertDialog) }, 2000)
                            return@setResultHandler
                        }
                    }
                }

                var gameInfoPanel = layoutInflater.inflate(R.layout.game_info_panel, alertDialog.main_layout, false)
                gameInfoPanel.players_amount.text = StringBuilder().append(gameInfoPanel.players_amount.text).append(resJsonSessionInfo.get("playersAmount").toString()).toString()
                alertDialog.main_layout.addView(gameInfoPanel)
                alertDialog.join_game_btn.setOnClickListener {
                    val intent = Intent(this, Session::class.java)
                    intent.putExtra("sessionID", sessionID)
                    intent.putExtra("password", password)
                    intent.putExtra("nickname", enter_name_edittext.text.toString())
                    startActivity(intent)
                    Animatoo.animateFade(this)
                    finish()
                }
            }
            catch (e: Exception)
            {
                Log.e("MyLog", Log.getStackTraceString(e))
                Toast.makeText(this, R.string.incorrect_qr_code, Toast.LENGTH_SHORT).show()
                val handler = Handler()
                handler.postDelayed({ StartScanner(alertDialog) }, 2000)
                return@setResultHandler
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val decorView=window.decorView
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
        val decorView=window.decorView
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            1 ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) { }
                else {
                    RequirePermission()
                }
                return
            }
        }
    }

}
