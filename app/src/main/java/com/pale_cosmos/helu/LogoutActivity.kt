package com.pale_cosmos.helu

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.view.Window
import android.widget.Button
import kotlinx.android.synthetic.main.logout.*

class LogoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.logout)
        window.setFeatureDrawableResource(Window.FEATURE_NO_TITLE, android.R.drawable.ic_dialog_alert)

        bbb1.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
        bbb2.setOnClickListener { finish() }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_OUTSIDE) {

        }

        return false
    }

    override fun onBackPressed() {
        finish()
        return
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            super.setRequestedOrientation(requestedOrientation)
        }

    }
}