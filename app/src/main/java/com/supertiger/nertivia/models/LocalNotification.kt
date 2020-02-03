package com.supertiger.nertivia.models

data class LocalNotification(
    var messages: MutableList<LocalNotificationMessage?>,
    var channelID: String
)
data class LocalNotificationMessage(
    var message: String?,
    var username: String?
)