package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchResult : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_result)

        // 뒤로가기 버튼
        val backBtn = findViewById<ImageButton>(R.id.SearchResultBackIcon)
        backBtn.setOnClickListener {
            val intent = Intent(this, SearchMain::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        // RecyclerView 세팅
        val recyclerView = findViewById<RecyclerView>(R.id.searchResultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 검색어 받기
        val searchKeyword = intent.getStringExtra("searchKeyword") ?: ""

        // 서버 호출
        RetrofitInstance.apiService.searchRecipes(title = searchKeyword)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (response.isSuccessful) {
                        val recipeList = response.body() ?: emptyList()
                        val adapter = RecipeSearchAdapter(recipeList)
                        recyclerView.adapter = adapter
                    } else {
                        Toast.makeText(this@SearchResult, "검색 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@SearchResult, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
