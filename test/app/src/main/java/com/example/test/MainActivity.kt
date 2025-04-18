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

        // tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageButton = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // nowTapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val nowTapVillageKitchenText: TextView = findViewById(R.id.nowTapVillageKitchenText)
        nowTapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageButton = findViewById(R.id.tapRecipeIcon)
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

        // tapCommunityIcon 클릭했을 때 CommunityMain 이동
        val tapCommunityIcon: ImageButton = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMain::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMain 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMain::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageButton = findViewById(R.id.tapFridgeIcon)
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

        // searchIcon 클릭했을 때 SearchMain 이동
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMainActivity::class.java)
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
            val intent = Intent(this, FridgeIngredientActivity::class.java)
            startActivity(intent)
        }

        // recipeManage 클릭했을 때 NoticeActivity 이동
        val recipeManage: LinearLayout = findViewById(R.id.recipeManage)
        recipeManage.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }

        // recipeWrite 클릭했을 때 RecipeWriteMain 이동
        val recipeWrite: LinearLayout = findViewById(R.id.recipeWrite)
        recipeWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteMain::class.java)
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
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // interestRecipeVideoList 클릭했을 때 NoticeActivity 이동
        val interestRecipeVideoList: LinearLayout = findViewById(R.id.interestRecipeVideoList)
        interestRecipeVideoList.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            startActivity(intent)
        }

        // themeRecipe 클릭했을 때 NoticeActivity 이동
        val themeRecipe: TextView = findViewById(R.id.themeRecipe)
        themeRecipe.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
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