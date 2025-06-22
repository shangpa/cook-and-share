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
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.Utils.TabBarUtils
import com.example.test.model.TradePost.UserProfileResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Stack



class MaterialMyProfileActivity : AppCompatActivity() {



    private lateinit var profile: LinearLayout
    private lateinit var userNameText: TextView
    private lateinit var reviewCountText: TextView
    private lateinit var ratingText: TextView
    private lateinit var transactionHistoryText: TextView


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile)
        TabBarUtils.setupTabBar(this)

        val token = "Bearer ${App.prefs.token.toString()}"

        profile = findViewById(R.id.profile)

        // 프로필 선언
        val profileLayout = findViewById<LinearLayout>(R.id.profile)
        val sale : ConstraintLayout = findViewById(R.id.saleLine)
        val purchase : ConstraintLayout = findViewById(R.id.purchaseLine)
        val saved : ConstraintLayout = findViewById(R.id.savedLine)
        val review : ConstraintLayout = findViewById(R.id.reviewLine)


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

        // 뒤로가기 버튼
        findViewById<ImageView>(R.id.otherProfileBack).setOnClickListener {
            finish()
        }

        userNameText = findViewById(R.id.userName)
        reviewCountText = findViewById(R.id.reviewCount)
        ratingText = findViewById(R.id.reviewCount1)
        transactionHistoryText = findViewById(R.id.transactionHistory)

        loadUserProfile(token)
    }
    private fun loadUserProfile(authToken: String) {
        RetrofitInstance.materialApi.getUserProfile(authToken)
            .enqueue(object : Callback<UserProfileResponseDTO> {
                override fun onResponse(
                    call: Call<UserProfileResponseDTO>,
                    response: Response<UserProfileResponseDTO>
                ) {
                    if (response.isSuccessful) {
                        val user = response.body() ?: return
                        Log.d("UserInfo", " 유저 정보: $user")

                        userNameText.text = user.username
                        ratingText.text = "${user.rating}  |"
                        reviewCountText.text = "후기 ${user.reviewCount}  |"
                        transactionHistoryText.text = "거래내역 ${user.transactionCount}"
                    } else {
                        Log.e("UserInfo", " 응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserProfileResponseDTO>, t: Throwable) {
                    Log.e("UserInfo", " 서버 연결 실패", t)
                }
            })
    }
}



