package com.supertiger.nertivia.cache

import android.util.Log
import com.supertiger.nertivia.models.Friend
import com.supertiger.nertivia.models.FriendsRecyclerData

var friends: List<Friend>? = null

fun recyclerViewData():  MutableList<FriendsRecyclerData> {
    val total: MutableList<FriendsRecyclerData> = mutableListOf();
    val online: MutableList<FriendsRecyclerData> = mutableListOf()
    val offline: MutableList<FriendsRecyclerData> = mutableListOf()

    friends?.forEach {
        val presence = userPresence[it.recipient.uniqueID];
        if (presence != null && presence >= 1)  {
            online.add(FriendsRecyclerData(0, it, null));
        } else {
            offline.add(FriendsRecyclerData(0, it, null));
        }
    }
    if (online.size >= 1) {
        total.add(FriendsRecyclerData(1, null, "Online"))
        total += online;
    }
    total.add(FriendsRecyclerData(1, null, "Offline"))
    total += offline;
    return total
}