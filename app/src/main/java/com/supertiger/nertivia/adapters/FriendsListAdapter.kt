package com.supertiger.nertivia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.supertiger.nertivia.NamedEvent
import com.supertiger.nertivia.R
import com.supertiger.nertivia.RxBus
import com.supertiger.nertivia.cache.friends
import com.supertiger.nertivia.models.User
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
        holder.itemView.username.text = user?.username ?: ""
        Picasso.get().load(
            "https://supertiger.tk/api/avatars/" + (user?.avatar ?: "default") + "?type=png"
        ).placeholder(R.drawable.nertivia_logo).error(R.drawable.nertivia_logo).into(holder.itemView.user_avatar)

        holder.user = user
    }

}

class FriendsViewHolder(v: View, var user: User? = null): RecyclerView.ViewHolder(v) {

    init {
        v.setOnClickListener{
            // Add event
            RxBus.publish(
                NamedEvent(
                    NamedEvent.FRIEND_CLICKED,
                    user?.uniqueID
                )
            )
        }
    }
}