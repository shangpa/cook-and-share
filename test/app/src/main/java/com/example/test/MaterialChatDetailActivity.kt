package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.databinding.ActivityMaterialChatDetailBinding
import com.example.test.model.ChatItem
import com.example.test.model.ChatType
import com.example.test.model.Chatting
import com.example.test.model.ChattingRoom
import com.example.test.model.chat.ChatMessage
import com.example.test.model.chat.UsernameResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import java.net.URISyntaxException
import io.socket.client.IO
import io.socket.client.Socket
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MaterialChatDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMaterialChatDetailBinding
    private lateinit var stompClient: StompClient
    private lateinit var roomKey: String
    private var senderId: Long = 0

    private val gson = Gson()
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter // RecyclerView 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaterialChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomKey = intent.getStringExtra("roomKey") ?: return
        senderId = App.prefs.userId.toLong()
        chatAdapter = ChatAdapter(chatList, senderId)
        binding.recyclerView.adapter = chatAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val receiverId = intent.getLongExtra("receiverId", -1L)
        val token = App.prefs.token.toString()
        if (receiverId != -1L) {
            RetrofitInstance.chatApi.getUserProfileById("Bearer $token", receiverId)
                .enqueue(object : Callback<UsernameResponse> {
                    override fun onResponse(call: Call<UsernameResponse>, response: Response<UsernameResponse>) {
                        if (response.isSuccessful) {
                            val username = response.body()?.username ?: "상대방"
                            binding.chatDetailTitle.text = username
                        } else {
                            binding.chatDetailTitle.text = "상대방"
                        }
                    }

                    override fun onFailure(call: Call<UsernameResponse>, t: Throwable) {
                        Log.e("Chat", "상대 이름 조회 실패", t)
                        binding.chatDetailTitle.text = "상대방"
                    }
                })        }
        connectStomp()

        binding.push.setOnClickListener {
            val messageText = binding.contentEdit.text.toString()
            if (messageText.isNotBlank()) {
                val message = ChatMessage(roomKey, senderId, messageText)
                val json = gson.toJson(message)
                Log.d("STOMP", "보내는 메시지 roomKey=${message.roomKey}, from=$senderId, content=${message.message}")
                stompClient.send("/app/chat.send", json)
                    .subscribe({
                        Log.d("STOMP", "✅ 메시지 전송 성공")
                    }, { error ->
                        Log.e("STOMP", "❌ 메시지 전송 실패", error)
                    })
                binding.contentEdit.setText("")

            }
        }

        binding.chatDetailBack.setOnClickListener {
            finish()
        }
    }
    @SuppressLint("CheckResult")
    private fun connectStomp() {
        val token = App.prefs.token
        val wsUrl = "${RetrofitInstance.BASE_URL}/ws/websocket?token=$token"
            .replace("http://", "ws://")
            .replace("https://", "wss://")

        // stompClient 초기화
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            wsUrl
        )

        // lifecycle 로그
        stompClient.lifecycle()
            .subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED ->
                        Log.d("STOMP", "WebSocket 연결됨")
                    LifecycleEvent.Type.CLOSED ->
                        Log.d("STOMP", " WebSocket 연결 종료됨")
                    LifecycleEvent.Type.ERROR ->
                        Log.e("STOMP", "WebSocket 오류", lifecycleEvent.exception)
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT ->
                        Log.w("STOMP", "서버 하트비트 실패")
                }
            }, { error ->
                Log.e("STOMP", "라이프사이클 에러", error)
            })

        // connect 헤더 추가 (JWT Authorization 포함)
        val headers = listOf(
            StompHeader("Authorization", "Bearer $token")
        )

        stompClient.connect(headers)
        // ✅ 4. topic 구독
        stompClient.topic("/topic/chatroom/$roomKey")
            .subscribe({ topicMessage ->
                val chat = gson.fromJson(topicMessage.payload, ChatMessage::class.java)
                runOnUiThread {
                    chatList.add(chat)
                    chatAdapter.notifyItemInserted(chatList.size - 1)
                    binding.recyclerView.scrollToPosition(chatList.size - 1)
                }
            }, { error ->
                Log.e("STOMP", "❌ 메시지 구독 실패", error)
            })
    }

}

