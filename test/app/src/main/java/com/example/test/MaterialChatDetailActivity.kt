package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityMaterialChatDetailBinding
import com.example.test.model.ChatItem
import com.example.test.model.ChatType
import com.example.test.model.Chatting
import com.example.test.model.ChattingRoom
import com.google.gson.Gson
import java.net.URISyntaxException
import io.socket.client.IO
import io.socket.client.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaterialChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialChatDetailBinding
    private lateinit var mSocket: Socket
    private lateinit var roomId: String
    private lateinit var roomName: String
    private lateinit var participants: String
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSocket()

        adapter = ChatAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.push.setOnClickListener {
            sendMessage()
        }

        binding.chatDetailBack.setOnClickListener {
            startActivity(Intent(this, MaterialChatActivity::class.java))
            finish()
        }

        // 인텐트 데이터 받기
        roomId = intent.getStringExtra("roomId").orEmpty()
        roomName = intent.getStringExtra("roomName").orEmpty()
        participants = App.prefs.userId.toString() // 로그인한 내 ID 가져오기

        mSocket.connect()

        // 방 입장하기
        val enterData = mapOf(
            "userName" to participants,
            "roomName" to roomId
        )
        mSocket.emit("enter", Gson().toJson(enterData))
    }

    private fun initSocket() {
        try {
            mSocket = IO.socket("http://10.0.2.2:3001")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        intent?.let {
            roomId = it.getStringExtra("roomId").orEmpty()
            roomName = it.getStringExtra("roomName").orEmpty()
        }

        mSocket.connect()

        mSocket.on("update") { args ->
            val data = Gson().fromJson(args[0].toString(), Chatting::class.java)

            runOnUiThread {
                addChat(data) // ✅ 채팅 메시지만 추가
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.emit("left", Gson().toJson(ChattingRoom(roomId, roomName, listOf(participants))))
        mSocket.disconnect()
    }

    private fun sendMessage() {
        val message = Chatting(
            type = "MESSAGE",
            from = participants,
            to = roomId,
            content = binding.contentEdit.text.toString(),
            sendTime = System.currentTimeMillis()
        )

        mSocket.emit("newMessage", Gson().toJson(message))
        addChat(message)
        binding.contentEdit.setText("")
    }

    private fun addChat(data: Chatting) {
        runOnUiThread {
            val chatItem = when (data.type) {
                "ENTER", "LEFT" -> ChatItem(
                    from = data.from,
                    content = data.content,
                    time = toDate(data.sendTime),
                    type = ChatType.CENTER_CONTENT
                )
                else -> {
                    val type = if (data.from == participants) ChatType.RIGHT_CONTENT else ChatType.LEFT_CONTENT
                    ChatItem(
                        from = data.from,
                        content = data.content,
                        time = toDate(data.sendTime),
                        type = type
                    )
                }
            }

            adapter.addItem(chatItem)
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun toDate(currentMillis: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(currentMillis))
    }
}
