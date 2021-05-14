package com.foxpoint.chattery

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.google.gson.Gson
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.android.synthetic.main.dialog_message.view.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.*
import kotlinx.android.synthetic.main.join_existing_game_dialog.main_layout
import kotlinx.android.synthetic.main.leave_game_dialog.*
import kotlinx.android.synthetic.main.new_round_dialog.*
import kotlinx.android.synthetic.main.restart_game_dialog.*
import kotlinx.android.synthetic.main.session_game_stage.*
import kotlinx.android.synthetic.main.session_game_stage.view.*
import kotlinx.android.synthetic.main.session_waiting_players.*
import kotlinx.android.synthetic.main.session_waiting_players.view.*
import net.glxn.qrgen.android.QRCode
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random


class Session : AppCompatActivity() {
    lateinit var SESSION_OBJECT : SessionObject
    lateinit var mSocket : Socket
    var backPressedDialogShowed = false
    var choiceDialogShowed = false
    var playerSelected = false
    var newRoundDialogShowed = false
    var restartGameDialogShowed = false

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
        if (savedInstanceState != null) return

        mSocket = ServerUtils().execute(JSONObject().put("function", "GetSessionSocket")).get().get("res") as Socket
        var pref = getSharedPreferences("DATA", Context.MODE_PRIVATE)
        val gameSettings : GameSettings = Gson().fromJson(
            pref.getString("GAME_SETTINGS", null),
            GameSettings::class.java
        )
        var accountsArray : ArrayList<String> = ArrayList()
        if (gameSettings.TELEGRAM_ACCESS_TOKEN != null) accountsArray.add("telegram")
        if (gameSettings.VK_ACCESS_TOKEN != null) accountsArray.add("vk")

        SESSION_OBJECT = SessionObject(mSocket,
            gameSettings,
            accountsArray.toTypedArray(),
            intent.getStringExtra(
                "sessionID"
            ),
            intent.getStringExtra("password"),
            intent.getStringExtra("nickname"),
            { args ->
                run {
                    //joinSessionResponse
                    if (!SESSION_OBJECT.isWaitingStage) return@run
                    this.waiting_players_panel.players_amount.text = StringBuilder().append(
                        JSONObject(
                            args?.get(
                                0
                            ).toString()
                        ).get("currentPlayersAmount")
                    )
                        .append("/")
                        .append(JSONObject(args?.get(0).toString()).get("playersAmount"))
                }
            },

            { args ->
                run {
                    //playerConnected
                    if (!SESSION_OBJECT.isWaitingStage) return@run
                    this.waiting_players_panel.players_amount.text = StringBuilder().append(
                        JSONObject(args?.get(0).toString()).get("currentPlayersAmount"))
                        .append("/")
                        .append(JSONObject(args?.get(0).toString()).get("playersAmount"))
                }
            },

            { args ->
                runOnUiThread {
                    //qrCode
                    if (!SESSION_OBJECT.isWaitingStage) return@runOnUiThread
                    var qrCode =
                        QRCode.from(args?.get(0).toString()).withSize(1000, 1000).withColor(
                            Color.BLACK,
                            Color.TRANSPARENT
                        ).bitmap()
                    this.waiting_players_panel.qr_code_progress_bar.visibility = View.GONE
                    this.waiting_players_panel.qr_code.setImageBitmap(qrCode)
                }
            },

            { args ->
                run {
                    //playerDisconnected
                    if (!SESSION_OBJECT.isWaitingStage) return@run
                    this.waiting_players_panel.players_amount.text = StringBuilder().append(
                        JSONObject(
                            args?.get(
                                0
                            ).toString()
                        ).get("currentPlayersAmount")
                    )
                        .append("/")
                        .append(JSONObject(args?.get(0).toString()).get("playersAmount"))
                }
            },

            { args ->
                run {
                    //sessionClose
                    val intent = Intent(this, Main_menu::class.java)
                    startActivity(intent)
                    Animatoo.animateFade(this)
                    finish()
                }
            },

            { args ->
                runOnUiThread {
                    //gameReady
                    if (!SESSION_OBJECT.isWaitingStage) return@runOnUiThread
                    SESSION_OBJECT.isWaitingStage = false
                    waiting_players_panel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_dialog_close))
                    this.main_layout.removeView(waiting_players_panel)

                    var gamePanel = layoutInflater.inflate(R.layout.session_game_stage, this.main_layout, false)
                    gamePanel.animation = AnimationUtils.loadAnimation(this, R.anim.anim_dialog_open)
                    gamePanel.setBackgroundColor(Color.TRANSPARENT)
                    this.main_layout.addView(gamePanel)
                }
            },

            { args ->
                runOnUiThread {
                    //newDialog
                    var dialog: JSONArray = (args[0] as JSONObject).get("dialog") as JSONArray
                    SESSION_OBJECT.nicknames = (args[0] as JSONObject).get("nicknames") as JSONArray
                    choiceDialogShowed = false

                    for (i in 0 until dialog.length()) {
                        val message = (dialog[i] as JSONObject)
                        val messageBlock = layoutInflater.inflate(R.layout.dialog_message, this.main_layout.session_game_panel.dialog_viewer, false)
                        messageBlock.text.text = message.getString("text")
                        val params = messageBlock.text.layoutParams as RelativeLayout.LayoutParams

                        if (message.getString("from") == "me") { messageBlock.animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_left_slide); params.addRule(RelativeLayout.ALIGN_PARENT_END) } else messageBlock.animation = AnimationUtils.loadAnimation(this, R.anim.anim_fade_right_slide)

                        messageBlock.text.layoutParams = params
                        this.main_layout.session_game_panel.dialog_viewer.addView(messageBlock)
                    }

                    make_choice_btn.setOnClickListener {
                        ShowChooseDialog(R.layout.make_choice_dialog)
                    }

                    val timer = object : CountDownTimer(60000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            main_layout.session_game_panel.timer.text =
                                (millisUntilFinished / 1000).toString()
                        }

                        override fun onFinish() {
                            choiceDialogShowed = true
                            playerSelected = true
                        }
                    }
                    timer.start()
                }
            },

            { args ->
                runOnUiThread {
                    //getDialog
                    val randKey = Random.nextInt(SESSION_OBJECT.accounts.size)
                    val randAcc = SESSION_OBJECT.accounts[randKey]
                    var dialog: JSONArray = JSONArray()
                    if (randAcc == "telegram") {
                        dialog = GameUtils().execute(
                            JSONObject().put("function", "GetTelegramMessages").put(
                                "activity",
                                this
                            )
                        ).get().get("res") as JSONArray
                    }


                    SESSION_OBJECT.socket.emit(
                        "get_dialog", JSONObject()
                            .put("dialog", dialog)
                            .put("socketID", SESSION_OBJECT.socket.id())
                            .put("sessionID", SESSION_OBJECT.sessionID)
                            .put("password", SESSION_OBJECT.password)
                    )
                }
            },
            { args ->
                runOnUiThread {
                    //request_for_round
                    if(!(args[0] as JSONObject).getBoolean("roundsEnd") && !(args[0] as JSONObject).getBoolean("isAd")) ShowNewRoundDialog(R.layout.new_round_dialog, args[0] as JSONObject)
                    if((args[0] as JSONObject).getBoolean("roundsEnd") && !(args[0] as JSONObject).getBoolean("isAd")) ShowRestartGameDialog(R.layout.restart_game_dialog, args[0] as JSONObject)
                }
            })

        var waitingPlayersPanel = layoutInflater.inflate(R.layout.session_waiting_players, this.main_layout, false)
        this.main_layout.addView(waitingPlayersPanel)
    }

    private fun ShowRestartGameDialog(layout: Int, args: JSONObject)
    {
        if (restartGameDialogShowed) return
        restartGameDialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {restartGameDialogShowed = false}
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(false)
        alertDialog.show()

        alertDialog.winner_name_text.text = args.getString("winnerName")
        alertDialog.vote_for_restart_button.setOnClickListener {
            SESSION_OBJECT.socket.emit("ready_for_restart", JSONObject().put("sessionID", SESSION_OBJECT.sessionID).put("password", SESSION_OBJECT.password))
            this.main_layout.session_game_panel.dialog_viewer.removeAllViews()
            alertDialog.dismiss()
            playerSelected = false
        }
        alertDialog.leave_button.setOnClickListener {
            mSocket.disconnect()
            val intent = Intent(this, Main_menu::class.java)
            startActivity(intent)
            Animatoo.animateFade(this)
            finish()
        }
    }

    private fun ShowNewRoundDialog(layout: Int, args: JSONObject)
    {
        if (newRoundDialogShowed) return
        newRoundDialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {newRoundDialogShowed = false}
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.setCancelable(false)
        alertDialog.show()

        alertDialog.round_status_text.text = if((args.get("winTab") as JSONObject).getBoolean(SESSION_OBJECT.socket.id())) resources.getString(R.string.you_won) else resources.getString(R.string.you_lose)
        alertDialog.correct_answer_text.text = args.getString("correctAnswer")
        alertDialog.vote_for_round_button.setOnClickListener {
            SESSION_OBJECT.socket.emit("ready_for_round", JSONObject().put("sessionID", SESSION_OBJECT.sessionID).put("password", SESSION_OBJECT.password))
            this.main_layout.session_game_panel.dialog_viewer.removeAllViews()
            alertDialog.dismiss()
            playerSelected = false
        }
    }

    private fun ShowChooseDialog(layout: Int)
    {
        if (choiceDialogShowed) return
        choiceDialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {choiceDialogShowed = false}
        alertDialog.show()

        for(i in 0 until  SESSION_OBJECT.nicknames.length())
        {
            var playerBlock = layoutInflater.inflate(
                R.layout.choose_player_button,
                alertDialog.main_layout,
                false
            )

            if(i % 2 == 0) playerBlock.animation = AnimationUtils.loadAnimation(
                this,
                R.anim.anim_fade_left_slide
            )
            else playerBlock.animation = AnimationUtils.loadAnimation(
                this,
                R.anim.anim_fade_right_slide
            )

            (playerBlock as Button).text = (SESSION_OBJECT.nicknames[i] as JSONObject).getString("nickname")
            (playerBlock as Button).setOnClickListener {
                if(playerSelected) return@setOnClickListener
                playerSelected = true
                alertDialog.setOnDismissListener { return@setOnDismissListener }
                alertDialog.dismiss()
                SESSION_OBJECT.socket.emit(
                    "send_choice", JSONObject()
                        .put(
                            "socketID",
                            (SESSION_OBJECT.nicknames[i] as JSONObject).getString("socketID")
                        )
                        .put("sessionID", SESSION_OBJECT.sessionID)
                        .put("password", SESSION_OBJECT.password)
                )
                Toast.makeText(
                    this,
                    resources.getString(R.string.player_selected),
                    Toast.LENGTH_LONG
                ).show()
            }
            alertDialog.window?.findViewById<LinearLayout>(R.id.main_layout)?.addView(playerBlock)
        }
    }

    override fun onBackPressed() {
        ShowBackPressedDialog(R.layout.leave_game_dialog)
    }

    private fun ShowBackPressedDialog(layout: Int)
    {
        if (backPressedDialogShowed) return
        backPressedDialogShowed = true
        var dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(layout)
        var alertDialog = dialogBuilder.create()
        alertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnDismissListener {backPressedDialogShowed = false}
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