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
import com.supertiger.nertivia.models.User
import kotlinx.android.synthetic.main.activity_drawer_layout.*
import kotlinx.android.synthetic.main.friends_list_template.view.*

class FriendsListAdapter: RecyclerView.Adapter<FriendsViewHolder>() {

    override fun getItemCount(): Int {
        return friends?.size!!
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.friends_list_template, parent, false)
        return FriendsViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val user = friends?.get(position)?.recipient
        val notifyCount = notifications.values.find {
            it.sender?.uniqueID == user?.uniqueID && channels[it.channelID]?.server_id === null
        }?.count
        holder.itemView.username.text = user?.username ?: ""

        if (notifyCount != null) {
            holder.itemView.notification_count.text = notifyCount.toString()
            holder.itemView.notification_count.visibility = View.VISIBLE
        } else {
            holder.itemView.notification_count.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load("https://supertiger.tk/api/avatars/" + (user?.avatar ?: "default") + "?type=webp")
            .apply(RequestOptions().override(200, 200))
            .placeholder(R.drawable.nertivia_logo)
            .into(holder.itemView.user_avatar);


        holder.user = user

        holder.itemView.setOnClickListener {
            RxBus.publish(
                NamedEvent(
                    NamedEvent.FRIEND_CLICKED,
                    user?.uniqueID
                )
            )
            selectedUniqueID = user?.uniqueID
            notifyDataSetChanged()
        }
        if (selectedUniqueID == user?.uniqueID) {
            holder.itemView.setBackgroundResource(R.drawable.friend_list_selected_background)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.friend_list_background)
        }
    }
}

class FriendsViewHolder(v: View, var user: User? = null): RecyclerView.ViewHolder(v) {


}