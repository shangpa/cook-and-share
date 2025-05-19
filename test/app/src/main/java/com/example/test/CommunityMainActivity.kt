/*커뮤니티 메인*/
package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.test.model.board.CommunityMainResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "popular")   // 인기게시판 모드
            startActivity(intent)
        }

        // cookAdd 클릭했을 때 CommunityCookActivity 이동
        val cookAdd: TextView = findViewById(R.id.cookAdd)
        cookAdd.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "cooking")
            startActivity(intent)
        }

        // hotAdd 클릭했을 때 CommunityPopularActivity 이동
        val hotAdd: TextView = findViewById(R.id.hotAdd)
        hotAdd.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "popular")   // 인기게시판 모드
            startActivity(intent)
        }


        // popularGroup 클릭했을 때 CommunityPopularActivity 이동
        val popularGroup: GridLayout = findViewById(R.id.popularGroup)
        popularGroup.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "popular")   // 인기게시판 모드
            startActivity(intent)
        }

        // freeGroup 클릭했을 때 CommunityFreeActivity 이동
        val freeGroup: GridLayout = findViewById(R.id.freeGroup)
        freeGroup.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "free")
            startActivity(intent)
        }

        // cookGroup 클릭했을 때 CommunityCookActivity 이동
        val cookGroup: GridLayout = findViewById(R.id.cookGroup)
        cookGroup.setOnClickListener {
            val intent = Intent(this, CommunityBoardActivity::class.java)
            intent.putExtra("boardType", "cooking")
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

        //메인 게시판
        loadCommunityMain()
        //인기 게시글 TOP 10
        loadPopularPosts()
        // 음식 레시피 어떤 레시피가 맛있을까?
        loadDeliciousRecipes()
        //hot 게시물
        loadHotPosts()

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
        val token = App.prefs.token ?: ""
        Log.d("TokenDebug", "loadPopularPosts token = $token")
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
    private fun loadCommunityMain() {
        val token = App.prefs.token ?: ""
        Log.d("TokenDebug", "loadCommunityMain token = $token")
        RetrofitInstance.communityApi.getCommunityMain("Bearer $token")
            .enqueue(object : Callback<CommunityMainResponse> {
                override fun onResponse(
                    call: Call<CommunityMainResponse>,
                    response: Response<CommunityMainResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return

                        // 각각의 게시글 목록 세팅
                        addCommunityPostToGroup(findViewById(R.id.popularGroup), data.popularBoards)
                        addCommunityPostToGroup(findViewById(R.id.freeGroup), data.freeBoards)
                        addCommunityPostToGroup(findViewById(R.id.cookGroup), data.cookBoards)
                    } else {
                        Toast.makeText(this@CommunityMainActivity, "게시글 로딩 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CommunityMainResponse>, t: Throwable) {
                    Toast.makeText(this@CommunityMainActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun addCommunityPostToGroup(
        group: GridLayout,
        posts: List<CommunityDetailResponse>
    ) {
        group.removeAllViews()
        val context = group.context
        val dp = { value: Int -> (value * context.resources.displayMetrics.density).toInt() }

        for ((idx, post) in posts.withIndex()) {
            // 바깥 LinearLayout (세로)
            val verticalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = LinearLayout.LayoutParams.WRAP_CONTENT
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    rightMargin = dp(20)
                    topMargin = if (idx == 0) 0 else dp(21)
                }
                gravity = android.view.Gravity.START
            }

            // 안쪽 LinearLayout (가로)
            val horizontalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(7), 0, 0)
            }

            // 이미지
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
                scaleType = ImageView.ScaleType.CENTER_CROP
                val imageUrl = post.imageUrls.firstOrNull()?.let { RetrofitInstance.BASE_URL + it }
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_kitchen1)
                    .into(this)
            }

            // 텍스트 세로 레이아웃
            val textLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(15), 0, 0, 0)
            }

            // 첫 번째 텍스트(내용)
            val contentText = TextView(context).apply {
                text = post.content.let {
                    if (it.length > 20) it.take(20) + "..." else it
                }
                textSize = 13f
                setTextColor(Color.parseColor("#2B2B2B"))
                setPadding(0, dp(11), 0, 0)
            }

            // 두 번째 줄(작성자 | 날짜)
            val bottomLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(7), 0, 0)
            }

            val authorText = TextView(context).apply {
                text = post.writer
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
            }

            val divider = TextView(context).apply {
                text = "|"
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
                setPadding(dp(6), 0, dp(6), 0)
            }

            val dateText = TextView(context).apply {
                // createdAt 예시 "2024-05-16T10:30:15" -> "05.16 10:30"
                text = post.createdAt.let {
                    try {
                        if (it.length >= 16) "${it.substring(5,7)}.${it.substring(8,10)} ${it.substring(11,16)}"
                        else it
                    } catch (e: Exception) { it }
                }
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
            }

            bottomLayout.addView(authorText)
            bottomLayout.addView(divider)
            bottomLayout.addView(dateText)

            textLayout.addView(contentText)
            textLayout.addView(bottomLayout)

            horizontalLayout.addView(imageView)
            horizontalLayout.addView(textLayout)
            verticalLayout.addView(horizontalLayout)

            // 클릭 이벤트
            verticalLayout.setOnClickListener {
                val intent = Intent(context, CommunityDetailActivity::class.java)
                intent.putExtra("postId", post.id)
                context.startActivity(intent)
            }

            group.addView(verticalLayout)
        }
    }
    private fun loadDeliciousRecipes() {
        val token = App.prefs.token ?: ""
        RetrofitInstance.communityApi.getCommunityMain("Bearer $token")
            .enqueue(object : Callback<CommunityMainResponse> {
                override fun onResponse(
                    call: Call<CommunityMainResponse>,
                    response: Response<CommunityMainResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        val cookTop3 = data.cookBoards.take(3)
                        addDeliciousRecipeToGroup(findViewById(R.id.categoryGroupTwo), cookTop3)
                    }
                }
                override fun onFailure(call: Call<CommunityMainResponse>, t: Throwable) {
                    Toast.makeText(this@CommunityMainActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun addDeliciousRecipeToGroup(
        group: GridLayout,
        posts: List<CommunityDetailResponse>
    ) {
        group.removeAllViews()
        val context = group.context
        val dp = { value: Int -> (value * context.resources.displayMetrics.density).toInt() }

        for ((idx, post) in posts.withIndex()) {
            // 1. 바깥 LinearLayout (vertical)
            val verticalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = LinearLayout.LayoutParams.WRAP_CONTENT
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    rightMargin = dp(20)
                    topMargin = if (idx == 0) 0 else dp(21)
                }
                gravity = android.view.Gravity.START
            }

            // 2. 안쪽 LinearLayout (horizontal)
            val horizontalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(7), 0, 0)
            }

            // 3. 이미지
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val imageUrl = post.imageUrls.firstOrNull()?.let { RetrofitInstance.BASE_URL + it }
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_kitchen1)
                    .into(this)
            }

            // 4. 텍스트(수직)
            val textVerticalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = dp(15)
                }
            }

            // 5. 첫 번째 텍스트(내용)
            val contentText = TextView(context).apply {
                text = post.content.let {
                    if (it.length > 20) it.take(20) + "..." else it
                }
                textSize = 13f
                setTextColor(Color.parseColor("#2B2B2B"))
                setPadding(0, dp(11), 0, 0)
            }

            // 6. 두 번째 줄(작성자 | 날짜)
            val bottomLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(7), 0, 0)
            }

            val authorText = TextView(context).apply {
                text = post.writer
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
            }

            val divider = TextView(context).apply {
                text = "|"
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
                setPadding(dp(6), 0, dp(6), 0)
            }

            val dateText = TextView(context).apply {
                text = post.createdAt.let {
                    try {
                        if (it.length >= 16) "${it.substring(5,7)}.${it.substring(8,10)} ${it.substring(11,16)}"
                        else it
                    } catch (e: Exception) { it }
                }
                textSize = 11f
                setTextColor(Color.parseColor("#8A8A8A"))
            }

            // 조립
            bottomLayout.addView(authorText)
            bottomLayout.addView(divider)
            bottomLayout.addView(dateText)
            textVerticalLayout.addView(contentText)
            textVerticalLayout.addView(bottomLayout)
            horizontalLayout.addView(imageView)
            horizontalLayout.addView(textVerticalLayout)
            verticalLayout.addView(horizontalLayout)

            // 클릭시 상세이동
            verticalLayout.setOnClickListener {
                val intent = Intent(context, CommunityDetailActivity::class.java)
                intent.putExtra("postId", post.id)
                context.startActivity(intent)
            }

            group.addView(verticalLayout)
        }
    }
    private fun loadHotPosts() {
        val group = findViewById<GridLayout>(R.id.categoryGroupThree)
        group.removeAllViews()
        val token = App.prefs.token ?: ""

        RetrofitInstance.communityApi.getPopularPosts("Bearer $token")
            .enqueue(object : Callback<List<CommunityDetailResponse>> {
                override fun onResponse(
                    call: Call<List<CommunityDetailResponse>>,
                    response: Response<List<CommunityDetailResponse>>
                ) {
                    if (response.isSuccessful) {
                        val posts = response.body()?.take(4) ?: return
                        addHotPostsToGroup(group, posts)
                    } else {
                        Toast.makeText(this@CommunityMainActivity, "HOT 게시물 로딩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<List<CommunityDetailResponse>>, t: Throwable) {
                    Toast.makeText(this@CommunityMainActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun addHotPostsToGroup(
        group: GridLayout,
        posts: List<CommunityDetailResponse>
    ) {
        group.removeAllViews()
        val context = group.context
        val dp = { value: Int -> (value * context.resources.displayMetrics.density).toInt() }

        for ((idx, post) in posts.withIndex()) {
            // 바깥 LinearLayout (세로)
            val cardLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(160)  // 적당한 카드 너비로 조절
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    rightMargin = if (idx % 2 == 0) dp(30) else dp(0)  // 2열마다 오른쪽 여백
                    topMargin = if (idx >= 2) dp(47) else dp(0)       // 3,4번째 줄 위 여백
                }
                gravity = android.view.Gravity.START
            }

            // 이미지
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(100)
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                val imageUrl = post.imageUrls.firstOrNull()?.let { RetrofitInstance.BASE_URL + it }
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_kitchen1)
                    .into(this)
            }

            // 게시글 내용 (2줄로 보이게)
            val contentText = TextView(context).apply {
                text = post.content.let {
                    if (it.length > 35) it.take(35) + "..." else it
                }
                textSize = 12f
                setTextColor(Color.parseColor("#2B2B2B"))
                setPadding(0, dp(11), 0, 0)
                maxLines = 2
            }

            cardLayout.addView(imageView)
            cardLayout.addView(contentText)

            cardLayout.setOnClickListener {
                val intent = Intent(context, CommunityDetailActivity::class.java)
                intent.putExtra("postId", post.id)
                context.startActivity(intent)
            }

            group.addView(cardLayout)
        }
    }


}