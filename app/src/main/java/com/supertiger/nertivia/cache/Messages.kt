package com.supertiger.nertivia.cache

import android.util.Log
import com.supertiger.nertivia.models.Message
import com.supertiger.nertivia.models.MessageRecyclerView

var messages: MutableMap<String?, MutableList<Message>> = mutableMapOf()

fun getRecyclerMessages(): List<MessageRecyclerView> {
    val total: MutableList<MessageRecyclerView> = mutableListOf()

    var groupedCount = 0;

    messages[selectedChannelID]?.forEachIndexed { index, message ->
        if (message.type > 0) {
            total.add(MessageRecyclerView(message.type + 1, message))
        } else if (messages[selectedChannelID]?.size!! <= 1) {
            total.add(MessageRecyclerView(0, message))
        } else if (index == 0) {
            total.add(MessageRecyclerView(0, message))
        } else {
            val prevMessage = messages[selectedChannelID]?.get(index - 1);
            if (prevMessage?.creator?.uniqueID == message.creator?.uniqueID && (prevMessage?.type === null || prevMessage.type == 0)) {
                groupedCount += 1;
                if (groupedCount <= 4) {
                    total.add(MessageRecyclerView(1, message))
                } else {
                    groupedCount = 0;
                    total.add(MessageRecyclerView(0, message))
                }

            } else {
                groupedCount = 0;
                total.add(MessageRecyclerView(0, message))
            }
        }
    }
    return total.reversed();
}