package com.supertiger.nertivia.models

data class LoginResponse(
    var token: String? = null,
    var user: User
)

