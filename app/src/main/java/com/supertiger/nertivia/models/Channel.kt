package com.supertiger.nertivia.models

data class Channel(
    var recipients: List<User?>?,
    var status: Int?,
    var channelID: String?,
    var lastMessaged: Long?,
    var server_id: String?
)