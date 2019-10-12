package com.supertiger.nertivia.models

data class Message (
    var message: String? = null,
    var creator: User?,
    var channelID: String? = null,
    var created: Any? = 0
)

