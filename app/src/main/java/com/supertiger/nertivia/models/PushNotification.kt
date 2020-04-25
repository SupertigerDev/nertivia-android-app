package com.supertiger.nertivia.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


@Entity(tableName = "push_notification")
data class LocalNotification(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "messages") val messages: MutableList<LocalNotificationMessage?>,
    @ColumnInfo(name = "channel_id") val channelID: String

)
data class LocalNotificationMessage(
    @ColumnInfo(name = "message") val message: String?,
    @ColumnInfo(name = "username") val username: String?
)