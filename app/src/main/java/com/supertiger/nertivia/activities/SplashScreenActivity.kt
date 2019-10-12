package com.supertiger.nertivia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.supertiger.nertivia.R
import com.supertiger.nertivia.SharedPreference

class SplashScreenActivity : AppCompatActivity() {
    private var sharedPreference: SharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_splash_screen)
        sharedPreference= SharedPreference(this)

        val activityIntent: Intent
        // Check if token is in prefs
        val token = sharedPreference!!.getValueString("token")

        activityIntent = if (token === null) {
            Intent(this, LoginActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        startActivity(activityIntent)
        finish()

    }
}
