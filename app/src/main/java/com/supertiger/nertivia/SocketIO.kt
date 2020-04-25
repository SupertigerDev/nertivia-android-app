package com.supertiger.nertivia


import android.util.Log
import com.google.gson.Gson
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.*
import com.supertiger.nertivia.models.Friend
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONObject
import java.net.URISyntaxException
import java.sql.Struct


class SocketIO {
    var mSocket: Socket? = null
    val gson = Gson()
    var authenticated: Boolean = false;

    var onConnect: (() -> Unit)? = null
    var onDisconnect: (() -> Unit)? = null
    var onAuthenticate: (() -> Unit)? = null
    var onMessageReceive: ((selectedChannel: Boolean?) -> Unit)? = null
    var onMessageNotification: ((uniqueID: String?) -> Unit)? = null
    var onPresenceChange: (() -> Unit)? = null;
    var onTyping: (() -> Unit)? = null;

    fun connect(address: String) {
        mSocket = IO.socket(address)
        mSocket?.connect()
    }
    fun startEvents() {
        mSocket?.on("connect") {
            val obj = JSONObject()
            obj.put("token", token)
            mSocket?.emit("authentication", obj)
            onConnect?.invoke()
        }
        mSocket?.on("success") { args ->
            authenticated = true;
            val obj = args[0] as JSONObject
            val user = obj.getJSONObject("user");
            val settings = obj.getJSONObject("settings");

            val friendsArr = user.getJSONArray("friends");
            val serversArr = user.getJSONArray("servers");
            val dmsArr = obj.getJSONArray("dms")
            val notifsArr = obj.getJSONArray("notifications")
            val presences = obj.getJSONArray("memberStatusArr");

            val srvMem = obj.getJSONArray("serverMembers");


            gson.fromJson(presences.toString(), Array<Any?>::class.java).toList().forEach {
                val arr = gson.fromJson(it.toString(), Array<String?>::class.java).toList()
                if (arr[0] != null) {
                    userPresence.put(arr[0], arr[1]?.toInt());
                }
            }



            val notifs = gson.fromJson(notifsArr.toString(), Array<Notification>::class.java).toList().associateBy({it.channelID}, {it})
            val dmChannels = gson.fromJson(dmsArr.toString(), Array<Channel>::class.java).toList().associateBy({it.channelID}, {it})
            val srvs = gson.fromJson(serversArr.toString(), Array<Server>::class.java).toList().associateBy({it.server_id}, {it}) as MutableMap<String?, Server>
            serverMembers = gson.fromJson(srvMem.toString(), Array<ServerMember>::class.java).toMutableList()
            Log.d("OOWWOO", serverMembers.toString())

            friends = gson.fromJson(friendsArr.toString(), Array<Friend>::class.java).toList()
            channels = dmChannels as MutableMap<String?, Channel>
            notifications = notifs as MutableMap<String?, Notification>




            for (i in 0 until serversArr.length()) {
                val serverChannels = gson.fromJson((serversArr[i] as JSONObject).getJSONArray("channels").toString(), Array<Channel>::class.java).toList().associateBy({ it.channelID }, {it})
                channels = (serverChannels as MutableMap<String?, Channel> + channels).toMutableMap()

                val server = srvs.toList()[i].second
                serverChannelIDs[server.server_id] = serverChannels.map { it.value.channelID }

            }


            if (settings.has("server_position")) {
                val serverPosArr: JSONArray = settings.getJSONArray("server_position");
                val tempServers = srvs.toMutableMap();
                for (i in 0 until serverPosArr.length()) {
                    val serverID = serverPosArr.get(i).toString()
                    val find = tempServers.values.find {
                        it.server_id == serverID
                    }
                    if (find != null) {
                        tempServers.remove(find.server_id)
                        servers[find.server_id] = find;
                    }

                }
                servers = (tempServers + servers).toMutableMap();
            } else {
                servers = srvs
            }




            onAuthenticate?.invoke()
        }
        mSocket?.on("notification:dismiss") {args ->
            val obj = args[0] as JSONObject
            val channelID = obj["channelID"].toString();
            val user = notifications[channelID]?.sender;
            notifications = notifications.filter {
                it.value.channelID != channelID
            }.toMutableMap()
            onMessageNotification?.invoke(user?.uniqueID);
        }
        mSocket?.on("userStatusChange") {args ->
            val obj = args[0] as JSONObject
            val uniqueID = obj["uniqueID"].toString();
            val status = obj["status"].toString().toInt();
            userPresence.put(uniqueID, status);
            onPresenceChange?.invoke();
        }
        mSocket?.on("typingStatus") {args ->
            val obj = args[0] as JSONObject
            val user = obj["user"] as JSONObject
            val uniqueID = user["unique_id"] as String;
            val username = user["username"] as String;
            val channelID = obj["channel_id"] as String;

            if (uniqueID == currentUser?.uniqueID) return@on;

            addToTyping(channelID, username, uniqueID)

            onTyping?.invoke();
        }
        mSocket?.on("receiveMessage" ) { args ->
            val obj = args[0] as JSONObject
            val message = gson.fromJson(obj.getJSONObject("message").toString(), Message::class.java)
            if (channels[message?.channelID] != null) {
                channels[message?.channelID]?.lastMessaged = System.currentTimeMillis()
            }
            val messagesCount = messages[message?.channelID]?.size
            if (messagesCount != null) {
                if (message != null) {
                    messages[message.channelID]?.add(message)
                }
            }
            if (selectedChannelID != message?.channelID) {
                // add notification if channel is not selected
                // check if it exists
                val notification = notifications[message?.channelID];
                if (notification !== null && notification.count != null) {
                    notification.count = notification.count!! + 1;
                    notification.lastMessageID = message?.messageID;
                    notifications[message?.channelID] = notification;
                } else {
                    notifications[message?.channelID] = Notification(message?.channelID,1, message?.messageID, message?.creator )
                }
                onMessageNotification?.invoke(message?.creator?.uniqueID)
            } else {
                if (inFocus) {
                    dismissNotification(message?.channelID);
                }
            }
            removeToTyping(message.channelID!!, message.creator?.uniqueID!!);
            onMessageReceive?.invoke(selectedChannelID == message?.channelID)
        }
        mSocket?.on("disconnect") {
            authenticated = false;
            onDisconnect?.invoke()
       }
    }
    fun connected (): Boolean {
        return mSocket != null && mSocket?.connected() == true
    }
    fun authenticated (): Boolean {
        return authenticated
    }
    fun id () : String? {
        return mSocket?.id()
    }
    fun dismissNotification (channelID: String?): Emitter? {
        if (channelID != null) {
            val obj = JSONObject()
            obj.put("channelID", channelID)
            return mSocket?.emit("notification:dismiss", obj)
        }
        return null
    }
    fun disconnect() {
        mSocket?.disconnect()
    }
    fun reconnect() {
        mSocket?.connect()
    }
    fun emit (event: String, args: Any?): Boolean {
        if (mSocket == null || mSocket?.connected() == false || mSocket?.id().isNullOrEmpty()) {
            return false
        }
        return try {
            mSocket?.emit(event, args)
            true
        } catch (e : URISyntaxException) {
            false
        }
    }

}