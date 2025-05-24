package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.SavedTradePostAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialMySavedActivity : AppCompatActivity() {

    private lateinit var material: LinearLayout
    private lateinit var distance: LinearLayout
    private lateinit var materialContainer: LinearLayout
    private lateinit var distanceContainer: LinearLayout
    private lateinit var materialText: TextView
    private lateinit var materialIcon: ImageView
    private lateinit var distanceText: TextView
    private lateinit var distanceIcon: ImageView
    private lateinit var sortArrow: ImageView
    private lateinit var w: TextView

    private var isMaterialVisible = false
    private var isDistanceVisible = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedTradePostAdapter
    private val savedPosts = mutableListOf<TradePostResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_saved)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavedTradePostAdapter(savedPosts)
        recyclerView.adapter = adapter

        loadSavedPosts()

        // 뒤로가기
        findViewById<ImageView>(R.id.savedTransactioneBack).setOnClickListener {
            finish()
        }

        // 필터 관련 뷰 초기화
        material = findViewById(R.id.material)
        distance = findViewById(R.id.distance)
        materialContainer = findViewById(R.id.materialContainer)
        distanceContainer = findViewById(R.id.distanceContainer)
        materialText = findViewById(R.id.materialText)
        materialIcon = findViewById(R.id.materialIcon)
        distanceText = findViewById(R.id.distanceText)
        distanceIcon = findViewById(R.id.distanceIcon)
        sortArrow = findViewById(R.id.sortArrow)
        w = findViewById(R.id.w)

        // 정렬 팝업
        sortArrow.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("최신순")
            popup.menu.add("거리순")
            popup.setOnMenuItemClickListener { item ->
                w.text = item.title
                true
            }
            popup.show()
        }

        // 필터 버튼 클릭 시 드롭다운
        materialContainer.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            updateFilterUI(isMaterialVisible, materialContainer, materialText, materialIcon)
        }

        distanceContainer.setOnClickListener {
            isDistanceVisible = !isDistanceVisible
            distance.visibility = if (isDistanceVisible) View.VISIBLE else View.GONE
            updateFilterUI(isDistanceVisible, distanceContainer, distanceText, distanceIcon)
        }
    }

    private fun updateFilterUI(visible: Boolean, container: LinearLayout, text: TextView, icon: ImageView) {
        if (visible) {
            container.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            text.setTextColor(Color.WHITE)
            icon.setImageResource(R.drawable.ic_arrow_up)
        } else {
            container.setBackgroundResource(R.drawable.rounded_rectangle_background)
            text.setTextColor(Color.parseColor("#8A8F9C"))
            icon.setImageResource(R.drawable.ic_arrow_down)
        }
    }

    private fun loadSavedPosts() {
        val token = "Bearer ${App.prefs.token}"

        RetrofitInstance.materialApi.getSavedTradePostIds(token).enqueue(object :
            Callback<List<Long>> {
            override fun onResponse(call: Call<List<Long>>, response: Response<List<Long>>) {
                response.body()?.forEach { postId ->
                    RetrofitInstance.apiService.getTradePostById(token, postId)
                        .enqueue(object : Callback<TradePostResponse> {
                            override fun onResponse(
                                call: Call<TradePostResponse>,
                                response: Response<TradePostResponse>
                            ) {
                                response.body()?.let {
                                    savedPosts.add(it)
                                    adapter.notifyItemInserted(savedPosts.size - 1)
                                }
                            }

                            override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {}
                        })
                }
            }

            override fun onFailure(call: Call<List<Long>>, t: Throwable) {
                Toast.makeText(this@MaterialMySavedActivity, "찜 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
