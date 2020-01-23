package com.supertiger.nertivia.models

data class Notification (
    var channelID: String? = null,
    var count: Int? = 0,
    var lastMessageID: String? = null,
    var sender: User?
)

