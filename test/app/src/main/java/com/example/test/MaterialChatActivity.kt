package com.example.test

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.ChatRoomAdapter
import com.example.test.model.chat.ChatRoomListResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_chat) // 다른 프로필 화면의 레이아웃 파일 연결



        // chatBack 클릭했을 때 MainActivity 이동
        val chatBack: ImageView = findViewById(R.id.chatBack)
        chatBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        loadChatRooms()


    }
    private fun loadChatRooms() {
        val token = App.prefs.token ?: return

        RetrofitInstance.chatApi.getChatRooms("Bearer $token")
            .enqueue(object : Callback<List<ChatRoomListResponseDTO>> {
                override fun onResponse(
                    call: Call<List<ChatRoomListResponseDTO>>,
                    response: Response<List<ChatRoomListResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val chatRooms = response.body() ?: return
                        val recyclerView = findViewById<RecyclerView>(R.id.chatRoomRecyclerView)
                        recyclerView.layoutManager = LinearLayoutManager(this@MaterialChatActivity)
                        recyclerView.adapter = ChatRoomAdapter(chatRooms) { chat ->
                            val intent = Intent(this@MaterialChatActivity, MaterialChatDetailActivity::class.java)
                            intent.putExtra("roomKey", chat.roomKey)
                            intent.putExtra("receiverId", chat.opponentId) // ✅ 추가!
                            intent.putExtra("opponentNickname", chat.opponentUsername)
                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(call: Call<List<ChatRoomListResponseDTO>>, t: Throwable) {
                    Log.e("Chat", "채팅방 리스트 불러오기 실패", t)
                }
            })
    }

}



