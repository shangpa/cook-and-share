package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // searchIcon 클릭했을 때 SearchMain 이동
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: ImageButton = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // person 클릭했을 때 MaterialSalesActivity 이동
        val person: ImageButton = findViewById(R.id.person)
        person.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // materialExchange 클릭했을 때 NoticeActivity 이동
        val materialExchange: LinearLayout = findViewById(R.id.materialExchange)
        materialExchange.setOnClickListener {
            val intent = Intent(this, FridgeMaterialActivity::class.java)
            startActivity(intent)
        }

        // recipeManage 클릭했을 때 NoticeActivity 이동
        val recipeManage: LinearLayout = findViewById(R.id.recipeManage)
        recipeManage.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }

        // seeMore 클릭했을 때 NoticeActivity 이동
        val seeMore: TextView = findViewById(R.id.seeMore)
        seeMore.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // seeMoreTwo 클릭했을 때 NoticeActivity 이동
        val seeMoreTwo: TextView = findViewById(R.id.seeMoreTwo)
        seeMoreTwo.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // moreSeeRecipe 클릭했을 때 NoticeActivity 이동
        val moreSeeRecipe: LinearLayout = findViewById(R.id.moreSeeRecipe)
        moreSeeRecipe.setOnClickListener {
            val intent = Intent(this, RecipeTotal::class.java)
            startActivity(intent)
        }

        // interestRecipeVideoList 클릭했을 때 NoticeActivity 이동
        val interestRecipeVideoList: LinearLayout = findViewById(R.id.interestRecipeVideoList)
        interestRecipeVideoList.setOnClickListener {
            val intent = Intent(this, VideoSee::class.java)
            startActivity(intent)
        }

        // themeRecipe 클릭했을 때 NoticeActivity 이동
        val themeRecipe: TextView = findViewById(R.id.themeRecipe)
        themeRecipe.setOnClickListener {
            val intent = Intent(this, RecipeTotal::class.java)
            startActivity(intent)
        }

        // logo 클릭했을 때 NoticeActivity 이동
        val logo: ImageButton = findViewById(R.id.logo)
        logo.setOnClickListener {
            val intent = Intent(this, CommunityMain::class.java)
            startActivity(intent)
        }

    }
}