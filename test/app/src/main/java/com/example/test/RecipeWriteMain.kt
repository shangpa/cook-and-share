/*레시피 작성 메인*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecipeWriteMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_main)

        // rigthArrow 클릭했을 때 RecipeWriteBothTitle 이동
        val rigthArrow: View = findViewById(R.id.rigthArrow)
        rigthArrow.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // rigthArrowTwo 클릭했을 때 RecipeWriteImageTitle 이동
        val rigthArrowTwo: View = findViewById(R.id.rigthArrowTwo)
        rigthArrowTwo.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageTitle::class.java)
            startActivity(intent)
        }

        // rigthArrowThree 클릭했을 때 RecipeWriteVideoTitle 이동
        val rigthArrowThree: View = findViewById(R.id.rigthArrowThree)
        rigthArrowThree.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoTitle::class.java)
            startActivity(intent)
        }

        // searchIcon 클릭했을 때 RecipeWriteVideoTitle 이동
        val searchIcon: View = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
        }

        // bellIcon 클릭했을 때 NoticeActivity 이동
        val bellIcon: View = findViewById(R.id.bellIcon)
        bellIcon.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // person 클릭했을 때 MypageActivity 이동
        val person: View = findViewById(R.id.person)
        person.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // bothWrite 클릭했을 때 RecipeWriteBothTitle 이동
        val bothWrite: TextView = findViewById(R.id.bothWrite)
        bothWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // imageWrite 클릭했을 때 RecipeWriteImageTitle 이동
        val imageWrite: TextView = findViewById(R.id.imageWrite)
        imageWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageTitle::class.java)
            startActivity(intent)
        }

        // videoWrite 클릭했을 때 RecipeWriteVideoTitle 이동
        val videoWrite: TextView = findViewById(R.id.videoWrite)
        videoWrite.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoTitle::class.java)
            startActivity(intent)
        }

    }
}

