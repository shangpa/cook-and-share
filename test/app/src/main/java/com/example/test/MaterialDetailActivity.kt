package com.example.test
//todo 상단에 거리 가져와야함
//todo 당연히 지도도 가져와야함 
//todo 하단에 프로필 정보 가져와야함
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.test.adapter.ImagePagerAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.chat.ChatRoomResponse
import com.example.test.model.chat.UsernameResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class MaterialDetailActivity : AppCompatActivity() {

    private lateinit var imagePager: ViewPager2
    private lateinit var imageCount: TextView
    private lateinit var imageCountTwo: TextView
    private lateinit var currentPost: TradePostResponse
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail) // MaterialDetailActivity의 레이아웃 파일 연결
        val tradePostId = intent.getLongExtra("tradePostId", -1L)

        //테스트 중 val tradePostId =3L
        val token = "Bearer ${App.prefs.token}"
        var writerName = ""
        val itemTitle = findViewById<TextView>(R.id.itemTitle)
        val category1 = findViewById<TextView>(R.id.category1)
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
                                category1.text = post.category
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

                                val adapter = ImagePagerAdapter(this@MaterialDetailActivity, images)
                                imagePager.adapter = adapter

                                imagePager.registerOnPageChangeCallback(object :
                                    ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        imageCount.text = "${position + 1}"
                                    }
                                })
                            }
                        } else {
                            Toast.makeText(this@MaterialDetailActivity, "조회 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }


        // detailViewIcon 클릭했을 때 MaterialOtherProfileActivity 이동
        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            startActivity(intent)
        }


        // chatButton 클릭했을 때 MaterialChatDetailActivity 이동
        val chatButton: Button = findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            fetchMyNickname { myNickname ->
                if (myNickname == null) {
                    Toast.makeText(this, "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show()
                    return@fetchMyNickname
                }

                if (::currentPost.isInitialized && currentPost.writer == myNickname) {
                    Toast.makeText(this, "자신의 게시글에는 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@fetchMyNickname
                }

            RetrofitInstance.chatApi.createOrGetRoom(token, tradePostId)
                .enqueue(object : Callback<ChatRoomResponse> {
                    override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                        if (response.isSuccessful) {
                            val body = response.body() ?: return
                            val roomKey = body.roomKey
                            val receiverId = if (App.prefs.userId.toLong() == body.userAId) body.userBId else body.userAId
                            println(" App.prefs.userId"+App.prefs.userId)

                            val intent = Intent(this@MaterialDetailActivity, MaterialChatDetailActivity::class.java)
                            intent.putExtra("roomKey", roomKey)
                            println("roomKey"+ roomKey)
                            intent.putExtra("postId", tradePostId)
                            println("postId"+ tradePostId)
                            intent.putExtra("receiverId", receiverId)
                            println("receiverId"+ receiverId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@MaterialDetailActivity, "채팅방 생성 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ChatRoomResponse>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
            }
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

        // 등록한 거래글 보기 하트버튼 선언
        val heartIcon = listOf(
            findViewById<ImageView>(R.id.heartIcon)
        )

        // 등록한 거래글 보기 하트버튼 클릭시 채워진 하트로 바뀜
        heartIcon.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.heartIcon, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.heartIcon) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_heart)
                } else {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.heartIcon, !isLiked)
            }
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