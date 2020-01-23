package com.supertiger.nertivia.cache

import android.util.Log
import com.supertiger.nertivia.models.Channel
import com.supertiger.nertivia.models.User

var channels: MutableMap<String?, Channel> = mutableMapOf()


fun dmRecentsSorted(): List<Channel> {
    val channelsSort = channels.toList().sortedBy {
        it.second.lastMessaged
    }.reversed()

    val unOpenedDms: MutableList<Channel> = mutableListOf()
    val highPriority: MutableList<Channel> = mutableListOf()
    val lowPriority: MutableList<Channel> = mutableListOf()

    for (v in channelsSort) {
        if (v.second.server_id === null) {
            val notify = notifications.values.find {
                it.sender?.uniqueID == v.second.recipients?.first()?.uniqueID
            }

            if (notify != null) {
                highPriority.add(v.second);
            } else {
                lowPriority.add(v.second);
            }
        }
    }


    // unopened dms test
    notifications.values.forEach {
        if (channels[it.channelID] === null && channels[it.channelID]?.server_id === null) {
            val username = it.sender?.username
            val tag = it.sender?.tag
            val avatar = it.sender?.avatar
            val uniqueID = it.sender?.uniqueID
            val recipients = List(1) {User(username, tag, avatar, uniqueID )}
            unOpenedDms.add(Channel(recipients, null, it.channelID, null, null))
        }
    }

    return unOpenedDms + highPriority + lowPriority;

    //Log.d("testowo", v.toString())
}