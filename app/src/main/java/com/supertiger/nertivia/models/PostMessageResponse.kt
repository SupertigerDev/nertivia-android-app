package com.supertiger.nertivia.models

data class PostMessageResponse (
    var messageCreated: Message,
    var socketID: String? = null,
    var tempID: String? = null
)

