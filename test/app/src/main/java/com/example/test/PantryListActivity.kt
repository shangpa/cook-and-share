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
import androidx.recyclerview.widget.RecyclerView
import com.example.test.App
import com.example.test.PantryDetailActivity
import com.example.test.R
import com.example.test.adapter.RefrigeratorAdapter
import com.example.test.model.pantry.PantryResponse
import com.example.test.network.RetrofitInstance
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
            // ✅ 클릭 시 디테일 화면으로 이동 + 이름/메모도 함께 전달
            val intent = Intent(this, PantryDetailActivity::class.java).apply {
                putExtra("id", fridge.id)
                putExtra("name", fridge.name)   // <- 여기서 넘겨야 함
                putExtra("memo", fridge.note)   // (선택) 필요하면 같이
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

        val token = App.prefs.token.toString()
        RetrofitInstance.pantryApi.listPantries("Bearer $token")
            .enqueue(object : retrofit2.Callback<List<PantryResponse>> {
                override fun onResponse(
                    call: Call<List<PantryResponse>>,
                    response: Response<List<PantryResponse>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body() ?: emptyList()
                        items.clear()
                        items.addAll(list)
                        adapter.submit(items.toList())
                        updateEmptyState()
                    } else {
                        Toast.makeText(this@PantryListActivity,
                            "조회 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<PantryResponse>>, t: Throwable) {
                    Toast.makeText(this@PantryListActivity,
                        "네트워크 오류: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
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
