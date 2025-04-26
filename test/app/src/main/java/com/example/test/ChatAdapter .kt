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

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val items = mutableListOf<ChatItem>()

    fun addItem(item: ChatItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            ChatType.LEFT_CONTENT -> 0
            ChatType.RIGHT_CONTENT -> 1
            ChatType.CENTER_CONTENT -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = when (viewType) {
            0 -> R.layout.chat_left
            1 -> R.layout.chat_right
            else -> R.layout.chat_center
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: ChatItem) {
            itemView.findViewById<TextView>(R.id.chatContent).text = item.content
            itemView.findViewById<TextView?>(R.id.chatTime)?.text = item.time
        }
    }
}
