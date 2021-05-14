package com.foxpoint.chattery

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_reg_menu.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.view.*
import kotlinx.android.synthetic.main.reg_telegram_dialog.*
import kotlinx.android.synthetic.main.settings_page.*
import kotlinx.android.synthetic.main.settings_page.view.*
import kotlinx.android.synthetic.main.telegram_login_button.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class Settings : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
    private var dialogShowed = false;

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
        //---------------------------------------------------------
        var pageAdapter = MyFragmentPagerAdapter(supportFragmentManager)
        viewPager.adapter = pageAdapter
        launch {
            DrawRecycler()
        }
    }

    fun DrawRecycler()
    {
        val tgDialogs = GameUtils().execute(JSONObject().put("function", "GetTelegramDialogs").put("activity", this)).get().get("res") as HashMap<*, *>

        if(tgDialogs.isNotEmpty())
        {
            viewPager.get(0).disable_acc_btn.visibility = View.VISIBLE
            viewPager.get(0).settings_progress_bar.visibility = View.GONE
            var recyclerAdapter = SettingsRecyclerViewAdapter(this, "telegram", toArray<String>(tgDialogs.values), toArray<Int>(tgDialogs.keys))
            (viewPager.get(0).recycler as RecyclerView).adapter = recyclerAdapter
            (viewPager.get(0).recycler as RecyclerView).layoutManager = LinearLayoutManager(this)
            viewPager.get(0).disable_acc_btn.setOnClickListener {
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
        }
        else
        {
            viewPager.get(0).settings_progress_bar.visibility = View.GONE
            layoutInflater.inflate(R.layout.telegram_login_button, viewPager.get(0).main_layout, true)
            viewPager.get(0).telegram_btn.setOnClickListener {
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
                    viewPager.get(0).main_layout.removeView(telegram_btn)
                    alertDialog.dismiss()
                    DrawRecycler()
                }
            }
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

    private class MyFragmentPagerAdapter(fm: FragmentManager?) :
        FragmentPagerAdapter(fm!!) {
        val PAGE_COUNT = 1;
        override fun getItem(position: Int): Fragment {
            return SettingsPage.newInstance(position)
        }

        override fun getCount(): Int {
            return PAGE_COUNT
        }
    }
}

