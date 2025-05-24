package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.model.TradePost.TpReviewResponseDTO
import com.example.test.network.RetrofitInstance
import java.util.Stack



class MaterialMyProfileActivity : AppCompatActivity() {


    private lateinit var buttons: List<Button> // 거리 버튼 리스트
    private lateinit var selectedFilterLayout: LinearLayout

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    private lateinit var profile: LinearLayout
    private lateinit var saleHistory: LinearLayout
    private lateinit var purchaseHistory: LinearLayout
    private lateinit var reviewContainer: ConstraintLayout
    private lateinit var savePost: ConstraintLayout
    private lateinit var review: LinearLayout

    private val viewStack = Stack<View>()


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile)
        val token = "Bearer ${App.prefs.token.toString()}"

        profile = findViewById(R.id.profile)

        // 프로필 선언
        val profileLayout = findViewById<LinearLayout>(R.id.profile)
        val sale : LinearLayout = findViewById(R.id.saleLine)
        val purchase : LinearLayout = findViewById(R.id.purchaseLine)
        val saved : LinearLayout = findViewById(R.id.savedLine)
        val review : LinearLayout = findViewById(R.id.reviewLine)


        sale.setOnClickListener{
            val intent = Intent(this, MaterialMyProfileSaleActivity::class.java)
            startActivity(intent)
        }

        purchase.setOnClickListener{
            val intent = Intent(this, MaterialPurchaseActivity::class.java)
            startActivity(intent)
        }
        saved.setOnClickListener{
            val intent = Intent(this, MaterialMySavedActivity::class.java)
            startActivity(intent)
        }
        review.setOnClickListener{
            val intent = Intent(this, MaterialReviewActivity::class.java)
            startActivity(intent)
        }

    }

    /*
    private fun loadReceivedReviews() {
        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getReviewsOnMyTradePosts("Bearer $token")
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, findViewById(R.id.receiveReview))
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyProfile", "받은 거래 후기 불러오기 실패: ${t.message}")
                }
            })
    }

    private fun loadWrittenReviews() {
        val token = App.prefs.token.toString()

        RetrofitInstance.apiService.getMyTpReviews("Bearer $token")
            .enqueue(object : retrofit2.Callback<List<TpReviewResponseDTO>> {
                override fun onResponse(
                    call: retrofit2.Call<List<TpReviewResponseDTO>>,
                    response: retrofit2.Response<List<TpReviewResponseDTO>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { reviews ->
                            addReviewsToLayout(reviews, findViewById(R.id.writtenReview))
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<TpReviewResponseDTO>>, t: Throwable) {
                    Log.e("MaterialMyProfile", "작성한 거래 후기 불러오기 실패: ${t.message}")
                }
            })
    }
    private fun addReviewsToLayout(reviews: List<TpReviewResponseDTO>, parentLayout: LinearLayout) {
        parentLayout.removeAllViews() // 기존 뷰 초기화

        for (review in reviews) {
            val reviewView = LayoutInflater.from(this).inflate(R.layout.item_trade_review, parentLayout, false)

            // 뷰 아이디 찾아서 데이터 넣기
            val itemImage = reviewView.findViewById<ImageView>(R.id.itemImage) // 기본 썸네일
            val itemTitle = reviewView.findViewById<TextView>(R.id.itemTitle)
            val itemSubTitle = reviewView.findViewById<TextView>(R.id.itemSubTitle)
            val reviewRating = reviewView.findViewById<TextView>(R.id.reviewRating)
            val reviewDate = reviewView.findViewById<TextView>(R.id.reviewDate)
            val buyerName = reviewView.findViewById<TextView>(R.id.buyerName)
            val buyerRole = reviewView.findViewById<TextView>(R.id.buyerRole)
            val reviewContent = reviewView.findViewById<TextView>(R.id.reviewContent)

            // 데이터 바인딩
            itemTitle.text = "거래한 재료" // 서버에서 제목 안오니까 고정값
            itemSubTitle.text = "거래 아이템 설명" // 이것도 나중에 추가 가능
            reviewRating.text = "${review.rating}.0 |"
            reviewDate.text = review.createdAt.substring(0, 10) // yyyy-MM-dd
            buyerName.text = review.username
            buyerRole.text = " | 구매자" // 고정
            reviewContent.text = review.content

            parentLayout.addView(reviewView)
        }
    }
    */
}



