/*마이페이지 메인*/
package com.example.test

import Prefs
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.Utils.TabBarUtils
import com.example.test.model.LoginInfoResponse
import com.example.test.model.notification.FcmTokenRequestDTO
import com.example.test.network.RetrofitInstance
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.bumptech.glide.Glide
import android.view.View
import java.text.NumberFormat
import java.util.Locale


class MypageActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        TabBarUtils.setupTabBar(this)

        val profileArea = findViewById<LinearLayout>(R.id.profileArea)
        val nicknameText = findViewById<TextView>(R.id.nicknameText)
        val profileImage = findViewById<ImageView>(R.id.profileImage)

        // logoutText 클릭했을 때 LoginActivity 이동
        val logoutText: TextView = findViewById(R.id.logoutText)
        logoutText.setOnClickListener {
            val prefs = Prefs(this)
            val authToken = prefs.token ?: ""
            if (authToken.isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                    val request = FcmTokenRequestDTO(token = fcmToken, platform = "ANDROID")
                    RetrofitInstance.notificationApi.deleteFcmToken(
                        "Bearer $authToken", request
                    ).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            Log.d("FCM", "로그아웃 시 FCM 토큰 해제 완료")
                            prefs.token = ""
                            val intent = Intent(this@MypageActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.e("FCM", "FCM 토큰 해제 실패", t)
                            prefs.token = ""
                            val intent = Intent(this@MypageActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                }
            } else {
                prefs.token = ""
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 찜한 레시피로 이동
        val likeRecipeText: LinearLayout = findViewById(R.id.recipeLike)
        likeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageLoveRecipeActivity::class.java)
            startActivity(intent)
        }

        // 작성한 레시피로 이동
        val writeRecipeText: LinearLayout = findViewById(R.id.recipeWrite)
        writeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageWriteRecipeActivity::class.java)
            startActivity(intent)
        }

        // 임시저장한 레시피로 이동
        val transientStorageRecipe: LinearLayout = findViewById(R.id.transientStorageRecipe)
        transientStorageRecipe.setOnClickListener {
            val intent = Intent(this, MypageTransientStorageRecipeActivity::class.java)
            startActivity(intent)
        }

        // 레시피 리뷰 내역으로 이동
        val recipeReviewListText: LinearLayout = findViewById(R.id.recipeReview)
        recipeReviewListText.setOnClickListener {
            val intent = Intent(this, MypageRecipeReviewActivity::class.java)
            startActivity(intent)
        }
        // 사용자 이름 텍스트뷰
        val userNameText: TextView = findViewById(R.id.mypageUserNameText1)

        // 토큰 가져오기
        val token = App.prefs.token.toString()
        val userPointText: TextView = findViewById(R.id.myPoint) //포인트 표시할 TextView

        // 사용자 정보 요청
        // 1. 사용자 정보 요청
        if (token.isNotEmpty()) {
            RetrofitInstance.apiService.getUserInfo("Bearer $token")
                .enqueue(object : Callback<LoginInfoResponse> {
                    override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                        if (response.isSuccessful) {
                            val userInfo = response.body()
                            userInfo?.let {
                                // 상단 텍스트 숨김, 프로필 영역 표시
                                App.prefs.userId = it.id
                                userNameText.visibility = View.GONE
                                profileArea.visibility = View.VISIBLE

                                nicknameText.text = "${it.name}님"
                                logoutText.text = "로그아웃"

                                if (!it.profileImageUrl.isNullOrEmpty()) {
                                    Glide.with(this@MypageActivity)
                                        .load(it.profileImageUrl)
                                        .placeholder(R.drawable.ic_cicrle_profile)
                                        .circleCrop()
                                        .into(profileImage)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                        userNameText.text = "로그인을 해주세요"
                        logoutText.text = "로그인하러 가기"
                    }
                })

            // 2. 사용자 포인트 요청
            RetrofitInstance.apiService.getMyPoint("Bearer $token")
                .enqueue(object : Callback<Int> {
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {
                        if (response.isSuccessful) {
                            val point = response.body() ?: 0
                            // 숫자에 3자리 콤마 추가
                            val formattedPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(point)
                            userPointText.text = "${formattedPoint} P"
                        } else {
                            userPointText.text = "?"
                        }
                    }

                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        userPointText.text = "?"
                    }
                })
        } else {
            userNameText.text = "로그인을 해주세요"
            userPointText.text = "0"
            logoutText.text = "로그인하러 가기"
        }


        // userEditIcon 클릭했을 때 MypagePersonalInfoActivity 이동
        val userEditIcon = findViewById<ImageButton>(R.id.userEditIcon)

        userEditIcon.setOnClickListener {
            val intent = Intent(this, MypagePersonalInfoActivity::class.java)
            startActivity(intent)
        }

        // editInformation 클릭했을 때 MypagePersonalInfoActivity 이동
        val editInformation: TextView = findViewById(R.id.editInformation)
        editInformation.setOnClickListener {
            val intent = Intent(this, MypagePersonalInfoActivity::class.java)
            startActivity(intent)
        }


        // fridgeMaterialListText 클릭했을 때 MypageFridgeMaterialListActivity 이동
        val fridgeMaterialListText: TextView = findViewById(R.id.fridgeMaterialListText)
        fridgeMaterialListText.setOnClickListener {
            val intent = Intent(this, MypageFridgeMaterialListActivity::class.java)
            startActivity(intent)
        }

        // MaterialListText 클릭했을 때 MaterialMyProfileActivity 이동
        val MaterialListText: TextView = findViewById(R.id.MaterialListText)
        MaterialListText.setOnClickListener {
            val intent = Intent(this, MaterialMyProfileActivity::class.java)
            startActivity(intent)
        }

        // writePostText 클릭했을 때 MypageWritePostActivity 이동
        val writePostText: TextView = findViewById(R.id.writePostText)
        writePostText.setOnClickListener {
            val intent = Intent(this, MypageWritePostActivity::class.java)
            startActivity(intent)
        }

        // myPoint 클릭했을 때 MypageWritePostActivity 이동
        val myPoint: TextView = findViewById(R.id.myPoint)
        myPoint.setOnClickListener {
            val intent = Intent(this, MyPagePointActivity::class.java)
            startActivity(intent)
        }

        // myChannelLink 클릭했을 때 MyProfileActivity 이동
        val myChannelLink: TextView = findViewById(R.id.myChannelLink)
        myChannelLink.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            intent.putExtra("targetUserId", App.prefs.userId.toInt())
            startActivity(intent)
        }
    }
}
