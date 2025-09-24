package com.example.test

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.test.Utils.TabBarUtils
import com.example.test.model.pantry.PantryStockCreateRequest
import com.example.test.model.pantry.PantryStockUpdateRequest
import com.example.test.model.pantry.UnitResponse
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class PantryMaterialAddDetailActivity : AppCompatActivity() {

    // 입력/표시 뷰들
    private lateinit var number: EditText
    private lateinit var dateEnter: EditText
    private lateinit var total: TextView               // 보관장소 라벨
    private lateinit var purchaseDate: TextView        // "구매일자"/"유통기한" 라벨
    private lateinit var register: AppCompatButton
    private lateinit var totalDropBox: View            // 보관장소 드롭 버튼
    private lateinit var purchaseDateDropBox: View     // 날짜종류 드롭 버튼
    private lateinit var unitText: TextView            // 단위 표시 TextView (선택불가)
    private lateinit var ingredientNameTv: TextView    // 상단 재료명
    private lateinit var materialCategoryTv: TextView  // 상단 카테고리(=greengrocery)
    private lateinit var materialImage: ImageView      // 상단 아이콘

    // 상태값
    private var pantryId: Long = -1L
    private var ingredientId: Long = -1L
    private var defaultUnitId: Long = -1L
    private var selectedUnitId: Long? = null
    private var selectedStorage: String = "FRIDGE"     // FRIDGE/FREEZER/PANTRY
    private var selectedDateKind: String = "구매일자"  // or "유통기한"
    private var units: List<UnitResponse> = emptyList()

    private var mode: String = "create"
    private var stockId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_material_add_detail)

        TabBarUtils.setupTabBar(this)
        findViewById<ImageView>(R.id.backArrow).setOnClickListener { finish() }

        // 바인딩
        total = findViewById(R.id.total)
        purchaseDate = findViewById(R.id.purchaseDate)
        number = findViewById(R.id.number)
        dateEnter = findViewById(R.id.dateEnter)
        register = findViewById(R.id.register)
        totalDropBox = findViewById(R.id.totalDropBox)
        purchaseDateDropBox = findViewById(R.id.purchaseDateDropBox)
        unitText = findViewById(R.id.tvUnit)                    // XML에 tvUnit 있어야 함
        ingredientNameTv = findViewById(R.id.tvIngredientName)
        materialCategoryTv = findViewById(R.id.materialStorage) // 기존 id 유지 사용
        materialImage = findViewById(R.id.materialImage)

        // 인텐트 데이터
        this.pantryId = intent.getLongExtra("pantryId", -1L)
        this.ingredientId = intent.getLongExtra("ingredientId", -1L)
        this.defaultUnitId = intent.getLongExtra("defaultUnitId", -1L)
        this.mode = intent.getStringExtra("mode") ?: "create"
        this.stockId = intent.getLongExtra("stockId", -1L)

        val ingredientName = intent.getStringExtra("ingredientName") ?: "재료"
        val categoryEnum = intent.getStringExtra("category") ?: ""
        val rawIcon = intent.getStringExtra("iconUrl")
        val intentUnitName = intent.getStringExtra("unitName") // 있으면 우선 사용

        Log.d("Fridge", "Detail pantryId=$pantryId, ingredientId=$ingredientId, mode=$mode")

        // 상단 UI 표시
        ingredientNameTv.text = ingredientName
        materialCategoryTv.text = mapCategoryKo(categoryEnum)

        // 아이콘 로드(절대경로 보정)
        Glide.with(this)
            .load(buildIconUrl(rawIcon))
            .placeholder(R.drawable.image_tomato)
            .error(R.drawable.image_tomato)
            .into(materialImage)

        // 초기 보관장소(표시/내부)
        total.text = "냉장"
        selectedStorage = "FRIDGE"

        // 단위 텍스트 표시: 인텐트의 unitName 우선, 없으면 /api/units에서 이름 조회 → 실패 시 "개"
        if (!intentUnitName.isNullOrBlank()) {
            unitText.text = intentUnitName
            selectedUnitId = if (defaultUnitId > 0) defaultUnitId else selectedUnitId
        } else {
            selectedUnitId = if (defaultUnitId > 0) defaultUnitId else null
            loadUnitNameOnly()
        }

        // 저장소 드롭다운
        val openStorageMenu: (View) -> Unit = { anchor ->
            val popup = PopupMenu(this, anchor)
            listOf("냉장", "냉동", "실외").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { mi ->
                total.text = mi.title
                total.setTextColor(Color.parseColor("#2B2B2B"))
                selectedStorage = when (mi.title) {
                    "냉장" -> "FRIDGE"
                    "냉동" -> "FREEZER"
                    "실외" -> "PANTRY"
                    else -> "FRIDGE"
                }
                updateButtonState()
                true
            }
            popup.show()
        }
        totalDropBox.setOnClickListener(openStorageMenu)
        total.setOnClickListener(openStorageMenu)

        // 날짜종류 드롭다운
        val openDateKindMenu: (View) -> Unit = { anchor ->
            val popup = PopupMenu(this, anchor)
            listOf("구매일자", "유통기한").forEach { popup.menu.add(it) }
            popup.setOnMenuItemClickListener { mi ->
                purchaseDate.text = mi.title
                purchaseDate.setTextColor(Color.parseColor("#2B2B2B"))
                selectedDateKind = mi.title.toString()
                // 날짜 종류를 바꿨다면 입력값은 유지(또는 필요 시 초기화)
                updateButtonState()
                true
            }
            popup.show()
        }
        purchaseDateDropBox.setOnClickListener(openDateKindMenu)
        purchaseDate.setOnClickListener(openDateKindMenu)

        // 날짜 입력: DatePickerDialog
        dateEnter.keyListener = null
        dateEnter.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(
                this,
                { _, y, m, d ->
                    val mm = (m + 1).toString().padStart(2, '0')
                    val dd = d.toString().padStart(2, '0')
                    dateEnter.setText("$y-$mm-$dd")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            dlg.setButton(DatePickerDialog.BUTTON_POSITIVE, "확인", dlg)
            dlg.setButton(DatePickerDialog.BUTTON_NEGATIVE, "취소", dlg)
            dlg.show()
        }

        // 등록 버튼 활성화 제어
        updateButtonState()
        number.addTextChangedListener { updateButtonState() }
        dateEnter.addTextChangedListener { updateButtonState() }


        // 버튼 리스너: 모드별로 분기
        if (mode == "edit") {
            register.text = "수정하기"
            register.setOnClickListener { updateStock(pantryId, stockId) }
            // (나머지 edit 초기화 로직: storage/quantity/date 채우기 등)
            if (this.pantryId > 0 && this.stockId > 0) {
                loadStockDetailForEdit(this.pantryId, this.stockId)
            }
        } else {
            register.text = "등록하기"
            register.setOnClickListener { saveToServer() } // ⬅ 생성만 담당
        }

        // (선택) 이미지 미리보기 URI (OCR 등 추후 사용)
        intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
    }

    private fun loadStockDetailForEdit(pantryId: Long, stockId: Long) {
        val token = getBearerToken() ?: return
        lifecycleScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.getPantryStock(token, pantryId, stockId)
                }

                // 저장소/수량/단위
                total.text = when (res.storage) {
                    "FRIDGE" -> "냉장"
                    "FREEZER" -> "냉동"
                    "PANTRY" -> "실외"
                    else -> "냉장"
                }
                selectedStorage = res.storage
                number.setText(res.quantity)
                unitText.text = res.unitName

                // 날짜 선택 UI: 값 유무에 따라 기본 탭/값 세팅
                if (!res.expiresAt.isNullOrBlank()) {
                    // 유통기한 모드
                    selectedDateKind = "유통기한"
                    purchaseDate.text = "유통기한"
                    dateEnter.setText(res.expiresAt)
                } else {
                    // 구매일자 모드
                    selectedDateKind = "구매일자"
                    purchaseDate.text = "구매일자"
                    dateEnter.setText(res.purchasedAt ?: "")
                }

                updateButtonState()
            } catch (e: Exception) {
                Toast.makeText(this@PantryMaterialAddDetailActivity, "상세 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStock(pantryId: Long, stockId: Long) {
        if (stockId <= 0) { toast("수정 대상 재고 ID가 없습니다."); return }

        val token = getBearerToken() ?: return
        val qty = number.text?.toString()?.trim().orEmpty()
        val dateStr = dateEnter.text?.toString()?.trim().orEmpty()

        val purchasedAt = if (selectedDateKind == "구매일자") dateStr else null
        val expiresAt   = if (selectedDateKind == "유통기한") dateStr else null

        val body = PantryStockUpdateRequest(
            quantity = qty.ifBlank { null },
            storage = selectedStorage,
            purchasedAt = purchasedAt,
            expiresAt = expiresAt
        )

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.updatePantryStock(
                        token = token,
                        pantryId = pantryId,
                        stockId = stockId,
                        body = body
                    )
                }
            }.onSuccess {
                if (it.isSuccessful) {
                    toast("수정 완료")
                    finish() // 목록으로 복귀
                } else {
                    toast("수정 실패(${it.code()})")
                }
            }.onFailure { e ->
                toast("수정 실패: ${e.message}")
            }
        }
    }

    /** /api/units 에서 목록을 받아 defaultUnitId의 이름만 표기 (선택 불가) */
    private fun loadUnitNameOnly() {
        val token = getBearerToken() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            try {
                units = withContext(Dispatchers.IO) { RetrofitInstance.pantryApi.getUnits(token) }
                val unitName = units.firstOrNull { it.id == defaultUnitId }?.name ?: ""
                unitText.text = if (unitName.isNotBlank()) unitName else "개"
                selectedUnitId = units.firstOrNull { it.id == defaultUnitId }?.id ?: selectedUnitId
            } catch (e: Exception) {
                unitText.text = "개"
                Log.e("Fridge API", "단위 이름 불러오기 실패", e)
                Toast.makeText(this@PantryMaterialAddDetailActivity, "단위 로딩 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 아이콘 절대경로 보정 */
    private fun buildIconUrl(icon: String?): String? {
        if (icon.isNullOrBlank()) return null
        if (icon.startsWith("http://", true) || icon.startsWith("https://", true)) return icon
        if (icon.startsWith("/uploads/", true)) return RetrofitInstance.toAbsoluteUrl(icon)
        val fileName = if (icon.contains('.')) icon else "$icon.png"
        return RetrofitInstance.toAbsoluteUrl("/uploads/icons/$fileName")
    }

    /** 서버 enum → 한글 라벨 */
    private fun mapCategoryKo(enumName: String): String = when (enumName) {
        "Vegetables"     -> "채소류"
        "Meats"          -> "육류"
        "Seafood"        -> "해산물"
        "Grains"         -> "곡류"
        "Fruits"         -> "과일류"
        "Dairy"          -> "유제품"
        "Seasonings"     -> "양념류"
        "ProcessedFoods" -> "가공식품"
        "Noodles"        -> "면류"
        "Kimchi"         -> "김치"
        "Beverages"      -> "음료"
        else             -> "기타"
    }

    /** 등록 버튼 활성/비활성 */
    private fun updateButtonState() {
        val hasQty = number.text.toString().isNotBlank()
        val hasDate = dateEnter.text.toString().isNotBlank()
        val enabled = if (mode == "edit") hasQty else (hasQty && hasDate)
        register.isEnabled = enabled
        if (enabled) {
            register.setBackgroundResource(R.drawable.btn_material_add)
            register.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            register.setBackgroundResource(R.drawable.btn_number_of_people)
            register.setTextColor(Color.parseColor("#A1A9AD"))
        }
    }

    /** 서버 저장 */
    private fun saveToServer() {
        val token = getBearerToken() ?: run {
            Log.e("Fridge API", "토큰이 없어 요청을 보낼 수 없습니다.")
            return
        }

        val qty = number.text?.toString()?.trim().orEmpty()
        val dateStr = dateEnter.text?.toString()?.trim().orEmpty()
        val unitId = selectedUnitId

        if (qty.isBlank()) { toast("수량을 입력하세요."); return }
        if (dateStr.isBlank()) { toast("날짜를 입력하세요."); return }
        if (unitId == null) { toast("단위가 선택되지 않았습니다."); return }
        if (pantryId <= 0 || ingredientId <= 0) { toast("잘못된 접근입니다."); return }

        val purchasedAt = if (selectedDateKind == "구매일자") dateStr else null
        val expiresAt   = if (selectedDateKind == "유통기한") dateStr else null

        val req = PantryStockCreateRequest(
            ingredientId = ingredientId,
            unitId = unitId,
            quantity = qty,
            storage = selectedStorage,
            purchasedAt = purchasedAt,
            expiresAt = expiresAt,
            memo = null,
            source = "MANUAL"
        )

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.addPantryStock(
                        token = token,
                        pantryId = pantryId,
                        req = req
                    )
                }
            }.onSuccess {
                toast("냉장고에 추가되었어요")
                startActivity(Intent(this@PantryMaterialAddDetailActivity, PantryDetailActivity::class.java).apply {
                    putExtra("id", pantryId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
                finish()
            }.onFailure { e ->
                Log.e("Fridge API", "addPantryStock 실패", e)
                toast("추가 중 오류")
            }
        }
    }

    /** 프로젝트에 맞는 토큰 포맷으로 반환 */
    private fun getBearerToken(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
