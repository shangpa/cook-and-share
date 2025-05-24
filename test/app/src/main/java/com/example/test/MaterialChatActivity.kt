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

// tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageView = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenText: TextView = findViewById(R.id.tapVillageKitchenText)
        tapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageView = findViewById(R.id.tapRecipeIcon)
        tapRecipeIcon.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeText 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeText: TextView = findViewById(R.id.tapRecipeText)
        tapRecipeText.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapHomeIcon 클릭했을 때 MainActivity 이동
        val tapHomeIcon: ImageView = findViewById(R.id.tapHomeIcon)
        tapHomeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityIcon 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityIcon: ImageView = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageView = findViewById(R.id.tapFridgeIcon)
        tapFridgeIcon.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeText 클릭했을 때 FridgeActivity 이동
        val tapFridgeText: TextView = findViewById(R.id.tapFridgeText)
        tapFridgeText.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

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



