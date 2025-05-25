package com.example.test

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

class MaterialReviewSendActivity : AppCompatActivity() {
    private lateinit var reviewContainer: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_material_send_review)
        reviewContainer = findViewById(R.id.reviewContainer)
        loadMyReviews()

        val receivedReview: TextView = findViewById(R.id.sendReview)
        receivedReview.setOnClickListener {
            val intent = Intent(this, MaterialReviewActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadMyReviews() {
        val token = App.prefs.token ?: return

        RetrofitInstance.materialApi.getMyReviews("Bearer $token")
            .enqueue(object : Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: Call<List<TpReviewResponseDTO>>,
                    response: Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val reviews = response.body() ?: return
                        Log.d("MyReview", "📦 작성한 리뷰 수: ${reviews.size}")

                        for (review in reviews) {
                            Log.d("MyReview", "👤 대상: ${review.username}, 내용: ${review.content}")

                            val itemView = layoutInflater.inflate(R.layout.item_trade_review, reviewContainer, false)
                            itemView.findViewById<TextView>(R.id.buyerName).text = review.username
                            itemView.findViewById<TextView>(R.id.reviewContent).text = review.content
                            itemView.findViewById<TextView>(R.id.reviewDate).text =
                                review.createdAt.replace("T", " ").substring(0, 16)
                            itemView.findViewById<TextView>(R.id.reviewRating).text = "${review.rating}"
                            itemView.findViewById<ImageView>(R.id.itemImage).visibility = View.GONE

                            reviewContainer.addView(itemView)
                        }
                    } else {
                        Log.e("MyReview", "❌ 응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MyReview", "🔥 서버 연결 실패", t)
                }
            })
    }
}
