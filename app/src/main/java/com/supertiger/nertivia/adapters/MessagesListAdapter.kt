package com.supertiger.nertivia.adapters

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.supertiger.nertivia.R
import com.supertiger.nertivia.cache.currentUser
import com.supertiger.nertivia.cache.getRecyclerMessages
import com.supertiger.nertivia.friendlyDate
import com.supertiger.nertivia.models.Message
import com.supertiger.nertivia.models.MessageRecyclerView
import kotlinx.android.synthetic.main.file_template.view.*
import kotlinx.android.synthetic.main.message_bubble.view.*
import kotlinx.android.synthetic.main.message_template.view.*
import kotlinx.android.synthetic.main.message_template.view.message_status
import kotlinx.android.synthetic.main.message_template.view.user_avatar
import kotlinx.android.synthetic.main.message_template.view.edit_icon
import kotlinx.android.synthetic.main.presence_message_template.view.*
import kotlinx.android.synthetic.main.presence_message_template.view.details
import kotlinx.android.synthetic.main.presence_message_template.view.time
import kotlinx.android.synthetic.main.presence_message_template.view.username
import kotlinx.android.synthetic.main.sub_message_template.view.*
import java.io.File


class MessagesListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages = getRecyclerMessages();

    fun addMessage() {
        messages = getRecyclerMessages()
        notifyItemInserted(0)
    }
    fun dataSetChanged() {
        messages = getRecyclerMessages()
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val cellForRow = layoutInflater.inflate(R.layout.message_template, parent, false)
            return MainMessageViewHolder(cellForRow)
        } else if (viewType == 1) {
            val layoutInflater = LayoutInflater.from(parent.context)
            val cellForRow = layoutInflater.inflate(R.layout.sub_message_template, parent, false)
            return SubMessageViewHolder(cellForRow)
        } else {
            val layoutInflater = LayoutInflater.from(parent.context)
            val cellForRow = layoutInflater.inflate(R.layout.presence_message_template, parent, false)
            return PresenceMessage(cellForRow)
        }

    }

    override fun getItemId(position: Int): Long {
        val message = messages[position];
        return message.message.messageID.hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position];
        return message.type;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when {
            message.type == 0 -> {
                (holder as MainMessageViewHolder).bind(message);
            }
            message.type == 1 -> {
                (holder as SubMessageViewHolder).bind(message);
            }
            message.type >= 2 -> {
                (holder as PresenceMessage).bind(message);
            }
        }
    }
}

fun messageReversed(isSelf: Boolean, view: View, type: Int) {

    if (isSelf) {
        if (view.triangle != null) {
            view.triangle.setBackgroundResource(R.drawable.message_triangle_reversed)
        }
        if (type == 1) {
            view.bubble.setBackgroundResource(R.drawable.sub_message_background_reversed)
        } else {
            view.bubble.setBackgroundResource(R.drawable.message_background_reversed)
        }

        view.bubble.layoutDirection = View.LAYOUT_DIRECTION_LTR

        view.layoutDirection = View.LAYOUT_DIRECTION_RTL

        val params = view.bubble.layoutParams as ConstraintLayout.LayoutParams
        params.startToEnd = view.message_status.id;
        if (view.triangle !== null) {
            params.endToStart = view.triangle.id
        } else {
            params.endToStart = view.user_avatar_fake.id
            params.horizontalBias = 1F
        }


    } else {
        if (view.triangle != null) {
            view.triangle.setBackgroundResource(R.drawable.message_triangle)
        }
        if (type == 1) {
            view.bubble.setBackgroundResource(R.drawable.sub_message_background)
        } else {
            view.bubble.setBackgroundResource(R.drawable.message_background)
        }


        view.layoutDirection = View.LAYOUT_DIRECTION_LTR
        val params = view.bubble.layoutParams as ConstraintLayout.LayoutParams
        params.endToStart = view.message_status.id;

        if (view.triangle !== null) {
            params.startToEnd = view.triangle.id
        } else {
            params.startToEnd = view.user_avatar_fake.id
            params.horizontalBias = 0F
        }

    }



}

fun fileMessage(view: View, item: Message) {
    if (item.files != null && item.files!!.isNotEmpty()) {
        val file = item.files?.get(0);
        if (item.message.isNullOrEmpty()) {
            view.message.visibility = View.GONE;
        } else {
            view.message.visibility = View.VISIBLE;
        }
        val imageFormats = arrayListOf("jpeg", "jpg", "gif", "png")
        if (file!!.url !== null || imageFormats.contains(File(file!!.fileName).extension)) {
            val url: String;
            if (file!!.url !== null) {
                url = file!!.url.toString();
            } else {
                url = "https://supertiger.tk/api/media/" + file!!.fileID;
            }
            view.file.visibility = View.GONE;
            view.image.visibility = View.VISIBLE
            Glide.with(view.context)
                .load(url)
                .apply(RequestOptions().override(Resources.getSystem().displayMetrics.widthPixels))
                .into(view.image);
        } else {
            view.file.visibility = View.VISIBLE
            view.file.file_text.text = file.fileName
            view.image.visibility = View.GONE;
            view.file.setOnClickListener {
                val url = "https://supertiger.tk/api/files/" + file.fileID + "/" + file.fileName
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                view.context.startActivity(i);
            }
        }

    } else {
        view.message.visibility = View.VISIBLE;
        view.file.visibility = View.GONE;
        view.image.visibility = View.GONE;
    }
}

class MainMessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
    private val messageArea = v.message;
    private val username = v.username;
    private val avatar = v.user_avatar;
    private val context = v.context;
    private val time = v.time;
    private val view = v;

    fun bind (item: MessageRecyclerView) {
        messageArea.text = item.message.message
        username.text = item.message.creator?.username
        time.text = friendlyDate(item.message.created);
        Glide.with(context)
            .load(if (item.message.creator?.avatar != null) "https://nertivia-media.tk/${item.message.creator?.avatar}?type=webp" else "")
            .apply(RequestOptions().override(200, 200))
            .placeholder(R.drawable.nertivia_logo)
            .into(avatar);
        messageReversed(currentUser?.uniqueID == item.message.creator?.uniqueID, view, 0);
        fileMessage(view, item.message);
        if (item.message.timeEdited !== null) {
            view.edit_icon.visibility = View.VISIBLE
        } else {
            view.edit_icon.visibility = View.GONE
        }
    }

}


class SubMessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
    private val messageArea = v.message;
    private val view = v;
    fun bind (item: MessageRecyclerView) {
        messageArea.text = item.message.message
        messageReversed(currentUser?.uniqueID == item.message.creator?.uniqueID, view, 1);
        fileMessage(view, item.message);
        if (item.message.timeEdited !== null) {
            view.edit_icon.visibility = View.VISIBLE
        } else {
            view.edit_icon.visibility = View.GONE
        }
    }

}

fun presenceType(pos: Int): Array<String> {
    when (pos) {
        0 -> return arrayOf(" joined the server!", "#29bf12")
        1 -> return arrayOf(" left the server.", "#9a9a9a")
        2 -> return arrayOf(" has been kicked.", "#ff9914")
        3 -> return arrayOf(" has been banned.", "#d92121")
    }
    return arrayOf("Unknown")
}

class PresenceMessage(v: View): RecyclerView.ViewHolder(v) {

    private val username = v.username;
    private val presenceMessage= v.presence_message;
    private val time = v.time;
    fun bind (item: MessageRecyclerView) {
        val type = presenceType(item.type - 2);
        username.text = item.message.creator?.username;
        time.text = friendlyDate(item.message.created);
        presenceMessage.setTextColor(Color.parseColor(type[1]))
        presenceMessage.text = type[0]
    }

}