package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.TradePostAdapter
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
    private lateinit var buttons: List<Button>
    private lateinit var totalResults: TextView

    private var isMaterialVisible = false
    private var isDistanceVisible = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TradePostAdapter
    private val savedPosts = mutableListOf<TradePostResponse>()
    private var filteredPosts = mutableListOf<TradePostResponse>()

    //필터변수
    private var selectedDistance: Double? = null
    private val selectedCategories = mutableSetOf<String>()
    private var currentSort = "최신순"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_saved)

        totalResults = findViewById(R.id.totalResults)


        val materialButtons = listOf(
            findViewById<Button>(R.id.all),
            findViewById(R.id.cookware),
            findViewById(R.id.fans_pots),
            findViewById(R.id.containers),
            findViewById(R.id.tableware),
            findViewById(R.id.storageSupplies),
            findViewById(R.id.sanitaryProducts),
            findViewById(R.id.smallAppliances),
            findViewById(R.id.disposableProducts),
            findViewById(R.id.etc)
        )
        materialButtons.forEach { button ->
            button.setOnClickListener {
                // 선택된 텍스트
                val category = button.text.toString()
                Log.d("카테고리필터", "클릭된 카테고리: $category")
                // 모두 초기화 (모든 버튼 기본 상태로 되돌리기)
                materialButtons.forEach {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    it.setTextColor(Color.parseColor("#8A8F9C"))
                }

                // 선택 상태로 설정
                if (selectedCategories.contains(category)) {
                    // 이미 선택된 버튼을 다시 클릭하면 선택 해제
                    selectedCategories.clear()
                    Log.d("카테고리필터", "선택 해제됨")
                } else {
                    selectedCategories.clear()
                    selectedCategories.add(category)
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    button.setTextColor(Color.WHITE)
                    Log.d("카테고리필터", "현재 선택된 카테고리: $selectedCategories")
                }

                // 필터 적용
                applySavedPostFilterAndSort()
            }
        }
        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )
        buttons.forEach { button ->
            button.setOnClickListener {
                buttons.forEach {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    it.setTextColor(Color.parseColor("#8A8F9C"))
                }

                button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                button.setTextColor(Color.WHITE)

                selectedDistance = when (button.id) {
                    R.id.threeHundred -> 0.3
                    R.id.fiveHundred -> 0.5
                    R.id.oneThousand -> 1.0
                    R.id.onefiveThousand -> 1.5
                    R.id.twoThousand -> 2.0
                    else -> null
                }

                applySavedPostFilterAndSort()
            }
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TradePostAdapter(
            tradePosts = mutableListOf(),
            onItemClick = { post ->
                val intent = Intent(this, MaterialDetailActivity::class.java)
                intent.putExtra("tradePostId", post.tradePostId)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter
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
            popup.menu.add("가격순")
            popup.menu.add("구입 날짜순")
            popup.setOnMenuItemClickListener { item ->
                currentSort = item.title.toString()
                applySavedPostFilterAndSort()
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
                val ids = response.body() ?: return
                if (ids.isEmpty()) return

                var loadedCount = 0
                val tempList = mutableListOf<TradePostResponse>()

                ids.forEach { postId ->
                    RetrofitInstance.apiService.getTradePostById(token, postId)
                        .enqueue(object : Callback<TradePostResponse> {
                            override fun onResponse(
                                call: Call<TradePostResponse>,
                                response: Response<TradePostResponse>
                            ) {
                                response.body()?.let {
                                    tempList.add(it)
                                }
                                loadedCount++
                                if (loadedCount == ids.size) {
                                    savedPosts.clear()
                                    savedPosts.addAll(tempList)
                                    applySavedPostFilterAndSort()
                                }
                            }

                            override fun onFailure(call: Call<TradePostResponse>, t: Throwable) {
                                loadedCount++
                            }
                        })
                }
            }

            override fun onFailure(call: Call<List<Long>>, t: Throwable) {
                Toast.makeText(this@MaterialMySavedActivity, "찜 목록 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun applySavedPostFilterAndSort() {
        filteredPosts = savedPosts
            .filter { post ->
                val matchCategory = selectedCategories.isEmpty() || selectedCategories.contains(post.category)
                val matchDistance = selectedDistance == null || (post.distance != null && post.distance!! <= selectedDistance!!)
                matchCategory && matchDistance
            }
            .sortedWith(
                when (currentSort) {
                    "최신순" -> compareByDescending { it.createdAt }
                    "거리순" -> compareBy { it.distance ?: Double.MAX_VALUE }
                    "가격순" -> compareBy { it.price }
                    "구입 날짜순" -> compareByDescending { it.purchaseDate }
                    else -> compareByDescending { it.createdAt }
                }
            ).toMutableList()
        totalResults.text = "총 ${filteredPosts.size}개 검색결과"
        adapter.updateData(filteredPosts)
    }

}
