package com.supertiger.nertivia.services

import com.supertiger.nertivia.models.LoginData
import com.supertiger.nertivia.models.LoginResponse
import com.supertiger.nertivia.models.RegisterDeviceData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface UserService {
    @POST("user/login")
    fun login(@Body body: LoginData):Call<LoginResponse>

    // registerDevice
    @POST("devices")
    fun registerDevice(@Body body: RegisterDeviceData):Call<Any?>
}