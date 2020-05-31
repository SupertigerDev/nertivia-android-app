package com.supertiger.nertivia.models

data class Message (
    var message: String? = null,
    var creator: User?,
    var channelID: String? = null,
    var messageID: String? = null,
    var created: Long? = 0,
    var timeEdited: Long? = null,
    var files: List<File?>?,
    var type: Int
)
