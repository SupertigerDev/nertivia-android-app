package com.supertiger.nertivia

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.supertiger.nertivia.models.Friend

@Dao
interface FriendDao {
    @Insert
    fun upsert (friends: List<Friend>)

    @Query("select * from  friends")
    fun getAll(): List<Friend>
}
