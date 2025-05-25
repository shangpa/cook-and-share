package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.network.RetrofitInstance
import java.util.Stack



class MaterialMyProfileActivity : AppCompatActivity() {


    private lateinit var buttons: List<Button> // 거리 버튼 리스트
    private lateinit var selectedFilterLayout: LinearLayout

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    private lateinit var profile: LinearLayout
    private lateinit var saleHistory: LinearLayout
    private lateinit var purchaseHistory: LinearLayout
    private lateinit var reviewContainer: ConstraintLayout
    private lateinit var savePost: ConstraintLayout
    private lateinit var review: LinearLayout

    private val viewStack = Stack<View>()


    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_profile)

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

        val token = "Bearer ${App.prefs.token.toString()}"

        profile = findViewById(R.id.profile)

        // 프로필 선언
        val profileLayout = findViewById<LinearLayout>(R.id.profile)
        val sale : LinearLayout = findViewById(R.id.saleLine)
        val purchase : LinearLayout = findViewById(R.id.purchaseLine)
        val saved : LinearLayout = findViewById(R.id.savedLine)
        val review : LinearLayout = findViewById(R.id.reviewLine)


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

    }

}



