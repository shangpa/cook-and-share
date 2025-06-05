package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.review.TpReviewResponseDTO
import com.example.test.network.RetrofitInstance

class MaterialMyReviewActivity : AppCompatActivity() {

    private lateinit var receiveReviewTab: TextView
    private lateinit var writeReviewTab: TextView
    private lateinit var indicator: View
    private lateinit var receivedLayout: LinearLayout
    private lateinit var writtenLayout: LinearLayout
    private lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_review)

        receiveReviewTab = findViewById(R.id.receiveReviewTap)
        writeReviewTab = findViewById(R.id.writeReviewTap)
        indicator = findViewById(R.id.indicatorTwo)
        receivedLayout = findViewById(R.id.receiveReview)
        writtenLayout = findViewById(R.id.writtenReview)
        backBtn = findViewById(R.id.receivedReviewBack)

        // 탭 클릭 이벤트
        receiveReviewTab.setOnClickListener {
            switchTab(receiveReviewTab, writeReviewTab, receivedLayout, writtenLayout)
        }
        writeReviewTab.setOnClickListener {
            switchTab(writeReviewTab, receiveReviewTab, writtenLayout, receivedLayout)
        }

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.receivedReviewBack).setOnClickListener {
            finish()
        }

        // 리뷰 데이터 로드
        loadReceivedReviews()
        loadWrittenReviews()
    }

    private fun switchTab(
        selectedTab: TextView,
        unselectedTab: TextView,
        selectedLayout: LinearLayout,
        unselectedLayout: LinearLayout
    ) {
        selectedTab.setTextColor(Color.parseColor("#35A825"))
        unselectedTab.setTextColor(Color.parseColor("#B3B3B3"))
        selectedLayout.visibility = View.VISIBLE
        unselectedLayout.visibility = View.GONE

        // 인디케이터 이동
        indicator.post {
            val location = IntArray(2)
            selectedTab.getLocationOnScreen(location)
            val textViewX = location[0]
            indicator.translationX = textViewX.toFloat()
        }
    }

    private fun loadReceivedReviews() {
        val token = "Bearer ${App.prefs.token.toString()}"
        RetrofitInstance.apiService.getReviewsOnMyTradePosts(token)
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, receivedLayout)
                        }
                    } else {
                        Log.e("MaterialMyReview", "받은 후기 불러오기 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyReview", "받은 후기 네트워크 에러: ${t.message}")
                }
            })
    }

    private fun loadWrittenReviews() {
        val token = "Bearer ${App.prefs.token.toString()}"
        RetrofitInstance.apiService.getMyTpReviews(token)
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, writtenLayout)
                        }
                    } else {
                        Log.e("MaterialMyReview", "작성한 후기 불러오기 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyReview", "작성한 후기 네트워크 에러: ${t.message}")
                }
            })
    }

    private fun addReviewsToLayout(reviews: List<TpReviewResponseDTO>, parentLayout: LinearLayout) {
        parentLayout.removeAllViews()
        for (review in reviews) {
            val reviewView = LayoutInflater.from(this).inflate(R.layout.item_trade_review, parentLayout, false)

            val itemTitle = reviewView.findViewById<TextView>(R.id.itemTitle)
            val itemSubTitle = reviewView.findViewById<TextView>(R.id.itemSubTitle)
            val reviewRating = reviewView.findViewById<TextView>(R.id.reviewRating)
            val reviewDate = reviewView.findViewById<TextView>(R.id.reviewDate)
            val buyerName = reviewView.findViewById<TextView>(R.id.buyerName)
            val buyerRole = reviewView.findViewById<TextView>(R.id.buyerRole)
            val reviewContent = reviewView.findViewById<TextView>(R.id.reviewContent)

            itemTitle.text = "거래한 주방용품"
            itemSubTitle.text = "아이템 설명"
            reviewRating.text = "${review.rating}.0 |"
            reviewDate.text = review.createdAt.substring(0, 10)
            buyerName.text = review.username
            buyerRole.text = " | 구매자"
            reviewContent.text = review.content

            parentLayout.addView(reviewView)
        }
    }
}
