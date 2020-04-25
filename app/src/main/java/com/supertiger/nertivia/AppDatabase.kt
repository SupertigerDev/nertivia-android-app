package com.supertiger.nertivia

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.supertiger.nertivia.models.LocalNotification

import com.supertiger.nertivia.models.PushNotificationDao


@Database(entities = arrayOf(LocalNotification::class), version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun pushNotificationDao(): PushNotificationDao

    companion object {
        private var INSTANCE: AppDatabase? = null;

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "database-name").build()
            }
            return INSTANCE!!
        }
        fun destroyInstance() {
            INSTANCE = null;
        }
    }
}

