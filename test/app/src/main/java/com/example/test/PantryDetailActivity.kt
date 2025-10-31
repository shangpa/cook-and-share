package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.IngredientAdapter
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryStockDto
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AlertDialog
import java.math.BigDecimal
import kotlin.math.abs

class PantryDetailActivity : AppCompatActivity() {

    private var pantryId: Long = -1L
    private lateinit var titleView: TextView

    // 상단 UI
    private lateinit var searchEdit: EditText
    private lateinit var btnSearch: ImageButton

    // 탭 버튼 & 컨테이너
    private lateinit var btnTotal: AppCompatButton
    private lateinit var btnRefrigeration: AppCompatButton
    private lateinit var btnFreeze: AppCompatButton
    private lateinit var btnOutside: AppCompatButton
    private lateinit var totalList: ConstraintLayout
    private lateinit var coldStorageList: ConstraintLayout
    private lateinit var freezeList: ConstraintLayout
    private lateinit var outsideList: ConstraintLayout

    // 선택/수정/삭제 바
    private lateinit var totalChoice: ConstraintLayout
    private lateinit var btnAllCheck: ImageButton
    private lateinit var tvAllCheck: TextView
    private lateinit var tvModify: TextView
    private lateinit var tvDelete: TextView

    // 리사이클러뷰
    private lateinit var rvTotal: RecyclerView
    private lateinit var rvFridge: RecyclerView
    private lateinit var rvFreeze: RecyclerView
    private lateinit var rvOutside: RecyclerView

    // 어댑터
    private lateinit var totalAdapter: IngredientAdapter
    private lateinit var fridgeAdapter: IngredientAdapter
    private lateinit var freezeAdapter: IngredientAdapter
    private lateinit var outsideAdapter: IngredientAdapter

    // 원본 데이터
    private var allStocks: List<PantryStockUi> = emptyList()

    // 현재 탭 (TOTAL/FRIDGE/FREEZER/PANTRY)
    private var currentTab: String = "TOTAL"

    private var showMergedView: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_detail)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        TabBarUtils.setupTabBar(this)


        // 인텐트
        pantryId = intent.getLongExtra("pantryId", -1L)
        if (pantryId <= 0) {
            pantryId = intent.getLongExtra("id", -1L)
        }
        android.util.Log.d("PantryDetail", "onCreate pantryId=$pantryId, name=${intent.getStringExtra("name")}")

        val incomingName = intent.getStringExtra("name")

        // 헤더
        findViewById<ImageButton>(R.id.backArrow).setOnClickListener { finish() }
        titleView = findViewById(R.id.mainRefrigerator)
        if (!incomingName.isNullOrBlank()) titleView.text = formatFridgeTitle(incomingName)

        // 분석 아이콘
        findViewById<ImageButton>(R.id.analysisIcon).setOnClickListener {
            startActivity(Intent(this, PantryStatsActivity::class.java))
        }
        
        // 분석 텍스트
        findViewById<TextView>(R.id.analysisText).setOnClickListener {
            startActivity(Intent(this, PantryStatsActivity::class.java))
        }

        // + 버튼
        findViewById<ImageButton>(R.id.plusIcon).setOnClickListener {
            if (pantryId <= 0) {
                Toast.makeText(this, "냉장고 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, PantryMaterialAddActivity::class.java).apply {
                putExtra("pantryId", pantryId)   // 이 한 줄이면 끝!
            })
        }

        // + 텍스트
        findViewById<TextView>(R.id.plusText).setOnClickListener {
            if (pantryId <= 0) {
                Toast.makeText(this, "냉장고 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, PantryMaterialAddActivity::class.java).apply {
                putExtra("pantryId", pantryId)   // 이 한 줄이면 끝!
            })
        }

        // 상단 검색
        searchEdit = findViewById(R.id.materialCook)
        btnSearch = findViewById(R.id.searchIcon)

        // 탭 & 컨테이너
        btnTotal = findViewById(R.id.total)
        btnRefrigeration = findViewById(R.id.refrigeration)
        btnFreeze = findViewById(R.id.freeze)
        btnOutside = findViewById(R.id.outside)
        totalList = findViewById(R.id.totalList)
        coldStorageList = findViewById(R.id.coldStorageList)
        freezeList = findViewById(R.id.freezeList)
        outsideList = findViewById(R.id.outsideList)

        // 선택/수정/삭제 바
        totalChoice = findViewById(R.id.totalChoice)
        btnAllCheck = findViewById(R.id.check)
        tvAllCheck = findViewById(R.id.allCheck)
        tvModify = findViewById(R.id.modification)
        tvDelete = findViewById(R.id.delete)

        // 리사이클러뷰
        rvTotal = findViewById(R.id.rvTotal)
        rvFridge = findViewById(R.id.rvFridge)
        rvFreeze = findViewById(R.id.rvFreeze)
        rvOutside = findViewById(R.id.rvOutside)

        totalAdapter = makeAdapter()
        fridgeAdapter = makeAdapter()
        freezeAdapter = makeAdapter()
        outsideAdapter = makeAdapter()

        rvTotal.adapter = totalAdapter
        rvFridge.adapter = fridgeAdapter
        rvFreeze.adapter = freezeAdapter
        rvOutside.adapter = outsideAdapter

        rvTotal.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvFridge.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvFreeze.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvOutside.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)


        // 탭 토글
        val tabs: List<Pair<AppCompatButton, Pair<ConstraintLayout, String>>> = listOf(
            btnTotal to (totalList to "TOTAL"),
            btnRefrigeration to (coldStorageList to "FRIDGE"),
            btnFreeze to (freezeList to "FREEZER"),
            btnOutside to (outsideList to "PANTRY")
        )
        fun setTabSelected(button: AppCompatButton, selected: Boolean) {
            button.setBackgroundResource(
                if (selected) R.drawable.btn_fridge_ct_ck else R.drawable.btn_fridge_ct
            )
            button.setTextColor(
                if (selected) Color.parseColor("#FFFFFF") else Color.parseColor("#8A8F9C")
            )
            button.isSelected = selected
        }
        tabs.forEach { (btn: AppCompatButton, pair: Pair<ConstraintLayout, String>) ->
            val (container: ConstraintLayout, tabKey: String) = pair
            btn.setOnClickListener {
                currentTab = tabKey
                tabs.forEach { (b, innerPair) ->
                    val (c, _) = innerPair
                    val sel = (b == btn)
                    setTabSelected(b, sel)
                    c.visibility = if (sel) View.VISIBLE else View.GONE
                }
                currentAdapter().clearSelection()
                resetAllSelectUi()                 // ← 아이콘/카운트 초기화
                applySearch(searchEdit.text?.toString().orEmpty())
            }
        }
        // 초기 탭: 전체
        currentTab = "TOTAL"
        tabs.forEach { (b, pc) ->
            val (c, tabKey) = pc
            val sel = (tabKey == "TOTAL")
            setTabSelected(b, sel)
            c.visibility = if (sel) View.VISIBLE else View.GONE
        }


        btnAllCheck.setOnClickListener { toggleAll() }
        tvAllCheck.setOnClickListener { toggleAll() }

        // 수정
        tvModify.setOnClickListener {
            val selected = currentAdapter().getSelected()
            if (selected.size != 1) {
                Toast.makeText(this, "수정은 하나만 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (showMergedView) {
                Toast.makeText(this, "합쳐보기 모드에서는 개별 수정이 불가해요.\n재료 상세에서 처리하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val s = selected.first()
            startActivity(Intent(this, PantryMaterialAddDetailActivity::class.java).apply {
                putExtra("mode", "edit")
                putExtra("stockId", s.id)
                putExtra("pantryId", pantryId)
                putExtra("ingredientId", s.ingredientId ?: -1L)
                putExtra("ingredientName", s.name)
                putExtra("category", s.categoryEnum)
                putExtra("iconUrl", s.iconUrl)                 // ⬅ 아이콘 URL
                putExtra("defaultUnitId", s.unitId ?: -1L)     // ⬅ 단위 ID
                putExtra("storage", s.storageEnum)
                putExtra("quantity", s.quantityRaw)
                putExtra("purchasedAt", s.purchasedAt)         // ⬅ 구매일
                putExtra("expiresAt", s.expiresAt)             // ⬅ 유통기한
            })
        }

        // 삭제
        tvDelete.setOnClickListener {
            val selected = currentAdapter().getSelected()
            if (selected.isEmpty()) {
                Toast.makeText(this, "삭제할 재료를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pantryId <= 0) {
                Toast.makeText(this, "냉장고 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 합쳐보기 여부와 상관없이 "실제 삭제 대상(진짜 id들)"로 확장
            val targets: List<PantryStockUi> = selected.flatMap { expandMerged(it) }
            // 중복 제거(혹시 같은 그룹을 여러 줄 선택한 경우)
            val uniqueTargets = targets.distinctBy { it.id }.filter { it.id > 0 }
            if (uniqueTargets.isEmpty()) {
                Toast.makeText(this, "삭제할 항목을 찾지 못했어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val previewName = if (uniqueTargets.size == 1) uniqueTargets.first().name else "${uniqueTargets.first().name} 외 ${uniqueTargets.size - 1}개"
            AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("선택한 재료(${previewName})\n총 ${uniqueTargets.size}개 항목을 삭제할까요?")
                .setPositiveButton("삭제") { _, _ ->
                    val token = getBearer()
                    if (token == null) {
                        Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    tvDelete.isEnabled = false
                    lifecycleScope.launch {
                        var success = 0
                        var fail = 0
                        for (item in uniqueTargets) {
                            val res = runCatching {
                                withContext(Dispatchers.IO) {
                                    RetrofitInstance.pantryApi.deletePantryStock(token, pantryId, item.id)
                                }
                            }.getOrNull()

                            if (res != null && res.isSuccessful) success++ else fail++
                        }
                        if (success > 0) {
                            val removedIds = uniqueTargets.map { it.id }.toSet()
                            allStocks = allStocks.filterNot { removedIds.contains(it.id) }
                            distributeToAdapters()
                            applySearch(searchEdit.text?.toString().orEmpty())
                            resetAllSelectUi()
                        }
                        tvDelete.isEnabled = true
                        Toast.makeText(
                            this@PantryDetailActivity,
                            if (fail == 0) "재료가 삭제되었어요." else "일부 삭제 실패($fail) — 다시 시도해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 검색 적용
        btnSearch.setOnClickListener { applySearch(searchEdit.text?.toString().orEmpty()) }
        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applySearch(s?.toString().orEmpty()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 상세 제목 최신화(옵션)
        if (pantryId > 0) {
            lifecycleScope.launch {
                val token = getBearer() ?: return@launch
                runCatching {
                    withContext(Dispatchers.IO) { RetrofitInstance.pantryApi.getPantry(token, pantryId) }
                }.onSuccess { detail: PantryResponse ->
                    titleView.text = formatFridgeTitle(detail.name)
                }
            }
        }
        setupCookButton()
    }

    private var allSelected = false

    private fun toggleAll() {
        val adapter = currentAdapter()
        if (adapter.itemCount == 0) return
        allSelected = !allSelected
        adapter.toggleSelectAll(allSelected)
        // ⬇⬇ 아이콘 토글
        btnAllCheck.setImageResource(if (allSelected) R.drawable.ic_fridge_checked else R.drawable.ic_fridge_check)
        updateSelectBarText()
    }

    // 탭 변경/검색 후 선택 초기화할 때 아이콘도 초기화
    private fun resetAllSelectUi() {
        allSelected = false
        btnAllCheck.setImageResource(R.drawable.ic_fridge_check)
        updateSelectBarText()
    }

    override fun onResume() {
        super.onResume()
        if (pantryId > 0) loadStocks(pantryId) else {
            Toast.makeText(this, "냉장고 정보를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------------- 데이터 로드 & 분배 ----------------------

    private fun loadStocks(id: Long) {
        val token = getBearer() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { RetrofitInstance.pantryApi.listPantryStocks(token, id) }
            }.onSuccess { stocks ->
                android.util.Log.d("PantryDetail", "stocks size=${stocks.size}")  // ← 여기!
                allStocks = stocks.map { it.toUi() }
                distributeToAdapters()
                applySearch(searchEdit.text?.toString().orEmpty())
            }.onFailure { e ->
                android.util.Log.e("PantryDetail", "listPantryStocks failed", e)
                Toast.makeText(this@PantryDetailActivity, "재고를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun distributeToAdapters() {
        val totalItems = if (showMergedView) mergeByIngredient(allStocks) else allStocks
        val fridgeItemsSrc = allStocks.filter { it.storageEnum == "FRIDGE" }
        val freezerItemsSrc = allStocks.filter { it.storageEnum == "FREEZER" }
        val pantryItemsSrc = allStocks.filter { it.storageEnum == "PANTRY" }

        val fridgeItems = if (showMergedView) mergeByIngredient(fridgeItemsSrc) else fridgeItemsSrc
        val freezerItems = if (showMergedView) mergeByIngredient(freezerItemsSrc) else freezerItemsSrc
        val pantryItems = if (showMergedView) mergeByIngredient(pantryItemsSrc) else pantryItemsSrc

        totalAdapter.submit(totalItems)
        fridgeAdapter.submit(fridgeItems)
        freezeAdapter.submit(freezerItems)
        outsideAdapter.submit(pantryItems)
    }

    private fun applySearch(q: String) {
        val query = q.trim()

        fun filter(list: List<PantryStockUi>): List<PantryStockUi> {
            val base = if (showMergedView) mergeByIngredient(list) else list
            if (query.isEmpty()) return base
            return base.filter { it.name.contains(query, ignoreCase = true) }
        }

        when (currentTab) {
            "TOTAL"  -> totalAdapter.submit(filter(allStocks))
            "FRIDGE" -> fridgeAdapter.submit(filter(allStocks.filter { it.storageEnum == "FRIDGE" }))
            "FREEZER"-> freezeAdapter.submit(filter(allStocks.filter { it.storageEnum == "FREEZER" }))
            "PANTRY" -> outsideAdapter.submit(filter(allStocks.filter { it.storageEnum == "PANTRY" }))
        }
        currentAdapter().clearSelection()
        resetAllSelectUi()
        updateSelectBarText()
    }

    private fun currentAdapter(): IngredientAdapter = when (currentTab) {
        "FRIDGE" -> fridgeAdapter
        "FREEZER" -> freezeAdapter
        "PANTRY" -> outsideAdapter
        else -> totalAdapter
    }

    private fun updateSelectBarText() {
        val count = currentAdapter().getSelected().size
        tvAllCheck.text = if (count > 0) "선택 ($count)" else "전체 선택"
    }

    // ---------------------- Adapter 팩토리 ----------------------

    private fun makeAdapter(): IngredientAdapter = IngredientAdapter(
        onClick = { item ->
            // 단일 클릭: 선택 토글
            currentAdapter().toggle(item.id)
            updateSelectBarText()
        },
        onLongClick = { item ->
            // 롱클릭: 단일 항목만 선택 유지
            val a = currentAdapter()
            a.clearSelection()
            a.toggle(item.id)
            updateSelectBarText()
        },
        onTransferArrow = { item ->
            startActivity(Intent(this, MypageIngredientListActivity::class.java).apply {
                putExtra("pantryId", pantryId)
                putExtra("ingredientName", item.name)
                putExtra("storage", item.storageEnum)
                putExtra("hideSearch", true)   // ⬅⬅ 추가: 검색 숨김 플래그
            })
        }
    )

    // ---------------------- 매핑 & 유틸 ----------------------

    private fun PantryStockDto.toUi(): PantryStockUi {
        val displayQty = quantity.trim().let { s ->
            s.toBigDecimalOrNull()?.stripTrailingZeros()?.toPlainString() ?: s
        }
        return PantryStockUi(
            id = id,
            ingredientId = ingredientId,
            unitId = unitId,
            name = ingredientName,
            categoryEnum = category,
            categoryKo = mapCategoryKo(category),
            storageEnum = storage,
            storageKo = mapStorageKo(storage),
            quantity = displayQty,
            quantityRaw = quantity,
            unit = unitName,
            iconUrl = RetrofitInstance.toIconUrl(iconUrl),
            purchasedAt = purchasedAt,
            expiresAt = expiresAt
        )
    }

    private fun mapCategoryKo(e: String) = when (e) {
        "Vegetables" -> "채소류"
        "Meats" -> "육류"
        "Seafood" -> "해산물"
        "Grains" -> "곡류"
        "Fruits" -> "과일류"
        "Dairy" -> "유제품"
        "Seasonings" -> "양념류"
        "ProcessedFoods" -> "가공식품"
        "Noodles" -> "면류"
        else -> "기타"
    }

    private fun mapStorageKo(s: String) = when (s) {
        "FRIDGE" -> "냉장"
        "FREEZER" -> "냉동"
        "PANTRY" -> "실온"
        else -> s
    }

    private fun buildIconUrl(icon: String?): String? {
        if (icon.isNullOrBlank()) return null
        if (icon.startsWith("http://", true) || icon.startsWith("https://", true)) return icon
        if (icon.startsWith("/uploads/", true)) return RetrofitInstance.toAbsoluteUrl(icon)
        val file = if (icon.contains('.')) icon else "$icon.png"
        return RetrofitInstance.toAbsoluteUrl("/icons/$file")
    }

    private fun getBearer(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }

    private fun formatFridgeTitle(name: String) =
        if (name.contains("냉장고")) name else "$name 냉장고"

    private fun setupCookButton() {
        val btnCook = findViewById<ConstraintLayout>(R.id.cookDongDong)
        btnCook.setOnClickListener {
            val selectedItems = currentAdapter().getSelected()
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "요리에 사용할 재료를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ingredientId 추출
            val selectedIds = selectedItems.mapNotNull { it.ingredientId }

            Log.d("PantryDetail", "선택된 재료 수: ${selectedIds.size}")
            selectedItems.forEachIndexed { index, item ->
                Log.d("PantryDetail", "[$index] ingredientId=${item.ingredientId}, name=${item.name}")
            }

            // Intent로 넘기기
            val intent = Intent(this, FridgeRecipeActivity::class.java)
            intent.putExtra("selectedIngredientIds", selectedIds.toLongArray()) // ✅ LongArray로 전달
            startActivity(intent)
        }
    }

    // 표시용: 동일 재료+단위+보관 기준으로 합치기
    private fun mergeByIngredient(list: List<PantryStockUi>): List<PantryStockUi> {
        if (list.isEmpty()) return list
        // ingredientId가 null일 수도 있어 name으로 보조키 수행
        return list
            .groupBy { Triple(listKeyOf(it), it.unitId ?: 0L, it.storageEnum) }
            .map { (key, items) ->
                val qtySum = items.fold(BigDecimal.ZERO) { acc, it ->
                    acc + (it.quantityRaw.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                }
                val first = items.first()
                // 합쳐진 항목은 실제 단일 stock이 아니므로 가짜(-) id 사용 (선택/강조엔 쓰되 수정/삭제는 금지)
                val syntheticId = -abs("${key.first}_${key.second}_${key.third}".hashCode().toLong())
                first.copy(
                    id = syntheticId,
                    quantityRaw = qtySum.toPlainString(),
                    quantity = qtySum.stripTrailingZeros().toPlainString(),
                    // 합치면 날짜 의미가 사라지니 화면 혼동 방지로 null 처리
                    purchasedAt = null,
                    expiresAt = null
                )
            }
            .sortedWith(compareBy({ it.name.lowercase() }, { it.storageEnum }))
    }

    private fun listKeyOf(it: PantryStockUi): Long {
        // ingredientId가 있으면 그걸, 없으면 name 해시로 보조키
        return it.ingredientId ?: -abs(it.name.hashCode().toLong())
    }

    private fun nameKey(n: String) = -abs(n.hashCode().toLong())

    private fun sameMergedGroup(a: PantryStockUi, b: PantryStockUi): Boolean {
        val aKey = a.ingredientId ?: nameKey(a.name)
        val bKey = b.ingredientId ?: nameKey(b.name)
        val aUnit = a.unitId ?: 0L
        val bUnit = b.unitId ?: 0L
        return aKey == bKey && aUnit == bUnit && a.storageEnum == b.storageEnum
    }

    /** 합쳐진(가짜 id<0) 아이템을 실제 항목들로 확장 */
    private fun expandMerged(item: PantryStockUi): List<PantryStockUi> {
        if (item.id > 0) return listOf(item) // 원래 단일 항목
        // 같은 재료키 + 같은 단위 + 같은 보관
        return allStocks.filter { sameMergedGroup(it, item) }
    }
}

// 화면 바인딩용 모델 (수정 모드 전달 필드 포함)
data class PantryStockUi(
    val id: Long,
    val ingredientId: Long?,
    val unitId: Long?,
    val name: String,
    val categoryEnum: String,
    val categoryKo: String,
    val storageEnum: String,
    val storageKo: String,
    val quantity: String,    // 표시용
    val quantityRaw: String, // 원본
    val unit: String,
    val iconUrl: String?,
    val purchasedAt: String?,
    val expiresAt: String?
)