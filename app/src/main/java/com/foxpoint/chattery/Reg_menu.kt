package com.foxpoint.chattery

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reg_menu.*
import kotlinx.android.synthetic.main.reg_telegram_dialog.*
import org.json.JSONObject


class Reg_menu : AppCompatActivity() {
    private var dialogShowed = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg_menu)
        val decorView=window.decorView;
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        choose_login_method_text.animation = AnimationUtils.loadAnimation(
            this,
            R.anim.anim_fade_up_slide
        )
        vk_btn.animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_right_slide)
        telegram_btn.animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_down_slide)
        //---------------------------------------------------------------------------------------------
        vk_btn.setOnClickListener {
            val pref: SharedPreferences = getSharedPreferences("DATA", MODE_PRIVATE)
            val gameSettings = GameSettings()
            gameSettings.VK_ACCESS_TOKEN = "111"
            pref.edit().putString("GAME_SETTINGS", Gson().toJson(gameSettings)).apply()
            startActivity(Intent(this, Main_menu::class.java))
            Animatoo.animateFade(this)
            finish()
        }
        telegram_btn.setOnClickListener {
            ShownDialog(R.layout.reg_telegram_dialog)
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
                    startActivity(Intent(this, Main_menu::class.java))
                    Animatoo.animateFade(this)
                    finish()
                }
            }
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
