package com.example.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MaterialSearchActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var searchHistoryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_search)

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

        // 뒤로가기
        val searchBack = findViewById<ImageView>(R.id.searchBack)
        searchBack.setOnClickListener {
            finish()
        }

        nameEditText = findViewById(R.id.nameEditText)
        val searchButton: ImageView = findViewById(R.id.seach)
        searchHistoryContainer = findViewById(R.id.searchHistoryContainer)

        // SharedPreferences에서 검색 기록 복원
        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

        // 타입 문제를 방지하기 위해 일단 getString 시도하고, 실패하면 ""로
        val savedSearchHistoryString = try {
            sharedPreferences.getString("search_history", "")
        } catch (e: ClassCastException) {
            // 이전에 StringSet으로 저장된 게 있을 경우 초기화
            sharedPreferences.edit().remove("search_history").apply()
            ""
        }

        val historyList = savedSearchHistoryString?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

        historyList.forEach { searchTerm ->
            addSearchHistoryView(searchTerm)
        }

        // 검색 버튼 클릭 시
        searchButton.setOnClickListener {
            val searchTerm = nameEditText.text.toString().trim()

            if (searchTerm.isNotEmpty()) { // 진짜 글자가 있는 경우만 저장
                // 기존 저장된 기록 가져오기
                val saved = sharedPreferences.getString("search_history", "")
                val updatedList = saved?.split(",")?.toMutableList() ?: mutableListOf()

                updatedList.remove(searchTerm) // 중복 제거
                updatedList.add(0, searchTerm)  // 맨 앞에 추가

                val editor = sharedPreferences.edit()
                editor.putString("search_history", updatedList.joinToString(","))
                editor.apply()

                // 검색 기록을 화면에 추가 (맨 앞에 추가)
                addSearchHistoryView(searchTerm)

                // 검색 결과 화면 이동
                val intent = Intent(this, MaterialSearchDetailActivity::class.java)
                intent.putExtra("keyword", searchTerm)
                startActivity(intent)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }


        // 전체 삭제
        val clearAllText = findViewById<TextView>(R.id.clearAllText)
        clearAllText.setOnClickListener {
            searchHistoryContainer.removeAllViews()
            clearAllSearchHistoryFromPreferences()
        }
    }

    private fun addSearchHistoryView(searchTerm: String) {
        if (searchTerm.isBlank()) {
            return
        }

        val searchHistoryView = layoutInflater.inflate(R.layout.search_history_item, null) as LinearLayout
        val searchText: TextView = searchHistoryView.findViewById(R.id.searchHistoryText)
        val closeButton: ImageView = searchHistoryView.findViewById(R.id.closeButton)

        searchText.text = searchTerm

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 15, 0, 15)
        searchHistoryView.layoutParams = layoutParams

        closeButton.setOnClickListener {
            searchHistoryContainer.removeView(searchHistoryView)
            removeSearchHistoryFromPreferences(searchTerm)
        }

        searchHistoryView.setOnClickListener {
            val keyword = searchText.text.toString()

            val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
            val saved = sharedPreferences.getString("search_history", "")
            val updatedList = saved?.split(",")?.toMutableList() ?: mutableListOf()

            updatedList.remove(keyword) // 중복 제거
            updatedList.add(0, keyword)  // 맨 앞에 추가

            val editor = sharedPreferences.edit()
            editor.putString("search_history", updatedList.joinToString(","))
            editor.apply()

            // 화면 갱신: 기존 검색 기록 뷰 모두 지우기
            searchHistoryContainer.removeAllViews()

            // SharedPreferences에서 다시 가져와서 최신순으로 추가
            val refreshedHistory = updatedList
            refreshedHistory.forEach { term ->
                addSearchHistoryView(term)
            }

            // 검색 결과 화면 이동
            val intent = Intent(this, MaterialSearchDetailActivity::class.java)
            intent.putExtra("keyword", keyword)
            startActivity(intent)
        }


        searchHistoryContainer.addView(searchHistoryView)
    }

    private fun removeSearchHistoryFromPreferences(searchTerm: String) {
        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val saved = sharedPreferences.getString("search_history", "")
        val updatedList = saved?.split(",")?.toMutableList() ?: mutableListOf()

        updatedList.remove(searchTerm)

        val editor = sharedPreferences.edit()
        editor.putString("search_history", updatedList.joinToString(","))
        editor.apply()
    }

    private fun clearAllSearchHistoryFromPreferences() {
        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("search_history")
        editor.apply()
    }
}
