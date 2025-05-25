package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.review.TpReviewRequestDTO
import com.example.test.model.review.TpReviewResponseDTO
import com.example.test.network.RetrofitInstance
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialReviewWriteActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var postBtn: Button
    private lateinit var descriptionText: EditText
    private lateinit var itemTitle: TextView
    private lateinit var itemSubTitle: TextView
    private lateinit var itemImage: ImageView
    private lateinit var moreBtn: ImageView
    private var selectedRating = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_review_write)

        // XML 바인딩
        backBtn = findViewById(R.id.reviewWriteBack)
        postBtn = findViewById(R.id.postBtn)
        descriptionText = findViewById(R.id.descriptionText)
        itemTitle = findViewById(R.id.reviewItemTitle)
        itemSubTitle = findViewById(R.id.reviewItemSubTitle)
        itemImage = findViewById(R.id.reviewimage)

        // TODO: 필요 시 intent로 데이터 받기 및 설정
        itemTitle.text = "거래한 주방용품"
        itemSubTitle.text = "미개봉 브리타 팔아요"
        itemImage.setImageResource(R.drawable.image_britta)

        // 뒤로가기
        backBtn.setOnClickListener {
            finish()
        }
        val stars = listOf(
            findViewById<ImageButton>(R.id.star),
            findViewById<ImageButton>(R.id.starTwo),
            findViewById<ImageButton>(R.id.starThree),
            findViewById<ImageButton>(R.id.starFour),
            findViewById<ImageButton>(R.id.starFive)
        )

        for (i in stars.indices) {
            stars[i].setOnClickListener {
                selectedRating = i + 1
                for (j in stars.indices) {
                    stars[j].setImageResource(
                        if (j <= i) R.drawable.ic_star else R.drawable.ic_star_no_fill
                    )
                }
            }
        }


        val tradePostId = intent.getLongExtra("tradePostId", -1L)
        if (tradePostId == -1L) {
            Toast.makeText(this, "유효하지 않은 거래글입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

    // 서버에서 거래글 정보 받아오기
        val token = "Bearer ${App.prefs.token}"
        RetrofitInstance.apiService.getTradePostById(token, tradePostId)
            .enqueue(object : Callback<TradePostResponse> {
                override fun onResponse(call: Call<TradePostResponse>, response: Response<TradePostResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        itemTitle.text = data?.title ?: ""
                        itemSubTitle.text = data?.description ?: ""

                        val imageUrlList = try {
                            JSONArray(data?.imageUrls ?: "[]")
                        } catch (e: Exception) {
                            JSONArray()
                        }

                        if (imageUrlList.length() > 0) {
                            val firstImage = imageUrlList.getString(0)
                            Glide.with(this@MaterialReviewWriteActivity)
                                .load(RetrofitInstance.BASE_URL + firstImage)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(itemImage)
                            itemImage.visibility = View.VISIBLE
                        } else {
                            itemImage.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this@MaterialReviewWriteActivity, "정보 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {
                    Toast.makeText(this@MaterialReviewWriteActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })

        // 게시하기 성공
        postBtn.setOnClickListener {
            val content = descriptionText.text.toString().trim()
            if (content.isEmpty() || selectedRating == 0) {
                Toast.makeText(this, "후기 내용과 평점을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tradePostId = intent.getLongExtra("tradePostId", -1L)
            if (tradePostId == -1L) {
                Toast.makeText(this, "유효하지 않은 거래글입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = "Bearer ${App.prefs.token}"
            val reviewRequest = TpReviewRequestDTO(tradePostId, content, selectedRating)

            RetrofitInstance.materialApi.createTpReview(token, reviewRequest)
                .enqueue(object : Callback<TpReviewResponseDTO> {
                    override fun onResponse(
                        call: Call<TpReviewResponseDTO>,
                        response: Response<TpReviewResponseDTO>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MaterialReviewWriteActivity, "후기가 게시되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@MaterialReviewWriteActivity, "업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TpReviewResponseDTO>, t: Throwable) {
                        Toast.makeText(this@MaterialReviewWriteActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
