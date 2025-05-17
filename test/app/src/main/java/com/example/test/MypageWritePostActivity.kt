package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.MyCommunityPostAdapter
import com.example.test.model.board.CommunityDetailResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageWritePostActivity : AppCompatActivity() {

    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private lateinit var backButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyCommunityPostAdapter
    private val postList = mutableListOf<CommunityDetailResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_write_post)

        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)
        backButton = findViewById(R.id.backButton)

        fridgeRecipeResultDropDownIcon.setOnClickListener { showDropdownMenu() }

        backButton.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewMyPost)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MyCommunityPostAdapter(postList) { post, anchorView ->
            val popupMenu = PopupMenu(this, anchorView)
            popupMenu.menu.add("수정")
            popupMenu.menu.add("삭제")
            popupMenu.setOnMenuItemClickListener {
                when (it.title) {
                    "수정" -> {
                        val intent = Intent(this, CommunityEditPostActivity::class.java).apply {
                            putExtra("postId", post.id)
                            putExtra("content", post.content)
                            putExtra("imageUrls", ArrayList(post.imageUrls)) // List<String> → ArrayList<String>
                            putExtra("boardType", post.boardType)
                        }
                        startActivity(intent)
                        true
                    }
                    "삭제" -> {
                        val token = App.prefs.token ?: return@setOnMenuItemClickListener false

                        RetrofitInstance.apiService.deletePost(post.id, "Bearer $token")
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@MypageWritePostActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                        postList.remove(post)
                                        adapter.notifyDataSetChanged()
                                        findViewById<TextView>(R.id.writePostResultNumber).text = postList.size.toString()
                                    } else {
                                        Toast.makeText(this@MypageWritePostActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(this@MypageWritePostActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                                }
                            })

                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
        recyclerView.adapter = adapter

        loadMyPosts()
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeRecipeResultDropDownIcon)
        popupMenu.menu.add("최신순")
        popupMenu.menu.add("댓글순")
        popupMenu.menu.add("추천순")

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            fridgeRecipefillterText.text = item.title

            when (item.title) {
                "최신순" -> {
                    postList.sortByDescending { it.createdAt }
                }
                "댓글순" -> {
                    postList.sortByDescending { it.commentCount }
                }
                "추천순" -> {
                    postList.sortByDescending { it.likeCount }
                }
            }

            adapter.notifyDataSetChanged()
            true
        }

        popupMenu.show()
    }

    private fun loadMyPosts() {
        val token = App.prefs.token ?: return

        RetrofitInstance.apiService.getMyPosts("Bearer $token")
            .enqueue(object : Callback<List<CommunityDetailResponse>> {
                override fun onResponse(
                    call: Call<List<CommunityDetailResponse>>,
                    response: Response<List<CommunityDetailResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        postList.clear()
                        postList.addAll(response.body()!!)
                        postList.sortByDescending { it.createdAt }
                        adapter.notifyDataSetChanged()

                        // 게시글 수 텍스트뷰 갱신
                        findViewById<TextView>(R.id.writePostResultNumber).text =
                            postList.size.toString()
                    }
                }

                override fun onFailure(call: Call<List<CommunityDetailResponse>>, t: Throwable) {
                    Log.e("MypageWritePost", "불러오기 실패: ${t.message}")
                }
            })
    }

}
