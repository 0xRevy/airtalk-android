package com.airtalk.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airtalk.app.AirTalkApp
import com.airtalk.app.R
import com.airtalk.app.rtc.CallListener
import com.airtalk.app.rtc.CallState

class CallActivity : AppCompatActivity(), CallListener {

    private val app by lazy { application as AirTalkApp }
    private lateinit var statusText: TextView
    private lateinit var muteButton: Button
    private lateinit var hangUpButton: Button
    private lateinit var reportButton: Button
    private var muted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        statusText = findViewById(R.id.callStatusText)
        muteButton = findViewById(R.id.muteButton)
        hangUpButton = findViewById(R.id.hangUpButton)
        reportButton = findViewById(R.id.reportButton)

        muteButton.setOnClickListener {
            muted = app.callManager.toggleMute()
            muteButton.text = if (muted) "Unmute" else "Mute"
        }
        hangUpButton.setOnClickListener {
            app.callManager.hangUp()
            finish()
        }
        reportButton.setOnClickListener { showReportDialog() }
    }

    override fun onResume() {
        super.onResume()
        app.callManager.listener = this
        render(app.callManager.state)
    }

    override fun onPause() {
        super.onPause()
        app.callManager.listener = null
    }

    override fun onBackPressed() {
        app.callManager.hangUp()
        finish()
    }

    private fun showReportDialog() {
        val categories = arrayOf(
            "INAPPROPRIATE_BEHAVIOR",
            "HARASSMENT",
            "SPAM_SCAM",
            "MINOR",
            "OTHER"
        )
        AlertDialog.Builder(this)
            .setTitle("Report user")
            .setItems(categories) { _, which ->
                app.callManager.report(categories[which])
                statusText.text = "Reported. Thanks!"
            }
            .show()
    }

    // ---------- CallListener ----------

    override fun onStateChanged(state: CallState, extra: String) {
        render(state)
    }

    override fun onPeerMuted(muted: Boolean) {
        statusText.text = if (muted) "Call connected — peer muted" else "Call connected"
    }

    override fun onRemoteHangUp() {
        statusText.text = "Stranger hung up"
        render(app.callManager.state)
    }

    private fun render(state: CallState) {
        statusText.text = when (state) {
            CallState.SEARCHING -> "Looking for someone to talk to…"
            CallState.CONNECTING -> "Connecting…"
            CallState.CONNECTED -> "Call connected"
            CallState.IDLE -> "Disconnected"
        }
        muteButton.isEnabled = state == CallState.CONNECTED
        reportButton.isEnabled = state == CallState.CONNECTED
    }
}