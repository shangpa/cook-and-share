/*검색 결과*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class SearchResult : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_result)

        // recipeResultBackIcon 클릭했을 때 SearchMain 이동
        val SearchResultBackIcon: ImageButton = findViewById(R.id.SearchResultBackIcon)
        SearchResultBackIcon.setOnClickListener {
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
        }

    }
}

