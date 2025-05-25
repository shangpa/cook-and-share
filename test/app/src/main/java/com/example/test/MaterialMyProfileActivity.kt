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
import com.example.test.Utils.TabBarUtils
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

    }

}



