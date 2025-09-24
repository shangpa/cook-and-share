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
import com.example.test.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response

class PantryListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAdd: ImageButton

    private val items: MutableList<PantryResponse> = mutableListOf()

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

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_list)

        recycler = findViewById(R.id.recycler)
        emptyState = findViewById(R.id.emptyState)
        btnAdd = findViewById(R.id.btnAdd)
        recycler.adapter = adapter

        // ActivityResultLauncher 초기화
        editLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val data: Intent = result.data ?: return@registerForActivityResult

            when (val mode: String? = data.getStringExtra("result_mode")) {
                "create" -> {
                    Toast.makeText(this, "목록에 추가됨", Toast.LENGTH_SHORT).show()
                    loadPantries() // 🔹 서버 다시 조회
                }

                "edit" -> {
                    Toast.makeText(this, "수정 반영됨", Toast.LENGTH_SHORT).show()
                    //todo 수정해야함
                    loadPantries() // 🔹 서버 다시 조회
                }

                "delete" -> {
                    Toast.makeText(this, "삭제 반영됨", Toast.LENGTH_SHORT).show()
                    loadPantries() // 🔹 서버 다시 조회
                }
            }
        }

        findViewById<View>(R.id.btnCreateFromEmpty).setOnClickListener {
            editLauncher.launch(
                Intent(this, PantryEditActivity::class.java).putExtra("mode", "create")
            )
        }
        btnAdd.setOnClickListener {
            editLauncher.launch(
                Intent(this, PantryEditActivity::class.java).putExtra("mode", "create")
            )
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
                // 네트워크는 IO에서
                val list = withContext(Dispatchers.IO) {
                    RetrofitInstance.pantryApi.listPantries(token)
                }
                // UI 업데이트는 메인에서
                items.clear()
                items.addAll(list)
                adapter.submit(items.toList())
                updateEmptyState()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PantryListActivity,
                    "조회 실패: ${e.localizedMessage ?: "알 수 없는 오류"}",
                    Toast.LENGTH_SHORT
                ).show()
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
}
