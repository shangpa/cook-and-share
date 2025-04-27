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
        numberTextView = findViewById(R.id.number)
        sortText = findViewById(R.id.w)
        sortArrow = findViewById(R.id.sortArrow)

        val recyclerView = findViewById<RecyclerView>(R.id.tradePostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val materialFilter = findViewById<LinearLayout>(R.id.materialFilter)
        val materialText = findViewById<TextView>(R.id.materialText)
        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val materialLayout = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val distanceLayout = findViewById<LinearLayout>(R.id.distance)

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

        // 정렬 기능
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
                        val sortedList = tradePosts.sortedByDescending { it.createdAt?.let { createdAt -> sdf.parse(createdAt) } }
                        setRecyclerViewAdapter(sortedList)
                    }
                    "가격순" -> {
                        val sortedList = tradePosts.sortedBy { it.price }
                        setRecyclerViewAdapter(sortedList)
                    }
                    "구입 날짜순" -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd")
                        val sortedList = tradePosts.sortedByDescending { sdf.parse(it.purchaseDate) }
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

                val selectedCategory = button.text.toString()
                if (selectedCategory == "전체") {
                    TradePostRepository.getAllTradePosts(null) { posts ->
                        posts?.let {
                            tradePosts = it
                            setRecyclerViewAdapter(tradePosts)
                        }
                    }
                } else {
                    TradePostRepository.getTradePostsByCategory(selectedCategory) { posts ->
                        posts?.let {
                            tradePosts = it
                            setRecyclerViewAdapter(tradePosts)
                        }
                    }
                }
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
            distanceLayout.visibility = if (isDistanceVisible) View.VISIBLE else View.GONE
            updateFilterStyle(distanceFilter, distanceText, distanceIcon, isDistanceVisible)
        }

        // 초기 전체 거래글 불러오기
        TradePostRepository.getAllTradePosts(null) { posts ->
            posts?.let {
                tradePosts = it
                setRecyclerViewAdapter(tradePosts)
            }
        }

        // 하단바 이동
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
}
