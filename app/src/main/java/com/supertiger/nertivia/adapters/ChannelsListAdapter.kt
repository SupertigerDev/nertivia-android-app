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
import com.supertiger.nertivia.models.Channel
import com.supertiger.nertivia.models.User
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import kotlinx.android.synthetic.main.channels_list_template.view.*
import kotlinx.android.synthetic.main.friends_list_template.view.*
import kotlinx.android.synthetic.main.friends_list_template.view.notification_count

class ChannelsListAdapter: RecyclerView.Adapter<ChannelViewHolder>() {

    override fun getItemCount(): Int {
        return if (serverChannelIDs[selectedServerID]?.size != null) serverChannelIDs[selectedServerID]?.size!! else 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.channels_list_template, parent, false)
        return ChannelViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[serverChannelIDs[selectedServerID]?.get(position)]


        holder.itemView.channel_name.text = channel?.name


        val notifyCount = notifications.values.find {
            it.channelID == channel?.channelID
        }?.count

        if (notifyCount != null) {
            holder.itemView.notification_count.text = notifyCount.toString()
            holder.itemView.notification_count.visibility = View.VISIBLE
        } else {
            holder.itemView.notification_count.visibility = View.GONE
        }


        holder.channel = channel

        holder.itemView.setOnClickListener {
            RxBus.publish(
                NamedEvent(
                    NamedEvent.CHANNEL_CLICKED,
                    channel?.channelID
                )
            )
            selectedChannelID = channel?.channelID
            selectedUniqueID = null;
            notifyDataSetChanged()
        }
        if (selectedChannelID == channel?.channelID) {
            holder.itemView.setBackgroundResource(R.drawable.friend_list_selected_background)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.friend_list_background)
        }

    }
}

class ChannelViewHolder(v: View, var channel: Channel? = null): RecyclerView.ViewHolder(v) {


}