package com.example.test

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.ChatRoomItem

class ChatRoomAdapter(
    private val context: Context,
    private val roomList: MutableList<ChatRoomItem>, // 🔥 반드시 MutableList!
    private val myUserId: String
) : RecyclerView.Adapter<ChatRoomAdapter.RoomViewHolder>() {

    inner class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val roomNameText: TextView = view.findViewById(R.id.roomNameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.roomNameText.text = room.roomName

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MaterialChatDetailActivity::class.java)
            intent.putExtra("roomId", room.roomId)
            intent.putExtra("roomName", room.roomName)
            intent.putExtra("participants", myUserId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = roomList.size

    // 🔥 [추가] 채팅방 존재 여부 확인
    fun hasRoom(roomId: String): Boolean {
        return roomList.any { it.roomId == roomId }
    }

    // 🔥 [추가] 채팅방 새로 추가
    fun addRoom(chatRoomItem: ChatRoomItem) {
        roomList.add(0, chatRoomItem) // 새 방은 리스트 맨 위에 추가
        notifyItemInserted(0)
    }

    // 🔥 [추가] 채팅방 업데이트 (마지막 메시지 갱신)
    fun updateRoom(roomId: String, lastMessage: String, lastTime: Long) {
        val index = roomList.indexOfFirst { it.roomId == roomId }
        if (index != -1) {
            val oldItem = roomList[index]
            val newItem = oldItem.copy(roomName = lastMessage) // 🔥 방 이름 대신 마지막 메시지로 업데이트
            roomList[index] = newItem
            notifyItemChanged(index)
        }
    }
}
