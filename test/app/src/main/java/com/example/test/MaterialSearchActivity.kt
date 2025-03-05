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
        setContentView(R.layout.activity_material_search) // MaterialSearchActivity의 레이아웃 파일 연결

        // EditText와 검색 버튼
        nameEditText = findViewById(R.id.nameEditText)
        val searchButton: ImageView = findViewById(R.id.seach)
        searchHistoryContainer = findViewById(R.id.searchHistoryContainer)

        // SharedPreferences에서 검색 기록 복원
        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val savedSearchHistory = sharedPreferences.getStringSet("search_history", mutableSetOf())

        // 이전 검색 기록을 화면에 표시
        savedSearchHistory?.forEach { searchTerm ->
            addSearchHistoryView(searchTerm)
        }

        // 검색 버튼 클릭 시 검색어 저장 및 화면 이동
        searchButton.setOnClickListener {
            val searchTerm = nameEditText.text.toString()
            if (searchTerm.isNotEmpty()) {
                // 검색어를 SharedPreferences에 저장
                val editor = sharedPreferences.edit()
                val updatedHistory = savedSearchHistory?.toMutableSet()
                if (updatedHistory != null) {
                    updatedHistory.add(searchTerm)
                }
                editor.putStringSet("search_history", updatedHistory)
                editor.apply()

                // 검색 기록을 화면에 추가
                addSearchHistoryView(searchTerm)

                // 검색 화면으로 이동
                val intent = Intent(this, MaterialSearchDetailActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 전체 삭제 버튼
        val clearAllText: TextView = findViewById(R.id.clearAllText)
        clearAllText.setOnClickListener {
            searchHistoryContainer.removeAllViews() // 모든 검색 기록 제거
            clearAllSearchHistoryFromPreferences() // SharedPreferences에서도 삭제
        }
    }

    // 검색 기록을 화면에 추가하는 메서드
    private fun addSearchHistoryView(searchTerm: String) {
        val searchHistoryView = layoutInflater.inflate(R.layout.search_history_item, null) as LinearLayout
        val searchText: TextView = searchHistoryView.findViewById(R.id.searchHistoryText)
        val closeButton: ImageView = searchHistoryView.findViewById(R.id.closeButton)

        searchText.text = searchTerm

        // 기존 LayoutParams 가져오기
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 15, 0, 15) // Bottom Margin 추가

        searchHistoryView.layoutParams = layoutParams // 적용

        // 검색 기록 삭제 버튼 클릭 이벤트
        closeButton.setOnClickListener {
            searchHistoryContainer.removeView(searchHistoryView)
        }

        searchHistoryContainer.addView(searchHistoryView)
    }


    // SharedPreferences에서 검색 기록을 제거하는 메서드
    private fun removeSearchHistoryFromPreferences(view: LinearLayout) {
        val searchText: TextView = view.findViewById(R.id.searchHistoryText)
        val searchTerm = searchText.text.toString()

        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val savedSearchHistory = sharedPreferences.getStringSet("search_history", mutableSetOf())?.toMutableSet()

        savedSearchHistory?.remove(searchTerm)

        val editor = sharedPreferences.edit()
        editor.putStringSet("search_history", savedSearchHistory)
        editor.apply()
    }

    // SharedPreferences에서 모든 검색 기록을 삭제하는 메서드
    private fun clearAllSearchHistoryFromPreferences() {
        val sharedPreferences = getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("search_history")
        editor.apply()
    }
}
