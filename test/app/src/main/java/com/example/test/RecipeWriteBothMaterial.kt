/*레시피 작성 STEP2*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteBothMaterial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_material)

        // nextFixButton 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteBothTitle 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteBothTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
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

        // six 클릭했을 때 RecipeWriteBothCookVideo 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
            startActivity(intent)
        }

        // seven 클릭했을 때 RecipeWriteBothDetailSettle 이동
        val seven: TextView = findViewById(R.id.seven)
        seven.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothDetailSettle::class.java)
            startActivity(intent)
        }

        // 재료명1 입력된 텍스트 가져오기
        val material = findViewById<EditText>(R.id.material)

        // 재료명2 입력된 텍스트 가져오기
        val materialTwo = findViewById<EditText>(R.id.materialTwo)

        // 재료명3 입력된 텍스트 가져오기
        val materialThree = findViewById<EditText>(R.id.materialThree)

        // 재료명4 입력된 텍스트 가져오기
        val materialFour = findViewById<EditText>(R.id.materialFour)

        // 재료명5 입력된 텍스트 가져오기
        val materialFive = findViewById<EditText>(R.id.materialFive)

        // 재료명6 입력된 텍스트 가져오기
        val materialSix = findViewById<EditText>(R.id.materialSix)

        // 계량1 입력된 텍스트 가져오기
        val measuring = findViewById<EditText>(R.id.measuring)

        // 계량2 입력된 텍스트 가져오기
        val measuringTwo = findViewById<EditText>(R.id.measuringTwo)

        // 계량3 입력된 텍스트 가져오기
        val measuringThree = findViewById<EditText>(R.id.measuringThree)

        // 계량4 입력된 텍스트 가져오기
        val measuringFour = findViewById<EditText>(R.id.measuringFour)

        // 계량5 입력된 텍스트 가져오기
        val measuringFive = findViewById<EditText>(R.id.measuringFive)

        // 계량6 입력된 텍스트 가져오기
        val measuringSix = findViewById<EditText>(R.id.measuringSix)

    }
}
