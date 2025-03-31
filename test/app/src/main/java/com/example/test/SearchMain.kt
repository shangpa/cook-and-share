package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SearchMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_main)

        val searchIcon: ImageButton = findViewById(R.id.searchIcon)
        val searchText: EditText = findViewById(R.id.writeReview) // 입력값 받을 뷰

        searchIcon.setOnClickListener {
            val keyword = searchText.text.toString()
            val intent = Intent(this, SearchResult::class.java)
            intent.putExtra("searchKeyword", keyword)
            startActivity(intent)
        }

    }
}