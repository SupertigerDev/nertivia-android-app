package com.supertiger.nertivia.models

data class LocalNotification(
    var messages: MutableList<String?>,
    var channelID: String
)