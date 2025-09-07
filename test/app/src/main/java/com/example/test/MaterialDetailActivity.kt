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
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.viewpager2.widget.ViewPager2
import com.example.test.adapter.ImagePagerAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradePostUpResult
import com.example.test.model.chat.ChatRoomResponse
import com.example.test.model.chat.UsernameResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate

class MaterialDetailActivity : AppCompatActivity() {

    private lateinit var imagePager: ViewPager2
    private lateinit var imageCount: TextView
    private lateinit var imageCountTwo: TextView
    private lateinit var currentPost: TradePostResponse

    private lateinit var chatButton: AppCompatButton
    private lateinit var pullingUpButton: AppCompatButton
    private lateinit var pullingUpLayout: ConstraintLayout
    private lateinit var cancel: AppCompatButton
    private lateinit var check: AppCompatButton

    private var myUsername: String? = null
    private var writerName: String = ""

    private var isSaved = false

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail)

        val tradePostId = intent.getLongExtra("tradePostId", -1L)
        val tokenHeader = "Bearer ${App.prefs.token}"

        // 뷰 바인딩
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

        chatButton = findViewById(R.id.chatButton)
        pullingUpButton = findViewById(R.id.pullingUpButton)
        pullingUpLayout = findViewById(R.id.pullingUp)
        cancel = findViewById(R.id.cancel)
        check = findViewById(R.id.check)

        // 초기 가시성 (데이터 로드 후 updateActionButtonsIfReady에서 결정)
        chatButton.visibility = View.GONE
        pullingUpButton.visibility = View.GONE

        /* ------------ 끌어올리기 팝업 ------------ */

        pullingUpButton.setOnClickListener {
            pullingUpLayout.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            pullingUpLayout.visibility = View.GONE
        }

        check.setOnClickListener {
            val id = intent.getLongExtra("tradePostId", -1L)
            if (id == -1L) return@setOnClickListener

            val token = "Bearer ${App.prefs.token}"

            // 버튼 중복 클릭 방지 (선택)
            check.isEnabled = false

            RetrofitInstance.apiService.upTradePost(token, id)
                .enqueue(object : retrofit2.Callback<TradePostUpResult> {
                    override fun onResponse(
                        call: retrofit2.Call<TradePostUpResult>,
                        response: retrofit2.Response<TradePostUpResult>
                    ) {
                        check.isEnabled = true

                        if (response.isSuccessful) {
                            Toast.makeText(this@MaterialDetailActivity, "500P를 차감하여 끌어올렸어요!", Toast.LENGTH_SHORT).show()
                            pullingUpLayout.visibility = View.GONE

                            val intent = Intent(this@MaterialDetailActivity, MaterialActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            intent.putExtra("forceSort", "UPDATED")
                            startActivity(intent)
                            finish()
                            return
                        }

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
                                Toast.makeText(this@MaterialDetailActivity, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                            }
                            isInsufficient -> {
                                Toast.makeText(this@MaterialDetailActivity, "포인트가 부족합니다", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(this@MaterialDetailActivity, "업 실패 ($code)", Toast.LENGTH_SHORT).show()
                            }
                        }
                        pullingUpLayout.visibility = View.GONE
                    }

                    override fun onFailure(call: retrofit2.Call<TradePostUpResult>, t: Throwable) {
                        check.isEnabled = true
                        pullingUpLayout.visibility = View.GONE
                        Toast.makeText(this@MaterialDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }


        /* ------------ 상세 데이터 로드 ------------ */

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
                                    "${parsed.monthValue.toString().padStart(2, '0')}.${parsed.dayOfMonth.toString().padStart(2, '0')}"
                                } catch (e: Exception) {
                                    "날짜 오류"
                                }
                                dateText.text = formattedDate

                                val images = parseImageUrls(post.imageUrls)
                                imageCountTwo.text = "/${images.size}"

                                val adapter = ImagePagerAdapter(this@MaterialDetailActivity, images)
                                imagePager.adapter = adapter
                                imagePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        imageCount.text = "${position + 1}"
                                    }
                                })

                                // ✅ 버튼 가시성 갱신 (작성자/로그인 여부에 따라)
                                updateActionButtonsIfReady()
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

        /* ------------ 찜(하트) ------------ */

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
                            Toast.makeText(this@MaterialDetailActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@MaterialDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        /* ------------ 프로필 상세 이동 ------------ */

        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            intent.putExtra("username", writerName)
            startActivity(intent)
        }

        /* ------------ 채팅 ------------ */

        chatButton.setOnClickListener {
            val raw = App.prefs.token?.toString().orEmpty()
            if (raw.isBlank() || raw == "null") {
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bearer = "Bearer $raw"

            fetchMyNickname { myNickname ->
                if (myNickname == null) {
                    Toast.makeText(this, "사용자 정보 조회 실패", Toast.LENGTH_SHORT).show()
                    return@fetchMyNickname
                }
                if (::currentPost.isInitialized && currentPost.writer == myNickname) {
                    Toast.makeText(this, "자신의 게시글에는 채팅할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@fetchMyNickname
                }

                RetrofitInstance.chatApi.createOrGetRoom(bearer, tradePostId)
                    .enqueue(object : Callback<ChatRoomResponse> {
                        override fun onResponse(call: Call<ChatRoomResponse>, response: Response<ChatRoomResponse>) {
                            if (response.isSuccessful) {
                                val body = response.body() ?: return
                                val roomKey = body.roomKey
                                val receiverId =
                                    if (App.prefs.userId.toLong() == body.userAId) body.userBId else body.userAId

                                val intent = Intent(this@MaterialDetailActivity, MaterialChatDetailActivity::class.java)
                                intent.putExtra("roomKey", roomKey)
                                intent.putExtra("postId", tradePostId)
                                intent.putExtra("receiverId", receiverId)
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

        /* ------------ 상단 버튼들 ------------ */

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

        /* ------------ 더보기 (신고) ------------ */

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

        /* ------------ 내 닉네임 가져오기 (버튼 가시성 판정용) ------------ */
        fetchMyNickname { nickname ->
            myUsername = nickname
            updateActionButtonsIfReady()
        }
    }

    /** 내 닉네임/게시글 정보가 준비되면 버튼 가시성/위치를 정한다. */
    private fun updateActionButtonsIfReady() {
        if (!::currentPost.isInitialized) return

        val isOwner = (myUsername != null && currentPost.writer == myUsername)

        if (isOwner) {
            chatButton.visibility = View.GONE
            pullingUpButton.visibility = View.VISIBLE
            relocatePullUpButtonToRight()
        } else {
            pullingUpButton.visibility = View.GONE
            chatButton.visibility = View.VISIBLE
            relocatePullUpButtonNextToChat()
        }
    }

    private fun relocatePullUpButtonToRight() {
        val root = findViewById<ConstraintLayout>(R.id.materailDetail)
        val set = ConstraintSet()
        set.clone(root)
        set.clear(R.id.pullingUpButton, ConstraintSet.END)
        set.connect(
            R.id.pullingUpButton, ConstraintSet.END,
            R.id.tapFixBarThree, ConstraintSet.END, 20
        )
        set.applyTo(root)
    }

    private fun relocatePullUpButtonNextToChat() {
        val root = findViewById<ConstraintLayout>(R.id.materailDetail)
        val set = ConstraintSet()
        set.clone(root)
        set.clear(R.id.pullingUpButton, ConstraintSet.END)
        set.connect(
            R.id.pullingUpButton, ConstraintSet.END,
            R.id.chatButton, ConstraintSet.START, 0
        )
        set.applyTo(root)
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

    private fun fetchMyNickname(onResult: (String?) -> Unit) {
        val raw = App.prefs.token?.toString().orEmpty()
        if (raw.isBlank() || raw == "null") {
            onResult(null)
            return
        }
        val bearer = "Bearer $raw"
        val userId = App.prefs.userId
        RetrofitInstance.chatApi.getUserProfileById(bearer, userId)
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
