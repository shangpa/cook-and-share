/*채팅*/
package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.ChatItem
import com.example.test.model.ChatType
import com.example.test.model.chat.ChatMessage

class ChatAdapter(
    private val chatList: List<ChatMessage>,
    private val myId: Long
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.chatText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == 0) R.layout.chat_left else R.layout.chat_right
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatList[position].senderId == myId) 1 else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = chatList[position].message
    }

    override fun getItemCount(): Int = chatList.size
}