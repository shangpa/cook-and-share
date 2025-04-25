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

class MypageActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
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
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // editInformation 클릭했을 때 MypagePersonalInfoActivity 이동
        val editInformation: TextView = findViewById(R.id.editInformation)
        editInformation.setOnClickListener {
            val intent = Intent(this, MypagePersonalInfoActivity::class.java)
            startActivity(intent)
        }

        // likeRecipeText 클릭했을 때 MypageLoveRecipeActivity 이동
        val likeRecipeText: TextView = findViewById(R.id.likeRecipeText)
        likeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageLoveRecipeActivity::class.java)
            startActivity(intent)
        }

        // writeRecipeText 클릭했을 때 MypageWriteRecipeActivity 이동
        val writeRecipeText: TextView = findViewById(R.id.writeRecipeText)
        writeRecipeText.setOnClickListener {
            val intent = Intent(this, MypageWriteRecipeActivity::class.java)
            startActivity(intent)
        }

        // recipeReviewListText 클릭했을 때 MypageRecipeReviewActivity 이동
        val recipeReviewListText: TextView = findViewById(R.id.recipeReviewListText)
        recipeReviewListText.setOnClickListener {
            val intent = Intent(this, MypageRecipeReviewActivity::class.java)
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

        // savePostText 클릭했을 때 MypageSavePostActivity 이동
        val savePostText: TextView = findViewById(R.id.savePostText)
        savePostText.setOnClickListener {
            val intent = Intent(this, MypageSavePostActivity::class.java)
            startActivity(intent)
        }

        // writePostText 클릭했을 때 MypageWritePostActivity 이동
        val writePostText: TextView = findViewById(R.id.writePostText)
        writePostText.setOnClickListener {
            val intent = Intent(this, MypageWritePostActivity::class.java)
            startActivity(intent)
        }
    }
}
