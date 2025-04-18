/*마이페이지 메인*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.model.LoginInfoResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageButton = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // likeFoodIcon 클릭했을 때 MainActivity 이동
        val likeFoodIcon: ImageView = findViewById(R.id.likeFoodIcon)
        likeFoodIcon.setOnClickListener {
            val intent = Intent(this, LikeFoodActivity::class.java)
            startActivity(intent)
        }

        // logoutText 클릭했을 때 LoginActivity 이동
        val logoutText: TextView = findViewById(R.id.logoutText)
        logoutText.setOnClickListener {
            App.prefs.token = ""
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        // 작성한 레시피로 이동
        val writeRecipeText: LinearLayout = findViewById(R.id.recipeWrite)
        writeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageWriteRecipeActivity::class.java)
            startActivity(intent)
        }

          // 레시피 리뷰 내역으로 이동
        val recipeReviewListText: LinearLayout = findViewById(R.id.recipeReview)
        recipeReviewListText.setOnClickListener {
            val intent = Intent(this, MypageRecipeReviewActivity::class.java)
            startActivity(intent)
        }

         // 찜한 레시피로 이동
        val likeRecipeText: LinearLayout = findViewById(R.id.recipeLike)
        likeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageLoveRecipeActivity::class.java)
            startActivity(intent)
        }
        // 사용자 이름 텍스트뷰
        val userNameText: TextView = findViewById(R.id.mypageUserNameText1)

        // 토큰 가져오기
        val token = App.prefs.token.toString()

        // 사용자 정보 요청
        RetrofitInstance.apiService.getUserInfo("Bearer $token")
            .enqueue(object : Callback<LoginInfoResponse> {
                override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                    if (response.isSuccessful) {
                        val userInfo = response.body()
                        userInfo?.let {
                            userNameText.text = "${it.userName}" // 여기서 필드명(username)을 실제 API 응답 필드에 맞게 바꿔줘
                        }
                    }
                }

                override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                    userNameText.text = "사용자님" // 실패 시 기본값
                }
            })

    }
}
