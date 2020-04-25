package com.supertiger.nertivia.activities

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.supertiger.nertivia.*
import com.supertiger.nertivia.adapters.*
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.*
import com.supertiger.nertivia.models.User
import com.supertiger.nertivia.services.ChannelService
import com.supertiger.nertivia.services.MessageService
import com.supertiger.nertivia.services.ServiceBuilder
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.image_upload_preview_area.*
import kotlinx.android.synthetic.main.servers_members_drawer_layout.*
import kotlinx.android.synthetic.main.typing_area.*
import okhttp3.MediaType.parse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.InputStream
import javax.security.auth.callback.Callback
import kotlin.collections.set


class MainActivity : AppCompatActivity()  {
    private val channelService = ServiceBuilder.buildService(ChannelService::class.java)
    private val messageService = ServiceBuilder.buildService(MessageService::class.java)
    private var sharedPreference: SharedPreference? = null
    private var imagePickedUri: Uri? = null;
    var gson = Gson()
    val sendTypingHandler = Handler();
    var isSendTypingHandlerRunning = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        sharedPreference = SharedPreference(this)
        val userString = sharedPreference?.getValueString("user")
        currentUser = gson.fromJson(userString, User::class.java)
        token = sharedPreference?.getValueString("token")
        send_button.setOnClickListener{
            if (socketIOInstance?.authenticated() == false) {
                Toast.makeText(this, "Can't send message. Check your internet. (socket_not_authenticated)",  Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedChannelID === null) {
                Toast.makeText(this, "Select a friend!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val message = text_area.text.trim()
            text_area.setText("")
            if (imagePickedUri !== null) {
                sendFileMessage(selectedChannelID!!, message.toString())
                return@setOnClickListener
            }
            if (message.isEmpty()) {
                return@setOnClickListener
            }

            sendMessage(selectedChannelID!!, message.toString())


        }




        text_area.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.trim()?.isEmpty()!!) return;
                if (isSendTypingHandlerRunning) return;
                isSendTypingHandlerRunning = true;

                sendTypingHandler.post(object : Runnable {
                    override fun run() {
                        if (s.trim().isEmpty() || !inFocus || !text_area.hasFocus()){
                            sendTypingHandler.removeCallbacksAndMessages(null);
                            isSendTypingHandlerRunning = false;
                            return;
                        }

                        postTyping()
                        sendTypingHandler.postDelayed(this, 2000)
                    }
                })

            }


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })
        socketConnect()
        setDrawer()

        RxBus.listen(NamedEvent::class.java).subscribe {
            when {
                it.code == NamedEvent.FRIEND_CLICKED -> {
                    val uniqueID = it.sent
                    Handler().postDelayed({
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }, 0)
                    recent_list.adapter?.notifyDataSetChanged()
                    friends_list.adapter?.notifyDataSetChanged()
                    getChannel(uniqueID)
                    selectedUniqueID = uniqueID
                    toolBarPresenceChange()
                }
                it.code == NamedEvent.SERVER_CLICKED -> {
                    serverClicked()
                    setMembersListAdapter()
                }
                it.code == NamedEvent.CHANNEL_CLICKED -> {
                    Handler().postDelayed({
                        drawer_layout.closeDrawer(GravityCompat.START)
                    }, 0)
                    getMessages(it.sent)
                    toolbar_title.text = channels[it.sent]?.name
                    friends_list.adapter?.notifyDataSetChanged()
                    recent_list.adapter?.notifyDataSetChanged()
                    selectedUniqueID = null
                    toolBarPresenceChange()
                }
            }
        }

        pushNotificationClicked(intent)



        runOnUiThread {
            Handler().postDelayed(object : Runnable {
                override fun run() { //do your work here..
                    Handler().postDelayed(this, 3500)
                    showTypingStatus()
                }
            }, 2000)
        }
        settings_button.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java);
            startActivity(intent)

        }
    }

    fun postTyping() {
        Log.d("owowo", text_area.text.trim().isEmpty().toString())
        val requestCall = messageService.sendTyping(selectedChannelID)
        requestCall.enqueue(object: Callback, retrofit2.Callback<Any?> {
            override fun onFailure(call: Call<Any?>, t: Throwable) {
            }

            override fun onResponse(call: Call<Any?>, response: Response<Any?>) {
            }

        })
    }

    fun checkPermissionForImage(v: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permission, 1001) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(permissionCoarse, 1002) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 0) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {
            imagePickedUri = data?.data;
            image_upload_preview_area.visibility = View.VISIBLE;
            image_upload_preview_filename.text = getFileName(data?.data!!);
            image_upload_preview.setImageURI(data.data);
        }
    }
    fun clearImagePicked(v: View? = null) {
        imagePickedUri = null;
        image_upload_preview_area.visibility = View.GONE;
    }
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if ((uri.scheme == "content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()

            }
        }
        if (result == null) {
            result = uri.path
            val cut: Int = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }


    fun showTypingStatus() {
        if (selectedChannelID === null) return;
        val typingList = getTypingChannel(selectedChannelID!!);
        if (typingList === null || typingList.values.isEmpty()) {
            typing_box.visibility = View.GONE;
            return;
        }
        var show = false
        var str = "";


        val iterator: MutableIterator<MutableMap.MutableEntry<String, com.supertiger.nertivia.cache.User>> = typingList.iterator()
        while (iterator.hasNext()) {
            val user = iterator.next()
            if ((System.currentTimeMillis() - user.value.ms) >= 3500) {
                iterator.remove()
            } else {
                show = true;
                if (str === "") {
                    str += user.value.username
                } else {
                    str += ", " +user.value.username
                }

            }
        }

        if (!show) {
            typing_box.visibility = View.GONE;
        } else {
            if (typingList.size >=2) {
                typing_text.text = str
                typing_end_test.text = " are typing..."
            } else {
                typing_text.text = str
                typing_end_test.text = " is typing...";
            }
            typing_box.visibility = View.VISIBLE;
        }
    }


    private fun sendFileMessage(channelID: String?, message: String) {
        if (imagePickedUri === null) {return};


        val imageUri = imagePickedUri!!
        val filename = getFileName(imageUri);
        clearImagePicked();


        val inputStream: InputStream? = contentResolver.openInputStream(imageUri)


        if (inputStream != null) {
            File(cacheDir.path, filename).outputStream().use { inputStream.copyTo(it) }
        }
        val file = File(cacheDir.path, filename);

        val mime = contentResolver.getType(imageUri)

        val requestFile: RequestBody =
            RequestBody.create(parse(mime),  file)

        val messageForm =
            RequestBody.create(parse(mime), message)
        val cdnForm =
            RequestBody.create(parse(mime), "1")

        val fileForm =
            MultipartBody.Part.createFormData("file", getFileName(imageUri), requestFile)

        Toast.makeText(applicationContext,  "Uploading...", Toast.LENGTH_SHORT).show()
        val requestCall = messageService.sendFileMessage(channelID!!, messageForm,  cdnForm, fileForm);

        requestCall.enqueue(object: Callback, retrofit2.Callback<PostMessageResponse?> {
            override fun onFailure(call: Call<PostMessageResponse?>, t: Throwable) {
                File(cacheDir.path, filename).delete()
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(
                call: Call<PostMessageResponse?>,
                response: Response<PostMessageResponse?>
            ) {
                File(cacheDir.path, filename).delete()
                Toast.makeText(applicationContext,  "Uploaded!", Toast.LENGTH_SHORT).show()
            }

        })
    }
    private fun sendMessage(channelID: String, message: String) {
        sendTypingHandler.removeCallbacksAndMessages(null);
        isSendTypingHandlerRunning = false;

        val jsonObject = JsonObject()

        jsonObject.addProperty("message", message)
        jsonObject.addProperty("channelID", channelID)
        jsonObject.addProperty("created", System.currentTimeMillis() )
        jsonObject.add("creator", gson.toJsonTree(currentUser) )


        addMessage( gson.fromJson(jsonObject, Message::class.java) )
        val addedMessage = messages[channelID]?.last()

        val requestCall = messageService.sendMessage(channelID, MessageSendData(
            message,
            socketIOInstance?.id()
        ))
        requestCall.enqueue(object: Callback, retrofit2.Callback<PostMessageResponse?> {
            override fun onFailure(call: Call<PostMessageResponse?>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<PostMessageResponse?>, response: Response<PostMessageResponse?>) {
                if (response.isSuccessful) {
                    if (addedMessage != null) {
                        addedMessage.messageID = response.body()?.messageCreated!!.messageID
                    }

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
            if (!inFocus) {
                socketIOInstance?.disconnect();
            }
        }
        socketIOInstance?.onDisconnect = {
            if (!inFocus) {
                socketIOInstance?.disconnect();
            }
        }
        socketIOInstance?.onAuthenticate = {
            socketIOInstance?.dismissNotification(selectedChannelID)
            runOnUiThread{
                Toast.makeText(applicationContext, "Authenticated", Toast.LENGTH_SHORT).show()
                setFriendAdapter()
                setRecentAdapter()
                setServerAdapter()
                if (intent.getStringExtra("notification:serverID") != null) {
                    serverClicked()
                    setMembersListAdapter()
                }
                checkPrivateNotification()
                toolBarPresenceChange()
                if (selectedChannelID !== null && inFocus) {
                    checkForNewMessages(selectedChannelID)
                }
            }
        }
        socketIOInstance?.onMessageReceive = { fromSelectedChannel ->
            runOnUiThread {
                recent_list.adapter?.notifyDataSetChanged()
                showTypingStatus()
                if (fromSelectedChannel == true) {
                    addMessageToAdapterAndScroll()
                }
            }
        }
        socketIOInstance?.onPresenceChange = {
            runOnUiThread {
                toolBarPresenceChange()
                updateFriendsListData()
            }
        }
        socketIOInstance?.onTyping = {
            runOnUiThread { showTypingStatus() }
        }
        socketIOInstance?.onMessageNotification = {uniqueID ->
            runOnUiThread {
                val index =  recyclerViewData().indexOfFirst { it.friend?.recipient?.uniqueID == uniqueID }
                if ( index >= 1) {
                    friends_list.adapter?.notifyItemChanged(index)
                }
                recent_list.adapter?.notifyDataSetChanged()
                channels_list.adapter?.notifyDataSetChanged()
                servers_list.adapter?.notifyDataSetChanged()
                checkPrivateNotification()
            }
        }
        socketIOInstance?.startEvents()

    }
    private fun toolBarPresenceChange() {

        val test = listOf("#919191","#27eb48", "#ffdd1e", "#ea0b1e", "#9a3dd3")


        if (selectedUniqueID != null) {
            if (userPresence[selectedUniqueID] != null) {

                presence_toolbar.background.setColorFilter(Color.parseColor(test[userPresence[selectedUniqueID]!!]), PorterDuff.Mode.SRC_ATOP)
            } else  {
                presence_toolbar.background.setColorFilter(Color.parseColor(test[0]), PorterDuff.Mode.SRC_ATOP)
            }
            presence_toolbar.visibility = View.VISIBLE
        } else {
            presence_toolbar.visibility = View.GONE
        }
    }
    private fun checkPrivateNotification() {

        for (notification in notifications) {
            if (channels[notification.value.channelID] === null || channels[notification.value.channelID]?.server_id == null) {
                dm_notification_alert.visibility = View.VISIBLE
                return
            }
        }
        dm_notification_alert.visibility = View.GONE
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
        tag.text = "@${currentUser?.tag}"


        Glide.with(this)
            .load(if (currentUser?.avatar != null) "https://nertivia-media.tk/${currentUser?.avatar}?type=webp" else "")
            .placeholder(R.drawable.nertivia_logo).into(header_avatar)


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
        removePushNotification(channelID, applicationContext)
        if(messages[channelID] !== null) {
            selectedChannelID = channelID
            setMessagesAdapter()
            // check if new messages exist. (just in case if client disconnected)
            checkForNewMessages(channelID)
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
                        messages[channelID] = response.body()?.messages!!.reversed().toMutableList()
                        selectedChannelID = channelID
                        setMessagesAdapter()

                    }
                } else {
                    //val jObjError = JSONObject(response.errorBody()?.string())
                    Toast.makeText(applicationContext, "Something bork while getting messages :/ report to fishie asap!!" , Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun checkForNewMessages(channelID: String?) {
        val msgs = messages[channelID]

        if (msgs === null) {
            return
        }
        val lastMessageID = msgs.last().messageID

        messageService.getMessagesBefore(channelID, lastMessageID).enqueue(object: Callback, retrofit2.Callback<GetMessagesResponse> {

            override fun onFailure(call: Call<GetMessagesResponse>, t: Throwable) {
                Toast.makeText(applicationContext,  t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<GetMessagesResponse>, response: Response<GetMessagesResponse>) {
                if (response.isSuccessful) {
                    if (response.body()?.messages !== null && response.body()?.messages!!.isNotEmpty()) {
                        val newMessages = response.body()?.messages
                        messages[channelID] = (messages[channelID]!!.plus(newMessages!!)).distinctBy { it.messageID }.toMutableList()
                        if (selectedChannelID === channelID) {
                            setMessagesAdapter()
                        }
                    }
                    Log.d("testowo", msgs.first().toString())
                    Log.d("testowo", response.body()?.messages?.size.toString())
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
                messages[message.channelID]?.add( message)
            }
        }
        if (selectedChannelID != message?.channelID) {
            return
        }
        runOnUiThread {
            addMessageToAdapterAndScroll()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        pushNotificationClicked(intent)
    }

    private fun pushNotificationClicked(intent: Intent?) {
        // check if notification clicked
        if (intent?.getStringExtra("notification:channelID") != null) {

            val channelID = intent.getStringExtra("notification:channelID")
            val username = intent.getStringExtra("notification:username")
            val uniqueID = intent.getStringExtra("notification:uniqueID")
            var channelName:String? = null
            var serverID:String? = null

            if (intent.getStringExtra("notification:serverID") != null) {
                serverID = intent.getStringExtra("notification:serverID")
                channelName = intent.getStringExtra("notification:channelName")
            }

            if (serverID != null) {
                toolbar_title.text = channelName
                getMessages(channelID)
                selectedUniqueID = null
                selectedServerID = serverID
                serverClicked()
                setMembersListAdapter()
            } else {
                toolbar_title.text = username
                removePushNotification(channelID, applicationContext)
                getChannel(uniqueID)
                selectedUniqueID = uniqueID
            }
            toolBarPresenceChange()
            updateFriendsListData()
            recent_list.adapter?.notifyDataSetChanged()
            friends_list.adapter?.notifyDataSetChanged()
        }
    }


    private fun serverClicked() {
        drawerTab = -1
        friends_button.alpha = 0.7F
        recent_button.alpha = 0.7F
        recent_list.visibility = View.GONE
        friends_list.visibility = View.GONE
        server_tab.visibility = View.VISIBLE
        if (servers[selectedServerID]?.banner === null) {
            server_banner.visibility = View.GONE;
        } else {
            server_banner.visibility = View.VISIBLE;
        }
        Glide.with(applicationContext)
            .load(if (servers[selectedServerID]?.banner != null) "https://nertivia-media.tk/${servers[selectedServerID]?.banner}?type=webp" else "")
            .apply(RequestOptions().override(240, 130).centerCrop())
            .into(server_banner)
        server_banner.clipToOutline = true

        setChannelsListAdapter()
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
        inFocus = false
    }

    override fun onResume() {
        super.onResume()
        inFocus = true
        if (socketIOInstance != null) {
            socketIOInstance?.reconnect();
        }
        if (selectedChannelID != null) {
            socketIOInstance?.dismissNotification(selectedChannelID)
            removePushNotification(selectedChannelID, applicationContext)
        }
    }

    fun friendsClicked(v: View) {
        selectedServerID = null
        servers_list.adapter?.notifyDataSetChanged()
        drawerTab = 0
        friends_button.alpha = 1F
        recent_button.alpha = 0.7F
        server_tab.visibility = View.GONE
        recent_list.visibility = View.GONE
        friends_list.visibility = View.VISIBLE

    }
    fun recentClicked(v: View) {
        selectedServerID = null
        servers_list.adapter?.notifyDataSetChanged()
        drawerTab = 1
        friends_button.alpha = 0.7F
        recent_button.alpha = 1F
        server_tab.visibility = View.GONE
        friends_list.visibility = View.GONE
        recent_list.visibility = View.VISIBLE
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
    private fun updateFriendsListData() {
        if (friends_list.adapter != null) {
            (friends_list.adapter as FriendsListAdapter).updateList();
        }
    }
    private fun addMessageToAdapterAndScroll() {
        if (messages_list.adapter != null) {
            (messages_list.adapter as MessagesListAdapter).addMessage();
            messages_list.scrollToPosition(0);
        }
    }
    private fun setChannelsListAdapter() {
        channels_list.layoutManager = LinearLayoutManager(this)
        channels_list.adapter = ChannelsListAdapter()
    }

    private fun setMembersListAdapter() {
        members_list.layoutManager = LinearLayoutManager(this)
        members_list.adapter = MembersListAdapter()
    }

}

