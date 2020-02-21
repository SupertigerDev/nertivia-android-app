package com.supertiger.nertivia.services


import com.supertiger.nertivia.models.GetMessagesResponse
import com.supertiger.nertivia.models.MessageSendData
import com.supertiger.nertivia.models.PostChannelResponse
import com.supertiger.nertivia.models.PostMessageResponse
import retrofit2.Call
import retrofit2.http.*


interface MessageService {
    // https://supertiger.tk/api/messages/channels/6971348971703772441
    @GET("messages/channels/{channelID}")
    fun getMessages(@Path("channelID") channelID: String?): Call<GetMessagesResponse>

    //https://supertiger.tk/api/messages/channels/6620366328135946240?before=6627977999679492096
    @GET("messages/channels/{channelID}")
    fun getMessagesBefore(@Path("channelID") channelID: String?, @Query("before") messageID:String?): Call<GetMessagesResponse>

    // https://supertiger.tk/api/messages/channels/6971348971703772441
    @POST("messages/channels/{channelID}")
    fun sendMessage(@Path("channelID") channelID: String?, @Body body: MessageSendData): Call<PostMessageResponse?>
}