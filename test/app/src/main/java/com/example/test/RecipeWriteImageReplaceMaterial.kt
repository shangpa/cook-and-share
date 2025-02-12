/*레시피 작성 동영상 STEP1*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteImageReplaceMaterial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image_replace_material)

        // nextFixButton 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteImageMaterial 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageMaterial::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteImageTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteImageMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
            startActivity(intent)
        }

        // five 클릭했을 때 RecipeWriteImageCookOrder 이동
        val five: TextView = findViewById(R.id.five)
        five.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageCookOrder::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteImageDetailSettle 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageDetailSettle::class.java)
            startActivity(intent)
        }

        // 재료명1 입력된 텍스트 가져오기
        val material = findViewById<EditText>(R.id.material)

        // 재료명2 입력된 텍스트 가져오기
        val materialTwo = findViewById<EditText>(R.id.materialTwo)

        // 계량1 입력된 텍스트 가져오기
        val measuring = findViewById<EditText>(R.id.measuring)

        // 계량2 입력된 텍스트 가져오기
        val measuringTwo = findViewById<EditText>(R.id.measuringTwo)
    }
}
