package com.supertiger.nertivia

import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity


fun removePushNotification(channelID: String?, context: Context){
    AsyncTask.execute {
        val db = AppDatabase.getInstance(context)
        val notification = db.pushNotificationDao().findByChannelID(channelID!!)
        if (notification != null) {
            db.pushNotificationDao().delete(notification);
            cancelNotification(notification.id, context);
        }
    }

}
 fun removePushNotificationByID(notificationID:Int, context: Context) {
     AsyncTask.execute {
        val db = AppDatabase.getInstance(context);
         val notification = db.pushNotificationDao().findByID(notificationID)
         if (notification != null) {
             db.pushNotificationDao().delete(notification)
             cancelNotification(notificationID, context)
         }
     }
 }

private fun cancelNotification(notificationID:Int, context: Context) {
   val notificationManager : NotificationManager =
        context.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager;
    notificationManager.cancel(notificationID)
}