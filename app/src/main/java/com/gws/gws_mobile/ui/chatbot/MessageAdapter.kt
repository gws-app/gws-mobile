package com.gws.gws_mobile.ui.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gws.gws_mobile.R

class MessageAdapter(private val messageList: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = if (viewType == Message.SENDER_USER) {
            LayoutInflater.from(parent.context).inflate(R.layout.item_user_message, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.item_bot_message, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        return messageList[position].sender
    }

    fun updateMessages(newMessages: MutableList<Message>) {
        messageList.clear()
        messageList.addAll(newMessages)
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.textViewMessage)

        fun bind(message: Message) {
            messageText.text = message.text
        }
    }
}
