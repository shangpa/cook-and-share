package com.example.test

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.FridgeHistoryAdapter
import com.example.test.model.Fridge.FridgeHistoryResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FridgeMaterialListActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<LinearLayout>
    private lateinit var categoryTexts: List<TextView>
    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private var selectedIndex: Int = 0 // 기본 선택: 전체
    private lateinit var fullList: List<FridgeHistoryResponse>
    private lateinit var adapter: FridgeHistoryAdapter
    private lateinit var resultCountTextView: TextView
    private lateinit var token: String
    private lateinit var ingredientName: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_fridge_material_list)

        resultCountTextView = findViewById(R.id.fridgeRecipeResultNumber)
        token = App.prefs.token.toString()
        ingredientName = intent.getStringExtra("ingredientName") ?: ""

        val recyclerView = findViewById<RecyclerView>(R.id.fridgeHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FridgeHistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        // 카테고리 버튼 및 텍스트 연결
        categoryButtons = listOf(
            findViewById(R.id.category_all),
            findViewById(R.id.category_cold),
            findViewById(R.id.category_freeze)
        )

        categoryTexts = listOf(
            findViewById(R.id.text_all),
            findViewById(R.id.text_cold),
            findViewById(R.id.text_freeze)
        )

        // 드롭다운 관련 뷰 연결
        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)

        // 드롭다운 메뉴 클릭 이벤트
        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showDropdownMenu()
        }

        // 초기 상태 설정
        updateCategorySelection(selectedIndex)


        categoryButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                updateCategorySelection(index)
                applyFilterAndSort()
            }
        }

        if (ingredientName.isBlank()) {
            Toast.makeText(this, "재료 이름이 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        RetrofitInstance.apiService.getFridgeHistory("Bearer $token", ingredientName)
            .enqueue(object : Callback<List<FridgeHistoryResponse>> {
                override fun onResponse(call: Call<List<FridgeHistoryResponse>>, response: Response<List<FridgeHistoryResponse>>) {
                    if (response.isSuccessful) {
                        fullList = response.body() ?: emptyList()
                        applyFilterAndSort()
                    }
                }
                override fun onFailure(call: Call<List<FridgeHistoryResponse>>, t: Throwable) {
                    // 오류 처리
                }
            })

    }

    private fun formatQuantity(q: Double): String {
        val formatted = java.text.DecimalFormat("#.#").format(q)
        return formatted
    }

    private fun applyFilterAndSort() {
        val filteredList = when (selectedIndex) {
            1 -> fullList.filter { it.actionType == "ADD" }
            2 -> fullList.filter { it.actionType == "USE" }
            else -> fullList
        }

        val sortedList = when (fridgeRecipefillterText.text.toString()) {
            "최신순" -> filteredList.sortedByDescending { it.actionDate }
            "오래된순" -> filteredList.sortedBy { it.actionDate }
            else -> filteredList
        }

        adapter.updateList(sortedList)

        val totalQuantity = sortedList.sumOf { it.quantity }
        resultCountTextView.text = formatQuantity(totalQuantity)
    }

    private fun updateCategorySelection(selected: Int) {
        categoryButtons.forEachIndexed { index, layout ->
            if (index == selected) {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                categoryTexts[index].setTextColor(Color.WHITE)
            } else {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct)
                categoryTexts[index].setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
        selectedIndex = selected
    }

    // 드롭다운 메뉴 보여주기
    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeRecipeResultDropDownIcon)
        popupMenu.menu.add("최신순")
        popupMenu.menu.add("오래된순")

        popupMenu.setOnMenuItemClickListener { item ->
            fridgeRecipefillterText.text = item.title
            applyFilterAndSort()
            true
        }

        popupMenu.show()
    }



}
