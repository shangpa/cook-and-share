package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.ChatItem
import com.example.test.model.ChatType

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatItem>()

    fun addItem(item: ChatItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            ChatType.LEFT_CONTENT -> 0    // 상대방 메시지
            ChatType.RIGHT_CONTENT -> 1   // 내 메시지
            ChatType.CENTER_CONTENT -> 2  // 입장/퇴장 알림
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.chat_left       // 상대방 말풍선
            1 -> R.layout.chat_right      // 내 말풍선
            else -> R.layout.chat_center   // 가운데 입장/퇴장
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val contentText: TextView = view.findViewById(R.id.chatContent)
        private val timeText: TextView? = view.findViewById(R.id.chatTime) // center 레이아웃은 시간 없을 수도 있음

        fun bind(item: ChatItem) {
            contentText.text = item.content
            timeText?.text = item.time
        }
    }
}
