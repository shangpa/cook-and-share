package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.Utils.ChatSessionManager
import com.example.test.databinding.ActivityMaterialChatDetailBinding
import com.example.test.model.chat.ChatMessage
import com.example.test.model.chat.ChatMessageDTO
import com.example.test.model.chat.UsernameResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

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
        val opponentUsername = intent.getStringExtra("opponentNickname") ?: "상대방"
        binding.chatDetailTitle.text = opponentUsername


        roomKey = intent.getStringExtra("roomKey") ?: return
        ChatSessionManager.currentChatRoomKey = roomKey
        senderId = App.prefs.userId.toLong()
        chatAdapter = ChatAdapter(chatList, senderId)
        binding.recyclerView.adapter = chatAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
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
        loadPreviousMessages()
        val roomParts = roomKey.split("-")
        val buyer = roomParts.getOrNull(0)?.toLongOrNull()     // 구매자 ID
        val seller = roomParts.getOrNull(1)?.toLongOrNull()    // 판매자 ID
        val postId = roomParts.getOrNull(2)?.toLongOrNull()    // 게시글 ID
        val myId = App.prefs.userId.toLong()

        Log.d("ChatDebug", "🧾 내 ID (myId): $myId")
        Log.d("ChatDebug", "📦 게시물 작성자 ID (receiverId): $")

        if (seller != null && myId == seller) {
            // 판매자라면 버튼 숨김
            binding.requestCompleteButton.visibility = View.GONE

            Log.d("Chat", "👑 판매자이므로 거래완료 요청 버튼 숨김")
        } else {
            // 구매자라면 버튼 보임
            binding.requestCompleteButton.visibility = View.VISIBLE
            Log.d("Chat", "🛒 구매자이므로 거래완료 요청 버튼 보임")
        }
        binding.requestCompleteButton.setOnClickListener {
            val postId = intent.getLongExtra("postId", -1L).takeIf { it != -1L }
                ?: run {
                    val parsedId = roomKey.split("-").lastOrNull()?.toLongOrNull()
                    if (parsedId == null) {
                        Log.e("Chat", "❌ roomKey에서 postId 파싱 실패")
                        -1L
                    } else {
                        Log.d("Chat", "✅ roomKey에서 postId 파싱 성공: $parsedId")
                        parsedId
                    }
                }

            Log.d("postId", "받은 거래글 ID: $postId")
            val token = App.prefs.token ?: return@setOnClickListener

            if (postId == -1L) {
                Log.e("RequestComplete", "거래글 ID가 없습니다.")
                return@setOnClickListener
            }

            RetrofitInstance.materialApi.requestComplete("Bearer $token", postId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MaterialChatDetailActivity, "거래완료 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e("RequestComplete", "❌ 요청 실패: ${response.code()} / $errorBody")

                            when {
                                errorBody?.contains("포인트가 부족합니다") == true -> {
                                    Toast.makeText(this@MaterialChatDetailActivity, "포인트가 부족해서 거래요청을 보내지 못합니다.", Toast.LENGTH_SHORT).show()
                                }
                                errorBody?.contains("이미 요청한 사용자입니다") == true -> {
                                    Toast.makeText(this@MaterialChatDetailActivity, "이미 거래요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Log.e("RequestComplete", "📦 서버에서 받은 에러 내용: $errorBody")
                                    Toast.makeText(this@MaterialChatDetailActivity, "서버 에러: $errorBody", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("RequestComplete", "❌ 요청 실패", t)
                        Toast.makeText(this@MaterialChatDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.chatDetailBack).setOnClickListener {
            finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        ChatSessionManager.currentChatRoomKey = null
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
    private fun loadPreviousMessages() {
        val token = App.prefs.token ?: return

        RetrofitInstance.chatApi.getMessages("Bearer $token", roomKey)
            .enqueue(object : Callback<List<ChatMessageDTO>> {
                override fun onResponse(
                    call: Call<List<ChatMessageDTO>>,
                    response: Response<List<ChatMessageDTO>>
                ) {
                    if (response.isSuccessful) {
                        val messages = response.body() ?: return
                        chatList.clear()
                        chatList.addAll(messages.map { ChatMessage(it.roomKey, it.senderId, it.message, it.createdAt) })
                        chatAdapter.notifyDataSetChanged()
                        binding.recyclerView.scrollToPosition(chatList.size - 1)
                    }
                }

                override fun onFailure(call: Call<List<ChatMessageDTO>>, t: Throwable) {
                    Log.e("Chat", "이전 메시지 불러오기 실패", t)
                }
            })
    }
}

