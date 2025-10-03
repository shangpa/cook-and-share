package com.example.test.ui.fridge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.test.App
import com.example.test.PantryDetailActivity
import com.example.test.R
import com.example.test.adapter.RefrigeratorAdapter
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryStockDto
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Semaphore
import kotlinx.coroutines.awaitAll

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private fun String.toLocalDateOrNull() =
    runCatching { LocalDate.parse(this, ISO_DATE) }.getOrNull()

class PantryListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAdd: ImageButton

    private val items: MutableList<PantryResponse> = mutableListOf()

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    private val adapter: RefrigeratorAdapter = RefrigeratorAdapter(
        onEdit = { fridge: PantryResponse ->
            val intent = Intent(this, PantryEditActivity::class.java).apply {
                putExtra("mode", "edit")
                putExtra("id", fridge.id)
                putExtra("name", fridge.name)
                putExtra("memo", fridge.note)
                putExtra("imageUrl", fridge.imageUrl)
            }
            editLauncher.launch(intent)
        },
        onClick = { fridge: PantryResponse ->
            val intent = Intent(this, PantryDetailActivity::class.java).apply {
                putExtra("pantryId", fridge.id)
                putExtra("name", fridge.name)
            }
            startActivity(intent)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_list)

        recycler = findViewById(R.id.recycler)
        emptyState = findViewById(R.id.emptyState)
        btnAdd = findViewById(R.id.btnAdd)
        recycler.adapter = adapter

        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            when (result.data?.getStringExtra("result_mode")) {
                "create", "edit", "delete" -> {
                    Toast.makeText(this, "목록 갱신", Toast.LENGTH_SHORT).show()
                    loadPantries()
                }
            }
        }

        findViewById<View>(R.id.btnCreateFromEmpty).setOnClickListener {
            editLauncher.launch(Intent(this, PantryEditActivity::class.java).putExtra("mode", "create"))
        }
        btnAdd.setOnClickListener {
            editLauncher.launch(Intent(this, PantryEditActivity::class.java).putExtra("mode", "create"))
        }

        updateEmptyState()
    }

    override fun onResume() {
        super.onResume()
        loadPantries()
    }

    private fun loadPantries() {
        val raw = App.prefs.token
        val token = if (!raw.isNullOrBlank()) "Bearer $raw" else null
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // 1) 팬트리 목록
                val list = withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.listPantries(token)
                }
                items.clear()
                items.addAll(list)
                adapter.submit(items.toList())
                updateEmptyState()

                // 2) 각 팬트리 재고 조회 → 14일 이내만 필터 → 맵 구성 → 어댑터에 반영
                val expiringMap = withContext(Dispatchers.IO) {
                    buildExpiringMapByStocks(token, list)
                }
                adapter.updateExpiringMap(expiringMap)

            } catch (e: Exception) {
                Toast.makeText(this@PantryListActivity, "조회 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateEmptyState() {
        if (items.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recycler.visibility = View.VISIBLE
        }
    }

    // 14일 이내 필터
    private fun List<PantryStockDto>.filterExpiringWithin14Days(): List<PantryStockDto> {
        val today = LocalDate.now()
        val limit = today.plusDays(14)
        return this.filter { s ->
            val d = s.expiresAt?.toLocalDateOrNull() ?: return@filter false
            !d.isBefore(today) && !d.isAfter(limit)
        }
    }

    // 팬트리별 재고 리스트를 가져와 14일 이내만 남긴 맵 구성
    private suspend fun buildExpiringMapByStocks(
        token: String,
        pantries: List<PantryResponse>
    ): Map<Long, List<PantryStockDto>> = coroutineScope {
        val semaphore = Semaphore(4) // 동시 요청 4개로 제한(원하면 숫자 조절)
        val jobs = pantries.map { p ->
            async(Dispatchers.IO) {
                runCatching {
                    semaphore.acquire()
                    val stocks = RetrofitInstance.pantryApi.listPantryStocks(token, p.id)
                    p.id to stocks.filterExpiringWithin14Days()
                }.getOrElse {
                    p.id to emptyList()
                }.also {
                    semaphore.release()
                }
            }
        }
        jobs.awaitAll().toMap()
    }
}
