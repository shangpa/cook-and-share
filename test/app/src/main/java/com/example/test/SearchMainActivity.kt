package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.SeasonalRecipeAdapter
import com.example.test.model.Recipe
import com.example.test.model.recipeDetail.SeasonalRecipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_main)

        TabBarUtils.setupTabBar(this)

        val keywordViews = listOf<TextView>(
            findViewById(R.id.One),
            findViewById(R.id.Two),
            findViewById(R.id.Three),
            findViewById(R.id.Four),
            findViewById(R.id.Five),
            findViewById(R.id.Six),
            findViewById(R.id.Seven),
            findViewById(R.id.Eight),
            findViewById(R.id.Nine),
            findViewById(R.id.Ten),
        )

        RetrofitInstance.apiService.getPopularKeywords().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val keywords = response.body() ?: emptyList()
                    for (i in keywords.indices) {
                        keywordViews[i].text = "${i + 1}.  ${keywords[i]}"
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(this@SearchMainActivity, "인기 검색어 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })

        val searchIcon: ImageButton = findViewById(R.id.searchIcon)
        val searchText: EditText = findViewById(R.id.writeSearchTxt)

        searchIcon.setOnClickListener {
            val keyword = searchText.text.toString()
            if (keyword.isNotBlank()) {
                // 서버에 검색어 저장
                RetrofitInstance.apiService.saveSearchKeyword(keyword).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.d("SearchMain", "검색어 저장 성공")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("SearchMain", "검색어 저장 실패", t)
                    }
                })

                // 검색 결과 화면 이동
                val intent = Intent(this, SearchResultActivity::class.java)
                intent.putExtra("searchKeyword", keyword)
                startActivity(intent)
            }
        }
        keywordViews.forEach { textView ->
            textView.setOnClickListener {
                val keyword = textView.text.toString().substringAfter(".").trim()
                val intent = Intent(this, SearchResultActivity::class.java)
                intent.putExtra("searchKeyword", keyword)
                startActivity(intent)
            }
        }

        findViewById<ImageButton>(R.id.SearchMainBackIcon).setOnClickListener {
            finish()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.seasonalRecipeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val adapter = SeasonalRecipeAdapter { r ->
            startActivity(Intent(this, RecipeSeeActivity::class.java).putExtra("recipeId", r.recipeId))
        }
        recyclerView.adapter = adapter

        RetrofitInstance.apiService.getSeasonalRecipes()
            .enqueue(object : Callback<List<SeasonalRecipe>> {
                override fun onResponse(
                    call: Call<List<SeasonalRecipe>>,
                    response: Response<List<SeasonalRecipe>>
                ) {
                    val data = if (response.isSuccessful) response.body().orEmpty() else emptyList()
                    adapter.submitList(data)   // ✅ 이제 인식됨
                }
                override fun onFailure(call: Call<List<SeasonalRecipe>>, t: Throwable) { /* ... */ }
            })

    }

    override fun onResume() {
        super.onResume()
        fetchPopularKeywords()
    }

    private fun fetchPopularKeywords() {
        val keywordViews = listOf<TextView>(
            findViewById(R.id.One), findViewById(R.id.Two), findViewById(R.id.Three),
            findViewById(R.id.Four), findViewById(R.id.Five), findViewById(R.id.Six),
            findViewById(R.id.Seven), findViewById(R.id.Eight), findViewById(R.id.Nine),
            findViewById(R.id.Ten),
        )
        RetrofitInstance.apiService.getPopularKeywords().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val keywords = response.body().orEmpty()
                    for (i in keywordViews.indices) {
                        keywordViews[i].text = if (i < keywords.size) "${i + 1}.  ${keywords[i]}" else ""
                    }
                }
            }
            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(this@SearchMainActivity, "인기 검색어 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
