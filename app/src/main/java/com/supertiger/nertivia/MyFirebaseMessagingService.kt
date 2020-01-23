package com.supertiger.nertivia


import android.app.*
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.supertiger.nertivia.activities.MainActivity
import android.content.Intent
import android.app.ActivityManager.RunningAppProcessInfo
import android.graphics.*



import android.graphics.Bitmap
import android.graphics.drawable.Drawable

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.supertiger.nertivia.cache.localNotifications
import com.supertiger.nertivia.models.LocalNotification


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var notificationManager: NotificationManagerCompat? = null
    val MESSAGE_CHANNEL = "message"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels();
    }


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                MESSAGE_CHANNEL,
                "Message Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "Message received"


            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel1)
            notificationManager = NotificationManagerCompat.from(this);
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (applicationInForeground()) {
            return;
        }
        val channelID = remoteMessage.data["channel_id"]
        val uniqueID = remoteMessage.data["unique_id"]
        val avatar = remoteMessage.data["avatar"]
        val username = remoteMessage.data["username"]
        val message = remoteMessage.data["message"]

        val notificationID = addAndReturnNotificationID(channelID, message);

        // added up messages
        val messages = localNotifications[notificationID].messages;

        val activityIntent = Intent(this, MainActivity::class.java)
        activityIntent.putExtra("notification:channelID", channelID);
        activityIntent.putExtra("notification:uniqueID", uniqueID);
        activityIntent.putExtra("notification:username", username);
        val contentIntent = PendingIntent.getActivity(
            this,
            notificationID, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(username + " (" + messages.size + ")")
            .setSubText("Click to view")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        val wew: Person = Person.Builder().setName("wew").build();
        val user: Person = Person.Builder().setName(username).build();

        val messagingStyle = NotificationCompat.MessagingStyle(wew);


        messages.takeLast(6).forEach {
            val message1 = NotificationCompat.MessagingStyle.Message(it,
                2,
                user)
            messagingStyle.addMessage(message1);
        }
        notification.setStyle(messagingStyle)

        //val inboxStyle : NotificationCompat.InboxStyle = NotificationCompat.InboxStyle()

        //messages.takeLast(6).forEach {
            //inboxStyle.addLine(it);
        //}
        //notification.setStyle(inboxStyle)


        Glide.with(this).asBitmap().load("https://supertiger.tk/api/avatars/" + (avatar ?: "default") + "?type=webp").into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                notification.setLargeIcon(getCircleBitmap(resource))
                notificationManager?.notify(notificationID, notification.build())
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                notificationManager?.notify(notificationID, notification.build())
            }
        })



    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
    private fun applicationInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.runningAppProcesses
        var isActivityFound = false

        if (services[0].processName
                .equals(
                    packageName,
                    ignoreCase = true
                ) && services[0].importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        ) {
            isActivityFound = true
        }

        return isActivityFound
    }

    private fun addAndReturnNotificationID(channelID: String?, message: String? ): Int {
        val notificationID = localNotifications.indexOfFirst { it.channelID == channelID };
        if (channelID != null) {
            if (notificationID >= 0) {
                localNotifications[notificationID].messages.add(message)
                if (localNotifications[notificationID].messages.size >= 20) {
                    localNotifications[notificationID].messages.removeAt(0);
                }
                return notificationID
            } else {
                val arr: MutableList<String?> = mutableListOf(message);
                localNotifications.add(LocalNotification(arr, channelID))
                return localNotifications.size - 1;
            }
        }
        return -1;
    }

    private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val color = Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawOval(rectF, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)


        return output
    }


}




