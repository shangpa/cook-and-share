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
import java.text.SimpleDateFormat

class MaterialActivity : AppCompatActivity() {

    private var isMaterialVisible = false
    private var isDistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button>
    private lateinit var selectedFilterLayout: LinearLayout
    private lateinit var numberTextView: TextView
    private lateinit var sortText: TextView
    private lateinit var sortArrow: ImageView

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    private var tradePosts: List<TradePostResponse> = listOf()
    private lateinit var tradePostAdapter: TradePostAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)
        val recyclerView = findViewById<RecyclerView>(R.id.tradePostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        numberTextView = findViewById(R.id.number)
        sortText = findViewById(R.id.w)
        sortArrow = findViewById(R.id.sortArrow)

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

        // 🔥 정렬 팝업
        sortArrow.setOnClickListener {
            val popupMenu = PopupMenu(this, sortArrow)
            popupMenu.menu.add("최신순")
            popupMenu.menu.add("거리순")
            popupMenu.menu.add("가격순")
            popupMenu.menu.add("구입 날짜순")

            popupMenu.setOnMenuItemClickListener { item ->
                sortText.text = item.title
                when (item.title) {
                    "최신순" -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        val sortedList = tradePosts.sortedByDescending {
                            it.createdAt?.let { createdAt -> sdf.parse(createdAt) }
                        }
                        setRecyclerViewAdapter(sortedList)
                    }
                    "가격순" -> {
                        val sortedList = tradePosts.sortedBy { it.price }
                        setRecyclerViewAdapter(sortedList)
                    }
                    "구입 날짜순" -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val sortedList = tradePosts.sortedByDescending {
                            sdf.parse(it.purchaseDate)
                        }
                        setRecyclerViewAdapter(sortedList)
                    }
                }
                true
            }

            popupMenu.show()
        }

        // 🔥 카테고리 버튼 클릭
        materialButtons.forEach { button ->
            button.setOnClickListener {
                setSelectedMaterialButton(button, findViewById(R.id.materialFilter), findViewById(R.id.materialText))
                showSelectedFilterBadge(button.text.toString(), findViewById(R.id.materialFilter), findViewById(R.id.materialText))

                val selectedCategory = button.text.toString()
                if (selectedCategory == "전체") {
                    TradePostRepository.getAllTradePosts(null) { posts ->
                        if (posts != null) {
                            tradePosts = posts
                            setRecyclerViewAdapter(tradePosts)
                        }
                    }
                } else {
                    TradePostRepository.getTradePostsByCategory(selectedCategory) { posts ->
                        if (posts != null) {
                            tradePosts = posts
                            setRecyclerViewAdapter(tradePosts)
                        }
                    }
                }
            }
        }

        // 🔥 초기 거래글 로드
        TradePostRepository.getAllTradePosts(null) { posts ->
            if (posts != null) {
                tradePosts = posts
                setRecyclerViewAdapter(tradePosts)
            }
        }

        // 하단 네비게이션
        findViewById<ImageView>(R.id.searchIcon).setOnClickListener { startActivity(Intent(this, MaterialSearchActivity::class.java)) }
        findViewById<LinearLayout>(R.id.myLocation).setOnClickListener { startActivity(Intent(this, MaterialMyLocationActivity::class.java)) }
        findViewById<ImageView>(R.id.profileIcon).setOnClickListener { startActivity(Intent(this, MaterialMyProfileActivity::class.java)) }
        findViewById<ImageView>(R.id.aa).setOnClickListener { startActivity(Intent(this, MaterialWritingActivity::class.java)) }
        findViewById<ImageView>(R.id.bb).setOnClickListener { startActivity(Intent(this, MaterialChatActivity::class.java)) }
        findViewById<ImageView>(R.id.plusIcon3).setOnClickListener {
            isPlusMenuVisible = !isPlusMenuVisible
            findViewById<ImageView>(R.id.aa).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
            findViewById<ImageView>(R.id.bb).visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setRecyclerViewAdapter(list: List<TradePostResponse>) {
        val recyclerView = findViewById<RecyclerView>(R.id.tradePostRecyclerView)
        tradePostAdapter = TradePostAdapter(list) { tradePost ->
            val intent = Intent(this, MaterialDetailActivity::class.java)
            intent.putExtra("tradePostId", tradePost.tradePostId)
            startActivity(intent)
        }
        recyclerView.adapter = tradePostAdapter
        numberTextView.text = list.size.toString()
    }

    private fun setSelectedMaterialButton(button: Button, filterLayout: LinearLayout, textView: TextView) {
        selectedMaterial = if (selectedMaterial == button) null else button
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
        selectedMaterial?.let {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            it.setTextColor(Color.WHITE)
        }
    }

    private fun showSelectedFilterBadge(text: String, materialFilter: LinearLayout, materialText: TextView) {
        selectedFilterLayout.visibility = View.VISIBLE
        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = text
        badge.tag = "material-$text"

        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)
            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                materialText.setTextColor(Color.parseColor("#8A8F9C"))

                TradePostRepository.getAllTradePosts(null) { posts ->
                    if (posts != null) {
                        tradePosts = posts
                        setRecyclerViewAdapter(tradePosts)
                    }
                }
            }
        }

        selectedFilterLayout.addView(badge)
    }
}
