/*요리 게시판*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.BoardPostAdapter
import com.example.test.model.board.CommunityDetailResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityBoardActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BoardPostAdapter
    private var currentSort: String = "latest"
    private var currentBoardType: String = "cooking"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_cook)
        val boardTypeFromIntent = intent.getStringExtra("boardType")
        if (boardTypeFromIntent == "popular") {
            currentBoardType = "popular"
            findViewById<TextView>(R.id.cookPost).text = "인기 게시판"
            loadPopularBoards()
        } else if (boardTypeFromIntent == "free") {
            currentBoardType = "free"
            findViewById<TextView>(R.id.cookPost).text = "자유 게시판"
            loadBoards(currentBoardType, currentSort)
        } else {
            currentBoardType = "cooking" // 기본값
            findViewById<TextView>(R.id.cookPost).text = "요리 게시판"
            loadBoards(currentBoardType, currentSort)
        }
//        // postWrite 클릭했을 때 WritePost 이동
//        val postWrite: ImageView = findViewById(R.id.postWrite)
//        postWrite.setOnClickListener {
//            val intent = Intent(this, CommunityWritePostActivity::class.java)
//            startActivity(intent)
//        }

        recyclerView = findViewById(R.id.boardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BoardPostAdapter { postId ->
            // 상세로 이동
            val intent = Intent(this, CommunityDetailActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadBoards(currentBoardType, currentSort)



        val dropDown = findViewById<ImageView>(R.id.dropDown)
        val dropDownTwo = findViewById<ImageView>(R.id.dropDownTwo)
        val cookPost = findViewById<TextView>(R.id.cookPost)
        val recommend = findViewById<TextView>(R.id.recommend)

        //요리 게시판 드롭다운 버튼 클릭
        val showBoardTypeMenu = {
            val popup = PopupMenu(this, dropDown)
            val items = listOf("인기 게시판", "요리 게시판", "자유 게시판")

            items.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title) {
                    "인기 게시판" -> {
                        cookPost.text = item.title
                        loadPopularBoards()
                    }
                    "요리 게시판" -> {
                        cookPost.text = item.title
                        currentBoardType = "cooking"
                        loadBoards(currentBoardType, currentSort)
                    }
                    "자유 게시판" -> {
                        cookPost.text = item.title
                        currentBoardType = "free"
                        loadBoards(currentBoardType, currentSort)
                    }
                }
                true
            }

            popup.show()
        }
        dropDown.setOnClickListener { showBoardTypeMenu() }
        cookPost.setOnClickListener { showBoardTypeMenu() }

        //최신순 드롭다운 버튼 클릭
        val showSortMenu = {
            val popup = PopupMenu(this, dropDownTwo)
            val items = listOf("추천순", "댓글순", "최신순")
            items.forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { item: MenuItem ->
                recommend.text = item.title
                currentSort = when (item.title) {
                    "추천순" -> "like"
                    "댓글순" -> "comment"
                    else -> "latest"
                }
                loadBoards(currentBoardType, currentSort)
                true
            }
            popup.show()
        }
        dropDownTwo.setOnClickListener { showSortMenu() }
        recommend.setOnClickListener { showSortMenu() }
    }
    private fun loadBoards(type: String, sort: String) {
        val token = App.prefs.token ?: ""
        RetrofitInstance.communityApi.getBoardsByType(
            type = type,
            token = "Bearer $token",
            sort = sort
        ).enqueue(object : Callback<List<CommunityDetailResponse>> {
            override fun onResponse(
                call: Call<List<CommunityDetailResponse>>,
                response: Response<List<CommunityDetailResponse>>
            ) {
                if (response.isSuccessful) {
                    val posts = response.body() ?: emptyList()
                    adapter.submitList(posts)
                } else {
                    Toast.makeText(this@CommunityBoardActivity, "불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CommunityDetailResponse>>, t: Throwable) {
                Toast.makeText(this@CommunityBoardActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadPopularBoards() {
        val token = App.prefs.token ?: ""
        RetrofitInstance.communityApi.getPopularPosts("Bearer $token")
            .enqueue(object : Callback<List<CommunityDetailResponse>> {
                override fun onResponse(
                    call: Call<List<CommunityDetailResponse>>,
                    response: Response<List<CommunityDetailResponse>>
                ) {
                    if (response.isSuccessful) {
                        val posts = response.body() ?: emptyList()
                        adapter.submitList(posts)
                    } else {
                        Toast.makeText(this@CommunityBoardActivity, "인기 게시판 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<CommunityDetailResponse>>, t: Throwable) {
                    Toast.makeText(this@CommunityBoardActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
}