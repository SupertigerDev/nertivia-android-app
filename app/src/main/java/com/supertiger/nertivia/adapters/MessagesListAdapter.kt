package com.supertiger.nertivia.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.supertiger.nertivia.cache.messages
import com.supertiger.nertivia.cache.selectedChannelID
import kotlinx.android.synthetic.main.friends_list_template.view.user_avatar
import kotlinx.android.synthetic.main.friends_list_template.view.username
import kotlinx.android.synthetic.main.message_template.view.*
import android.view.Gravity
import android.widget.LinearLayout
import com.supertiger.nertivia.R
import com.supertiger.nertivia.cache.currentUser


class MessagesListAdapter: RecyclerView.Adapter<MessagesViewHolder>() {

    override fun getItemCount(): Int {
        return messages[selectedChannelID]?.size!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.message_template, parent, false)
        return MessagesViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val message = messages[selectedChannelID]?.get(position)?.message
        val user = messages[selectedChannelID]?.get(position)?.creator
        val username = user?.username
        val uniqueID = user?.uniqueID

        holder.itemView.username.text = username
        holder.itemView.message.text = message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // if the message is by us, set the gravity to right.
            if (currentUser?.uniqueID == uniqueID) {
                holder.itemView.triangle.setBackgroundResource(R.drawable.message_triangle_reversed)
                holder.itemView.details.setBackgroundResource(R.drawable.message_background_reversed)
                holder.itemView.details.layoutDirection = View.LAYOUT_DIRECTION_LTR

                holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_RTL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.END
                holder.itemView.layoutParams = params
            } else {
                holder.itemView.triangle.setBackgroundResource(R.drawable.message_triangle)
                holder.itemView.details.setBackgroundResource(R.drawable.message_background)
                holder.itemView.details.layoutDirection = View.LAYOUT_DIRECTION_LTR

                holder.itemView.layoutDirection = View.LAYOUT_DIRECTION_LTR
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.gravity = Gravity.START
                holder.itemView.layoutParams = params
            }
        }

        Picasso.get().load(
            "https://supertiger.tk/api/avatars/" + (messages[selectedChannelID]?.get(position)?.creator?.avatar ?: "default") + "?type=png"
        ).placeholder(R.drawable.nertivia_logo).error(R.drawable.nertivia_logo).into(holder.itemView.user_avatar)
    }

}

class MessagesViewHolder(v: View): RecyclerView.ViewHolder(v) {


}