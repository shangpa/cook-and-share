package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.TradePostAdapter
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.model.TradePost.TradeUserResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialActivity : AppCompatActivity() {

    private var isMaterialVisible = false
    private var isDistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button>                 // 거리 버튼들
    private lateinit var materialButtons: List<Button>         // 카테고리 버튼들

    private lateinit var selectedFilterLayout: LinearLayout
    private lateinit var numberTextView: TextView
    private lateinit var sortLabel: TextView
    private lateinit var sortArrow: ImageView
    private lateinit var materialFilter: LinearLayout
    private lateinit var materialText: TextView

    private var selectedDistance: Double? = null
    private val selectedCategories = mutableSetOf<String>()

    // 기본 정렬: UPDATED = 끌올 반영 최신순(노출순)
    private var currentSort: String = "UPDATED"

    private var tradePosts: List<TradePostResponse> = listOf()
    private lateinit var tradePostAdapter: TradePostAdapter
    private lateinit var recyclerView: RecyclerView
    private var skipReloadOnce = false
    private var keepAtTop: Boolean = false
    private var rvState: Parcelable? = null

    var userLatitude: Double? = null
    var userLongitude: Double? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        TabBarUtils.setupTabBar(this)

        // 기본 UI 바인딩
        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)
        numberTextView = findViewById(R.id.number)
        sortLabel = findViewById(R.id.materailMainFilterText)
        sortArrow = findViewById(R.id.sortArrow)
        materialFilter = findViewById(R.id.materialFilter)
        materialText = findViewById(R.id.materialText)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val materialLayout = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val distanceLayout = findViewById<LinearLayout>(R.id.distance)

        // 카테고리 버튼들
        materialButtons = listOf(
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

        // 거리 버튼들
        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )

        // 내 위치 관리
        val myLocationButton: LinearLayout = findViewById(R.id.myLocation)
        myLocationButton.setOnClickListener {
            val token = App.prefs.token.toString()
            if (token.isBlank() || token == "null") {
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, MaterialMyLocationActivity::class.java))
            }
        }

        // 상세에서 forceSort 전달되면 즉시 반영
        applyForceSortFromIntent()

        // 초기 데이터 로드(기본 1km) — 사용자 위치 먼저 가져오고 호출
        fetchUserLocationAndLoadPosts(defaultDistanceKm = 1.0)

        // 카테고리 토글
        materialButtons.forEach { button ->
            button.setOnClickListener {
                onCategoryButtonClicked(button)
                materialLayout.visibility = View.GONE
                isMaterialVisible = false
            }
        }

        // 거리 토글
        buttons.forEach { btn ->
            btn.setOnClickListener { setSelectedDistanceButton(btn as Button) }
        }

        // 필터 드롭다운 토글
        materialFilter.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            materialLayout.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            updateFilterStyle(materialFilter, materialText, materialIcon, isMaterialVisible)
        }
        distanceFilter.setOnClickListener {
            isDistanceVisible = !isDistanceVisible
            distanceLayout.visibility = if (isDistanceVisible) View.VISIBLE else View.GONE

            if (isDistanceVisible) {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                distanceText.setTextColor(Color.WHITE)
                distanceIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                distanceText.setTextColor(Color.parseColor("#8A8F9C"))
                distanceIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        // 하단바 이동
        findViewById<ImageView>(R.id.searchIcon).setOnClickListener {
            startActivity(Intent(this, MaterialSearchActivity::class.java))
        }
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener {
            startActivity(Intent(this, MaterialMyProfileActivity::class.java))
        }
        findViewById<ImageView>(R.id.aa).setOnClickListener {
            startActivity(Intent(this, MaterialWritingActivity::class.java))
        }
        findViewById<ImageView>(R.id.bb).setOnClickListener {
            startActivity(Intent(this, MaterialChatActivity::class.java))
        }
        findViewById<ImageView>(R.id.plusIcon3).setOnClickListener {
            isPlusMenuVisible = !isPlusMenuVisible
            findViewById<ImageView>(R.id.aa).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.bb).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
        }

        // 스크롤 상단 고정 상태 추적
        keepAtTop = getKeepTopSticky()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val first = (rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                keepAtTop = (first == 0)
            }
        })

        // ▼▼ 정렬 팝업: 전용 레이아웃 없이 PopupMenu만 사용 ▼▼
        val showSortPopup: (View) -> Unit = { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add("최신순")
            popup.menu.add("거리순")
            popup.menu.add("가격순")
            popup.menu.add("구입 날짜순")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "최신순" -> setSortAndReload("UPDATED")        // 끌올 반영(노출순)
                    "거리순" -> setSortAndReload("DISTANCE")
                    "가격순" -> setSortAndReload("PRICE")
                    "구입 날짜순" -> setSortAndReload("PURCHASE_DATE")
                }
                true
            }
            popup.show() // sortLabel 바로 아래에 뜸
        }
        sortLabel.setOnClickListener { showSortPopup(sortLabel) }
        sortArrow.setOnClickListener { showSortPopup(sortLabel) }
        // ▲▲ 정렬 팝업 끝 ▲▲

        updateSortLabel()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        skipReloadOnce = intent?.getBooleanExtra("skipReloadOnce", false) == true

        // 새 글 즉시 상단에 추가(선택)
        intent?.getStringExtra("newTradePost")?.let { json ->
            val newPost = Gson().fromJson(json, TradePostResponse::class.java)
            prependNewPost(newPost)
        }

        // forceSort 수용
        val changed = applyForceSortFromIntent()
        if (changed) {
            loadNearbyPosts(
                distanceKm = selectedDistance,
                categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
                sort = currentSort
            )
        }
    }

    private fun prependNewPost(post: TradePostResponse) {
        if (!::tradePostAdapter.isInitialized) return
        val list = tradePosts.toMutableList()
        list.add(0, post)
        setRecyclerViewAdapter(list)
        recyclerView.scrollToPosition(0)
        setKeepTopSticky(true)
    }

    private fun setKeepTopSticky(enabled: Boolean) {
        keepAtTop = enabled
        getSharedPreferences("material_prefs", MODE_PRIVATE)
            .edit().putBoolean("keep_top_sticky", enabled).apply()
    }

    private fun getKeepTopSticky(): Boolean =
        getSharedPreferences("material_prefs", MODE_PRIVATE)
            .getBoolean("keep_top_sticky", false)

    /* -------------------------------- UI 헬퍼 -------------------------------- */

    private fun updateFilterStyle(layout: LinearLayout, text: TextView, icon: ImageView, expanded: Boolean) {
        if (expanded) {
            layout.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            text.setTextColor(Color.WHITE)
            icon.setImageResource(R.drawable.ic_arrow_up)
        } else {
            layout.setBackgroundResource(R.drawable.rounded_rectangle_background)
            text.setTextColor(Color.parseColor("#8A8F9C"))
            icon.setImageResource(R.drawable.ic_arrow_down)
        }
    }

    private fun setRecyclerViewAdapter(list: List<TradePostResponse>) {
        tradePosts = list

        val onItemClick: (TradePostResponse) -> Unit = { tradePost ->
            val token = App.prefs.token.toString()
            RetrofitInstance.apiService.increaseViewCount(
                tradePost.tradePostId, "Bearer $token"
            ).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("ViewCount", "조회수 증가 성공")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("ViewCount", "조회수 증가 실패", t)
                }
            })

            val intent = Intent(this, MaterialDetailActivity::class.java)
            intent.putExtra("tradePostId", tradePost.tradePostId)
            startActivity(intent)
        }

        tradePostAdapter = TradePostAdapter(
            list.toMutableList(),
            onItemClick = onItemClick,
            userLat = userLatitude,
            userLng = userLongitude
        )
        recyclerView.adapter = tradePostAdapter

        numberTextView.text = list.size.toString()

        val lm = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.post {
            if (keepAtTop || getKeepTopSticky()) {
                lm.scrollToPositionWithOffset(0, 0)
            } else {
                rvState?.let { lm.onRestoreInstanceState(it); rvState = null }
            }
        }
    }

    /* ----------------------------- 카테고리 처리 ----------------------------- */

    private fun onCategoryButtonClicked(button: Button) {
        val label = button.text.toString()

        if (button.id == R.id.all) {
            selectedCategories.clear()
            removeAllMaterialBadges()
            updateMaterialButtonsUI()
            materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
            materialText.setTextColor(Color.parseColor("#8A8F9C"))

            loadNearbyPosts(
                distanceKm = selectedDistance,
                categories = null,
                sort = currentSort
            )
            return
        }

        if (selectedCategories.contains(label)) {
            selectedCategories.remove(label)
            removeMaterialBadge(label)
        } else {
            selectedCategories.add(label)
            showSelectedMaterialBadge(label)
        }

        updateMaterialButtonsUI()

        if (selectedCategories.isEmpty()) {
            materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
            materialText.setTextColor(Color.parseColor("#8A8F9C"))
        } else {
            materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            materialText.setTextColor(Color.WHITE)
        }

        loadNearbyPosts(
            distanceKm = selectedDistance,
            categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
            sort = currentSort
        )
    }

    private fun updateMaterialButtonsUI() {
        materialButtons.forEach { btn ->
            val label = btn.text.toString()
            val selected = selectedCategories.contains(label)
            btn.setBackgroundResource(
                if (selected) R.drawable.rounded_rectangle_background_selected
                else R.drawable.rounded_rectangle_background
            )
            btn.setTextColor(if (selected) Color.WHITE else Color.parseColor("#8A8F9C"))
        }
    }

    private fun showSelectedMaterialBadge(text: String) {
        selectedFilterLayout.visibility = View.VISIBLE
        if (selectedFilterLayout.children.any { it.tag == "material-$text" }) return

        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 15, 0) }
        badge.layoutParams = lp

        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = text
        badge.tag = "material-$text"

        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)
            selectedCategories.remove(text)
            updateMaterialButtonsUI()

            if (selectedCategories.isEmpty()) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                materialText.setTextColor(Color.parseColor("#8A8F9C"))
            }

            loadNearbyPosts(
                distanceKm = selectedDistance,
                categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
                sort = currentSort
            )
        }
        selectedFilterLayout.addView(badge)
    }

    private fun removeMaterialBadge(category: String) {
        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag == "material-$category") {
                selectedFilterLayout.removeView(badge)
            }
        }
        if (selectedCategories.isEmpty() &&
            selectedFilterLayout.children.none { it.tag == "distance" }) {
            selectedFilterLayout.visibility = View.GONE
        }
    }

    private fun removeAllMaterialBadges() {
        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag.toString().startsWith("material-")) {
                selectedFilterLayout.removeView(badge)
            }
        }
        if (selectedFilterLayout.children.none { it.tag == "distance" }) {
            selectedFilterLayout.visibility = View.GONE
        }
    }

    /* ------------------------------- 거리 처리 ------------------------------- */

    private fun setSelectedDistanceButton(button: Button) {
        buttons.forEach {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background)
            it.setTextColor(Color.parseColor("#8A8F9C"))
        }
        button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
        button.setTextColor(Color.WHITE)

        val distanceKm = when (button.id) {
            R.id.alll -> null
            R.id.threeHundred -> 0.3
            R.id.fiveHundred -> 0.5
            R.id.oneThousand -> 1.0
            R.id.onefiveThousand -> 1.5
            R.id.twoThousand -> 2.0
            else -> null
        }
        selectedDistance = distanceKm

        // 기존 distance 배지 제거
        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag == "distance") selectedFilterLayout.removeView(badge)
        }

        // "전체"가 아니면 distance 배지 표시
        if (distanceKm != null) {
            val badge = layoutInflater.inflate(R.layout.filter_badge, null)
            badge.tag = "distance"
            badge.findViewById<TextView>(R.id.filterText).text = button.text.toString()
            badge.findViewById<ImageView>(R.id.filterClose).setOnClickListener {
                selectedFilterLayout.removeView(badge)
                selectedDistance = null
                buttons.forEach {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    it.setTextColor(Color.parseColor("#8A8F9C"))
                }
                loadNearbyPosts(
                    distanceKm = null,
                    categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
                    sort = currentSort
                )
            }
            selectedFilterLayout.addView(badge)
            selectedFilterLayout.visibility = View.VISIBLE
        } else {
            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                selectedFilterLayout.visibility = View.GONE
            }
        }

        // 드롭다운 닫기
        findViewById<LinearLayout>(R.id.distance).visibility = View.GONE
        isDistanceVisible = false

        loadNearbyPosts(
            distanceKm = selectedDistance,
            categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
            sort = currentSort
        )
    }

    private fun setSortAndReload(sort: String) {
        if (currentSort != sort) {
            currentSort = sort
            updateSortLabel()
            loadNearbyPosts(
                distanceKm = selectedDistance,
                categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
                sort = currentSort
            )
        }
    }

    private fun applyForceSortFromIntent(): Boolean {
        val forced = intent.getStringExtra("forceSort") ?: return false
        intent.removeExtra("forceSort")
        val changed = currentSort != forced
        currentSort = forced
        updateSortLabel()
        return changed
    }

    private fun updateSortLabel() {
        sortLabel.text = when (currentSort) {
            "UPDATED" -> "최신순"        // 업 반영(노출순)
            "DISTANCE" -> "거리순"
            "PRICE" -> "가격순"
            "PURCHASE_DATE" -> "구입 날짜순"
            "LATEST" -> "등록순"
            else -> "최신순"
        }
    }

    /* ----------------------- 위치 가져오기 + 초기 로딩 ----------------------- */

    private fun fetchUserLocationAndLoadPosts(defaultDistanceKm: Double? = 1.0) {
        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getUserLocation("Bearer $token")
            .enqueue(object : Callback<TradeUserResponse> {
                override fun onResponse(
                    call: Call<TradeUserResponse>,
                    response: Response<TradeUserResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            userLatitude = it.latitude
                            userLongitude = it.longitude
                        }
                    } else {
                        Log.e("UserLocation", "서버 응답 오류: ${response.code()}")
                    }
                    selectedDistance = defaultDistanceKm
                    loadNearbyPosts(distanceKm = defaultDistanceKm, categories = null, sort = currentSort)
                }

                override fun onFailure(call: Call<TradeUserResponse>, t: Throwable) {
                    Log.e("UserLocation", "위치 요청 실패", t)
                    selectedDistance = defaultDistanceKm
                    loadNearbyPosts(distanceKm = defaultDistanceKm, categories = null, sort = currentSort)
                }
            })
    }

    /* ----------------------------- 서버 호출 통합 ----------------------------- */

    private fun loadNearbyPosts(
        distanceKm: Double? = null,
        categories: List<String>? = null,
        sort: String = "UPDATED"
    ) {
        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getNearbyFlexible("Bearer $token", distanceKm, categories, sort)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body().orEmpty()
                        setRecyclerViewAdapter(result)
                    } else {
                        Log.e("nearby", "응답 실패: ${response.code()}")
                        Toast.makeText(this@MaterialActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Log.e("nearby", "요청 실패", t)
                    Toast.makeText(this@MaterialActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /* ----------------------------- 생명주기 보정 ----------------------------- */

    override fun onResume() {
        super.onResume()

        if (skipReloadOnce) {
            skipReloadOnce = false
            if (keepAtTop) recyclerView.scrollToPosition(0)
            return
        }

        // 현재 선택 상태로 재호출
        loadNearbyPosts(
            distanceKm = selectedDistance ?: 1.0,
            categories = selectedCategories.takeIf { it.isNotEmpty() }?.toList(),
            sort = currentSort
        )
    }

    override fun onPause() {
        super.onPause()
        if (!keepAtTop) {
            rvState = recyclerView.layoutManager?.onSaveInstanceState()
        }
    }
}
