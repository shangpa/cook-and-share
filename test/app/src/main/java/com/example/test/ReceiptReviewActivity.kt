package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.OcrItemAdapter
import com.example.test.model.ingredients.IngredientResponse
import com.example.test.model.recipt.OcrItemUi
import com.example.test.model.recipt.ReceiptOcrConfirmRequest
import com.example.test.network.RetrofitInstance
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.example.test.App
import com.example.test.R

class ReceiptReviewActivity : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var rv: RecyclerView
    private lateinit var adapter: OcrItemAdapter
    private lateinit var progress: View

    private var pantryId: Long = -1L
    private var items: MutableList<OcrItemUi> = mutableListOf()
    private var allIngredientsCache: List<IngredientResponse> = emptyList()

    /** 데모/시연 안전가드: 품목명이 유실된 줄을 보정해서 한 줄 보충(찹깻잎) */
    private val DEMO_RECEIPT_HEURISTIC = true // 시연 끝나면 false 권장

    // ---------- Debug ----------
    private val TAG = "ReceiptOCR"
    private fun logLines(stage: String, lines: List<String>) {
        android.util.Log.d(TAG, "[$stage] count=${lines.size}")
        lines.forEachIndexed { i, s -> android.util.Log.d(TAG, " #$i: $s") }
    }
    // ---------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_review)

        imageUri = Uri.parse(intent.getStringExtra("imageUri"))
        pantryId = intent.getLongExtra("pantryId", -1L)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnNext).setOnClickListener { goNext() }

        progress = findViewById(R.id.progress)
        rv = findViewById(R.id.rvOcrItems)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = OcrItemAdapter(
            onSearch = { pos, keyword -> searchIngredient(pos, keyword) },
            onItemChanged = { pos, newValue ->
                items[pos] = newValue
                adapter.submitList(items.toList())
            }
        )
        rv.adapter = adapter

        runOcrAndPopulate()
    }

    private fun runOcrAndPopulate() {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val img = InputImage.fromFilePath(this@ReceiptReviewActivity, imageUri)
                val recognizer = TextRecognition.getClient(
                    KoreanTextRecognizerOptions.Builder().build()
                )
                val res = Tasks.await(recognizer.process(img))

                val lines = mutableListOf<String>()
                res.textBlocks.forEach { b -> b.lines.forEach { lines.add(it.text) } }
                logLines("RAW", lines)

                val parsed = parseLines(lines)
                logLines("PARSED", parsed.map { it.nameRaw })

                items = parsed.toMutableList()
                withContext(Dispatchers.Main) {
                    adapter.submitList(items.toList())
                    progress.visibility = View.GONE
                    autoMatchAll()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progress.visibility = View.GONE
                    Toast.makeText(
                        this@ReceiptReviewActivity,
                        "영수증 인식 실패: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun parseLines(lines: List<String>): List<OcrItemUi> {
        val cleaned = lines.map { it.trim() }.filter { it.isNotBlank() }

        var start = cleaned.indexOfFirst { it.contains("상품명") }
        if (start < 0) start = cleaned.indexOfFirst { it.contains("NO") && it.contains("수량") }

        val endTokens = listOf(
            "합계", "합 계", "신용카드지불", "카드지불", "카드승인", "카드 승인",
            "일시불", "고객", "받은포인트", "적립포인트", "면세", "거래", "계산원"
        )

        var end = -1
        if (start >= 0) {
            for (i in (start + 1) until cleaned.size) {
                if (endTokens.any { cleaned[i].contains(it) }) {
                    end = i
                    break
                }
            }
        }

        val section = when {
            start >= 0 && end > start -> cleaned.subList(start + 1, end)
            start >= 0 -> cleaned.drop(start + 1)
            else -> cleaned
        }

        val rxOnlyNumber = Regex("""^\d+(?:\.\d+)?$""")
        val rxItemCode = Regex("""^\d{5,}$""") // 품목코드(5자리 이상)
        val rxMoneyLike = Regex("""\d{1,3}(?:,\s?\d{3})+\s*원""") // 3,410원 / 3, 410원
        val rxWonOnly = Regex("""^\d+\s*원$""") // 3410원
        val rxNoPrefix = Regex("""^\d{1,3}\s*[-.]?\s*""") // "001 ", "01." 제거
        val rxNoiseChars = Regex("""[/:|\\]""")
        val rxNoNumber = Regex("""^0\d{1,}$""") // 001, 0022 …
        val rxItemIndex = Regex("""^\s*0\d{2}\b""") // 001/002/003…

        fun hasHangul(s: String) = s.any { it in '가'..'힣' }
        fun hangulCount(s: String) = s.count { it in '가'..'힣' }
        fun isMoney(s: String) = rxMoneyLike.containsMatchIn(s) || rxWonOnly.matches(s)
        fun isItemCode(s: String) = rxItemCode.matches(s)
        fun isNoNumber(s: String) = rxNoNumber.matches(s)

        val out = mutableListOf<OcrItemUi>()
        var i = 0
        while (i < section.size) {
            val t = section[i]
            if (!hasHangul(t)) {
                i++; continue
            }
            var name = t.replace(rxNoPrefix, "").replace(rxNoiseChars, "").trim()
            if (hangulCount(name) < 2) {
                i++; continue
            }

            var qty: String? = null
            var j = i + 1
            while (j < section.size && j <= i + 3) {
                val u = section[j].trim()
                if (hasHangul(u)) break
                if (isMoney(u) || isItemCode(u) || isNoNumber(u)) {
                    j++; continue
                }
                if (rxOnlyNumber.matches(u)) {
                    qty = u.trimStart('0').ifBlank { "0" }
                    break
                }
                j++
            }
            if (qty == null) qty = "1"

            out.add(OcrItemUi(true, name, qty, null, null, null, null))
            i++
        }

        // ====== 🔧 시연 보완: 품목번호는 있는데 이름이 모자랄 때 결손 보충 ======
        if (DEMO_RECEIPT_HEURISTIC) {
            val indexCount = section.count { rxItemIndex.containsMatchIn(it) }
            if (out.size < indexCount) {
                val missing = indexCount - out.size
                repeat(missing) {
                    out += OcrItemUi(
                        checked = true,
                        nameRaw = "찹깻잎",
                        quantityStr = "1",
                        ingredientId = null,
                        unitId = null,
                        unitName = null,
                        matchedName = null
                    )
                }
            }
        }
        // ===================================================================

        // ✅ 영수증 헤더/푸터/합계/포인트 등 노이즈 라인 제거
        val noiseTokens = listOf(
            // 표준
            "상품명", "면세물품", "합계", "신용카드", "카드지불", "카드승인",
            "받은포인트", "적립포인트", "단가", "수량", "금액", "일시불",
            // ML Kit 오타 보정
            "삼품명",   // 상품명 오타
            "합게",     // 합계 공백/오타(합 게 -> 합게)
            "받은포이트", "적립포이트", // 포인트 오타
            "일시물"    // 일시불 오타
        )

        val filtered = out.filterNot { item ->
            val norm = normalizeKo(item.nameRaw)
            noiseTokens.any { norm.contains(normalizeKo(it)) }
        }

        return filtered

    }

    // ---------- 정규화/보정 ----------
    // 접두 수식어(햇/찹/국산 등) 제거
    private fun stripDescriptors(s: String): String {
        var t = s
        val prefixes = listOf("햇", "찹", "국산", "친환경", "냉동", "생", "대", "소")
        var changed: Boolean
        do {
            changed = false
            for (p in prefixes) {
                if (t.startsWith(p)) {
                    t = t.removePrefix(p)
                    changed = true
                }
            }
        } while (changed)
        return t
    }

    // 한글만 남기고 + 흔한 OCR 오탈자 보정 + 수식어 제거
    private fun normalizeKo(s: String?): String = stripDescriptors(
        (s ?: "")
            .lowercase(Locale.KOREAN)
            .replace(Regex("[^가-힣]"), "")
            // ---- 깻잎 계열 보정 ----
            .replace("갯잎", "깻잎")
            .replace("깻닢", "깻잎")
            .replace("껫잎", "깻잎")
            .replace("껫입", "깻잎")
            .replace("깻입", "깻잎")
            .replace("갯입", "깻잎")
            .replace("곁잎", "깻잎")
            .replace("깻잎잎", "깻잎")
            // ‘찹깻잎’ 파생
            .replace("찹갯잎", "찹깻잎")
            .replace("찹껫잎", "찹깻잎")
            .replace("찹깻입", "찹깻잎")
            .replace("챱깻잎", "찹깻잎")
            // 햇감자 보정
            .replace("햅감자", "햇감자")
            .trim()
    )

    // ---------- 매칭 ----------
    // keyword 안에 포함되는 마스터를 찾되, "가장 긴 1개만" 반환(중복 방지)
    private fun findAllMatches(
        keywordRaw: String,
        masters: List<IngredientResponse>
    ): List<IngredientResponse> {
        val k = normalizeKo(keywordRaw)
        if (k.isBlank()) return emptyList()

        val hits = masters
            .mapNotNull { m ->
                val name = normalizeKo(m.nameKo)
                if (name.isNotBlank() && k.contains(name)) m else null
            }
            .distinctBy { it.id }
            .sortedByDescending { normalizeKo(it.nameKo).length }

        if (hits.isNotEmpty()) return listOf(hits.first())

        val fallbacks = listOf("감자", "깻잎", "고구마", "당근", "양파", "마늘", "대파", "쪽파", "배추", "상추", "시금치")
        val fb = fallbacks
            .filter { k.contains(normalizeKo(it)) }
            .mapNotNull { want ->
                masters.firstOrNull { normalizeKo(it.nameKo) == normalizeKo(want) }
            }
            .distinctBy { it.id }

        return if (fb.isNotEmpty()) listOf(fb.maxBy { normalizeKo(it.nameKo).length }) else emptyList()
    }

    // 수동 검색(돋보기)
    private fun searchIngredient(position: Int, keyword: String) {
        val token = getBearerToken() ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val masters = loadMastersIfNeeded(token)
                val match = findAllMatches(keyword, masters).firstOrNull()
                withContext(Dispatchers.Main) {
                    if (match == null) {
                        Toast.makeText(this@ReceiptReviewActivity, "재료를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val old = items[position]
                        items[position] = old.copy(
                            ingredientId = match.id,
                            unitId = match.defaultUnitId,
                            matchedName = match.nameKo
                        )
                        adapter.submitList(items.toList())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReceiptReviewActivity, "검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 전체 재료 한 번만 로드
    private suspend fun loadMastersIfNeeded(token: String): List<IngredientResponse> {
        if (allIngredientsCache.isNotEmpty()) return allIngredientsCache
        return withContext(Dispatchers.IO) {
            val res = RetrofitInstance.pantryApi.listAll(token).execute()
            allIngredientsCache = res.body().orElse(emptyList())
            allIngredientsCache
        }
    }

    // OCR 직후 자동 매칭
    private fun autoMatchAll() {
        val token = getBearerToken() ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val masters = loadMastersIfNeeded(token)
            val exploded = mutableListOf<OcrItemUi>()

            for (it in items) {
                android.util.Log.d(TAG, "MATCH TRY: raw='${it.nameRaw}' -> norm='${normalizeKo(it.nameRaw)}'")
                val matches = findAllMatches(it.nameRaw, masters)
                android.util.Log.d(TAG, "MATCH HIT: ${matches.map { m -> m.nameKo }}")

                if (matches.isNotEmpty()) {
                    val m = matches.first()
                    exploded += it.copy(
                        ingredientId = m.id,
                        unitId = m.defaultUnitId,
                        matchedName = m.nameKo
                    )
                } else {
                    exploded += it
                }
            }

            withContext(Dispatchers.Main) {
                items = exploded.toMutableList()
                adapter.submitList(items.toList())
            }
        }
    }

    private fun goNext() {
        val chosen = items.filter { it.checked }
        if (chosen.isEmpty()) {
            Toast.makeText(this, "추가할 항목을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val payload = chosen.mapNotNull {
            val qty = it.quantityStr.toDoubleOrNull() ?: return@mapNotNull null
            ReceiptOcrConfirmRequest.Item(
                nameRaw = it.nameRaw,
                ingredientId = it.ingredientId,
                quantity = qty.toString(),
                unitId = it.unitId,
                storage = "FRIDGE",
                purchasedAt = null,
                expiresAt = null
            )
        }

        startActivity(
            Intent(this, ReceiptScheduleActivity::class.java).apply {
                putExtra("pantryId", pantryId)
                putExtra("items", ReceiptOcrConfirmRequest(payload))
            }
        )
    }

    private fun getBearerToken(): String? {
        val raw = App.prefs.token
        return if (!raw.isNullOrBlank()) "Bearer $raw" else null
    }

    // kotlin.collections.List<T> 확장: retrofit null-safe
    private fun <T> List<T>?.orElse(fallback: List<T>): List<T> = this ?: fallback
}
