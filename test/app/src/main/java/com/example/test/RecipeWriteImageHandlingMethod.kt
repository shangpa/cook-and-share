/*레시피 작성 동영상 STEP1*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class RecipeWriteImageHandlingMethod : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image_handling_method)

        // nextFixButton 클릭했을 때 RecipeWriteImageCookOrder 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageCookOrder::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteImageReplaceMaterial 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageReplaceMaterial::class.java)
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

        // three 클릭했을 때 RecipeWriteImageReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageReplaceMaterial::class.java)
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

        // 처리방법1 입력된 텍스트 가져오기
        val handlingMethod = findViewById<EditText>(R.id.handlingMethod)

        // 처리방법2 입력된 텍스트 가져오기
        val handlingMethodTwo = findViewById<EditText>(R.id.handlingMethodTwo)

        // 선언
        val addFixButton = findViewById<Button>(R.id.addFixButton)
        val materialThree = findViewById<EditText>(R.id.materialThree)
        val measuringThree = findViewById<EditText>(R.id.measuringThree)
        val deleteThree = findViewById<ImageButton>(R.id.deleteThree)
        val divideRectangleBarFive = findViewById<View>(R.id.divideRectangleBarFive)
        val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams

        // 추가하기 눌렀을때 재료명7이랑 계량 7나옴
        addFixButton.setOnClickListener {
            materialThree.visibility = View.VISIBLE
            measuringThree.visibility = View.VISIBLE
            deleteThree.visibility = ImageButton.VISIBLE
            divideRectangleBarFive.visibility = View.VISIBLE

            // 버튼을 아래로 87dp 이동
            params.topMargin += 87
            addFixButton.layoutParams = params
        }

        // 재료7, 계량 7 삭제 버튼 눌렀을때 사라짐
        deleteThree.setOnClickListener {
            // 재료명7, 계량7, 아래 바 숨기기
            materialThree.visibility = View.GONE
            measuringThree.visibility = View.GONE
            divideRectangleBarFive.visibility = View.GONE
            deleteThree.visibility = ImageButton.GONE

            // 추가하기 버튼 위치 조정(위로 87dp)올라감
            val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin -= 87 // 87dp 위로 이동
            addFixButton.layoutParams = params
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
