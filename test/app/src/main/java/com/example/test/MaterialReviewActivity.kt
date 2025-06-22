package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.review.TpReviewResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialReviewActivity : AppCompatActivity() {
    private lateinit var reviewContainer: LinearLayout

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_received_review) // 다른 프로필 화면의 레이아웃 파일 연결
        reviewContainer = findViewById(R.id.reviewContainer)

        loadReceivedReviews()

        // receivedReview 클릭했을 때 MaterialReviewSendActivity 이동
        val receivedReview: TextView = findViewById(R.id.receivedReview)
        receivedReview.setOnClickListener {
            val intent = Intent(this, MaterialReviewSendActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<ImageView>(R.id.receivedReviewBack).setOnClickListener {
            finish()
        }
    }
    private fun loadReceivedReviews() {
        val token = App.prefs.token ?: return

        RetrofitInstance.materialApi.getReviewsOnMyTradePosts("Bearer $token")
            .enqueue(object : Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: Call<List<TpReviewResponseDTO>>,
                    response: Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {

                        val reviews = response.body() ?: return
                        Log.d("Review", "📦 받은 리뷰 수: ${reviews.size}")
                        for (review in reviews) {
                            Log.d("Review", "👤 작성자: ${review.username}, ⭐ ${review.rating}, 내용: ${review.content}, 날짜: ${review.createdAt}")

                            val itemView = layoutInflater.inflate(R.layout.item_trade_review, reviewContainer, false)
                            itemView.findViewById<TextView>(R.id.buyerName).text = review.username
                            itemView.findViewById<TextView>(R.id.reviewContent).text = review.content
                            itemView.findViewById<TextView>(R.id.reviewDate).text =
                                review.createdAt.replace("T", " ").substring(0, 16)
                            itemView.findViewById<TextView>(R.id.reviewRating).text = "${review.rating}"
                            itemView.findViewById<TextView>(R.id.itemSubTitle).text = review.tradeTitle
                            itemView.findViewById<ImageView>(R.id.itemImage).visibility= View.GONE
                            reviewContainer.addView(itemView)
                        }
                    } else {
                        Log.e("Review", "응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("Review", "서버 연결 실패", t)
                }
            })
    }
}
