package com.foxpoint.chattery

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.leave_game_dialog.*
import kotlinx.android.synthetic.main.reg_telegram_dialog.*
import kotlinx.android.synthetic.main.telegram_login_button.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class Settings : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    private var dialogShowed = false;
    private var disableDialogShowed = false
    lateinit var buttonClickSoundMP : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val decorView=window.decorView
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        buttonClickSoundMP = MediaPlayer.create(this, R.raw.button_click_sound)
        //---------------------------------------------------------
        launch {
            DrawRecycler()
        }
    }

    fun DrawRecycler()
    {
        val tgDialogs = GameUtils().execute(JSONObject().put("function", "GetTelegramDialogs").put("activity", this)).get().get("res") as HashMap<*, *>

        if(tgDialogs.isNotEmpty())
        {
            disable_acc_btn.visibility = View.VISIBLE
            settings_progress_bar.visibility = View.GONE
            var recyclerAdapter = SettingsRecyclerViewAdapter(this, "telegram", toArray<String>(tgDialogs.values), toArray<Int>(tgDialogs.keys))
            (recycler as RecyclerView).adapter = recyclerAdapter
            (recycler as RecyclerView).layoutManager = LinearLayoutManager(this)
            disable_acc_btn.setOnClickListener {
                buttonClickSoundMP.start()
                ShowDisableAccDialog(R.layout.leave_game_dialog)
            }
        }
        else
        {
            settings_progress_bar.visibility = View.GONE
            layoutInflater.inflate(R.layout.telegram_login_button, main_layout, true)
            telegram_btn.setOnClickListener {
                buttonClickSoundMP.start()
                ShownDialog(R.layout.reg_telegram_dialog)
            }
        }
    }

    private fun ShownDialog(layout: Int)
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

        alertDialog.send_telegram_number_btn.setOnClickListener {
            buttonClickSoundMP.start()
            var number = alertDialog.enter_number_edittext.text.toString()
            var resJsonNumber : JSONObject = LoginUtils().execute(
                JSONObject().put(
                    "function",
                    "RequestTelegramCode"
                ).put("activity", this)
                    .put("number", number)
            )
                .get().get("res") as JSONObject

            if (resJsonNumber.has("error"))
            {
                Toast.makeText(
                    this,
                    resources.getString(R.string.telegram_number_error),
                    Toast.LENGTH_LONG
                ).show()
                alertDialog.dismiss()
            }

            alertDialog.enter_number_edittext.setText("")
            alertDialog.enter_number_edittext.hint = resources.getString(R.string.input_telegram_code)
            alertDialog.send_telegram_number_btn.setOnClickListener {
                buttonClickSoundMP.start()
                var resJsonCode = LoginUtils().execute(
                    JSONObject().put("function", "TelegramAuth")
                        .put("activity", this)
                        .put("number", number)
                        .put("sentCode", resJsonNumber.get("sentCode"))
                        .put("code", alertDialog.enter_number_edittext.text.toString())
                        .put("password", alertDialog.enter_password_edittext.text.toString())
                )
                    .get().get("res") as JSONObject

                if (resJsonCode.has("error"))
                {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.telegram_number_error),
                        Toast.LENGTH_LONG
                    ).show()
                    alertDialog.dismiss()
                }
                else
                {
                    main_layout.removeView(telegram_btn)
                    alertDialog.dismiss()
                    DrawRecycler()
                }
            }
        }
    }

    private fun ShowDisableAccDialog(layout: Int)
    {
        if (disableDialogShowed) return
        disableDialogShowed= true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {disableDialogShowed = false}
        alertDialog.show()

        alertDialog.text.text = resources.getText(R.string.you_sure)
        alertDialog.yes_btn.setOnClickListener {
            buttonClickSoundMP.start()
            var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
            val gameSettings : GameSettings = Gson().fromJson(pref.getString("GAME_SETTINGS", null), GameSettings::class.java)
            gameSettings.TELEGRAM_ACCESS_TOKEN = byteArrayOf()
            gameSettings.TELEGRAM_BLACKLIST = HashMap<Int, String>()
            gameSettings.TELEGRAM_CLIENT_ID = 0
            gameSettings.TELEGRAM_DATA_CENTER = ""
            pref.edit().putString("GAME_SETTINGS", Gson().toJson(gameSettings)).apply()
            startActivity(intent)
            finish()
        }
        alertDialog.no_btn.setOnClickListener {
            buttonClickSoundMP.start()
            alertDialog.dismiss()
        }
    }

    inline fun <reified T> toArray(list: MutableCollection<*>): Array<T> {
        return (list as MutableCollection<T>).toTypedArray()
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
}

