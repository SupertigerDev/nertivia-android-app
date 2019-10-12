package com.supertiger.nertivia

data class NamedEvent(val code: Int, val sent: String?){
    companion object {
        val FRIEND_CLICKED = 1
    }
}