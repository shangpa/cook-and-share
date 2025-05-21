package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.chat.ChatRoomListResponseDTO

class ChatRoomAdapter(
    private val chatRooms: List<ChatRoomListResponseDTO>,
    private val onItemClick: (ChatRoomListResponseDTO) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val opponentName: TextView = view.findViewById(R.id.chatUsernameTextView)
        val lastMessage: TextView = view.findViewById(R.id.chatLastMessageTextView)
        val lastTime: TextView = view.findViewById(R.id.chatTimeTextView)

        init {
            view.setOnClickListener {
                onItemClick(chatRooms[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = chatRooms.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatRooms[position]
        holder.opponentName.text = chat.opponentUsername
        holder.lastMessage.text = chat.lastMessageContent
        //마지막 메시지 시간에서 HH:mm만 추출
        val time = chat.lastMessageTime
        val formattedTime = if (time.contains("T") && time.length >= 16) {
            time.substring(0, 16).replace("T", " ")
        } else {
            ""
        }
        holder.lastTime.text = formattedTime
    }
}