package com.supertiger.nertivia.cache

import java.util.*

data class User (
    var username: String,
    var ms: Long
)


private val typingUsers: MutableMap<String, MutableMap<String, User>> = mutableMapOf()


fun addToTyping(channelID: String, username: String, uniqueID: String): MutableMap<String, User>? {
    if (typingUsers[channelID] === null) {
        typingUsers[channelID] = mutableMapOf(uniqueID to User(username, System.currentTimeMillis()))
    } else {
        typingUsers[channelID]?.put(uniqueID, User(username, System.currentTimeMillis()))
    }
    return typingUsers[channelID]
}

fun removeToTyping(channelID: String, uniqueID: String): MutableMap<String, User>? {
    typingUsers[channelID]?.remove(uniqueID);
    return typingUsers[channelID];
}
fun getTypingChannel(channelID: String): MutableMap<String, User>? {
    return typingUsers[channelID]
}

fun getAllTypingUsers (): MutableMap<String, MutableMap<String, User>> {
    return typingUsers;
}