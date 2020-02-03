package com.supertiger.nertivia.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.supertiger.nertivia.NamedEvent
import com.supertiger.nertivia.R
import com.supertiger.nertivia.RxBus
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.Server
import com.supertiger.nertivia.models.User
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import kotlinx.android.synthetic.main.friends_list_template.view.*
import kotlinx.android.synthetic.main.friends_list_template.view.user_avatar
import kotlinx.android.synthetic.main.servers_list_template.view.*

private var row_index: Int = -1
class ServersListAdapter: RecyclerView.Adapter<ServerViewHolder>() {

    override fun getItemCount(): Int {
        return servers.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.servers_list_template, parent, false)
        return ServerViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {

        val server = servers.toList()[position].second

        holder.itemView.server_notification_alert.visibility = View.GONE
        serverChannelIDs[server.server_id]?.forEach {
            if (notifications[it] != null) {
                holder.itemView.server_notification_alert.visibility = View.VISIBLE
            }
        }

        Glide.with(holder.itemView.context)
        .load("https://supertiger.tk/api/avatars/" + (server.avatar ?: "default") + "?type=webp")
        .apply(RequestOptions().override(200, 200))
        .placeholder(R.drawable.nertivia_logo)
        .into(holder.itemView.user_avatar);

        holder.server = server

        holder.itemView.setOnClickListener {
            selectedServerID = server.server_id;
            RxBus.publish(
                NamedEvent(
                    NamedEvent.SERVER_CLICKED,
                    server.server_id
                )
            )
            notifyDataSetChanged()
        }

        if (selectedServerID == server.server_id) {
            holder.itemView.selected_status.visibility = View.VISIBLE
        } else {
            holder.itemView.selected_status.visibility = View.GONE
        }

    }
}

class ServerViewHolder(v: View, var server: Server? = null): RecyclerView.ViewHolder(v) {


}