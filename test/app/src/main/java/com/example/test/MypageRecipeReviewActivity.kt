package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.MyReviewAdapter
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageRecipeReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyReviewAdapter
    private lateinit var reviewCountText: TextView

    private var selectedCategoryButton: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_recipe_review)

        recyclerView = findViewById(R.id.reviewRecyclerView)
        reviewCountText = findViewById(R.id.fridgeRecipeResultNumber)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupCategoryButtons()
        setupBackButton()
        loadMyReviews()
    }

    private fun setupCategoryButtons() {
        val categoryButtons = listOf(
            findViewById<AppCompatButton>(R.id.allBtn),
            findViewById(R.id.krBtn),
            findViewById(R.id.wsBtn),
            findViewById(R.id.jpBtn),
            findViewById(R.id.cnBtn),
            findViewById(R.id.vgBtn),
            findViewById(R.id.snBtn),
            findViewById(R.id.asBtn),
            findViewById(R.id.sdBtn)
        )

        categoryButtons.forEach { button ->
            button.setOnClickListener {
                // 카테고리 코드 매핑
                val categoryCode = when (button.id) {
                    R.id.krBtn -> "koreaFood"
                    R.id.wsBtn -> "westernFood"
                    R.id.jpBtn -> "japaneseFood"
                    R.id.cnBtn -> "chineseFood"
                    R.id.vgBtn -> "vegetarianDiet"
                    R.id.snBtn -> "snack"
                    R.id.asBtn -> "alcoholSnack"
                    R.id.sdBtn -> "sideDish"
                    else -> "all"
                }

                // 서버 요청
                loadMyReviews(categoryCode)

                // 버튼 스타일 변경
                selectedCategoryButton?.apply {
                    setBackgroundResource(R.drawable.btn_recipe_add)
                    setTextColor(Color.parseColor("#8A8F9C"))
                }

                button.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                button.setTextColor(Color.WHITE)
                selectedCategoryButton = button
            }
        }
    }

    private fun setupBackButton() {
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadMyReviews() {
        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"

        RetrofitInstance.apiService.getMyReviews(authHeader)
            .enqueue(object : Callback<List<ReviewResponseDTO>> {
                override fun onResponse(
                    call: Call<List<ReviewResponseDTO>>,
                    response: Response<List<ReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val reviewList = response.body() ?: emptyList()
                        reviewCountText.text = reviewList.size.toString()

                        adapter = MyReviewAdapter(
                            reviewList,
                            onEditClick = { review -> onEditReview(review.reviewId) },
                            onDeleteClick = { review -> onDeleteReview(review.reviewId) }
                        )
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@MypageRecipeReviewActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ReviewResponseDTO>>, t: Throwable) {
                    Toast.makeText(this@MypageRecipeReviewActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onEditReview(reviewId: Long) {
        // TODO: 리뷰 수정 화면으로 이동 구현 예정
        Toast.makeText(this, "수정 기능은 준비 중입니다. (리뷰 ID: $reviewId)", Toast.LENGTH_SHORT).show()
    }

    private fun onDeleteReview(reviewId: Long) {
        val token = App.prefs.token ?: return
        val authHeader = "Bearer $token"

        RetrofitInstance.apiService.deleteReview(authHeader, reviewId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MypageRecipeReviewActivity, "리뷰가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                        loadMyReviews() // 목록 새로고침
                    } else {
                        Toast.makeText(this@MypageRecipeReviewActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MypageRecipeReviewActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadMyReviews(category: String = "all") {
        val token = App.prefs.token ?: return
        val authHeader = "Bearer $token"

        RetrofitInstance.apiService.getMyReviewsByCategory(authHeader, category)
            .enqueue(object : Callback<List<ReviewResponseDTO>> {
                override fun onResponse(
                    call: Call<List<ReviewResponseDTO>>,
                    response: Response<List<ReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        val reviewList = response.body() ?: emptyList()
                        reviewCountText.text = reviewList.size.toString()
                        adapter = MyReviewAdapter(
                            reviewList,
                            onEditClick = { review -> onEditReview(review.reviewId) },
                            onDeleteClick = { review -> onDeleteReview(review.reviewId) }
                        )
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@MypageRecipeReviewActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ReviewResponseDTO>>, t: Throwable) {
                    Toast.makeText(this@MypageRecipeReviewActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
