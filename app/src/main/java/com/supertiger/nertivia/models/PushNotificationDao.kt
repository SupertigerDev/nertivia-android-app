package com.supertiger.nertivia.models

import androidx.room.*

@Dao
interface PushNotificationDao {
    @Query("SELECT * FROM push_notification")
    fun getAll(): List<LocalNotification>

    @Query("SELECT * FROM push_notification WHERE id LIKE :id LIMIT 1")
    fun findByID(id: Int): LocalNotification?

    @Query("SELECT * FROM push_notification WHERE channel_id LIKE :channelID LIMIT 1")
    fun findByChannelID(channelID: String): LocalNotification?

    @Insert
    fun insertAll(vararg notifications: LocalNotification)

    @Delete
    fun delete(notification: LocalNotification)

    @Update
    fun update(notification: LocalNotification)

    @Query("DELETE FROM push_notification")
    fun deleteAll()
}