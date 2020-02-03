package com.supertiger.nertivia.adapters

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.supertiger.nertivia.cache.messages
import com.supertiger.nertivia.cache.selectedChannelID
import kotlinx.android.synthetic.main.friends_list_template.view.user_avatar
import kotlinx.android.synthetic.main.friends_list_template.view.username
import kotlinx.android.synthetic.main.message_template.view.*
import android.view.Gravity
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.supertiger.nertivia.R
import com.supertiger.nertivia.cache.currentUser
import com.supertiger.nertivia.friendlyDate


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
        val message = messages[selectedChannelID]?.get(position)

        val user = messages[selectedChannelID]?.get(position)?.creator
        val username = user?.username
        val uniqueID = user?.uniqueID

        holder.itemView.username.text = username
        holder.itemView.message.text = message?.message
        holder.itemView.time.text = friendlyDate(message?.created);

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


        Glide.with(holder.itemView.context)
            .load("https://supertiger.tk/api/avatars/" + (message?.creator?.avatar ?: "default") + "?type=webp")
            .apply(RequestOptions().override(200, 200))
            .placeholder(R.drawable.nertivia_logo)
            .into(holder.itemView.user_avatar);

        if (message?.files != null && message.files?.size!! > 0) {
            if (message.message == null) {
                holder.itemView.message.visibility = View.GONE;
            }
            holder.itemView.image.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load("https://supertiger.tk/api/media/" + message.files!![0]?.fileID)
                .apply(RequestOptions().override(Resources.getSystem().displayMetrics.widthPixels))
                .into(holder.itemView.image);
        } else {
            holder.itemView.image.visibility = View.GONE;
        }

    }

}

class MessagesViewHolder(v: View): RecyclerView.ViewHolder(v) {


}