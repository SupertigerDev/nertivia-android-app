package com.supertiger.nertivia.models

data class FriendsRecyclerData<out T> (
    val value: T?,
    var headerName: String? = null
)