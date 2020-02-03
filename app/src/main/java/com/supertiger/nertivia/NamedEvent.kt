package com.supertiger.nertivia

data class NamedEvent(val code: Int, val sent: String?){
    companion object {
        const val FRIEND_CLICKED = 1
        const val SERVER_CLICKED = 2
        const val CHANNEL_CLICKED = 3
    }
}