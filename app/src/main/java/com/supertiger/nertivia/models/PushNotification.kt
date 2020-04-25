package com.supertiger.nertivia.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "push_notification")
data class LocalNotification(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "messages") val messages: MutableList<LocalNotificationMessage?>,
    @ColumnInfo(name = "channel_id") val channelID: String

)
data class LocalNotificationMessage(
    var message: String?,
    var username: String?
)