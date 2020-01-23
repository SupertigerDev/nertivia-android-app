package com.supertiger.nertivia.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.*
import com.supertiger.nertivia.services.ChannelService
import com.supertiger.nertivia.services.MessageService
import com.supertiger.nertivia.services.ServiceBuilder
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback
import com.google.gson.JsonObject
import com.supertiger.nertivia.*
import com.supertiger.nertivia.adapters.FriendsListAdapter
import com.supertiger.nertivia.adapters.MessagesListAdapter
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.supertiger.nertivia.adapters.RecentListAdapter
import com.supertiger.nertivia.adapters.ServersListAdapter


class MainActivity : AppCompatActivity()  {
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
        token = sharedPreference?.getValueString("token")
        send_button.setOnClickListener{
            if (socketIOInstance?.connected() == false) {
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
                Handler().postDelayed({
                    drawer_layout.closeDrawer(GravityCompat.START)
                }, 0)

                    getChannel(uniqueID)
            }
        }
        notificationClicked(intent)

    }


    private fun sendMessage(selectedChannelID: String, message: String) {
        val jsonObject = JsonObject()

        jsonObject.addProperty("message", message)
        jsonObject.addProperty("channelID", selectedChannelID)
        jsonObject.add("creator", gson.toJsonTree(currentUser) )


        addMessage( gson.fromJson(jsonObject, Message::class.java) )

        val requestCall = messageService.sendMessage(selectedChannelID, MessageSendData(
            message,
            socketIOInstance?.id()
        ))
        requestCall.enqueue(object: Callback, retrofit2.Callback<Any?> {
            override fun onFailure(call: Call<Any?>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
                if (response.isSuccessful) {
                    //Toast.makeText(applicationContext, "Message sent! " , Toast.LENGTH_SHORT).show()
                } else {
                    //val jObjError = JSONObject(response.errorBody()?.string())
                    Toast.makeText(applicationContext, "Something bork while sending message :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun socketConnect() {
        socketIOInstance = SocketIO()
        socketIOInstance?.connect(getString(R.string.domain))
        socketIOInstance?.onConnect = {
            socketIOInstance?.dismissNotification(selectedChannelID)
            runOnUiThread {
                Toast.makeText(applicationContext, "Connected!", Toast.LENGTH_SHORT).show()
            }
        }
        socketIOInstance?.onDisconnect = {
            runOnUiThread {
                Toast.makeText(applicationContext, "disconnected :(", Toast.LENGTH_SHORT).show()
            }
        }
        socketIOInstance?.onAuthenticate = {
            runOnUiThread{
                Toast.makeText(applicationContext, "Authenticated", Toast.LENGTH_SHORT).show()
                setFriendAdapter()
                setRecentAdapter()
                setServerAdapter()
            }
        }
        socketIOInstance?.onMessageReceive = { fromSelectedChannel ->
            runOnUiThread {
                recent_list.adapter?.notifyDataSetChanged()
                if (fromSelectedChannel == true) {
                    recent_list.adapter?.notifyDataSetChanged()
                    messages_list.adapter?.notifyItemInserted(0)
                    messages_list.scrollToPosition(0)
                }
            }
        }
        socketIOInstance?.onMessageNotification = {uniqueID ->
            runOnUiThread {
                val index = friends?.indexOfFirst { it.recipient.uniqueID == uniqueID }
                if (index != null && index >= 1) {
                    friends_list.adapter?.notifyItemChanged(index)
                }
                recent_list.adapter?.notifyDataSetChanged()
            }

        }
        socketIOInstance?.startEvents()

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


        Glide.with(this).load("https://supertiger.tk/api/avatars/" + (currentUser?.avatar ?: "default") + "?type=webp").placeholder(R.drawable.nertivia_logo).into(header_avatar);


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
                    //val jObjError = JSONObject(response.errorBody()?.string())
                    Toast.makeText(applicationContext, "Something bork while getting channel :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }

            }
        })
    }

    private fun getMessages(channelID: String?) {
        // check if message already loaded from before
        // check if notifications exist and dismiss it
        val notifyExist = notifications[channelID]
        if (notifyExist != null) {
            socketIOInstance?.dismissNotification(channelID)
        }
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        notificationClicked(intent)
    }

    private fun notificationClicked(intent: Intent?) {
        // check if notification clicked
        if (intent?.getStringExtra("notification:channelID") != null) {
            val channelID = intent.getStringExtra("notification:channelID");
            val username = intent.getStringExtra("notification:username");
            val uniqueID = intent.getStringExtra("notification:uniqueID");
            toolbar_title.text = username;
            removeNotification(channelID)
            getChannel(uniqueID)

        }
    }

    private fun removeNotification(channelID: String?) {
        val notificationInt = localNotifications.indexOfFirst { it.channelID == channelID };
        if (notificationInt >=0) {
            localNotifications.removeAt(notificationInt);
        }
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        inFocus = false;
    }

    override fun onResume() {
        super.onResume()
        inFocus = true;
        if (selectedChannelID != null) {
            socketIOInstance?.dismissNotification(selectedChannelID)
            removeNotification(selectedChannelID)
        }
    }

    fun friendsClicked(v: View) {
        drawerTab = 0;
        friends_button.alpha = 1F
        recent_button.alpha = 0.7F
        recent_list.visibility = View.GONE
        friends_list.visibility = View.VISIBLE

    }
    fun recentClicked(v: View) {
        drawerTab = 1;
        friends_button.alpha = 0.7F
        recent_button.alpha = 1F
        friends_list.visibility = View.GONE
        recent_list.visibility = View.VISIBLE;

    }
    private fun setRecentAdapter() {
        recent_list.layoutManager = LinearLayoutManager(this)
        recent_list.adapter = RecentListAdapter()
    }
    private fun setServerAdapter() {

        servers_list.layoutManager = LinearLayoutManager(this)
        servers_list.adapter = ServersListAdapter()
    }
    private fun setFriendAdapter() {
        friends_list.layoutManager = LinearLayoutManager(this)
        friends_list.adapter = FriendsListAdapter()
    }

}

