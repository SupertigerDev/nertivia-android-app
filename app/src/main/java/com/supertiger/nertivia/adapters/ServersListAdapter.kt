package com.supertiger.nertivia.adapters

import android.content.Context
import android.graphics.Color
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


        Glide.with(holder.itemView.context)
        .load("https://supertiger.tk/api/avatars/" + (server.avatar ?: "default") + "?type=webp")
        .apply(RequestOptions().override(200, 200))
        .placeholder(R.drawable.nertivia_logo)
        .into(holder.itemView.user_avatar);

        holder.server = server

    }
}

class ServerViewHolder(v: View, var server: Server? = null): RecyclerView.ViewHolder(v) {


}