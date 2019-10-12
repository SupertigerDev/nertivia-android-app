package com.supertiger.nertivia.services


import com.supertiger.nertivia.models.GetMessagesResponse
import com.supertiger.nertivia.models.MessageSendData
import com.supertiger.nertivia.models.PostChannelResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path




interface MessageService {
    // https://supertiger.tk/api/messages/channels/6971348971703772441
    @GET("messages/channels/{channelID}")
    fun getMessages(@Path("channelID") channelID: String?): Call<GetMessagesResponse>

    // https://supertiger.tk/api/messages/channels/6971348971703772441
    @POST("messages/channels/{channelID}")
    fun sendMessage(@Path("channelID") channelID: String?, @Body body: MessageSendData): Call<Any?>
}
