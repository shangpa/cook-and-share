/*레시피 작성 STEP6*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteBothCookVideo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_cook_video)

        // nextFixButton 클릭했을 때 RecipeWriteBothDetailSettle 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothDetailSettle::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteBothCookOrder 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookOrder::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteBothTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteBothMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothMaterial::class.java)
            startActivity(intent)
        }

        // three 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteBothHandlindMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothHandlindMethod::class.java)
            startActivity(intent)
        }

        // five 클릭했을 때 RecipeWriteBothCookOrder 이동
        val five: TextView = findViewById(R.id.five)
        five.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookOrder::class.java)
            startActivity(intent)
        }

        // seven 클릭했을 때 RecipeWriteBothDetailSettle 이동
        val seven: TextView = findViewById(R.id.seven)
        seven.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothDetailSettle::class.java)
            startActivity(intent)
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}