package com.benmohammad.vidz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class VidzSplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vidz_splash_screen)

        initListener()
    }

    private fun initListener() {
        val handler = Handler()
        Context.HARDWARE_PROPERTIES_SERVICE
        handler.postDelayed( {callActivityIntent()}, 3000)
    }

    private fun callActivityIntent() {
        val intentFlag = Intent(this@VidzSplashScreen, MainActivity::class.java)
        intentFlag.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentFlag)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}