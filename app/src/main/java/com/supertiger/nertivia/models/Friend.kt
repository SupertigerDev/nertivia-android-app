package com.supertiger.nertivia.models




data class Friend (
    var status: Int?,

    var recipient: User
){
    var id: Int = 0
}