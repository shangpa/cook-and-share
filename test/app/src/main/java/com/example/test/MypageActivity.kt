/*마이페이지 메인*/
package com.example.test

import android.annotation.SuppressLint
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
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageView = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenText: TextView = findViewById(R.id.tapVillageKitchenText)
        tapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageView = findViewById(R.id.tapRecipeIcon)
        tapRecipeIcon.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeText 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeText: TextView = findViewById(R.id.tapRecipeText)
        tapRecipeText.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapHomeIcon 클릭했을 때 MainActivity 이동
        val tapHomeIcon: ImageView = findViewById(R.id.tapHomeIcon)
        tapHomeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityIcon 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityIcon: ImageView = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageView = findViewById(R.id.tapFridgeIcon)
        tapFridgeIcon.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeText 클릭했을 때 FridgeActivity 이동
        val tapFridgeText: TextView = findViewById(R.id.tapFridgeText)
        tapFridgeText.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageButton = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
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
        val userPointText: TextView = findViewById(R.id.myPoint) //포인트 표시할 TextView
        // 사용자 정보 요청
        // 1. 사용자 이름 불러오기
        if (token.isNotEmpty()) {
            RetrofitInstance.apiService.getUserInfo("Bearer $token")
                .enqueue(object : Callback<LoginInfoResponse> {
                    override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                        if (response.isSuccessful) {
                            val userInfo = response.body()
                            userInfo?.let {
                                userNameText.text = it.name
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                        userNameText.text = "사용자"
                        logoutText.text ="로그인"
                    }
                })

            // 2. 🔥 사용자 포인트 불러오기
            RetrofitInstance.apiService.getMyPoint("Bearer $token")
                .enqueue(object : Callback<Int> {
                    override fun onResponse(call: Call<Int>, response: Response<Int>) {
                        if (response.isSuccessful) {
                            val point = response.body() ?: 0
                            userPointText.text = "${point}P"
                        } else {
                            userPointText.text = "?"
                        }
                    }

                    override fun onFailure(call: Call<Int>, t: Throwable) {
                        userPointText.text = "?"
                    }
                })

        } else {
            // 로그인 안 되어있을 때
            userNameText.text = "로그인을 해주세요"
            userPointText.text = "0"
            logoutText.text ="로그인하러 가기"
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
    }
}
