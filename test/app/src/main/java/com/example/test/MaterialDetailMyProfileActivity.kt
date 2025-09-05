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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.test.model.chat.ChatRoomResponse
import com.example.test.model.chat.UsernameResponse
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
        setContentView(R.layout.activity_material_my_profile_detail) // MaterialDetailActivity의 레이아웃 파일 연결

        val tradePostId = intent.getLongExtra("tradePostId", -1L)

        //테스트 중 val tradePostId =3L
        val token = "Bearer ${App.prefs.token}"
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
        imagePager = findViewById<ViewPager2>(R.id.imagePager)
        imageCount = findViewById(R.id.imageCount)
        imageCountTwo = findViewById(R.id.imageCountTwo)

        val pullingUpButton = findViewById<AppCompatButton>(R.id.pullingUpButton)
        val pullingUpLayout = findViewById<ConstraintLayout>(R.id.pullingUp)
        val cancel = findViewById<AppCompatButton>(R.id.cancel)
        val check = findViewById<AppCompatButton>(R.id.check)
        val dimView = findViewById<View>(R.id.dimView)

        // "끌어올리기" 버튼 클릭 → 다이얼로그 나타남
        pullingUpButton.setOnClickListener {
            pullingUpLayout.visibility = View.VISIBLE
        }

        // "취소" 버튼 클릭 → 다이얼로그 닫기
        cancel.setOnClickListener {
            pullingUpLayout.visibility = View.GONE
        }

        check.setOnClickListener {
            val id = tradePostId  // Long
            PinStore.pin(this, id)
            Toast.makeText(this, "맨 위로 고정", Toast.LENGTH_SHORT).show()

            // MaterialActivity로 이동
            val intent = Intent(this, MaterialActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)

            finish() // 현재 Activity 닫기
        }


        if (tradePostId != -1L) {
            RetrofitInstance.apiService.getTradePostById(token, tradePostId)
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
                                itemPrice.text =
                                    if (post.price == 0) "나눔" else "${post.price} P"
                                userName.text = post.writer
                                writerName= post.writer

                                val original = post.purchaseDate  // 예: "2024-04-12"
                                val formattedDate = try {
                                    val parsed = LocalDate.parse(original)
                                    "${parsed.monthValue.toString().padStart(2, '0')}.${parsed.dayOfMonth.toString().padStart(2, '0')}"
                                } catch (e: Exception) {
                                    "날짜 오류"
                                }
                                dateText.text = formattedDate
                                val images = parseImageUrls(post.imageUrls)
                                imageCountTwo.text = "/${images.size}"

                                val adapter = ImagePagerAdapter(this@MaterialDetailMyProfileActivity, images)
                                imagePager.adapter = adapter

                                imagePager.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
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

        RetrofitInstance.materialApi.isTradePostSaved(token, tradePostId)
            .enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        isSaved = response.body() == true
                        val heartBtn = findViewById<ImageView>(R.id.heartIcon)
                        heartBtn.setImageResource(
                            if (isSaved) R.drawable.ic_heart_fill else R.drawable.ic_heart
                        )
                    }
                }
                override fun onFailure(call: Call<Boolean>, t: Throwable) {}
            })
        val heartBtn = findViewById<ImageView>(R.id.heartIcon)

        heartBtn.setOnClickListener {
            RetrofitInstance.materialApi.toggleSavedTradePost(token, tradePostId)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            isSaved = !isSaved
                            heartBtn.setImageResource(
                                if (isSaved) R.drawable.ic_heart_fill else R.drawable.ic_heart
                            )
                            val message = if (isSaved) "관심 거래글로 저장되었습니다." else "관심 목록에서 제거되었습니다."
                            Toast.makeText(this@MaterialDetailMyProfileActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailMyProfileActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
        // detailViewIcon 클릭했을 때 MaterialOtherProfileActivity 이동
        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            intent.putExtra("username", writerName)
            startActivity(intent)
        }

        // imageSearch 클릭했을 때 MaterialSearchActivity 이동
        val imageSearch: ImageView = findViewById(R.id.imageSearch)
        imageSearch.setOnClickListener {
            val intent = Intent(this, MaterialSearchActivity::class.java)
            startActivity(intent)
        }

        // img_m 클릭했을 때 MaterialDetailMapActivity 이동
        val img_m: ImageView = findViewById(R.id.img_m)
        img_m.setOnClickListener {
            val intent = Intent(this, MaterialDetailMapActivity::class.java)
            startActivity(intent)
        }

        //뒤로가기 클릭시 이전화면으로 화면 이동
        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener {
            finish()  // 현재 액티비티 종료 = 이전 화면으로 이동
        }

        // 등록한 거래글 보기 더하기 버튼 클릭시 수정, 삭제 나타남
        // 더하기 버튼 클릭시 신고하기 나타남
        val itemMore = findViewById<ImageView>(R.id.itemMore)

        itemMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {

                    finish() // 현재 액티비티 종료 → 이전 화면으로 돌아감
                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        val postId = intent.getLongExtra("postId", -1L)
        if (postId != -1L) {
            // postId로 서버 API 호출해서 상세 정보 불러오기
            Log.d("DetailActivity", "받은 postId = $postId")
        }

    }

    private fun parseImageUrls(json: String): List<String> {
        return try {
            val gson = Gson()
            val urls = gson.fromJson(json, Array<String>::class.java).toList()
            urls.map { RetrofitInstance.BASE_URL + it.trim() }  // 🔥 앞에 BASE_URL 붙이기
        } catch (e: Exception) {
            emptyList()
        }
    }
    private fun fetchMyNickname(onResult: (String?) -> Unit) {
        val token = "Bearer ${App.prefs.token}"
        val userId = App.prefs.userId
        RetrofitInstance.chatApi.getUserProfileById(token, userId)
            .enqueue(object : Callback<UsernameResponse> {
                override fun onResponse(call: Call<UsernameResponse>, response: Response<UsernameResponse>) {
                    if (response.isSuccessful) {
                        val nickname = response.body()?.username
                        onResult(nickname)
                    } else {
                        onResult(null)
                    }
                }

                override fun onFailure(call: Call<UsernameResponse>, t: Throwable) {
                    onResult(null)
                }
            })
    }
}