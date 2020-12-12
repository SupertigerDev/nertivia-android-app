package com.supertiger.nertivia.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.supertiger.nertivia.R
import com.supertiger.nertivia.cache.*
import com.supertiger.nertivia.models.User
import kotlinx.android.synthetic.main.friends_list_template.view.*

class MembersListAdapter: RecyclerView.Adapter<MembersViewHolder>() {

    override fun getItemCount(): Int {
        return selectedServerMembers().size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.server_member_template, parent, false)
        return MembersViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val user = selectedServerMembers()[position].member;


        holder.itemView.username.text = user?.username ?: ""


        Glide.with(holder.itemView.context)
            .load(if (user?.avatar != null) "https://media.nertivia.net/${user.avatar}?type=webp" else "")
            .apply(RequestOptions().override(200, 200))
            .placeholder(R.drawable.nertivia_logo)
            .into(holder.itemView.user_avatar);


        holder.user = user

    }
}

class MembersViewHolder(v: View, var user: User? = null): RecyclerView.ViewHolder(v) {


}