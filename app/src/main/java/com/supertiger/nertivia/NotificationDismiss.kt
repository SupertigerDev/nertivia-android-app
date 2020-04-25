package com.supertiger.nertivia

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService


class NotificationDismiss : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.extras?.getInt("notificationID")
        if (notificationId != null) {
            if (context != null) {
                removePushNotificationByID(notificationId, context)
            }
        }

    }
}