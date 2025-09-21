package com.example.test.ui.fridge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.test.App
import com.example.test.R
import com.example.test.adapter.RefrigeratorAdapter
import com.example.test.model.refrigerator.Refrigerator
import com.example.test.repository.RefrigeratorRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefrigeratorListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAdd: ImageButton

    private val adapter = RefrigeratorAdapter { fridge: Refrigerator ->
        val intent = Intent(this, RefrigeratorEditActivity::class.java)
        intent.putExtra("mode", "edit")
        intent.putExtra("id", fridge.id)
        intent.putExtra("name", fridge.name)
        intent.putExtra("memo", fridge.memo)
        intent.putExtra("imageUrl", fridge.imageUrl)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refrigerator_list)

        recycler = findViewById(R.id.recycler)
        emptyState = findViewById(R.id.emptyState)
        btnAdd = findViewById(R.id.btnAdd)

        recycler.adapter = adapter

        findViewById<View>(R.id.btnCreateFromEmpty).setOnClickListener {
            startActivity(Intent(this, RefrigeratorEditActivity::class.java).putExtra("mode", "create"))
        }
        btnAdd.setOnClickListener {
            startActivity(Intent(this, RefrigeratorEditActivity::class.java).putExtra("mode", "create"))
        }
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    private fun loadList() {
        val token = App.prefs.token ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RefrigeratorRepository.list(token).enqueue(object : Callback<List<Refrigerator>> {
            override fun onResponse(
                call: Call<List<Refrigerator>>,
                response: Response<List<Refrigerator>>
            ) {
                val list = response.body().orEmpty()
                if (list.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    adapter.submit(list)
                }
            }

            override fun onFailure(call: Call<List<Refrigerator>>, t: Throwable) {
                Toast.makeText(this@RefrigeratorListActivity, "불러오기 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
