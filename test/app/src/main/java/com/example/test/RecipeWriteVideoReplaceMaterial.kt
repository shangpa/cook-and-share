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

class RecipeWriteVideoReplaceMaterial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_video_replace_material)

        // nextFixButton 클릭했을 때 RecipeWriteVideoHandlingMethod 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoHandlingMethod::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteVideoMaterial 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoMaterial::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteVideoTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteVideoMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteVideoHandlingMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoHandlingMethod::class.java)
            startActivity(intent)
        }

        // five 클릭했을 때 RecipeWriteVideoCookVideo 이동
        val five: TextView = findViewById(R.id.five)
        five.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoCookVideo::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteVideoDetailSettle 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoDetailSettle::class.java)
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
