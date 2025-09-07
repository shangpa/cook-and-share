package com.example.test

//todo 상단에 거리 가져와야함
//todo 당연히 지도도 가져와야함
//todo 하단에 프로필 정보 가져와야함
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.test.adapter.ImagePagerAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradePostUpResult
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class MaterialDetailMyProfileActivity : AppCompatActivity() {

    private lateinit var imagePager: ViewPager2
    private lateinit var imageCount: TextView
    private lateinit var imageCountTwo: TextView
    private lateinit var currentPost: TradePostResponse

    private var isSaved = false

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile_detail)

        // postId 또는 tradePostId 어떤 키로 와도 처리
        val tradePostId = intent.getLongExtra("postId", -1L)
            .takeIf { it != -1L }
            ?: intent.getLongExtra("tradePostId", -1L)

        val tokenHeader = "Bearer ${App.prefs.token}"
        var writerName = ""

        val itemTitle = findViewById<TextView>(R.id.itemTitle)
        val quantity1 = findViewById<TextView>(R.id.quantity1)
        val quantity2 = findViewById<TextView>(R.id.quantity2)
        val itemSub = findViewById<TextView>(R.id.itemSub)
        val locationText = findViewById<TextView>(R.id.transactionPlace2)
        val itemPrice = findViewById<TextView>(R.id.itemPrice)
        val userName = findViewById<TextView>(R.id.userName)
        val dateText = findViewById<TextView>(R.id.dateText)
        val distance = findViewById<TextView>(R.id.distanceOnly)

        imagePager = findViewById(R.id.imagePager)
        imageCount = findViewById(R.id.imageCount)
        imageCountTwo = findViewById(R.id.imageCountTwo)

        // 끌어올리기 UI
        val pullingUpButton = findViewById<AppCompatButton>(R.id.pullingUpButton)
        val pullingUpLayout = findViewById<ConstraintLayout>(R.id.pullingUp)
        val cancel = findViewById<AppCompatButton>(R.id.cancel)
        val check = findViewById<AppCompatButton>(R.id.check)
        val dimView = findViewById<View>(R.id.dimView)

        // "끌어올리기" 버튼 → 팝업 표시
        pullingUpButton.setOnClickListener {
            pullingUpLayout.visibility = View.VISIBLE
            dimView.visibility = View.VISIBLE
        }

        // "취소" → 팝업 닫기
        cancel.setOnClickListener {
            pullingUpLayout.visibility = View.GONE
            dimView.visibility = View.GONE
        }

        // "확인" → 서버에 끌어올리기 요청 + 포인트 차감 처리
        check.setOnClickListener {
            val id = tradePostId
            if (id == -1L) return@setOnClickListener

            val bearer = "Bearer ${App.prefs.token}"

            // 중복 클릭 방지 + 팝업 닫고 딤 표시
            check.isEnabled = false
            pullingUpLayout.visibility = View.GONE
            dimView.visibility = View.VISIBLE

            RetrofitInstance.apiService.upTradePost(bearer, id)
                .enqueue(object : Callback<TradePostUpResult> {
                    override fun onResponse(
                        call: Call<TradePostUpResult>,
                        response: Response<TradePostUpResult>
                    ) {
                        check.isEnabled = true
                        dimView.visibility = View.GONE

                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MaterialDetailMyProfileActivity,
                                "끌어올렸어요! 500P 차감",
                                Toast.LENGTH_SHORT
                            ).show()

                            // 목록을 UPDATED(노출순)로 강제 재조회
                            val intent = Intent(
                                this@MaterialDetailMyProfileActivity,
                                MaterialActivity::class.java
                            )
                            intent.addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            )
                            intent.putExtra("forceSort", "UPDATED")
                            startActivity(intent)
                            finish()
                            return
                        }

                        // 실패 처리: 코드/본문으로 포인트 부족 판별
                        val code = response.code()
                        val raw = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                        val lower = raw.lowercase()

                        val isInsufficient =
                            code == 402 || code == 403 || code == 409 ||
                                    lower.contains("insufficient_point") ||
                                    lower.contains("insufficient") ||
                                    lower.contains("not enough") ||
                                    lower.contains("포인트") ||
                                    lower.contains("point")

                        when {
                            code == 401 -> {
                                Toast.makeText(this@MaterialDetailMyProfileActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                            }
                            isInsufficient -> {
                                Toast.makeText(this@MaterialDetailMyProfileActivity, "포인트가 부족합니다", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@MaterialDetailMyProfileActivity, "업 실패 ($code)", Toast.LENGTH_SHORT).show()
                                Log.w("TradeUp", "fail code=$code body=$raw")
                            }
                        }
                    }

                    override fun onFailure(call: Call<TradePostUpResult>, t: Throwable) {
                        check.isEnabled = true
                        dimView.visibility = View.GONE
                        Toast.makeText(
                            this@MaterialDetailMyProfileActivity,
                            "네트워크 오류: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

        // 상세 데이터 로드
        if (tradePostId != -1L) {
            RetrofitInstance.apiService.getTradePostById(tokenHeader, tradePostId)
                .enqueue(object : Callback<TradePostResponse> {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onResponse(
                        call: Call<TradePostResponse>,
                        response: Response<TradePostResponse>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { post ->
                                currentPost = post
                                itemTitle.text = post.title
                                quantity1.text = post.category
                                quantity2.text = "${post.quantity}개"
                                itemSub.text = post.description
                                locationText.text = post.location
                                itemPrice.text = if (post.price == 0) "나눔" else "${post.price} P"
                                userName.text = post.writer
                                writerName = post.writer

                                val original = post.purchaseDate // 예: "2024-04-12"
                                val formattedDate = try {
                                    val parsed = LocalDate.parse(original)
                                    "${parsed.monthValue.toString().padStart(2, '0')}." +
                                            "${parsed.dayOfMonth.toString().padStart(2, '0')}"
                                } catch (e: Exception) {
                                    "날짜 오류"
                                }
                                dateText.text = formattedDate

                                val images = parseImageUrls(post.imageUrls)
                                imageCountTwo.text = "/${images.size}"

                                val adapter = ImagePagerAdapter(this@MaterialDetailMyProfileActivity, images)
                                imagePager.adapter = adapter
                                imagePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        imageCount.text = "${position + 1}"
                                    }
                                })
                            }
                        } else {
                            Toast.makeText(this@MaterialDetailMyProfileActivity, "조회 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailMyProfileActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 찜(하트)
        RetrofitInstance.materialApi.isTradePostSaved(tokenHeader, tradePostId)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        isSaved = response.body() == true
                        val heartBtn = findViewById<ImageView>(R.id.heartIcon)
                        heartBtn.setImageResource(if (isSaved) R.drawable.ic_heart_fill else R.drawable.ic_heart)
                    }
                }
                override fun onFailure(call: Call<Boolean>, t: Throwable) {}
            })

        val heartBtn = findViewById<ImageView>(R.id.heartIcon)
        heartBtn.setOnClickListener {
            RetrofitInstance.materialApi.toggleSavedTradePost(tokenHeader, tradePostId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            isSaved = !isSaved
                            heartBtn.setImageResource(if (isSaved) R.drawable.ic_heart_fill else R.drawable.ic_heart)
                            val message = if (isSaved) "관심 거래글로 저장되었습니다." else "관심 목록에서 제거되었습니다."
                            Toast.makeText(this@MaterialDetailMyProfileActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailMyProfileActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 프로필 상세
        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            intent.putExtra("username", writerName)
            startActivity(intent)
        }

        // 상단 버튼들
        val imageSearch: ImageView = findViewById(R.id.imageSearch)
        imageSearch.setOnClickListener {
            startActivity(Intent(this, MaterialSearchActivity::class.java))
        }

        val img_m: ImageView = findViewById(R.id.img_m)
        img_m.setOnClickListener {
            startActivity(Intent(this, MaterialDetailMapActivity::class.java))
        }

        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener { finish() }

        // 더보기(신고)
        val itemMore = findViewById<ImageView>(R.id.itemMore)
        itemMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("신고하기")
            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    finish()
                    true
                } else false
            }
            popup.show()
        }
    }

    private fun parseImageUrls(json: String): List<String> {
        return try {
            val gson = Gson()
            val urls = gson.fromJson(json, Array<String>::class.java).toList()
            urls.map { RetrofitInstance.BASE_URL + it.trim() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
