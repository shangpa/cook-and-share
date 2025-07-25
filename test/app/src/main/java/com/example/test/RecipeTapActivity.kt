package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Utils.TabBarUtils
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeTapActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<AppCompatButton>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeSearchAdapter
    private lateinit var sortTextView: TextView
    private var currentSelectedCategory: String? = null
    private var sortOrder: String? = null
    private lateinit var categoryButton: List<AppCompatButton>
    private var selectedButton: AppCompatButton? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_tap)

        TabBarUtils.setupTabBar(this)

        // searchIcon 클릭했을 때 SearchMainActivity 이동
        val searchIcon: ImageButton = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, SearchMainActivity::class.java)
            startActivity(intent)
        }

        // recipeCard 클릭했을 때 SearchMainActivity 이동
        val recipeCard: ConstraintLayout = findViewById(R.id.recipeCard)
        recipeCard.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            startActivity(intent)
        }

        // recipeCardTwo 클릭했을 때 SearchMainActivity 이동
        val recipeCardTwo: ConstraintLayout = findViewById(R.id.recipeCardTwo)
        recipeCardTwo.setOnClickListener {
            val intent = Intent(this, RecipeSeeActivity::class.java)
            startActivity(intent)
        }

        // recipeCardThree 클릭했을 때 SearchMainActivity 이동
        val recipeCardThree: ConstraintLayout = findViewById(R.id.recipeCardThree)
        recipeCardThree.setOnClickListener {
            val intent = Intent(this, RecipeSeeVideoActivity::class.java)
            startActivity(intent)
        }

        // circleRecipe 클릭했을 때 SearchMainActivity 이동
        val circleRecipe: TextView = findViewById(R.id.circleRecipe)
        circleRecipe.setOnClickListener {
            val intent = Intent(this, RecipeWriteMain::class.java)
            startActivity(intent)
        }

        val heartButton = findViewById<ImageButton>(R.id.heartButton)
        val heartButtonTwo = findViewById<ImageButton>(R.id.heartButtonTwo)
        val heartButtonThree = findViewById<ImageButton>(R.id.heartButtonThree)
        val allBtn = findViewById<AppCompatButton>(R.id.allBtn)
        val krBtn = findViewById<AppCompatButton>(R.id.krBtn)
        val wsBtn = findViewById<AppCompatButton>(R.id.wsBtn)
        val jpBtn = findViewById<AppCompatButton>(R.id.jpBtn)
        val cnBtn = findViewById<AppCompatButton>(R.id.cnBtn)
        val vgBtn = findViewById<AppCompatButton>(R.id.vgBtn)
        val snBtn = findViewById<AppCompatButton>(R.id.snBtn)
        val asBtn = findViewById<AppCompatButton>(R.id.asBtn)
        val sdBtn = findViewById<AppCompatButton>(R.id.sdBtn)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val writeRecipe = findViewById<ConstraintLayout>(R.id.writeRecipe)
        val writeRecipeAdd = findViewById<ConstraintLayout>(R.id.writeRecipeAdd)
        val writeIcon = findViewById<ImageButton>(R.id.writeIcon)
        val write = findViewById<TextView>(R.id.write)

        // 레시피 이거 어때요 카테고리 리스트
        categoryButton = listOf(
            findViewById(R.id.lateNightMeal),
            findViewById(R.id.rainsDay),
            findViewById(R.id.cool),
            findViewById(R.id.heat),
            findViewById(R.id.vegan),
            findViewById(R.id.superSimple)
        )

        //레시피 이거 어때요 카테고리 버튼 클릭시
        categoryButton.forEach { button ->
            button.setOnClickListener {
                setCategoryButtonStyleTwo(button)
            }
        }

        // 하트 버튼 초기 태그 세팅
        heartButton.tag = "filled"         // 하트 채워진거
        heartButtonTwo.tag = "empty"       // 하트 비어있는거
        heartButtonThree.tag = "empty"

        heartButton.setOnClickListener { toggleHeart(it as ImageButton) }
        heartButtonTwo.setOnClickListener { toggleHeart(it as ImageButton) }
        heartButtonThree.setOnClickListener { toggleHeart(it as ImageButton) }


        // 레시피 둘러보기 카테고리 버튼 세팅
        categoryButtons = listOf(allBtn, krBtn, wsBtn, jpBtn, cnBtn, vgBtn, snBtn, asBtn, sdBtn)

        allBtn.setOnClickListener { setCategoryButtonStyle(allBtn); filterByCategory(null) }
        krBtn.setOnClickListener { setCategoryButtonStyle(krBtn); filterByCategory("koreaFood") }
        wsBtn.setOnClickListener { setCategoryButtonStyle(wsBtn); filterByCategory("westernFood") }
        jpBtn.setOnClickListener { setCategoryButtonStyle(jpBtn); filterByCategory("japaneseFood") }
        cnBtn.setOnClickListener { setCategoryButtonStyle(cnBtn); filterByCategory("chineseFood") }
        vgBtn.setOnClickListener { setCategoryButtonStyle(vgBtn); filterByCategory("vegetarianDiet") }
        snBtn.setOnClickListener { setCategoryButtonStyle(snBtn); filterByCategory("snack") }
        asBtn.setOnClickListener { setCategoryButtonStyle(asBtn); filterByCategory("alcoholSnack") }
        sdBtn.setOnClickListener { setCategoryButtonStyle(sdBtn); filterByCategory("sideDish") }

        // 레시피 둘러보기 리사이클러뷰 세팅
        recyclerView = findViewById(R.id.searchResultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeSearchAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            intent.putExtra("recipeId", recipe.recipeId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 레시피 둘러보기 정렬 텍스트뷰 & 드롭다운
        sortTextView = findViewById(R.id.elementaryLevel)
        val sortButton: ImageButton = findViewById(R.id.dowArrow)

        sortButton.setOnClickListener {
            val popup = android.widget.PopupMenu(this, sortButton)
            popup.menuInflater.inflate(R.menu.recipe_result_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                sortOrder = when (menuItem.itemId) {
                    R.id.menu_view_count -> "viewCount"
                    R.id.menu_likes -> "likes"
                    R.id.menu_latest -> "latest"
                    R.id.menu_cooking_time_short -> "shortTime"
                    R.id.menu_cooking_time_long -> "longTime"
                    else -> null
                }
                sortTextView.text = menuItem.title
                filterByCategory(currentSelectedCategory)
                true
            }
            popup.show()
        }

        val initialCategory = intent.getStringExtra("selectedCategory")

        val categoryMap = mapOf(
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

        val selectedBtn = categoryMap[initialCategory] ?: allBtn
        setCategoryButtonStyle(selectedBtn)
        filterByCategory(initialCategory)


        // 화면 아래로 내리면 작성하기도 같이 내려감
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            writeRecipe.translationY = scrollY.toFloat()
            writeRecipeAdd.translationY = scrollY.toFloat()
        }

        // 작성 아이콘 누르면 레시피, 숏폼 작성 나타남
        val toggleWriteRecipeAdd = {
            writeRecipeAdd.visibility =
                if (writeRecipeAdd.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        writeRecipe.setOnClickListener { toggleWriteRecipeAdd() }
        writeIcon.setOnClickListener { toggleWriteRecipeAdd() }
        write.setOnClickListener { toggleWriteRecipeAdd() }

    }

    // 레시피 이건 어때요 카테고리 버튼
    private fun setCategoryButtonStyleTwo(selectedButton: AppCompatButton) {
        categoryButton.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(Color.parseColor("#35A825")) // 선택된 버튼 배경색
                }
                button.background = selectedDrawable
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add) // 기본 버튼 배경 복원
                button.setTextColor(Color.parseColor("#8A8F9C")) // 기본 글자색 복원
            }
        }
    }

    // 하트 버튼 클릭시
    private fun toggleHeart(button: ImageButton) {
        val tag = button.tag as? String
        if (tag == "filled") {
            button.setImageResource(R.drawable.ic_heart_list)
            button.tag = "empty"
        } else {
            button.setImageResource(R.drawable.ic_heart_fill)
            button.tag = "filled"
        }
    }

    private fun filterByCategory(category: String?) {
        val token = "Bearer ${App.prefs.token}"
        currentSelectedCategory = category
        RetrofitInstance.apiService.getAllPublicRecipes(token = token,sort = sortOrder)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (response.isSuccessful) {
                        val recipeList = response.body()?.filter {
                            category == null || it.category == category
                        } ?: emptyList()

                        adapter.updateData(recipeList)
                    } else {
                        Toast.makeText(this@RecipeTapActivity, "레시피 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@RecipeTapActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // 레시피 둘러보기 카테고리 버튼
    private fun setCategoryButtonStyle(selectedButton: AppCompatButton) {
        categoryButtons.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(ContextCompat.getColor(this@RecipeTapActivity, R.color.black))
                }
                button.background = selectedDrawable
                button.setTextColor(Color.WHITE)
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
