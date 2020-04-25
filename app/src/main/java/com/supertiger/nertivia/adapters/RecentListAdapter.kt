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

class RecentListAdapter: RecyclerView.Adapter<RecentViewHolder>() {

    override fun getItemCount(): Int {
        return dmRecentsSorted().size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.friends_list_template, parent, false)
        return RecentViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: RecentViewHolder, position: Int) {

        val user = dmRecentsSorted()[position].recipients?.first();

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
            .load(if (user?.avatar != null) "https://nertivia-media.tk/${user.avatar}?type=webp" else "")
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

class RecentViewHolder(v: View, var user: User? = null): RecyclerView.ViewHolder(v) {


}