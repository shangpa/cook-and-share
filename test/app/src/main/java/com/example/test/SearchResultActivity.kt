package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class SearchResultActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeSearchAdapter
    private lateinit var number: TextView
    private lateinit var emptyImage: ImageView
    private lateinit var emptyText: TextView
    private lateinit var searchKeyword: String
    private lateinit var categoryButtons: List<AppCompatButton>
    private var sortOrder: String? = null
    private var currentSelectedCategory: String? = null
    private lateinit var totalTap: TextView
    private lateinit var recipeTap: TextView
    private lateinit var videoTap: TextView
    private lateinit var totalLayout: View
    private lateinit var recipeLayout: View
    private lateinit var videoLayout: View
    private val ACTIVE   = Color.parseColor("#000000")
    private val INACTIVE = Color.parseColor("#8A8A8A")
    private lateinit var indicatorBar: View
    private val density: Float get() = resources.displayMetrics.density

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        TabBarUtils.setupTabBar(this)

        val dowArrow: ImageButton = findViewById(R.id.dowArrow)
        val elementaryLevel: TextView = findViewById(R.id.elementaryLevel)
        val dowArrowTwo: ImageButton = findViewById(R.id.dowArrowTwo)
        val elementaryLevelTwo: TextView = findViewById(R.id.elementaryLevelTwo)
        val dowArrowThree: ImageButton = findViewById(R.id.dowArrowThree)
        val elementaryLevelThree: TextView = findViewById(R.id.elementaryLevelThree)
        val allBtn = findViewById<AppCompatButton>(R.id.allBtn)
        val krBtn = findViewById<AppCompatButton>(R.id.krBtn)
        val wsBtn = findViewById<AppCompatButton>(R.id.wsBtn)
        val jpBtn = findViewById<AppCompatButton>(R.id.jpBtn)
        val cnBtn = findViewById<AppCompatButton>(R.id.cnBtn)
        val vgBtn = findViewById<AppCompatButton>(R.id.vgBtn)
        val snBtn = findViewById<AppCompatButton>(R.id.snBtn)
        val asBtn = findViewById<AppCompatButton>(R.id.asBtn)
        val sdBtn = findViewById<AppCompatButton>(R.id.sdBtn)
        totalTap   = findViewById(R.id.totalTap)
        recipeTap  = findViewById(R.id.recipeTap)
        videoTap   = findViewById(R.id.videoTap)
        totalLayout  = findViewById(R.id.totalLayout)
        recipeLayout = findViewById(R.id.recipeLayout)
        videoLayout  = findViewById(R.id.videoLayout)
        indicatorBar   = findViewById(R.id.indicatorBar)
        val videoSeeLayout = findViewById<ConstraintLayout>(R.id.videoSeeLayout)
        val videoSeeLayoutTwo = findViewById<ConstraintLayout>(R.id.videoSeeLayoutTwo)
        val recipeSeeLayout = findViewById<ConstraintLayout>(R.id.recipeSeeLayout)
        val recipeSeeLayoutTwo = findViewById<ConstraintLayout>(R.id.recipeSeeLayoutTwo)
        val recipeSeeLayoutThree = findViewById<ConstraintLayout>(R.id.recipeSeeLayoutThree)
        val recipeSeeLayoutFour = findViewById<ConstraintLayout>(R.id.recipeSeeLayoutFour)
        val recipeSeeLayoutFive = findViewById<ConstraintLayout>(R.id.recipeSeeLayoutFive)
        val recipeSeeLayoutSix = findViewById<ConstraintLayout>(R.id.recipeSeeLayoutSix)
        val totalRecipeSeeLayout = findViewById<ConstraintLayout>(R.id.totalRecipeSeeLayout)
        val totalRecipeSeeLayoutTwo = findViewById<ConstraintLayout>(R.id.totalRecipeSeeLayoutTwo)
        val totalRecipeSeeLayoutThree = findViewById<ConstraintLayout>(R.id.totalRecipeSeeLayoutThree)
        val recipeListLayout = findViewById<ConstraintLayout>(R.id.recipeListLayout)
        val recipeListLayoutTwo = findViewById<ConstraintLayout>(R.id.recipeListLayoutTwo)
        val recipeListLayoutThree = findViewById<ConstraintLayout>(R.id.recipeListLayoutThree)
        val recipeListLayoutFour = findViewById<ConstraintLayout>(R.id.recipeListLayoutFour)
        val recipeListLayoutFive = findViewById<ConstraintLayout>(R.id.recipeListLayoutFive)
        val recipeListLayoutSix = findViewById<ConstraintLayout>(R.id.recipeListLayoutSix)
        val recipeListLayoutSeven = findViewById<ConstraintLayout>(R.id.recipeListLayoutSeven)
        val recipeListLayoutEight = findViewById<ConstraintLayout>(R.id.recipeListLayoutEight)
        val heartIcon = findViewById<ImageView>(R.id.heartIcon)
        val heartIconTwo = findViewById<ImageView>(R.id.heartIconTwo)
        val heartIconThree = findViewById<ImageView>(R.id.heartIconThree)
        val recipeListHeartIcon = findViewById<ImageView>(R.id.recipeListHeartIcon)
        val recipeListHeartIconTwo = findViewById<ImageView>(R.id.recipeListHeartIconTwo)
        val recipeListHeartIconThree = findViewById<ImageView>(R.id.recipeListHeartIconThree)
        val recipeListHeartIconFour = findViewById<ImageView>(R.id.recipeListHeartIconFour)
        val recipeListHeartIconFive = findViewById<ImageView>(R.id.recipeListHeartIconFive)
        val recipeListHeartIconSix = findViewById<ImageView>(R.id.recipeListHeartIconSix)
        val recipeListHeartIconSeven = findViewById<ImageView>(R.id.recipeListHeartIconSeven)
        val recipeListHeartIconEight = findViewById<ImageView>(R.id.recipeListHeartIconEight)
        val profileLayoutOne = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayout)
        val profileLayoutTwo = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayoutTwo)
        val profileLayoutThree = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayoutThree)
        val profileLayoutFour = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayoutFour)
        val profileLayoutFive = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayoutFive)
        val profileLayoutSix = findViewById<ConstraintLayout>(R.id.recipeSeeProfileLayoutSix)

        fun show(index: Int) {
            totalLayout.visibility  = if (index == 0) View.VISIBLE else View.GONE
            recipeLayout.visibility = if (index == 1) View.VISIBLE else View.GONE
            videoLayout.visibility  = if (index == 2) View.VISIBLE else View.GONE

            totalTap.setTextColor(if (index == 0) ACTIVE else INACTIVE)
            recipeTap.setTextColor(if (index == 1) ACTIVE else INACTIVE)
            videoTap.setTextColor(if (index == 2) ACTIVE else INACTIVE)

            val target = when (index) {
                0 -> totalTap
                1 -> recipeTap
                else -> videoTap
            }
            moveIndicatorTo(target)
        }

        totalTap.setOnClickListener  { show(0) }
        recipeTap.setOnClickListener { show(1) }
        videoTap.setOnClickListener  { show(2) }

        show(0)

        // 드롭다운 메뉴 클릭 처리
        setupPopupMenu(dowArrow, elementaryLevel)
        setupPopupMenu(dowArrowTwo, elementaryLevelTwo)
        setupPopupMenu(dowArrowThree, elementaryLevelThree)

        searchKeyword = intent.getStringExtra("searchKeyword") ?: ""
        val searchText: EditText = findViewById(R.id.writeSearchTxt)
        searchText.setText(searchKeyword)

        recyclerView = findViewById(R.id.searchResultRecyclerView)
        number = findViewById(R.id.number)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyImage = findViewById(R.id.searchEmptyImage)
        emptyText = findViewById(R.id.searchEmptyText)
        adapter = RecipeSearchAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            intent.putExtra("recipeId", recipe.recipeId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        categoryButtons = listOf(allBtn, krBtn, wsBtn, jpBtn, cnBtn, vgBtn, snBtn, asBtn, sdBtn)

        // 카테고리 클릭 처리
        allBtn.setOnClickListener { setCategoryButtonStyle(allBtn); filterByCategory(null) }
        krBtn.setOnClickListener { setCategoryButtonStyle(krBtn); filterByCategory("koreaFood") }
        wsBtn.setOnClickListener { setCategoryButtonStyle(wsBtn); filterByCategory("westernFood") }
        jpBtn.setOnClickListener { setCategoryButtonStyle(jpBtn); filterByCategory("japaneseFood") }
        cnBtn.setOnClickListener { setCategoryButtonStyle(cnBtn); filterByCategory("chineseFood") }
        vgBtn.setOnClickListener { setCategoryButtonStyle(vgBtn); filterByCategory("vegetarianDiet") }
        snBtn.setOnClickListener { setCategoryButtonStyle(snBtn); filterByCategory("snack") }
        asBtn.setOnClickListener { setCategoryButtonStyle(asBtn); filterByCategory("alcoholSnack") }
        sdBtn.setOnClickListener { setCategoryButtonStyle(sdBtn); filterByCategory("sideDish") }

        setCategoryButtonStyle(allBtn)
        filterByCategory(null)

        findViewById<ImageButton>(R.id.searchIcon).setOnClickListener {
            val keyword = searchText.text.toString().trim()
            if (keyword.isNotBlank()) {
                searchKeyword = keyword
                setCategoryButtonStyle(allBtn)
                filterByCategory(null)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        if (searchKeyword.isNotBlank()) {
            RetrofitInstance.apiService.saveSearchKeyword(searchKeyword)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        Log.d("SearchResult", "검색어 저장 성공")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("SearchResult", "검색어 저장 실패", t)
                    }
                })
        }

        // 숏츠 클릭시 ShortsActivity로 넘어감
        val goToShorts = View.OnClickListener {
            val intent = Intent(this, ShortsActivity::class.java)
            startActivity(intent)
        }

        val clickableLayouts = listOf(
            findViewById<ConstraintLayout>(R.id.videoSeeLayout),
            findViewById<ConstraintLayout>(R.id.videoSeeLayoutTwo),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayout),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayoutTwo),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayoutThree),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayoutFour),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayoutFive),
            findViewById<ConstraintLayout>(R.id.recipeSeeLayoutSix)
        )

        clickableLayouts.forEach { layout ->
            layout.setOnClickListener(goToShorts)
        }

        findViewById<ImageView>(R.id.SearchResultBackIcon).setOnClickListener {
            finish()
        }

        // 하트 아이콘 클릭시 채워진 하트로 바뀜
        fun setupHeartToggle(imageView: ImageView) {
            var isLiked = false
            imageView.setOnClickListener {
                isLiked = !isLiked
                if (isLiked) {
                    imageView.setImageResource(R.drawable.ic_heart_fill) // 채워진 하트
                } else {
                    imageView.setImageResource(R.drawable.image_search_result_list_heart) // 원래 하트
                }
            }
        }

        listOf(
            heartIcon, heartIconTwo, heartIconThree,
            recipeListHeartIcon, recipeListHeartIconTwo, recipeListHeartIconThree,
            recipeListHeartIconFour, recipeListHeartIconFive, recipeListHeartIconSix,
            recipeListHeartIconSeven, recipeListHeartIconEight
        ).forEach(::setupHeartToggle)

        // 동영상 리스트 클릭시 RecipeSeeMainActivity로 넘어감
        val goToRecipeSee = View.OnClickListener {
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            startActivity(intent)
        }

        listOfNotNull(
            totalRecipeSeeLayout,
            totalRecipeSeeLayoutTwo,
            totalRecipeSeeLayoutThree,
            recipeListLayout,
            recipeListLayoutTwo,
            recipeListLayoutThree,
            recipeListLayoutFour,
            recipeListLayoutFive,
            recipeListLayoutSix,
            recipeListLayoutSeven,
            recipeListLayoutEight
        ).forEach { it.setOnClickListener(goToRecipeSee) }

        //동영상 리스트에서 프로필 클릭시 이동
        val goToProfile = View.OnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        }

        listOf(
            profileLayoutOne,
            profileLayoutTwo,
            profileLayoutThree,
            profileLayoutFour,
            profileLayoutFive,
            profileLayoutSix
        ).forEach { layout ->
            layout.setOnClickListener(goToProfile)
        }
    }

    private fun setCategoryButtonStyle(selectedButton: AppCompatButton) {
        categoryButtons.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dp(40f)
                    setColor(ContextCompat.getColor(this@SearchResultActivity, R.color.black))
                }
                button.background = selectedDrawable
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    private fun filterByCategory(category: String?) {
        currentSelectedCategory = category
        val token = "Bearer ${App.prefs.token}"
        RetrofitInstance.apiService.searchRecipes(title = searchKeyword, sort = sortOrder,token = token,)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (response.isSuccessful) {
                        val recipeList = response.body()?.filter {
                            category == null || it.category == category
                        } ?: emptyList()

                        adapter.updateData(recipeList)
                        number.text = recipeList.size.toString()

                        if (recipeList.isEmpty()) {
                            emptyImage.visibility = View.VISIBLE
                            emptyText.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            emptyImage.visibility = View.GONE
                            emptyText.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@SearchResultActivity, "검색 실패", Toast.LENGTH_SHORT).show()
                        number.text = "0"
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@SearchResultActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    number.text = "0"
                }
            })
    }

    private fun setupPopupMenu(arrowButton: ImageButton, targetTextView: TextView) {
        arrowButton.setOnClickListener {
            val popup = PopupMenu(this, arrowButton)
            popup.menuInflater.inflate(R.menu.recipe_result_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                sortOrder = when (menuItem.itemId) {
                    R.id.menu_view_count -> {
                        targetTextView.text = "조회수순"
                        "viewCount"
                    }
                    R.id.menu_likes -> {
                        targetTextView.text = "찜순"
                        "likes"
                    }
                    R.id.menu_latest -> {
                        targetTextView.text = "최신순"
                        "latest"
                    }
                    R.id.menu_cooking_time_short -> {
                        targetTextView.text = "요리시간 짧은순"
                        "shortTime"
                    }
                    R.id.menu_cooking_time_long -> {
                        targetTextView.text = "요리시간 긴순"
                        "longTime"
                    }
                    else -> null
                }
                filterByCategory(currentSelectedCategory)
                true
            }

            popup.show()
        }
    }

    // 👇 추가
    private fun ensureChildIds(parent: ConstraintLayout) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.id == View.NO_ID) child.id = View.generateViewId()
        }
    }


    private fun moveIndicatorTo(target: View) {
        val parent = indicatorBar.parent as? ConstraintLayout
            ?: error("indicatorBar의 parent는 ConstraintLayout이어야 합니다.")

        // 🔒 ConstraintSet 쓰기 전에 반드시 id 보장
        ensureChildIds(parent)

        val cs = ConstraintSet()
        cs.clone(parent)

        cs.clear(indicatorBar.id, ConstraintSet.TOP)
        cs.connect(
            indicatorBar.id,
            ConstraintSet.TOP,
            target.id,
            ConstraintSet.BOTTOM,
            dp(13f).roundToInt()
        )

        cs.applyTo(parent)

        // 가로 가운데 정렬 애니메이션
        indicatorBar.post {
            val targetCenter = target.x + target.width / 2f
            val barHalf = indicatorBar.width / 2f
            indicatorBar.animate()
                .x(targetCenter - barHalf)
                .setDuration(200L)
                .start()
        }
    }

    private fun dp(value: Float): Float = value * density

}
