package com.supertiger.nertivia.models

data class Server (
    var name: String? = null,
    var creator: User? = null,
    var default_channel_id: String? = null,
    var server_id: String? = null,
    var avatar: String? = null
)
