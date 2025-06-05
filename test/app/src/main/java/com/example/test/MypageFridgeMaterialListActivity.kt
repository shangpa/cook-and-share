package com.example.test

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.FridgeHistoryAdapter
import com.example.test.model.Fridge.FridgeHistoryResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageFridgeMaterialListActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<LinearLayout>
    private lateinit var categoryTexts: List<TextView>
    private lateinit var fridgeResultDropDownIcon: ImageView
    private lateinit var fridgeFilterText: TextView
    private lateinit var resultCountTextView: TextView

    private var selectedIndex = 0
    private lateinit var fullList: List<FridgeHistoryResponse>
    private lateinit var adapter: FridgeHistoryAdapter
    private lateinit var token: String
    private lateinit var searchInput: EditText
    private var currentKeyword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_fridge_material_list)

        resultCountTextView = findViewById(R.id.fridgeRecipeResultNumber)
        token = App.prefs.token ?: ""

        val recyclerView = findViewById<RecyclerView>(R.id.fridgeHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FridgeHistoryAdapter(emptyList())
        recyclerView.adapter = adapter

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

        fridgeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeFilterText = findViewById(R.id.fridgeRecipefillterText)

        fridgeResultDropDownIcon.setOnClickListener {
            showDropdownMenu()
        }

        categoryButtons.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                updateCategorySelection(index)
                applyFilterAndSort()
            }
        }

        updateCategorySelection(0)
        fetchAllFridgeHistories()

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        searchInput = findViewById(R.id.fridgeSearchInput)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s.toString()
                applyFilterAndSort()  // 검색 포함된 필터링 실행
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchAllFridgeHistories() {
        RetrofitInstance.apiService.getAllFridgeHistories("Bearer $token")
            .enqueue(object : Callback<List<FridgeHistoryResponse>> {
                override fun onResponse(call: Call<List<FridgeHistoryResponse>>, response: Response<List<FridgeHistoryResponse>>) {
                    if (response.isSuccessful) {
                        fullList = response.body() ?: emptyList()
                        applyFilterAndSort()
                    } else {
                        Toast.makeText(this@MypageFridgeMaterialListActivity, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<FridgeHistoryResponse>>, t: Throwable) {
                    Toast.makeText(this@MypageFridgeMaterialListActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun applyFilterAndSort() {
        val filteredList = when (selectedIndex) {
            1 -> fullList.filter { it.actionType == "ADD" }
            2 -> fullList.filter { it.actionType == "USE" }
            else -> fullList
        }.filter {
            it.ingredientName.contains(currentKeyword, ignoreCase = true)
        }

        val sortedList = when (fridgeFilterText.text.toString()) {
            "최신순" -> filteredList.sortedByDescending { it.actionDate }
            "오래된순" -> filteredList.sortedBy { it.actionDate }
            else -> filteredList
        }

        adapter.updateList(sortedList)

        val totalQuantity = sortedList.sumOf { it.quantity }
        resultCountTextView.text = formatQuantity(totalQuantity)
    }

    private fun formatQuantity(q: Double): String {
        return if (q % 1.0 == 0.0) {
            q.toInt().toString()
        } else {
            String.format("%.1f", q)
        }
    }

    private fun updateCategorySelection(selected: Int) {
        selectedIndex = selected
        categoryButtons.forEachIndexed { index, layout ->
            val textView = categoryTexts[index]
            if (index == selected) {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                textView.setTextColor(Color.WHITE)
            } else {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct)
                textView.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeResultDropDownIcon)
        popupMenu.menu.add("최신순")
        popupMenu.menu.add("오래된순")
        popupMenu.setOnMenuItemClickListener { item ->
            fridgeFilterText.text = item.title
            applyFilterAndSort()
            true
        }
        popupMenu.show()
    }
}
