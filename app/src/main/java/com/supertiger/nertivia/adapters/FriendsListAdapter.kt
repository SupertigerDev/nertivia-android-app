package com.supertiger.nertivia.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.supertiger.nertivia.NamedEvent
import com.supertiger.nertivia.R
import com.supertiger.nertivia.RxBus
import com.supertiger.nertivia.cache.channels
import com.supertiger.nertivia.cache.notifications
import com.supertiger.nertivia.cache.recyclerViewData
import com.supertiger.nertivia.cache.selectedUniqueID
import com.supertiger.nertivia.models.FriendsRecyclerData
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.friends_list_header.view.*
import kotlinx.android.synthetic.main.friends_list_template.view.*

class FriendsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var data = recyclerViewData()

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateList() {
        data = recyclerViewData()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val type = data[position].type;
        if (type == 0) {
            return 0
        } else {
            return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View;
        if (viewType == 1) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.friends_list_header, parent, false);
            return HeaderViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.friends_list_template, parent, false);
            return FriendViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position];

        when (item.type) {
            0 -> {
                val notificationsCount = notifications.values.find {
                    it.sender?.uniqueID == item.friend?.recipient?.uniqueID && channels[it.channelID]?.server_id === null
                }?.count
                (holder as FriendViewHolder).bind(item, notificationsCount);
            }
            1 -> (holder as HeaderViewHolder).bind(item);
        }

        //val user = friends?.get(position)?.recipient
        //val notifyCount = notifications.values.find {
        //  it.sender?.uniqueID == user?.uniqueID && channels[it.channelID]?.server_id === null
        // }?.count
        // holder.itemView.username.text = user?.username ?: ""

        // if (notifyCount != null) {
        //     holder.itemView.notification_count.text = notifyCount.toString()
        //     holder.itemView.notification_count.visibility = View.VISIBLE
        //} else {
        //     holder.itemView.notification_count.visibility = View.GONE
        // }

        // Glide.with(holder.itemView.context)
        //   .load("https://supertiger.tk/api/avatars/" + (user?.avatar ?: "default") + "?type=webp")
        //   .apply(RequestOptions().override(200, 200))
        //    .placeholder(R.drawable.nertivia_logo)
        //    .into(holder.itemView.user_avatar);


        // holder.user = user

        //holder.itemView.setOnClickListener {
        //  RxBus.publish(
        //      NamedEvent(
        //          NamedEvent.FRIEND_CLICKED,
        //         user?.uniqueID
        //     )
        // )
        // selectedUniqueID = user?.uniqueID
        // notifyDataSetChanged()
        //}
        //if (selectedUniqueID == user?.uniqueID) {
        //    holder.itemView.setBackgroundResource(R.drawable.friend_list_selected_background)
        // } else {
        //   holder.itemView.setBackgroundResource(R.drawable.friend_list_background)
        /// }
        //}
    }

}

class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private var title: TextView = v.title;
    fun bind(item: FriendsRecyclerData) {
        title.text = item.headerName;
    }
}

class FriendViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    private var view = v;
    private var username: TextView = v.username;
    private var userAvatar: CircleImageView = v.user_avatar
    private var notificationCount: TextView = v.notification_count;
    private var context: Context = v.context

    fun bind(item: FriendsRecyclerData, notifyCount: Int?) {
        if (item.friend?.recipient === null) return;
        username.text = item.friend.recipient.username;

        if (notifyCount != null) {
            notificationCount.text = notifyCount.toString()
            notificationCount.visibility = View.VISIBLE
        } else {
            notificationCount.visibility = View.GONE
        }
        if (selectedUniqueID == item.friend.recipient.uniqueID) {
            view.setBackgroundResource(R.drawable.friend_list_selected_background)
        } else {
            view.setBackgroundResource(R.drawable.friend_list_background)
        }

        view.setOnClickListener {
            selectedUniqueID = item.friend.recipient.uniqueID
            RxBus.publish(
                NamedEvent(
                    NamedEvent.FRIEND_CLICKED,
                    item.friend.recipient.uniqueID
                )
            )

        }

        val avatar = item.friend.recipient.avatar
        Glide.with(context)
            .load(if (avatar != null) "https://nertivia-media.tk/$avatar?type=webp" else "")
            .apply(RequestOptions().override(200, 200))
            .placeholder(R.drawable.nertivia_logo)
            .into(userAvatar);
    }

}