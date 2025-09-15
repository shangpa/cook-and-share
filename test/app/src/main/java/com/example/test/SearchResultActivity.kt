// SearchResultActivity.kt (핵심 부분 교체/추가)
package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.*
import com.example.test.model.Recipe
import com.example.test.model.recipeDetail.ShortsSearchItem
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class SearchResultActivity : AppCompatActivity() {

    private lateinit var totalTap: TextView
    private lateinit var recipeTap: TextView
    private lateinit var videoTap: TextView
    private lateinit var indicatorBar: View

    private lateinit var rvTotal: RecyclerView
    private lateinit var rvRecipesOnly: RecyclerView
    private lateinit var rvShortsOnly: RecyclerView
    private lateinit var emptyView: View
    private lateinit var number: TextView

    private lateinit var searchKeyword: String
    private var sortOrder: String? = "latest"
    private var currentSelectedCategory: String? = null

    private val ACTIVE = Color.parseColor("#000000")
    private val INACTIVE = Color.parseColor("#8A8A8A")
    private val density: Float get() = resources.displayMetrics.density

    // 데이터 캐시
    private var fullRecipeList: List<Recipe> = emptyList()
    private var fullShortsList: List<ShortsSearchItem> = emptyList()

    // 어댑터
    private lateinit var totalAdapter: TotalFeedAdapter
    private lateinit var recipeOnlyAdapter: RecipeSearchAdapter
    private lateinit var shortsOnlyAdapter: ShortsGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        TabBarUtils.setupTabBar(this)

        // 탭
        totalTap = findViewById(R.id.totalTap)
        recipeTap = findViewById(R.id.recipeTap)
        videoTap = findViewById(R.id.videoTap)
        indicatorBar = findViewById(R.id.indicatorBar)

        rvTotal = findViewById(R.id.rvTotal)
        rvRecipesOnly = findViewById(R.id.rvRecipesOnly)
        rvShortsOnly = findViewById(R.id.rvShortsOnly)
        emptyView = findViewById(R.id.emptyView)
        number = findViewById(R.id.number)

        // 어댑터/레이아웃매니저
        totalAdapter = TotalFeedAdapter(emptyList()) { short ->
            // 쇼츠 클릭 이동 (ShortsActivity 등)
            startActivity(Intent(this, ShortsActivity::class.java))
        }
        rvTotal.layoutManager = LinearLayoutManager(this)
        rvTotal.adapter = totalAdapter

        recipeOnlyAdapter = RecipeSearchAdapter(emptyList()) { recipe ->
            val i = Intent(this, RecipeSeeActivity::class.java)
            i.putExtra("recipeId", recipe.recipeId)
            startActivity(i)
        }
        rvRecipesOnly.layoutManager = LinearLayoutManager(this)
        rvRecipesOnly.adapter = recipeOnlyAdapter

        shortsOnlyAdapter = ShortsGridAdapter(emptyList()) { short ->
            startActivity(Intent(this, ShortsActivity::class.java))
        }
        rvShortsOnly.layoutManager = GridLayoutManager(this, 2)
        rvShortsOnly.adapter = shortsOnlyAdapter

        // 정렬 팝업
        val dowArrow: ImageButton = findViewById(R.id.dowArrow)
        val elementaryLevel: TextView = findViewById(R.id.elementaryLevel)
        setupPopupMenu(dowArrow, elementaryLevel)

        // 검색어
        searchKeyword = intent.getStringExtra("searchKeyword") ?: ""
        val searchText: EditText = findViewById(R.id.writeSearchTxt)
        searchText.setText(searchKeyword)
        findViewById<ImageButton>(R.id.searchIcon).setOnClickListener {
            val keyword = searchText.text.toString().trim()
            if (keyword.isNotBlank()) {
                searchKeyword = keyword
                currentSelectedCategory = null
                fetchAndRenderAll()
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<ImageView>(R.id.SearchResultBackIcon).setOnClickListener { finish() }

        // 카테고리
        setupCategoryButtons()

        // 탭 클릭
        totalTap.setOnClickListener { showTab(0) }
        recipeTap.setOnClickListener { showTab(1) }
        videoTap.setOnClickListener { showTab(2) }
        showTab(0)

        // 최초 로드
        fetchAndRenderAll()
    }

    private fun showTab(index: Int) {
        rvTotal.visibility = if (index == 0) View.VISIBLE else View.GONE
        rvRecipesOnly.visibility = if (index == 1) View.VISIBLE else View.GONE
        rvShortsOnly.visibility = if (index == 2) View.VISIBLE else View.GONE

        totalTap.setTextColor(if (index == 0) ACTIVE else INACTIVE)
        recipeTap.setTextColor(if (index == 1) ACTIVE else INACTIVE)
        videoTap.setTextColor(if (index == 2) ACTIVE else INACTIVE)

        moveIndicatorTo(when (index) { 0 -> totalTap; 1 -> recipeTap; else -> videoTap })
        updateEmptyView()
    }

    private fun fetchAndRenderAll() {
        // 1) 레시피
        val token = "Bearer ${App.prefs.token}"
        RetrofitInstance.apiService.searchRecipes(
            token = token, title = searchKeyword, sort = sortOrder
        ).enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, resp: Response<List<Recipe>>) {
                fullRecipeList = (resp.body() ?: emptyList()).let { list ->
                    // 카테고리 필터(프론트측)
                    currentSelectedCategory?.let { cat -> list.filter { it.category == cat } } ?: list
                }
                renderRecipeOnly()
                maybeBuildTotal()
            }
            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                fullRecipeList = emptyList()
                renderRecipeOnly()
                maybeBuildTotal()
            }
        })

        // 2) 쇼츠
        val bearer = App.prefs.token?.let { "Bearer $it" }
        RetrofitInstance.apiService.searchShorts(searchKeyword)
            .enqueue(object : Callback<List<ShortsSearchItem>> {
                override fun onResponse(call: Call<List<ShortsSearchItem>>, resp: Response<List<ShortsSearchItem>>) {
                    fullShortsList = resp.body().orEmpty()
                    renderShortsOnly()
                    maybeBuildTotal()
                }
                override fun onFailure(call: Call<List<ShortsSearchItem>>, t: Throwable) {
                    fullShortsList = emptyList()
                    renderShortsOnly()
                    maybeBuildTotal()
                }
            })
    }

    private fun maybeBuildTotal() {
        // 두 리스트 중 하나라도 바뀔 때마다 전체 피드 재빌드
        val blocks = buildAlternatingBlocks(fullRecipeList, fullShortsList)
        totalAdapter.submit(blocks)
        number.text = (fullRecipeList.size + fullShortsList.size).toString()
        updateEmptyView()
    }

    private fun renderRecipeOnly() {
        recipeOnlyAdapter.updateData(fullRecipeList)
    }

    private fun renderShortsOnly() {
        shortsOnlyAdapter.submit(fullShortsList)
    }

    /** 레시피 5개 블록, 쇼츠 4개(2×2) 블록 번갈아 배치 */
    private fun buildAlternatingBlocks(recipes: List<Recipe>, shorts: List<ShortsSearchItem>): List<TotalBlock> {
        val rChunks = recipes.chunked(5).map { RecipeBlock(it) }
        val sChunks = shorts.chunked(4).map { ShortsBlock(it) }

        val out = mutableListOf<TotalBlock>()
        var i = 0
        while (i < rChunks.size || i < sChunks.size) {
            if (i < rChunks.size) out += rChunks[i]
            if (i < sChunks.size) out += sChunks[i]
            i++
        }
        return out
    }

    private fun setupPopupMenu(arrowButton: ImageButton, targetTextView: TextView) {
        arrowButton.setOnClickListener {
            val popup = PopupMenu(this, arrowButton)
            popup.menuInflater.inflate(R.menu.recipe_result_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                sortOrder = when (menuItem.itemId) {
                    R.id.menu_view_count -> { targetTextView.text = "조회수순"; "viewCount" }
                    R.id.menu_likes ->     { targetTextView.text = "찜순";   "likes" }
                    R.id.menu_latest ->    { targetTextView.text = "최신순"; "latest" }
                    R.id.menu_cooking_time_short -> { targetTextView.text = "요리시간 짧은순"; "shortTime" }
                    R.id.menu_cooking_time_long  -> { targetTextView.text = "요리시간 긴순";   "longTime" }
                    else -> sortOrder
                }
                fetchAndRenderAll()
                true
            }
            popup.show()
        }
    }

    private fun setupCategoryButtons() {
        val allBtn = findViewById<AppCompatButton>(R.id.allBtn)
        val krBtn  = findViewById<AppCompatButton>(R.id.krBtn)
        val wsBtn  = findViewById<AppCompatButton>(R.id.wsBtn)
        val jpBtn  = findViewById<AppCompatButton>(R.id.jpBtn)
        val cnBtn  = findViewById<AppCompatButton>(R.id.cnBtn)
        val vgBtn  = findViewById<AppCompatButton>(R.id.vgBtn)
        val snBtn  = findViewById<AppCompatButton>(R.id.snBtn)
        val asBtn  = findViewById<AppCompatButton>(R.id.asBtn)
        val sdBtn  = findViewById<AppCompatButton>(R.id.sdBtn)
        val buttons = listOf(allBtn, krBtn, wsBtn, jpBtn, cnBtn, vgBtn, snBtn, asBtn, sdBtn)

        fun select(b: AppCompatButton) {
            buttons.forEach { btn ->
                if (btn == b) {
                    val d = GradientDrawable().apply {
                        cornerRadius = dp(40f); setColor(ContextCompat.getColor(this@SearchResultActivity, R.color.black))
                    }
                    btn.background = d; btn.setTextColor(Color.WHITE)
                } else {
                    btn.setBackgroundResource(R.drawable.btn_recipe_add)
                    btn.setTextColor(Color.parseColor("#8A8F9C"))
                }
            }
        }

        allBtn.setOnClickListener { select(allBtn); currentSelectedCategory = null; fetchAndRenderAll() }
        krBtn.setOnClickListener  { select(krBtn);  currentSelectedCategory = "koreaFood";      fetchAndRenderAll() }
        wsBtn.setOnClickListener  { select(wsBtn);  currentSelectedCategory = "westernFood";    fetchAndRenderAll() }
        jpBtn.setOnClickListener  { select(jpBtn);  currentSelectedCategory = "japaneseFood";   fetchAndRenderAll() }
        cnBtn.setOnClickListener  { select(cnBtn);  currentSelectedCategory = "chineseFood";    fetchAndRenderAll() }
        vgBtn.setOnClickListener  { select(vgBtn);  currentSelectedCategory = "vegetarianDiet"; fetchAndRenderAll() }
        snBtn.setOnClickListener  { select(snBtn);  currentSelectedCategory = "snack";          fetchAndRenderAll() }
        asBtn.setOnClickListener  { select(asBtn);  currentSelectedCategory = "alcoholSnack";   fetchAndRenderAll() }
        sdBtn.setOnClickListener  { select(sdBtn);  currentSelectedCategory = "sideDish";       fetchAndRenderAll() }

        select(allBtn)
    }

    private fun updateEmptyView() {
        val active = when {
            rvTotal.visibility == View.VISIBLE    -> totalAdapter.itemCount
            rvRecipesOnly.visibility == View.VISIBLE -> recipeOnlyAdapter.itemCount
            else -> shortsOnlyAdapter.itemCount
        }
        emptyView.visibility = if (active == 0) View.VISIBLE else View.GONE
    }

    private fun moveIndicatorTo(target: View) {
        val parent = indicatorBar.parent as? ConstraintLayout ?: return
        ensureChildIds(parent)
        val cs = ConstraintSet().apply { clone(parent) }
        cs.clear(indicatorBar.id, ConstraintSet.TOP)
        cs.connect(indicatorBar.id, ConstraintSet.TOP, target.id, ConstraintSet.BOTTOM, dp(13f).roundToInt())
        cs.applyTo(parent)
        indicatorBar.post {
            val targetCenter = target.x + target.width / 2f
            val barHalf = indicatorBar.width / 2f
            indicatorBar.animate().x(targetCenter - barHalf).setDuration(200L).start()
        }
    }
    private fun ensureChildIds(parent: ConstraintLayout) {
        for (i in 0 until parent.childCount) if (parent.getChildAt(i).id == View.NO_ID)
            parent.getChildAt(i).id = View.generateViewId()
    }
    private fun dp(v: Float) = v * density
}
