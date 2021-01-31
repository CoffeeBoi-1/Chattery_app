package com.foxpoint.chattery

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class Main_menu : AppCompatActivity() {

    var dialogShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        val decorView=window.decorView;
        decorView.systemUiVisibility= (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        logo_img.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_up_slide)
        enter_name_edittext.animation=AnimationUtils.loadAnimation(
            this,
            R.anim.anim_fade_left_slide
        )
        host_game_btn.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_right_slide)
        join_game_btn.animation=AnimationUtils.loadAnimation(this, R.anim.anim_fade_down_slide)
        //---------------------------------------------------------
        var queryData:Uri? = intent.data
        var authMethod = queryData?.getQueryParameter("authMethod").toString()

        when(authMethod)
        {
            "discord" -> {
                if (queryData?.getQueryParameter("error") == null) {
                    DiscordUtils.GetAndSaveAccessToken(
                        this,
                        queryData?.getQueryParameter("code").toString()
                    )
                } else {
                    startActivity(Intent(this@Main_menu, Reg_menu::class.java))
                    finish()
                }
            }
        }

        join_game_btn.setOnClickListener {
            ShowDialog(R.layout.join_existing_game_dialog)
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

        startScanner(alertDialog)
    }

    private fun startScanner(alertDialog: AlertDialog)
    {
        var scannerView = alertDialog.scannerView
        var mScannerView = ZXingScannerView(alertDialog.context)
        if(scannerView.findViewWithTag<ZXingScannerView>("mScannerView") != null) scannerView.removeView(scannerView.findViewWithTag<ZXingScannerView>("mScannerView"))
        mScannerView.tag = "mScannerView"
        scannerView.addView(mScannerView)
        mScannerView.startCamera();
        mScannerView.setResultHandler { rawResult ->
            var text = rawResult.text

            if(text.length<4)
            {
                Toast.makeText(this, R.string.incorrect_qr_code, Toast.LENGTH_SHORT).show();
                val handler = Handler()
                handler.postDelayed({startScanner(alertDialog)},2000)
                return@setResultHandler
            }

            if(text.slice(0..3) != "CHR$")
            {
                Toast.makeText(this, R.string.incorrect_qr_code, Toast.LENGTH_SHORT).show();
                val handler = Handler()
                handler.postDelayed({startScanner(alertDialog)},2000)
                return@setResultHandler
            }

            var joinCode = text.substring(4)
        }
    }
}
