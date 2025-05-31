package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.test.Utils.TabBarUtils
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.model.review.TpReviewResponseDTO
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialOtherProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_other_profile) // 다른 프로필 화면의 레이아웃 파일 연결

        TabBarUtils.setupTabBar(this)
        val username = intent.getStringExtra("username") ?: return
        loadUserTradePosts(username)

        val reviewContainer = findViewById<LinearLayout>(R.id.reviewContainer)
        loadReviewsByUsername(username, reviewContainer)


        val indicator = findViewById<View>(R.id.indicator)


        // 판매내역 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.total),
            findViewById<TextView>(R.id.deal),
            findViewById<TextView>(R.id.dealComplete)
        )

        // 판매내역 LinearLayout 리스트 (TextView와 1:1 매칭)
        val layouts = listOf(
            findViewById<LinearLayout>(R.id.profileItemGroup),
            findViewById<LinearLayout>(R.id.profileItemGroup2),
            findViewById<LinearLayout>(R.id.profileItemGroup3)
        )

        // 판매내역 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 모든 ConstraintLayout 숨김
                layouts.forEach { it.visibility = View.GONE }

                // 클릭된 TextView에 해당하는 ConstraintLayout만 표시
                layouts[index].visibility = View.VISIBLE

                // 모든 TextView 색상 초기화
                textViews.forEach { it.setTextColor(Color.parseColor("#B3B3B3")) }

                // 클릭된 TextView만 색상 변경 (#2B2B2B)
                textView.setTextColor(Color.parseColor("#35A825"))

                // indicator를 클릭된 TextView 아래로 이동
                val params = indicator.layoutParams as ViewGroup.MarginLayoutParams
                indicator.post {
                    val location = IntArray(2)
                    textView.getLocationOnScreen(location)
                    val textViewX = location[0]

                    // 바 위치를 TextView의 x 좌표로 이동
                    indicator.translationX = textViewX.toFloat()
                }
            }
        }


        fun showReviewWritePopup(anchorView: View, targetLayout: LinearLayout) {
            val popup = PopupMenu(this, anchorView)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    targetLayout.visibility = View.GONE
                    true
                } else {
                    false
                }
            }

            popup.show()
        }


    }

    private fun loadReviewsByUsername(username: String, container: LinearLayout) {
        val token = App.prefs.token ?: return
        Log.d("REVIEW 검색", token)
        RetrofitInstance.materialApi.getReviewsByUsername("Bearer $token", username)
            .enqueue(object : Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: Call<List<TpReviewResponseDTO>>,
                    response: Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val reviews = response.body() ?: return
                        container.removeAllViews()
                        Log.d("Review", "📦 받은 리뷰 수: ${reviews.size}")
                        for (review in reviews) {
                            val itemView =
                                layoutInflater.inflate(R.layout.item_other_review, container, false)
                            Log.d(
                                "Review",
                                "👤 작성자: ${review.username}, ⭐ ${review.rating}, 내용: ${review.content}, 날짜: ${review.createdAt}"
                            )
                            itemView.findViewById<TextView>(R.id.reviewerName).text =
                                review.username
                            itemView.findViewById<TextView>(R.id.reviewContent).text =
                                review.content
                            itemView.findViewById<TextView>(R.id.reviewDate).text =
                                review.createdAt.replace("T", " ").substring(0, 16)

                            container.addView(itemView)
                        }
                    }
                }

                override fun onFailure(call: Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("Review", "❌ 리뷰 조회 실패", t)
                }
            })
    }

    private fun loadUserTradePosts(username: String) {
        val token = App.prefs.token ?: return

        RetrofitInstance.materialApi.getUserTradePosts("Bearer $token", username)
            .enqueue(object : Callback<List<TradePostSimpleResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostSimpleResponse>>,
                    response: Response<List<TradePostSimpleResponse>>
                ) {
                    if (response.isSuccessful) {
                        val posts = response.body() ?: return

                        val allPosts = posts
                        val ongoingPosts = posts.filter { it.status == 0 }
                        val completedPosts = posts.filter { it.status == 1 }

                        renderPosts(allPosts, findViewById(R.id.profileItemGroup))
                        renderPosts(ongoingPosts, findViewById(R.id.profileItemGroup2))
                        renderPosts(completedPosts, findViewById(R.id.profileItemGroup3))
                    } else {
                        Log.e("TradePost", "서버 응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TradePostSimpleResponse>>, t: Throwable) {
                    Log.e("TradePost", "네트워크 오류", t)
                }
            })
    }

    private fun renderPosts(posts: List<TradePostSimpleResponse>, container: LinearLayout) {
        container.removeAllViews()

        for (post in posts) {
            val itemView = layoutInflater.inflate(R.layout.item_trade_post, container, false)

            itemView.findViewById<TextView>(R.id.itemTitle).text = post.title
            itemView.findViewById<TextView>(R.id.itemPrice).text =
                if (post.price == 0) "나눔" else "${post.price} P"
            //itemView.findViewById<TextView>(R.id.sellerLabel).visibility = View.GONE
            //itemView.findViewById<TextView>(R.id.temperatureText).text =
                post.createdAt.substring(5, 10)
            val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
            if (post.firstImageUrl.isNullOrBlank()) {
                imageView.visibility = View.GONE
            } else {
                imageView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(RetrofitInstance.BASE_URL + post.firstImageUrl)
                    .into(imageView)
            }

            container.addView(itemView)
        }
    }


}
