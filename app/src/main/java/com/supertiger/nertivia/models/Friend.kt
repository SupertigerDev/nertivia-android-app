package com.supertiger.nertivia.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend (
    var status: Int?,
    @Embedded(prefix = "recipient_")
    var recipient: User
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}