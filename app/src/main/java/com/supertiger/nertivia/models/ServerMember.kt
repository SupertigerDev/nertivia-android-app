package com.supertiger.nertivia.models

data class ServerMember (
    var type: String? = null,
    var member: User? = null,
    var server_id: String? = null
)