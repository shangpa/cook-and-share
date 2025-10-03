package com.example.test

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.recipt.ReceiptOcrConfirmRequest
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

// UI 전용 아이템(각 재료별 개별 설정용)
data class ItemScheduleUi(
    val nameRaw: String,
    val ingredientId: Long?,
    val unitId: Long?,
    val quantity: String,
    var storage: String = "FRIDGE",          // FRIDGE / FREEZER / PANTRY
    var dateKind: String = "구매일자",        // "구매일자" or "유통기한"
    var date: String? = null                 // YYYY-MM-DD
)

private fun String?.orFridge(): String = this ?: "FRIDGE"

class ReceiptScheduleActivity : AppCompatActivity() {

    private var pantryId: Long = -1L
    private lateinit var req: ReceiptOcrConfirmRequest

    private lateinit var rv: RecyclerView
    private lateinit var adapter: ReceiptScheduleAdapter
    private lateinit var btnSave: Button

    // 화면 내 임시 상태(각 재료별 개별 설정)
    private val uiItems = mutableListOf<ItemScheduleUi>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_schedule)

        pantryId = intent.getLongExtra("pantryId", -1L)
        req = intent.getParcelableExtra("items") ?: ReceiptOcrConfirmRequest(emptyList())

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        btnSave = findViewById(R.id.btnSave)

        // req -> UI 모델로 변환
        uiItems.clear()
        req.items.forEach { it ->
            uiItems += ItemScheduleUi(
                nameRaw = it.nameRaw,
                ingredientId = it.ingredientId,
                unitId = it.unitId,
                quantity = it.quantity,
                storage = it.storage ?: "FRIDGE",
                dateKind = if (it.expiresAt != null) "유통기한" else "구매일자",
                date = it.purchasedAt ?: it.expiresAt
            )
        }

        rv = findViewById(R.id.rvSchedule)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ReceiptScheduleAdapter(
            items = uiItems,
            onPickStorage = { pos ->
                showStoragePopup(pos)
            },
            onPickDateKind = { pos ->
                showDateKindPopup(pos)
            },
            onPickDate = { pos ->
                showDatePicker(pos)
            }
        )
        rv.adapter = adapter

        btnSave.setOnClickListener { save() }
    }

    /** 행별: 보관 장소 선택 팝업 */
    private fun showStoragePopup(pos: Int) {
        val anchor = rv.findViewHolderForAdapterPosition(pos)?.itemView?.findViewById<TextView>(R.id.tvStorage)
            ?: return
        val popup = PopupMenu(this, anchor)
        val opts = listOf("냉장" to "FRIDGE", "냉동" to "FREEZER", "실온" to "PANTRY")
        opts.forEach { popup.menu.add(it.first) }
        popup.setOnMenuItemClickListener { mi ->
            val code = opts.first { it.first == mi.title }.second
            uiItems[pos].storage = code
            adapter.notifyItemChanged(pos)
            true
        }
        popup.show()
    }

    /** 행별: 날짜 종류 선택 팝업 */
    private fun showDateKindPopup(pos: Int) {
        val anchor = rv.findViewHolderForAdapterPosition(pos)?.itemView?.findViewById<TextView>(R.id.tvDateKind)
            ?: return
        val popup = PopupMenu(this, anchor)
        val kinds = listOf("구매일자", "유통기한")
        kinds.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { mi ->
            uiItems[pos].dateKind = mi.title.toString()
            // 선택 바뀌면 기존 date 그대로 두되, 저장 시 분기 처리
            adapter.notifyItemChanged(pos)
            true
        }
        popup.show()
    }

    /** 행별: 날짜 선택 다이얼로그 */
    private fun showDatePicker(pos: Int) {
        val cal = Calendar.getInstance()
        val dlg = DatePickerDialog(
            this,
            { _, y, m, d ->
                val mm = "${m + 1}".padStart(2, '0')
                val dd = "$d".padStart(2, '0')
                uiItems[pos].date = "$y-$mm-$dd"
                adapter.notifyItemChanged(pos)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dlg.show()
    }

    private fun save() {
        val token = getBearerToken() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 1) UI → DTO 변환 + 키 정규화
        val rawItems = uiItems.mapNotNull { ui ->
            val qtyInt = ui.quantity.toDoubleOrNull()?.toInt() ?: 0
            if (ui.ingredientId == null || qtyInt <= 0) null
            else {
                val normalizedStorage = (ui.storage ?: "FRIDGE").uppercase()
                val normalizedDate =
                    if (ui.date.isNullOrBlank()) null else ui.date!!.trim() // yyyy-MM-dd 형식 유지
                ReceiptOcrConfirmRequest.Item(
                    nameRaw = ui.nameRaw,
                    ingredientId = ui.ingredientId,
                    quantity = qtyInt.toString(),           // 정수화
                    unitId = ui.unitId,                     // null이면 서버에서 기본단위 적용
                    storage = normalizedStorage,
                    purchasedAt = if (ui.dateKind == "구매일자") normalizedDate else null,
                    expiresAt   = if (ui.dateKind == "유통기한") normalizedDate else null
                )
            }
        }

        // 2) 클라에서 1차 병합 (동일 키 합치기)
        data class Key(
            val ingredientId: Long,
            val unitId: Long?,
            val storage: String,
            val purchasedAt: String?,
            val expiresAt: String?
        )

        val merged = rawItems
            .groupBy {
                Key(
                    ingredientId = it.ingredientId!!,
                    unitId = it.unitId,
                    storage = (it.storage ?: "FRIDGE").uppercase(),
                    purchasedAt = it.purchasedAt,
                    expiresAt = it.expiresAt
                )
            }
            .map { (k, list) ->
                val sum = list.fold(BigDecimal.ZERO) { acc, it ->
                    acc + (it.quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                }
                ReceiptOcrConfirmRequest.Item(
                    nameRaw = list.first().nameRaw,
                    ingredientId = k.ingredientId,
                    quantity = sum.stripTrailingZeros().toPlainString(),
                    unitId = k.unitId,
                    storage = k.storage,
                    purchasedAt = k.purchasedAt,
                    expiresAt = k.expiresAt
                )
            }

        val payload = ReceiptOcrConfirmRequest(items = merged)

        // 3) 서버 전송
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi
                        .confirmReceiptOcr(token, pantryId, payload)   // ← 이 한 줄
                        .execute()
                        .also {
                            if (!it.isSuccessful) throw RuntimeException("OCR 확인 실패: ${it.code()}")
                        }
                }
                onDone()
            } catch (e: Exception) {
                Toast.makeText(this@ReceiptScheduleActivity, "저장 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun onDone() {
        Toast.makeText(this, "추가되었어요!", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, PantryDetailActivity::class.java).apply {
            putExtra("id", pantryId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun getBearerToken(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }
}
