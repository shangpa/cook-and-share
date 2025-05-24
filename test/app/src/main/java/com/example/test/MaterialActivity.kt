package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.TradePostAdapter
import com.example.test.model.TradePost.TradePostRepository
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.network.RetrofitInstance
import java.text.SimpleDateFormat
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MaterialActivity : AppCompatActivity() {

    private var isMaterialVisible = false
    private var isDistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button>
    private lateinit var selectedFilterLayout: LinearLayout
    private lateinit var numberTextView: TextView
    private lateinit var sortText: TextView
    private lateinit var sortArrow: ImageView
    private lateinit var materialFilter: LinearLayout
    private lateinit var materialText: TextView

    private var selectedDistance: Double? = null
    private val selectedCategories = mutableSetOf<String>()
    private var isDistanceBasedData: Boolean = false

    private var tradePosts: List<TradePostResponse> = listOf()
    private lateinit var tradePostAdapter: TradePostAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)
        numberTextView = findViewById(R.id.number)
        sortText = findViewById(R.id.materailMainFilterText)
        sortArrow = findViewById(R.id.sortArrow)

        val recyclerView = findViewById<RecyclerView>(R.id.tradePostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        materialFilter = findViewById(R.id.materialFilter)
        materialText = findViewById(R.id.materialText)

        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val materialLayout = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val distance = findViewById<LinearLayout>(R.id.distance)

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

        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )

        //내위치 이동
        val myLocationButton: LinearLayout = findViewById(R.id.myLocation)

        myLocationButton.setOnClickListener {
            val token = App.prefs.token.toString()

            if (token.isBlank() || token == "null") {
                // 로그인 안 되어 있으면 LoginActivity로 이동
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            } else {
                // 로그인 되어 있으면 MaterialMyLocationActivity로 이동
                val intent = Intent(this, MaterialMyLocationActivity::class.java)
                startActivity(intent)
            }
        }

        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getNearbyTradePosts("Bearer $token", 1.0)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val nearbyPosts = response.body() ?: emptyList()
                        setRecyclerViewAdapter(nearbyPosts)
                        isDistanceBasedData = true
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Log.e("거리 필터", "초기 로딩 실패", t)
                }
            })

        // 정렬 기능
        sortArrow.setOnClickListener {
            val popupMenu = PopupMenu(this, sortArrow)
            popupMenu.menu.add("최신순")
            popupMenu.menu.add("거리순")
            popupMenu.menu.add("가격순")
            popupMenu.menu.add("구입 날짜순")

            popupMenu.setOnMenuItemClickListener { item ->
                sortText.text = item.title
                findViewById<TextView>(R.id.materailMainFilterText).text = item.title

                when (item.title) {
                    "최신순" -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val sortedList = tradePosts.sortedByDescending {
                            it.createdAt?.let { createdAt ->
                                sdf.parse(createdAt)
                            }
                        }
                        tradePosts = sortedList
                        setRecyclerViewAdapter(sortedList)
                    }

                    "거리순" -> {
                        if (!isDistanceBasedData) {
                            Toast.makeText(this, "먼저 거리 필터를 선택해야 합니다", Toast.LENGTH_SHORT).show()
                            return@setOnMenuItemClickListener true
                        }

                        if (tradePosts.any { it.distance != null }) {
                            val sortedList = tradePosts.sortedBy { it.distance ?: Double.MAX_VALUE }
                            tradePosts = sortedList
                            setRecyclerViewAdapter(sortedList)
                        } else {
                            Toast.makeText(this, "거리 정보가 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                    "가격순" -> {
                        val sortedList = tradePosts.sortedBy { it.price }
                        tradePosts = sortedList
                        setRecyclerViewAdapter(sortedList)
                    }

                    "구입 날짜순" -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val sortedList = tradePosts.sortedByDescending { sdf.parse(it.purchaseDate) }
                        tradePosts = sortedList
                        setRecyclerViewAdapter(sortedList)
                    }
                }
                true
            }
            popupMenu.show()
        }

        // 카테고리 버튼 클릭
        materialButtons.forEach { button ->
            button.setOnClickListener {
                setSelectedMaterialButton(button, materialFilter, materialText)
                showSelectedFilterBadge(button.text.toString(), materialFilter, materialText)
                materialLayout.visibility = View.GONE
                isMaterialVisible = false
            }
        }

        // 거리 필터 버튼 클릭
        buttons.forEach {
            it.setOnClickListener { button -> setSelectedDistanceButton(button as Button) }
        }

        materialFilter.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            materialLayout.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            updateFilterStyle(materialFilter, materialText, materialIcon, isMaterialVisible)
        }

        distanceFilter.setOnClickListener {
            isDistanceVisible = !isDistanceVisible
            /*updateFilterStyle(distanceFilter, distanceText, distanceIcon, isDistanceVisible)*/

            distance.visibility = if (isDistanceVisible) View.VISIBLE else View.GONE
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

        // 초기 전체 거래글 불러오기
        /*        TradePostRepository.getAllTradePosts(null) { posts ->
            posts?.let {
                tradePosts = it
                setRecyclerViewAdapter(tradePosts)
            }
        }*/

        // 하단바 이동
        findViewById<ImageView>(R.id.searchIcon).setOnClickListener { startActivity(Intent(this, MaterialSearchActivity::class.java)) }
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener { startActivity(Intent(this, MaterialMyProfileActivity::class.java)) }
        findViewById<ImageView>(R.id.aa).setOnClickListener { startActivity(Intent(this, MaterialWritingActivity::class.java)) }
        findViewById<ImageView>(R.id.bb).setOnClickListener { startActivity(Intent(this, MaterialChatActivity::class.java)) }
        findViewById<ImageView>(R.id.plusIcon3).setOnClickListener {
            isPlusMenuVisible = !isPlusMenuVisible
            findViewById<ImageView>(R.id.aa).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.bb).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setSelectedButton(selectedButton: Button) {
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceLayout = findViewById<LinearLayout>(R.id.distance)

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }

        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag == "distance") selectedFilterLayout.removeView(badge)
        }

        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        badge.tag = "distance"
        badge.findViewById<TextView>(R.id.filterText).text = selectedButton.text.toString()
        badge.findViewById<ImageView>(R.id.filterClose).setOnClickListener {
            selectedFilterLayout.removeView(badge)
            selectedDistance = null
            buttons.forEach {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                it.setTextColor(Color.parseColor("#8A8F9C"))
            }
            distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
            distanceText.setTextColor(Color.parseColor("#8A8F9C"))
        }

        selectedFilterLayout.addView(badge)
        distanceLayout.visibility = View.GONE
        isDistanceVisible = false
        distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
        distanceText.setTextColor(Color.WHITE)
    }


    private fun setRecyclerViewAdapter(list: List<TradePostResponse>) {
        tradePosts = list

        val recyclerView = findViewById<RecyclerView>(R.id.tradePostRecyclerView)

        tradePostAdapter = TradePostAdapter(list.toMutableList()) { tradePost ->
            // 여기서 조회수 증가 먼저 호출
            val token = App.prefs.token.toString()
            RetrofitInstance.apiService.increaseViewCount(tradePost.tradePostId, "Bearer $token")
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.d("ViewCount", "조회수 증가 성공")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ViewCount", "조회수 증가 실패", t)
                    }
                })

            // 상세 페이지 이동
            val intent = Intent(this, MaterialDetailActivity::class.java)
            intent.putExtra("tradePostId", tradePost.tradePostId)
            startActivity(intent)
        }

        recyclerView.adapter = tradePostAdapter
        numberTextView.text = list.size.toString()
    }


    private fun setSelectedMaterialButton(button: Button, filterLayout: LinearLayout, textView: TextView) {
        val category = button.text.toString()
        listOf(
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
        ).forEach {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background)
            it.setTextColor(Color.parseColor("#8A8F9C"))
        }

        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category)
            button.setBackgroundResource(R.drawable.rounded_rectangle_background)
            button.setTextColor(Color.parseColor("#8A8F9C"))
        } else {
            selectedCategories.add(category)
            showSelectedFilterBadge(category, materialFilter, materialText)
            button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            button.setTextColor(Color.WHITE)
        }

        if (selectedCategories.isEmpty()) {
            filterLayout.setBackgroundResource(R.drawable.rounded_rectangle_background)
            textView.setTextColor(Color.parseColor("#8A8F9C"))
        } else {
            filterLayout.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            textView.setTextColor(Color.WHITE)
        }

        loadPostsByMultipleCategories()
    }

    private fun removeMaterialBadge(category: String) {
        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag == "material-$category") {
                selectedFilterLayout.removeView(badge)
            }
        }
    }

    private fun loadPostsByMultipleCategories() {
        val token = App.prefs.token.toString()
        if (selectedDistance != null) {
            loadNearbyPostsByMultipleCategories(selectedDistance!!, selectedCategories.toList())
        } else {
            TradePostRepository.getTradePostsByMultipleCategories(token, selectedCategories.toList()) { posts ->
                posts?.let {
                    tradePosts = it
                    setRecyclerViewAdapter(tradePosts)
                }
            }
        }
    }

    private fun showSelectedFilterBadge(text: String, materialFilter: LinearLayout, materialText: TextView) {
        selectedFilterLayout.visibility = View.VISIBLE
        if (selectedFilterLayout.children.any { it.tag == "material-$text" }) return
        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = text
        badge.tag = "material-$text"

        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)
            selectedCategories.remove(text)

            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("distance-") }) {
                isDistanceBasedData = false
            }

            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                materialText.setTextColor(Color.parseColor("#8A8F9C"))
                TradePostRepository.getAllTradePosts(null) { posts ->
                    posts?.let {
                        tradePosts = it
                        setRecyclerViewAdapter(tradePosts)
                    }
                }
            }
        }
        selectedFilterLayout.addView(badge)
    }

    private fun setSelectedDistanceButton(button: Button) {

        buttons.forEach {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background)
            it.setTextColor(Color.parseColor("#8A8F9C"))
        }
        button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
        button.setTextColor(Color.WHITE)

        // 거리 값 추출
        val distanceKm = when (button.id) {
            R.id.threeHundred -> 0.3
            R.id.fiveHundred -> 0.5
            R.id.oneThousand -> 1.0
            R.id.onefiveThousand -> 1.5
            R.id.twoThousand -> 2.0
            else -> 100.0
        }

        selectedDistance = distanceKm
        val token = App.prefs.token.toString()

        if (token != "null" && token.isNotBlank()) {
            if (selectedCategories.isNotEmpty()) {
                loadNearbyPostsByMultipleCategories(distanceKm, selectedCategories.toList())
            } else {
                // 거리 필터만 적용
                RetrofitInstance.apiService.getNearbyTradePosts("Bearer $token", distanceKm)
                    .enqueue(object : Callback<List<TradePostResponse>> {
                        override fun onResponse(
                            call: Call<List<TradePostResponse>>,
                            response: Response<List<TradePostResponse>>
                        ) {
                            if (response.isSuccessful) {
                                val result = response.body() ?: emptyList()
                                setRecyclerViewAdapter(result)
                            } else {
                                Log.e("거리 필터", "응답 실패: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                            Log.e("거리 필터", "요청 실패", t)
                        }
                    })
            }
        }
    }

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

    override fun onResume() {
        super.onResume()

        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getNearbyTradePosts("Bearer $token", 1.0)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val nearbyPosts = response.body() ?: emptyList()
                        setRecyclerViewAdapter(nearbyPosts)
                        isDistanceBasedData = true
                    } else {
                        Log.e("거리필터", "응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Log.e("거리필터", "요청 실패", t)
                }
            })
    }

    private val LOCATION_REQUEST_CODE = 1001

    private fun openLocationSetting() {
        val intent = Intent(this, MaterialMyLocationActivity::class.java)
        startActivityForResult(intent, LOCATION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOCATION_REQUEST_CODE && resultCode == RESULT_OK) {
            val locationSaved = data?.getBooleanExtra("locationSaved", false) ?: false
            if (locationSaved) {
                // 위치 저장됨 → 근처 거래글 다시 로드
                loadNearbyTradePosts()
            }
        }
    }

    private fun loadNearbyTradePosts() {
        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getNearbyTradePosts("Bearer $token", 1.0)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val nearbyPosts = response.body() ?: emptyList()
                        setRecyclerViewAdapter(nearbyPosts)
                        isDistanceBasedData = true
                    } else {
                        Log.e("거리필터", "응답 실패: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Log.e("거리필터", "요청 실패", t)
                }
            })
    }

    private fun loadNearbyPostsByMultipleCategories(distanceKm: Double, categories: List<String>) {
        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.getNearbyTradePostsByMultipleCategories("Bearer $token", distanceKm, categories)
            .enqueue(object : Callback<List<TradePostResponse>> {
                override fun onResponse(
                    call: Call<List<TradePostResponse>>,
                    response: Response<List<TradePostResponse>>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body() ?: emptyList()
                        setRecyclerViewAdapter(result)
                        isDistanceBasedData = true
                    } else {
                        Toast.makeText(this@MaterialActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                    Toast.makeText(this@MaterialActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            })
    }

/*    private fun loadNearbyPostsByCategory(distanceKm: Double, category: String) {
        val token = App.prefs.token.toString()
        if (token != "null" && token.isNotBlank()) {
            RetrofitInstance.apiService.getNearbyTradePostsByCategory("Bearer $token", distanceKm, category)
                .enqueue(object : Callback<List<TradePostResponse>> {
                    override fun onResponse(
                        call: Call<List<TradePostResponse>>,
                        response: Response<List<TradePostResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val result = response.body() ?: emptyList()
                            isDistanceBasedData = true
                            setRecyclerViewAdapter(result)
                        } else {
                            Toast.makeText(this@MaterialActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<TradePostResponse>>, t: Throwable) {
                        Toast.makeText(this@MaterialActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }*/
}
