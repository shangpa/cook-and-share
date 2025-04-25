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
    private lateinit var loginid: String
    private lateinit var loginpassword: String
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        // 서버에서 온 메시지를 수신 → UI에 표시
        mSocket.on("update") { args ->
            val data = Gson().fromJson(args[0].toString(), Chatting::class.java)
            addChat(data)
        }

        // 전송 버튼 클릭 시 메시지 보내기
        binding.push.setOnClickListener {
            sendMessage()
        }

        // RecyclerView 어댑터 설정
        adapter = ChatAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // 뒤로가기 버튼 클릭 시
        val chatDetailBack: ImageView = findViewById(R.id.chatDetailBack)
        chatDetailBack.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun init() {
        try {
            mSocket = IO.socket("http://10.0.2.2:3001")
            Log.d("SOCKET", "Connection success : ${mSocket.id()}")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        intent?.let {
            loginid = it.getStringExtra("loginid").orEmpty()
            loginpassword = it.getStringExtra("loginpassword").orEmpty()
        }

        mSocket.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.emit("left", Gson().toJson(ChattingRoom(loginid, loginpassword)))
        mSocket.disconnect()
    }

    private fun sendMessage() {
        val message = Chatting(
            type = "MESSAGE",
            from = loginid,
            to = loginpassword,
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
                    val type = if (data.from == loginid) ChatType.RIGHT_CONTENT else ChatType.LEFT_CONTENT
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

