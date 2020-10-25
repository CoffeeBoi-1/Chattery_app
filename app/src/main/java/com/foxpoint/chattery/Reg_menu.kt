package com.foxpoint.chattery

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_reg_menu.*

class Reg_menu : AppCompatActivity() {

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
        choose_login_method_text.animation=AnimationUtils.loadAnimation(this,R.anim.anim_fade_up_slide)
        discord_btn.animation=AnimationUtils.loadAnimation(this,R.anim.anim_fade_left_slide)
        vk_btn.animation=AnimationUtils.loadAnimation(this,R.anim.anim_fade_right_slide)
        telegram_btn.animation=AnimationUtils.loadAnimation(this,R.anim.anim_fade_down_slide)


    }
}
