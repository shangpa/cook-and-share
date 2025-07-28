/*레시피 작성 메인*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.Utils.TabBarUtils

class RecipeWriteMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_main)

        TabBarUtils.setupTabBar(this)

        // searchIcon 클릭했을 때 SearchMain 이동
        val searchIcon: View = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMainActivity::class.java)
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

        // both 클릭했을 때 RecipeWriteBothActivity 이동
        val both: ConstraintLayout = findViewById(R.id.both)
        both.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothActivity::class.java)
            startActivity(intent)
            finish()
        }

        // image 클릭했을 때 RecipeWriteImageActivity 이동
        val image: ConstraintLayout = findViewById(R.id.image)
        image.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // video 클릭했을 때 RecipeWriteVideoActivity 이동
        val video: ConstraintLayout = findViewById(R.id.video)
        video.setOnClickListener {
            val intent = Intent(this, RecipeWriteVideoActivity::class.java)
            startActivity(intent)
            finish()
        }

        // oneMinute 클릭했을 때 RecipeWriteOneMinuteActivity 이동
        val oneMinute: ConstraintLayout = findViewById(R.id.oneMinute)
        oneMinute.setOnClickListener {
            val intent = Intent(this, RecipeWriteOneMinuteActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}

