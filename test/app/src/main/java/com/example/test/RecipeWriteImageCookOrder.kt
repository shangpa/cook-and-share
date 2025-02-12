/*레시피 작성 동영상 STEP1*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteImageCookOrder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image_cook_order)

        // nextFixButton 클릭했을 때 RecipeWriteImageDetailSettle 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageDetailSettle::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
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

        // four 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteImageDetailSettle 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageDetailSettle::class.java)
            startActivity(intent)
        }

        // 1-1. 레시피를 입력해주세요.에 입력된 텍스트 가져오기
        val recipeWrite = findViewById<EditText>(R.id.recipeWrite)

        // 타이머 추가 버튼 클릭 이벤트
        val timerAddButton = findViewById<Button>(R.id.timerAdd)
        val contentAddButton = findViewById<Button>(R.id.contentAdd)

        val timer = findViewById<TextView>(R.id.timer)
        val hour = findViewById<EditText>(R.id.hour)
        val time = findViewById<TextView>(R.id.time)
        val minute = findViewById<EditText>(R.id.minute)
        val settle = findViewById<TextView>(R.id.settle)
        val middle = findViewById<TextView>(R.id.middle)
        val delete = findViewById<TextView>(R.id.delete)

        timerAddButton.setOnClickListener {
            // 버튼들 사라지게 하기
            timerAddButton.visibility = View.GONE
            contentAddButton.visibility = View.GONE

            // 타이머 관련 요소들 나타나게 하기
            timer.visibility = View.VISIBLE
            hour.visibility = View.VISIBLE
            time.visibility = View.VISIBLE
            minute.visibility = View.VISIBLE
            settle.visibility = View.VISIBLE
            middle.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        }
    }
}
