package com.supertiger.nertivia.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.supertiger.nertivia.AppDatabase
import com.supertiger.nertivia.R
import com.supertiger.nertivia.SharedPreference
import com.supertiger.nertivia.cache.socketIOInstance
import kotlinx.android.synthetic.main.activity_settings.*
import kotlin.system.exitProcess


class SettingsActivity : AppCompatActivity() {
    private var sharedPreference: SharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPreference = SharedPreference(this)

        logout_button.setOnClickListener {
            Toast.makeText(this, "Re-open the app.", Toast.LENGTH_LONG).show()
            sharedPreference?.clearSharedPreference();

            if (socketIOInstance != null && socketIOInstance?.connected()!!) {
                socketIOInstance!!.disconnect()
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            AsyncTask.execute {
                AppDatabase.getInstance(this).pushNotificationDao().deleteAll()

                clearAppData()
                val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName);
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent)
            }
        }
    }
    private fun clearAppData() {
        try {
            val runtime = Runtime.getRuntime()
            runtime.exec("pm clear " + applicationContext.packageName + " HERE")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


