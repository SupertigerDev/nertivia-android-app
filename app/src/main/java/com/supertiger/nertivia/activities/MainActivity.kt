package com.supertiger.nertivia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.view.GravityCompat
import com.squareup.picasso.Picasso
import io.socket.client.IO
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.*
import com.supertiger.nertivia.services.ChannelService
import com.supertiger.nertivia.services.MessageService
import com.supertiger.nertivia.services.ServiceBuilder
import java.net.URISyntaxException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback
import com.google.gson.JsonObject
import com.supertiger.nertivia.*
import com.supertiger.nertivia.adapters.FriendsListAdapter
import com.supertiger.nertivia.adapters.MessagesListAdapter


class MainActivity : DrawerActivity() {
    private val channelService = ServiceBuilder.buildService(ChannelService::class.java)
    private val messageService = ServiceBuilder.buildService(MessageService::class.java)
    private var sharedPreference: SharedPreference? = null
    var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreference = SharedPreference(this)
        val userString = sharedPreference?.getValueString("user")
        currentUser = gson.fromJson(userString, User::class.java)
        token = sharedPreference?.getValueString("token");

        send_button.setOnClickListener{
            if (mSocket == null || mSocket?.connected() == false || mSocket?.id().isNullOrEmpty()) {
                Toast.makeText(this, "Can't send message. Check your internet. (socket_not_connected)",  Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = text_area.text.trim()
            text_area.setText("")
            if (message.isEmpty()) {
                return@setOnClickListener
            }
            if (selectedChannelID === null) {
                Toast.makeText(this, "Select a friend!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendMessage(selectedChannelID!!, message.toString())
        }
        socketConnect()
        setDrawer()

        RxBus.listen(NamedEvent::class.java).subscribe {
            if (it.code == NamedEvent.FRIEND_CLICKED) {
                val uniqueID = it.sent
                getChannel(uniqueID)
                drawer_layout.closeDrawer(GravityCompat.START)
            }
        }

    }

    private fun sendMessage(selectedChannelID: String, message: String) {
        val jsonObject = JsonObject()

        jsonObject.addProperty("message", message)
        jsonObject.addProperty("channelID", selectedChannelID)
        jsonObject.add("creator", gson.toJsonTree(currentUser) )


        addMessage( gson.fromJson(jsonObject, Message::class.java) )

        val requestCall = messageService.sendMessage(selectedChannelID, MessageSendData(
            message,
            mSocket?.id()
        ))
        requestCall.enqueue(object: Callback, retrofit2.Callback<Any?> {
            override fun onFailure(call: Call<Any?>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                if (response.isSuccessful) {
                    //Toast.makeText(applicationContext, "Message sent! " , Toast.LENGTH_SHORT).show()
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    Toast.makeText(applicationContext, "Something bork while sending message :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun socketConnect() {
        try {
            mSocket = IO.socket(getString(R.string.domain));
            mSocket?.connect()
            mSocket?.on("connect") {
                runOnUiThread {
                    Toast.makeText(applicationContext, "CONNECTED!!!", Toast.LENGTH_SHORT).show()
                }
                val obj = JSONObject()
                obj.put("token", token)
                mSocket?.emit("authentication", obj)
            }
            mSocket?.on("disconnect") {
                runOnUiThread {
                    Toast.makeText(applicationContext, "disconnected :(", Toast.LENGTH_SHORT).show()
                }
            }
            mSocket?.on("success") { args ->
                runOnUiThread {
                    Toast.makeText(applicationContext, "Authenticated :)", Toast.LENGTH_SHORT)
                        .show()
                }
                val obj = args[0] as JSONObject
                val user = obj.getJSONObject("user");
                val friendsArr = user.getJSONArray("friends");
                val dmsArr = obj.getJSONArray("dms")

                val dmChannels = gson.fromJson(dmsArr.toString(), Array<Channel>::class.java).toList().associateBy({it.channelID}, {it})
                channels = dmChannels as MutableMap<String?, Channel>

                friends = gson.fromJson(friendsArr.toString(), Array<Friend>::class.java).toList()

                runOnUiThread{
                    // Friends List Recycler View
                    friends_list.layoutManager = LinearLayoutManager(this)
                    friends_list.adapter = FriendsListAdapter()
                }
            }
            mSocket?.on("receiveMessage"){ args ->
                val obj = args[0] as JSONObject
                val message = gson.fromJson(obj.getJSONObject("message").toString(), Message::class.java)
                addMessage(message)
            }

        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    private fun setDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        username.text = currentUser?.username
        tag.text = "@" + currentUser?.tag

        Picasso.get().load(
            "https://supertiger.tk/api/avatars/" + (currentUser?.avatar ?: "default") + "?type=png"
        ).placeholder(R.drawable.nertivia_logo).error(R.drawable.nertivia_logo).into(header_avatar)
    }

    private fun getChannel(uniqueID: String?) {
        // check if channel already exists
        val channelsMap = channels.filterValues { it.recipients?.get(0)?.uniqueID == uniqueID }.values
        if (channelsMap.isNotEmpty()) {
            toolbar_title.text = channelsMap.first().recipients?.get(0)?.username ?: "Select Friend"
            getMessages(channelsMap.first().channelID)
            return
        }
        chatting.visibility = View.GONE
        chat_loading_progress_bar.visibility = View.VISIBLE
        val requestCall = channelService.getChannelByUniqueID(uniqueID)
        requestCall.enqueue(object: Callback, retrofit2.Callback<PostChannelResponse> {

            override fun onFailure(call: Call<PostChannelResponse>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<PostChannelResponse>, response: Response<PostChannelResponse>) {
                if (response.isSuccessful) {
                    val channel = response.body()?.channel
                    if (channel != null) {
                        channels[channel.channelID] = channel
                    }
                    toolbar_title.text = channel?.recipients?.get(0)?.username ?: "Select Friend"
                    getMessages(channel?.channelID)
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    Toast.makeText(applicationContext, "Something bork while getting channel :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    private fun getMessages(channelID: String?) {
        // check if message already loaded from before
        if(messages[channelID] !== null) {
            selectedChannelID = channelID
            setMessagesAdapter()
            return
        }
        chatting.visibility = View.GONE
        chat_loading_progress_bar.visibility = View.VISIBLE
        messageService.getMessages(channelID).enqueue(object: Callback, retrofit2.Callback<GetMessagesResponse> {

            override fun onFailure(call: Call<GetMessagesResponse>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<GetMessagesResponse>, response: Response<GetMessagesResponse>) {
                if (response.isSuccessful) {
                    if (channelID != null) {
                        messages[channelID] = response.body()?.messages!!.toMutableList()
                        selectedChannelID = channelID
                        setMessagesAdapter()

                    }
                } else {
                    val jObjError = JSONObject(response.errorBody()?.string())
                    Log.d("testuuu", jObjError.toString())
                    Toast.makeText(applicationContext, "Something bork while getting messages :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun setMessagesAdapter() {
        chat_loading_progress_bar.visibility = View.GONE
        chatting.visibility = View.VISIBLE
        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.reverseLayout = true
        messages_list.layoutManager = mLayoutManager
        messages_list.adapter = MessagesListAdapter()

    }

    private fun addMessage(message: Message?) {
        val messagesCount = messages[message?.channelID]?.size
        if (messagesCount != null) {
            if (message != null) {
                messages[message.channelID]?.add(0, message)
            }
        }
        if (selectedChannelID != message?.channelID) {
            return
        }
        runOnUiThread {
            messages_list.adapter?.notifyItemInserted(0)
            messages_list.scrollToPosition(0)
        }
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.off("connect")
        mSocket?.off("disconnect")
        mSocket?.off("success")
        mSocket?.off("receiveMessage")
        mSocket?.disconnect()
    }
}

