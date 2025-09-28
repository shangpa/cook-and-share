package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Repository.IngredientHistoryRepository
import com.example.test.adapter.IngredientHistoryAdapter
import com.example.test.model.pantry.HistoryType
import com.example.test.model.pantry.IngredientHistoryUi
import com.example.test.model.pantry.toUi
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class MypageIngredientListActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PANTRY_ID = "pantryId"
    }

    private lateinit var categoryButtons: List<LinearLayout>
    private lateinit var categoryTexts: List<TextView>
    private lateinit var fridgeResultDropDownIcon: ImageView
    private lateinit var fridgeFilterText: TextView
    private lateinit var resultCountTextView: TextView

    private var selectedIndex = 0
    private var fullList: List<IngredientHistoryUi> = emptyList()
    private lateinit var adapter: IngredientHistoryAdapter
    private lateinit var searchInput: EditText
    private var currentKeyword: String = ""

    private val repo = IngredientHistoryRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_ingredient_list)

        val hideSearch = intent.getBooleanExtra("hideSearch", false)

        // 검색 레이아웃 자체를 감추기 (레이아웃 id는 실제 xml과 맞춰주세요)
        val searchLayout: LinearLayout = findViewById(R.id.fridgeSearchLayout)
        val searchEdit: EditText = findViewById(R.id.fridgeSearchInput)

        if (hideSearch) {
            searchLayout.visibility = View.GONE
        } else {
            // 검색을 보일 때만 TextWatcher 연결
            searchEdit.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    currentKeyword = s?.toString().orEmpty()
                    applyFilterAndSort()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        resultCountTextView = findViewById(R.id.fridgeRecipeResultNumber)

        val recyclerView = findViewById<RecyclerView>(R.id.IngredientHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = IngredientHistoryAdapter()
        recyclerView.adapter = adapter

        categoryButtons = listOf(
            findViewById(R.id.category_all),   // 전체
            findViewById(R.id.category_cold),  // ADD
            findViewById(R.id.category_freeze) // USE/DISCARD/ADJUST
        )

        categoryTexts = listOf(
            findViewById(R.id.text_all),
            findViewById(R.id.text_cold),
            findViewById(R.id.text_freeze)
        )

        fridgeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeFilterText = findViewById(R.id.fridgeRecipefillterText)
        if (fridgeFilterText.text.isNullOrBlank()) fridgeFilterText.text = "최신순"

        fridgeResultDropDownIcon.setOnClickListener { showDropdownMenu() }

        categoryButtons.forEachIndexed { index, layout ->
            layout.setOnClickListener {
                updateCategorySelection(index)
                applyFilterAndSort()
            }
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        searchInput = findViewById(R.id.fridgeSearchInput)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s?.toString().orEmpty()
                applyFilterAndSort()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ 조건적으로 호출: keyword 있으면 개별, 없으면 전체
        val keyword = intent.getStringExtra("ingredientName")
        if (keyword.isNullOrBlank()) {
            fetchAllFridgeHistories()
        } else {
            currentKeyword = keyword
            fetchHistory(keyword)
        }

        updateCategorySelection(0)
    }

    private fun fetchHistory(keyword: String?) {
        val pantryId = intent.getLongExtra(EXTRA_PANTRY_ID, -1L)
        if (pantryId <= 0L) {
            Toast.makeText(this, "잘못된 냉장고 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val bearer = "Bearer ${App.prefs.token ?: ""}"
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.listPantryHistory(bearer, pantryId, keyword)
                }
            }.onSuccess { list ->
                fullList = list.map { it.toUi() }
                applyFilterAndSort()
            }.onFailure { e ->
                Toast.makeText(this@MypageIngredientListActivity,
                    "내역 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchAllFridgeHistories() {
        val pantryId = intent.getLongExtra(EXTRA_PANTRY_ID, -1L)
        if (pantryId <= 0L) {
            Toast.makeText(this, "잘못된 냉장고 ID입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val bearer = "Bearer ${App.prefs.token ?: ""}"
            val result = withContext(Dispatchers.IO) {
                repo.loadByPantry(bearer, pantryId, null)
            }
            result.onSuccess { list ->
                fullList = list
                applyFilterAndSort()
            }.onFailure { e ->
                Toast.makeText(
                    this@MypageIngredientListActivity,
                    "데이터 불러오기 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun applyFilterAndSort() {
        // 1) 탭 필터: 0 전체, 1 ADD, 2 USE/DISCARD/ADJUST
        val filteredByType = when (selectedIndex) {
            1 -> fullList.filter { it.type == HistoryType.ADD }
            2 -> fullList.filter {
                it.type == HistoryType.USE || it.type == HistoryType.DISCARD || it.type == HistoryType.ADJUST
            }
            else -> fullList
        }

        // 2) 키워드 필터
        val keyword = currentKeyword.trim()
        val filtered = if (keyword.isNotEmpty())
            filteredByType.filter { it.ingredientName.contains(keyword, ignoreCase = true) }
        else filteredByType

        // 3) 정렬
        val sorted = when (fridgeFilterText.text.toString()) {
            "오래된순" -> filtered.sortedBy { it.createdAt }
            else       -> filtered.sortedByDescending { it.createdAt }
        }

        adapter.submitList(sorted)
        resultCountTextView.text = sorted.size.toString()
        android.util.Log.d("HIST", "listSize=${sorted.size}, ids=${sorted.joinToString { it.id.toString() }}")
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
