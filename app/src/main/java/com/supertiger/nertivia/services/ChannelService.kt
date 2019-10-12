package com.supertiger.nertivia.services


import com.supertiger.nertivia.models.PostChannelResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Path




interface ChannelService {
    @POST("channels/{uniqueID}")
    fun getChannelByUniqueID(@Path("uniqueID") uniqueID: String?): Call<PostChannelResponse>
}