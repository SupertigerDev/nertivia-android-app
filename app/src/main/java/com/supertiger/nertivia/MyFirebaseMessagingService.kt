package com.supertiger.nertivia


import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.supertiger.nertivia.activities.MainActivity
import com.supertiger.nertivia.cache.selectedChannelID
import com.supertiger.nertivia.models.LocalNotification
import com.supertiger.nertivia.models.LocalNotificationMessage

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


        val channelID = remoteMessage.data["channel_id"]
        val uniqueID = remoteMessage.data["unique_id"]
        val avatar = remoteMessage.data["avatar"]
        val username = remoteMessage.data["username"]
        val message = remoteMessage.data["message"]
        var serverID: String? = null
        var serverName: String? = null
        var channelName: String? = null;


        if (remoteMessage.data["server_id"] != null) {
            serverID = remoteMessage.data["server_id"]
        }
        if (remoteMessage.data["server_name"] != null) {
            serverName = remoteMessage.data["server_name"]
        }
        if (remoteMessage.data["channel_name"] != null) {
            channelName = remoteMessage.data["channel_name"]
        }



        if (selectedChannelID == channelID && applicationInForeground()) {
            return
        }

        val notificationItm = addAndReturnNotification(channelID, message, username);
        val notificationID = notificationItm?.id

        // added up messages
        val messages = notificationItm?.messages;

        val dismissIntent = Intent(this, NotificationDismiss::class.java)
        dismissIntent.putExtra("notificationID", notificationID);

        val dismissPendingIntent = PendingIntent.getBroadcast(this,
            notificationID!!, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        val activityIntent = Intent(this, MainActivity::class.java)



        activityIntent.putExtra("notification:channelID", channelID);
        if (serverID != null) {
            activityIntent.putExtra("notification:channelName", channelName);
            activityIntent.putExtra("notification:serverID", serverID);
        }
        activityIntent.putExtra("notification:uniqueID", uniqueID);
        activityIntent.putExtra("notification:username", username);
        val contentIntent = PendingIntent.getActivity(
            this,
            notificationID, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(username + " (" + messages!!.size + ")")
            .setSubText("Click to view")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentIntent)
            .setDeleteIntent(dismissPendingIntent)
            .setAutoCancel(true)

        val wew: Person = Person.Builder().setName("wew").build();

        val messagingStyle = NotificationCompat.MessagingStyle(wew);

        messagingStyle.isGroupConversation = true
        messagingStyle.conversationTitle = username;
        if (serverName != null) {
            messagingStyle.conversationTitle = "$serverName#$channelName ";
        }

        messages.takeLast(4).forEach {
            val user: Person = Person.Builder().setName(it?.username).build();
            val message1 = NotificationCompat.MessagingStyle.Message(it?.message,
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


        Glide.with(this).asBitmap()
            .load(if (avatar != null) "https://nertivia-media.tk/${avatar}?type=webp" else "")
            .fallback(R.drawable.nertivia_logo)
            .placeholder(R.drawable.nertivia_logo)
            .into(object : CustomTarget<Bitmap>() {
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

    private fun addAndReturnNotification(channelID: String?, message: String?, username: String? ): LocalNotification? {
        val allNotifications = AppDatabase.getInstance(applicationContext).pushNotificationDao().getAll()
        Log.d("testty", allNotifications.size.toString())
        val notification = allNotifications.find { it.channelID == channelID };
        if (channelID != null) {
            if (notification != null) {
                notification.messages.add(LocalNotificationMessage(message, username))
                if (notification.messages.size >= 20) {
                    notification.messages.removeAt(0);
                }
                AppDatabase.getInstance(applicationContext).pushNotificationDao().update(notification);
                return notification
            } else {
                val arr: MutableList<LocalNotificationMessage?> = mutableListOf(LocalNotificationMessage(message, username));
                val newNotification = LocalNotification(0, arr, channelID);
                AppDatabase.getInstance(applicationContext).pushNotificationDao().insertAll(newNotification)
                return AppDatabase.getInstance(applicationContext).pushNotificationDao().findByChannelID(channelID);
            }
        }
        return null;
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




