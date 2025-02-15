/*리뷰 작성*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class WriteReview : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.write_review)

        // nextFixButton 클릭했을 때 StepRecipeReview 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, StepRecipeReview::class.java)
            startActivity(intent)
        }

        // 리뷰 내용을 작성해주세요에 입력된 텍스트 가져오기
        val reviewContentWrite = findViewById<EditText>(R.id.reviewContentWrite)

        // 별 선언
        val star = findViewById<ImageButton>(R.id.star)
        val starTwo = findViewById<ImageButton>(R.id.starTwo)
        val starThree = findViewById<ImageButton>(R.id.starThree)
        val starFour = findViewById<ImageButton>(R.id.starFour)
        val starFive = findViewById<ImageButton>(R.id.starFive)
        val starSix = findViewById<ImageButton>(R.id.starSix)
        val starSeven = findViewById<ImageButton>(R.id.starSeven)
        val starEight = findViewById<ImageButton>(R.id.starEight)
        val starNine = findViewById<ImageButton>(R.id.starNine)
        val starTen = findViewById<ImageButton>(R.id.starTen)

        // 별1 눌렀을때 별1 없어지고 별6 나타남
        star.setOnClickListener {
            star.visibility = View.GONE
            starSix.visibility = View.VISIBLE
        }

        // 별6 눌렀을때 별6 없어지고 별1 나타남
        starSix.setOnClickListener {
            starSix.visibility = View.GONE
            star.visibility = View.VISIBLE
        }

        // 별2 눌렀을때 별2 없어지고 별7 나타남
        starTwo.setOnClickListener {
            starTwo.visibility = View.GONE
            starSeven.visibility = View.VISIBLE
        }

        // 별7 눌렀을때 별7 없어지고 별2 나타남
        starSeven.setOnClickListener {
            starSeven.visibility = View.GONE
            starTwo.visibility = View.VISIBLE
        }

        // 별3 눌렀을때 별3 없어지고 별8 나타남
        starThree.setOnClickListener {
            starThree.visibility = View.GONE
            starEight.visibility = View.VISIBLE
        }

        // 별8 눌렀을때 별8 없어지고 별3 나타남
        starEight.setOnClickListener {
            starEight.visibility = View.GONE
            starThree.visibility = View.VISIBLE
        }

        // 별4 눌렀을때 별4 없어지고 별9 나타남
        starFour.setOnClickListener {
            starFour.visibility = View.GONE
            starNine.visibility = View.VISIBLE
        }

        // 별9 눌렀을때 별9 없어지고 별4 나타남
        starNine.setOnClickListener {
            starNine.visibility = View.GONE
            starFour.visibility = View.VISIBLE
        }

        // 별5 눌렀을때 별5 없어지고 별10 나타남
        starFive.setOnClickListener {
            starFive.visibility = View.GONE
            starTen.visibility = View.VISIBLE
        }

        // 별10 눌렀을때 별10 없어지고 별5 나타남
        starTen.setOnClickListener {
            starTen.visibility = View.GONE
            starFive.visibility = View.VISIBLE
        }
    }
}