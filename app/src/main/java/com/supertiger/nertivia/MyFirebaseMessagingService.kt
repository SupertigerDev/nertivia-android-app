package com.supertiger.nertivia


import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("ddd", "From: ${remoteMessage.from}")
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            val data = JSONObject(remoteMessage.data)
            Log.d("ddd", "Message Notification Body: ${it.body}")
            //Message Services handle notification
            val notification = NotificationCompat.Builder(this, "t")
                .setContentTitle(remoteMessage.from)
                .setContentText(it.body)
                .setSmallIcon(R.drawable.nertivia_logo)
                .setGroup(data.getString("channel_id"))
                .build()
            val manager = NotificationManagerCompat.from(applicationContext)
            manager.notify(/*notification id*/0, notification)

        }
    }

    override fun onNewToken(token: String) {

    }

}
