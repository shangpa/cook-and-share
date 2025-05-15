/*커뮤니티 메인*/
package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.test.model.board.CommunityDetailResponse
import com.example.test.model.board.CommunityPostResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
val token = App.prefs.token ?: ""
class CommunityMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_main)

        // postWrite 클릭했을 때 CommunityDetailActivity 이동
        val postWrite: ImageView = findViewById(R.id.postWrite)
        postWrite.setOnClickListener {
            val intent = Intent(this, CommunityWritePostActivity::class.java)
            startActivity(intent)
        }

        // categoryGroup 클릭했을 때 CommunityPopularActivity 이동
        val categoryGroup: GridLayout = findViewById(R.id.categoryGroup)
        categoryGroup.setOnClickListener {
            val intent = Intent(this, CommunityPopularActivity::class.java)
            startActivity(intent)
        }

        // cookAdd 클릭했을 때 CommunityCookActivity 이동
        val cookAdd: TextView = findViewById(R.id.cookAdd)
        cookAdd.setOnClickListener {
            val intent = Intent(this, CommunityCookActivity::class.java)
            startActivity(intent)
        }

        // hotAdd 클릭했을 때 CommunityPopularActivity 이동
        val hotAdd: TextView = findViewById(R.id.hotAdd)
        hotAdd.setOnClickListener {
            val intent = Intent(this, CommunityPopularActivity::class.java)
            startActivity(intent)
        }

        // postTwo 클릭했을 때 CommunityDetailActivity 이동
        val postTwo: LinearLayout = findViewById(R.id.postTwo)
        postTwo.setOnClickListener {
            val intent = Intent(this, CommunityDetailActivity::class.java)
            startActivity(intent)
        }

        // postThree 클릭했을 때 CommunityDetailActivity 이동
        val postThree: LinearLayout = findViewById(R.id.postThree)
        postThree.setOnClickListener {
            val intent = Intent(this, CommunityDetailActivity::class.java)
            startActivity(intent)
        }

        // postFour 클릭했을 때 CommunityDetailActivity 이동
        val postFour: LinearLayout = findViewById(R.id.postFour)
        postFour.setOnClickListener {
            val intent = Intent(this, CommunityDetailActivity::class.java)
            startActivity(intent)
        }

        // popularGroup 클릭했을 때 CommunityPopularActivity 이동
        val popularGroup: GridLayout = findViewById(R.id.popularGroup)
        popularGroup.setOnClickListener {
            val intent = Intent(this, CommunityPopularActivity::class.java)
            startActivity(intent)
        }

        // freeGroup 클릭했을 때 CommunityFreeActivity 이동
        val freeGroup: GridLayout = findViewById(R.id.freeGroup)
        freeGroup.setOnClickListener {
            val intent = Intent(this, CommunityFreeActivity::class.java)
            startActivity(intent)
        }

        // cookGroup 클릭했을 때 CommunityCookActivity 이동
        val cookGroup: GridLayout = findViewById(R.id.cookGroup)
        cookGroup.setOnClickListener {
            val intent = Intent(this, CommunityCookActivity::class.java)
            startActivity(intent)
        }

        val popularPost = findViewById<TextView>(R.id.popularPost)
        val freePost = findViewById<TextView>(R.id.freePost)
        val cookPost = findViewById<TextView>(R.id.cookPost)

        val tabList = listOf(
            Triple(popularPost, popularGroup, "popular"),
            Triple(freePost, freeGroup, "free"),
            Triple(cookPost, cookGroup, "cook")
        )

        //인기 게시글 TOP 10
        loadPopularPosts()

        fun showGroup(selected: GridLayout) {
            for ((tab, group, _) in tabList) {
                if (group == selected) {
                    tab.setBackgroundResource(R.drawable.ic_community_main_rect) // 선택 배경
                    tab.setTextColor(Color.parseColor("#2B2B2B")) // 선택 글자색
                    group.visibility = View.VISIBLE
                } else {
                    tab.setBackgroundResource(R.drawable.ic_community_main_rect_gray) // 비선택 배경
                    tab.setTextColor(Color.parseColor("#A1A9AD")) // 비선택 글자색
                    group.visibility = View.GONE
                }
            }
        }

        popularPost.setOnClickListener { showGroup(popularGroup) }
        freePost.setOnClickListener { showGroup(freeGroup) }
        cookPost.setOnClickListener { showGroup(cookGroup) }

        // 초기 탭
        showGroup(popularGroup)
    }
    private fun loadPopularPosts() {
        val categoryGroup = findViewById<GridLayout>(R.id.categoryGroup)
        categoryGroup.removeAllViews()

        RetrofitInstance.communityApi.getPopularPosts("Bearer $token")
            .enqueue(object : Callback<List<CommunityDetailResponse>> {
                override fun onResponse(
                    call: Call<List<CommunityDetailResponse>>,
                    response: Response<List<CommunityDetailResponse>>
                ) {
                    if (response.isSuccessful) {
                        val posts = response.body() ?: return
                        val inflater = LayoutInflater.from(this@CommunityMainActivity)

                        for (post in posts) {
                            val postView = inflater.inflate(R.layout.item_community_popular_post, categoryGroup, false)

                            val imageView = postView.findViewById<ImageView>(R.id.postImage)
                            val titleText = postView.findViewById<TextView>(R.id.postTitle)
                            val authorText = postView.findViewById<TextView>(R.id.postAuthor)
                            val likeCount = postView.findViewById<TextView>(R.id.postLike)

                            val imageUrl = post.imageUrls.firstOrNull()?.let {
                                RetrofitInstance.BASE_URL + it
                            }

                            Glide.with(this@CommunityMainActivity)
                                .load(imageUrl)
                                .placeholder(R.drawable.img_kitchen1)
                                .into(imageView)

                            titleText.text = post.content.take(20) + "..."
                            authorText.text = post.writer
                            likeCount.text = post.likeCount.toString()

                            postView.setOnClickListener {
                                val intent = Intent(this@CommunityMainActivity, CommunityDetailActivity::class.java)
                                intent.putExtra("postId", post.id)
                                startActivity(intent)
                            }

                            categoryGroup.addView(postView)
                        }
                    }
                }

                override fun onFailure(call: Call<List<CommunityDetailResponse>>, t: Throwable) {
                    Toast.makeText(this@CommunityMainActivity, "인기 게시물 로딩 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }
}