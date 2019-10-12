package com.supertiger.nertivia.models

data class MessageSendData(
    var message: String? = null,
    var socketID: String? = null,
    var tempID: String? = null
)