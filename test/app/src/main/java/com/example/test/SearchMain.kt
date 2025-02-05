/*검색 메인*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SearchMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_main)

        // searchIcon 클릭했을 때 MaterialSalesActivity 이동
        val searchIcon: ImageButton = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchResult::class.java)
            startActivity(intent)
        }
    }
}