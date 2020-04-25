package com.supertiger.nertivia

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.supertiger.nertivia.models.LocalNotificationMessage
import java.lang.reflect.Type

class Converters {
    @TypeConverter
    fun fromString(value: String?): MutableList<LocalNotificationMessage> {
        val listType: Type = object : TypeToken<MutableList<LocalNotificationMessage?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromMutableList(list: MutableList<LocalNotificationMessage?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}