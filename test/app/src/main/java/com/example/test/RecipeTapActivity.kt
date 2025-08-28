package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.adapter.SuggestRecipeAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeTapActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<AppCompatButton> // “레시피 둘러보기” 카테고리 버튼들
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeSearchAdapter

    private lateinit var sortTextView: TextView
    private var currentSelectedCategory: String? = null
    private var sortOrder: String? = null

    // “레시피 이거 어때요?” 상단 카테고리 버튼들
    private lateinit var suggestButtons: List<AppCompatButton>

    // “레시피 이거 어때요?” 가로 리스트
    private lateinit var suggestRecycler: RecyclerView
    private lateinit var suggestAdapter: SuggestRecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_tap)

        TabBarUtils.setupTabBar(this)

        // 상단 우측 검색
        findViewById<ImageButton>(R.id.searchIcon).setOnClickListener {
            startActivity(Intent(this, SearchMainActivity::class.java))
        }

        // 레시피 작성 (플로팅)
        findViewById<TextView>(R.id.circleRecipe).setOnClickListener {
            startActivity(Intent(this, RecipeWriteMain::class.java))
        }

        // 숏츠 작성 (플로팅)
        findViewById<TextView>(R.id.circleShortVideo).setOnClickListener {
            startActivity(Intent(this, RecipeWriteOneMinuteActivity::class.java))
        }

        // 더보기 클릭시 숏츠로 이동
        findViewById<TextView>(R.id.add).setOnClickListener {
            startActivity(Intent(this, ShortsActivity::class.java))
        }

        // 더보기 클릭시 숏츠로 이동
        findViewById<TextView>(R.id.add).setOnClickListener {
            startActivity(Intent(this, ShortsActivity::class.java))
        }

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val writeRecipe = findViewById<ConstraintLayout>(R.id.writeRecipe)
        val writeRecipeAdd = findViewById<ConstraintLayout>(R.id.writeRecipeAdd)
        val writeIcon = findViewById<ImageButton>(R.id.writeIcon)
        val write = findViewById<TextView>(R.id.write)

        // 화면 스크롤 시 플로팅 같이 이동
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val y = scrollView.scrollY.toFloat()
            writeRecipe.translationY = y
            writeRecipeAdd.translationY = y
        }
        val toggleWrite = {
            writeRecipeAdd.visibility =
                if (writeRecipeAdd.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        writeRecipe.setOnClickListener { toggleWrite() }
        writeIcon.setOnClickListener { toggleWrite() }
        write.setOnClickListener { toggleWrite() }

        // --- “레시피 이거 어때요?” 영역 세팅 ---
        suggestRecycler = findViewById(R.id.suggestRecycler) // 레이아웃에 이 ID가 있어야 합니다.
        suggestRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        suggestAdapter = SuggestRecipeAdapter { recipe ->
            Intent(this, RecipeSeeMainActivity::class.java).apply {
                putExtra("recipeId", recipe.recipeId)
                startActivity(this)
            }
        }
        suggestRecycler.adapter = suggestAdapter

        // 상단 카테고리 버튼들 참조 및 클릭 연결
        val btnLate = findViewById<AppCompatButton>(R.id.lateNightMeal)
        val btnRain = findViewById<AppCompatButton>(R.id.rainsDay)
        val btnCool = findViewById<AppCompatButton>(R.id.cool)
        val btnHeat = findViewById<AppCompatButton>(R.id.heat)
        val btnVegan = findViewById<AppCompatButton>(R.id.vegan)
        val btnSimple = findViewById<AppCompatButton>(R.id.superSimple)

        suggestButtons = listOf(btnLate, btnRain, btnCool, btnHeat, btnVegan, btnSimple)

        fun selectSuggestButton(selected: AppCompatButton) {
            suggestButtons.forEach { b ->
                if (b == selected) {
                    val d = GradientDrawable().apply {
                        cornerRadius = dpToPx(40f)
                        setColor(Color.parseColor("#35A825"))
                    }
                    b.background = d
                    b.setTextColor(Color.WHITE)
                } else {
                    b.setBackgroundResource(R.drawable.btn_recipe_add)
                    b.setTextColor(Color.parseColor("#8A8F9C"))
                }
            }
        }

        btnLate.setOnClickListener { selectSuggestButton(btnLate); loadSuggest("lateNightMeal") }
        btnRain.setOnClickListener { selectSuggestButton(btnRain); loadSuggest("rainsDay") }
        btnCool.setOnClickListener { selectSuggestButton(btnCool); loadSuggest("cool") }
        btnHeat.setOnClickListener { selectSuggestButton(btnHeat); loadSuggest("heat") }
        btnVegan.setOnClickListener { selectSuggestButton(btnVegan); loadSuggest("vegan") }
        btnSimple.setOnClickListener { selectSuggestButton(btnSimple); loadSuggest("superSimple") }

        // 처음 진입 시 기본 선택
        selectSuggestButton(btnLate)
        loadSuggest("lateNightMeal")

        // --- 아래 “레시피 둘러보기” 리스트 세팅 ---
        recyclerView = findViewById(R.id.searchResultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeSearchAdapter(emptyList()) { recipe ->
            Intent(this, RecipeSeeMainActivity::class.java).apply {
                putExtra("recipeId", recipe.recipeId)
                startActivity(this)
            }
        }
        recyclerView.adapter = adapter

        // 정렬 팝업
        sortTextView = findViewById(R.id.elementaryLevel)
        val sortButton = findViewById<ImageButton>(R.id.dowArrow)
        sortButton.setOnClickListener {
            val popup = android.widget.PopupMenu(this, sortButton)
            popup.menuInflater.inflate(R.menu.recipe_result_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                sortOrder = when (item.itemId) {
                    R.id.menu_view_count -> "viewCount"
                    R.id.menu_likes -> "likes"
                    R.id.menu_latest -> "latest"
                    R.id.menu_cooking_time_short -> "shortTime"
                    R.id.menu_cooking_time_long -> "longTime"
                    else -> null
                }
                sortTextView.text = item.title
                filterByCategory(currentSelectedCategory)
                true
            }
            popup.show()
        }

        // “레시피 둘러보기” 카테고리 버튼들
        val allBtn = findViewById<AppCompatButton>(R.id.allBtn)
        val krBtn = findViewById<AppCompatButton>(R.id.krBtn)
        val wsBtn = findViewById<AppCompatButton>(R.id.wsBtn)
        val jpBtn = findViewById<AppCompatButton>(R.id.jpBtn)
        val cnBtn = findViewById<AppCompatButton>(R.id.cnBtn)
        val vgBtn = findViewById<AppCompatButton>(R.id.vgBtn)
        val snBtn = findViewById<AppCompatButton>(R.id.snBtn)
        val asBtn = findViewById<AppCompatButton>(R.id.asBtn)
        val sdBtn = findViewById<AppCompatButton>(R.id.sdBtn)

        categoryButtons = listOf(allBtn, krBtn, wsBtn, jpBtn, cnBtn, vgBtn, snBtn, asBtn, sdBtn)

        allBtn.setOnClickListener { setBrowseCategoryStyle(allBtn); filterByCategory(null) }
        krBtn.setOnClickListener { setBrowseCategoryStyle(krBtn); filterByCategory("koreaFood") }
        wsBtn.setOnClickListener { setBrowseCategoryStyle(wsBtn); filterByCategory("westernFood") }
        jpBtn.setOnClickListener { setBrowseCategoryStyle(jpBtn); filterByCategory("japaneseFood") }
        cnBtn.setOnClickListener { setBrowseCategoryStyle(cnBtn); filterByCategory("chineseFood") }
        vgBtn.setOnClickListener { setBrowseCategoryStyle(vgBtn); filterByCategory("vegetarianDiet") }
        snBtn.setOnClickListener { setBrowseCategoryStyle(snBtn); filterByCategory("snack") }
        asBtn.setOnClickListener { setBrowseCategoryStyle(asBtn); filterByCategory("alcoholSnack") }
        sdBtn.setOnClickListener { setBrowseCategoryStyle(sdBtn); filterByCategory("sideDish") }

        // 인텐트에서 초기 카테고리 받기
        val initialCategory: String? = intent.getStringExtra("selectedCategory")

        val map: Map<String?, AppCompatButton> = mapOf(
            null to allBtn,
            "koreaFood" to krBtn,
            "westernFood" to wsBtn,
            "japaneseFood" to jpBtn,
            "chineseFood" to cnBtn,
            "vegetarianDiet" to vgBtn,
            "snack" to snBtn,
            "alcoholSnack" to asBtn,
            "sideDish" to sdBtn
        )

        val initialBtn = map[initialCategory] ?: allBtn
        setBrowseCategoryStyle(initialBtn)
        filterByCategory(initialCategory)
    }

    // “레시피 둘러보기” 카테고리 버튼 스타일
    private fun setBrowseCategoryStyle(selected: AppCompatButton) {
        categoryButtons.forEach { b ->
            if (b == selected) {
                val d = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(ContextCompat.getColor(this@RecipeTapActivity, R.color.black))
                }
                b.background = d
                b.setTextColor(Color.WHITE)
            } else {
                b.setBackgroundResource(R.drawable.btn_recipe_add)
                b.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    // 공개 레시피 + 정렬 + 카테고리 필터
    private fun filterByCategory(category: String?) {
        val token: String = App.prefs.token?.let { "Bearer $it" } ?: ""
        currentSelectedCategory = category

        RetrofitInstance.apiService.getAllPublicRecipes(token = token, sort = sortOrder)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@RecipeTapActivity, "레시피 불러오기 실패(${response.code()})", Toast.LENGTH_SHORT).show()
                        adapter.updateData(emptyList())
                        return
                    }
                    val list = response.body().orEmpty()
                    val filtered = list.filter { category == null || it.category == category }
                    adapter.updateData(filtered)
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@RecipeTapActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    adapter.updateData(emptyList())
                }
            })
    }

    // “레시피 이거 어때요?” 추천 호출
    private fun loadSuggest(type: String) {
        val token: String = App.prefs.token?.let { "Bearer $it" } ?: ""
        RetrofitInstance.apiService.suggestRecipes(token = token, type = type)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@RecipeTapActivity, "추천 로드 실패(${response.code()})", Toast.LENGTH_SHORT).show()
                        suggestAdapter.submit(emptyList())
                        return
                    }
                    suggestAdapter.submit(response.body().orEmpty())
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@RecipeTapActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    suggestAdapter.submit(emptyList())
                }
            })
    }

    private fun dpToPx(dp: Float): Float = dp * resources.displayMetrics.density
}
