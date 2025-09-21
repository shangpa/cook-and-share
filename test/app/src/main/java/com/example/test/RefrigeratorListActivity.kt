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
import com.example.test.R
import com.example.test.adapter.RefrigeratorAdapter
import com.example.test.model.refrigerator.Refrigerator

class RefrigeratorListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAdd: ImageButton

    private val items: MutableList<Refrigerator> = mutableListOf()

    private val adapter: RefrigeratorAdapter = RefrigeratorAdapter { fridge: Refrigerator ->
        val intent = Intent(this, RefrigeratorEditActivity::class.java).apply {
            putExtra("mode", "edit")
            putExtra("id", fridge.id)
            putExtra("name", fridge.name)
            putExtra("memo", fridge.memo)
            putExtra("imageUrl", fridge.imageUrl)
        }
        editLauncher.launch(intent)
    }

    private lateinit var editLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refrigerator_list)

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
                    val newId: Long = data.getLongExtra("result_id", System.currentTimeMillis())
                    val newName: String = data.getStringExtra("result_name") ?: ""
                    val newMemo: String? = data.getStringExtra("result_memo")
                    val newImageUrl: String? = data.getStringExtra("result_imageUrl")

                    val newItem: Refrigerator = Refrigerator(
                        id = newId,
                        name = newName,
                        memo = newMemo,
                        imageUrl = newImageUrl
                    )
                    items.add(0, newItem)
                    adapter.submit(items.toList())
                    updateEmptyState()
                    Toast.makeText(this, "목록에 추가됨", Toast.LENGTH_SHORT).show()
                }

                "edit" -> {
                    val editId: Long = data.getLongExtra("result_id", -1L)
                    val idx: Int = items.indexOfFirst { it.id == editId }
                    if (idx >= 0) {
                        val old: Refrigerator = items[idx]

                        val newName: String = data.getStringExtra("result_name") ?: old.name
                        val newMemo: String? =
                            if (data.hasExtra("result_memo")) data.getStringExtra("result_memo") else old.memo
                        val newImageUrl: String? =
                            if (data.hasExtra("result_imageUrl")) data.getStringExtra("result_imageUrl") else old.imageUrl

                        val updated: Refrigerator = old.copy(
                            name = newName,
                            memo = newMemo,
                            imageUrl = newImageUrl
                        )
                        items[idx] = updated
                        adapter.submit(items.toList())
                        Toast.makeText(this, "수정 반영됨", Toast.LENGTH_SHORT).show()
                    }
                }

                "delete" -> {
                    val delId: Long = data.getLongExtra("result_id", -1L)
                    val removed: Boolean = items.removeAll { it.id == delId }
                    if (removed) {
                        adapter.submit(items.toList())
                        updateEmptyState()
                        Toast.makeText(this, "삭제 반영됨", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<View>(R.id.btnCreateFromEmpty).setOnClickListener {
            editLauncher.launch(
                Intent(this, RefrigeratorEditActivity::class.java).putExtra("mode", "create")
            )
        }
        btnAdd.setOnClickListener {
            editLauncher.launch(
                Intent(this, RefrigeratorEditActivity::class.java).putExtra("mode", "create")
            )
        }

        updateEmptyState()
    }

    override fun onResume() {
        super.onResume()
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
