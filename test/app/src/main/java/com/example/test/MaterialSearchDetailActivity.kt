package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.TradePostAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialSearchDetailActivity : AppCompatActivity() {

    private lateinit var tradePostRecyclerView: RecyclerView
    private lateinit var nameEditText: EditText
    private lateinit var numberTextView: TextView
    private lateinit var searchEmptyImage: ImageView
    private lateinit var searchEmptyText: TextView
    private lateinit var searchButton: ImageView

    private lateinit var tradePostAdapter: TradePostAdapter
    private var tradePosts: List<TradePostResponse> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_search_details)

        // tapVillageKitchenIcon 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenIcon: ImageView = findViewById(R.id.tapVillageKitchenIcon)
        tapVillageKitchenIcon.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapVillageKitchenText 클릭했을 때 MaterialActivity 이동
        val tapVillageKitchenText: TextView = findViewById(R.id.tapVillageKitchenText)
        tapVillageKitchenText.setOnClickListener {
            val intent = Intent(this, MaterialActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeIcon 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeIcon: ImageView = findViewById(R.id.tapRecipeIcon)
        tapRecipeIcon.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapRecipeText 클릭했을 때 RecipeSeeMainActivity 이동
        val tapRecipeText: TextView = findViewById(R.id.tapRecipeText)
        tapRecipeText.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // tapHomeIcon 클릭했을 때 MainActivity 이동
        val tapHomeIcon: ImageView = findViewById(R.id.tapHomeIcon)
        tapHomeIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityIcon 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityIcon: ImageView = findViewById(R.id.tapCommunityIcon)
        tapCommunityIcon.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapCommunityText 클릭했을 때 CommunityMainActivity 이동
        val tapCommunityText: TextView = findViewById(R.id.tapCommunityText)
        tapCommunityText.setOnClickListener {
            val intent = Intent(this, CommunityMainActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeIcon 클릭했을 때 FridgeActivity 이동
        val tapFridgeIcon: ImageView = findViewById(R.id.tapFridgeIcon)
        tapFridgeIcon.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // tapFridgeText 클릭했을 때 FridgeActivity 이동
        val tapFridgeText: TextView = findViewById(R.id.tapFridgeText)
        tapFridgeText.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        tradePostRecyclerView = findViewById(R.id.tradePostRecyclerView)
        nameEditText = findViewById(R.id.nameEditText)
        numberTextView = findViewById(R.id.number)
        searchEmptyImage = findViewById(R.id.searchEmptyImage)
        searchEmptyText = findViewById(R.id.searchEmptyText)
        searchButton = findViewById(R.id.search)

        tradePostRecyclerView.layoutManager = LinearLayoutManager(this)

        val keyword = intent.getStringExtra("keyword") ?: ""

        nameEditText.setText(keyword)

        if (keyword.isNotEmpty()) {
            searchTradePosts(keyword)
        } else {
            Toast.makeText(this, "검색어가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageView>(R.id.searchBack).setOnClickListener {
            finish()
        }

        searchButton.setOnClickListener {
            val newKeyword = nameEditText.text.toString().trim()
            if (newKeyword.isNotEmpty()) {
                searchTradePosts(newKeyword)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchTradePosts(keyword: String) {
        val token = "Bearer ${App.prefs.token}"

        RetrofitInstance.apiService.searchTradePosts(token, keyword)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val posts = response.body() ?: emptyList()

                        if (posts.isNotEmpty()) {
                            showResultView()
                            tradePosts = posts
                            setRecyclerViewAdapter(tradePosts)
                        } else {
                            showEmptyView()
                        }
                    } else {
                        Toast.makeText(this@MaterialSearchDetailActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Toast.makeText(this@MaterialSearchDetailActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setRecyclerViewAdapter(list: List<TradePostResponse>) {
        tradePostAdapter = TradePostAdapter(list) { tradePost ->
            val token = App.prefs.token.toString()

            // 조회수 증가 호출
            RetrofitInstance.apiService.increaseViewCount(tradePost.tradePostId, "Bearer $token")
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.d("ViewCount", "조회수 증가 성공")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.d("ViewCount", "조회수 증가 실패")
                    }
                })

            // 상세 페이지 이동
            val intent = Intent(this, MaterialDetailActivity::class.java)
            intent.putExtra("tradePostId", tradePost.tradePostId)
            startActivity(intent)
        }
        tradePostRecyclerView.adapter = tradePostAdapter
        numberTextView.text = list.size.toString()
    }

    private fun showResultView() {
        searchEmptyImage.visibility = View.GONE
        searchEmptyText.visibility = View.GONE
        tradePostRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        numberTextView.text = "0"
        searchEmptyImage.visibility = View.VISIBLE
        searchEmptyText.visibility = View.VISIBLE
        tradePostRecyclerView.visibility = View.GONE
    }
}
