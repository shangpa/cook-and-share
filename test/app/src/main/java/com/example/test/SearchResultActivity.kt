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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        TabBarUtils.setupTabBar(this)

        val dowArrow: ImageButton = findViewById(R.id.dowArrow)
        val elementaryLevel: TextView = findViewById(R.id.elementaryLevel)

        // 드롭다운 메뉴 클릭 처리
        dowArrow.setOnClickListener {
            val popup = PopupMenu(this, dowArrow)
            popup.menuInflater.inflate(R.menu.recipe_result_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                sortOrder = when (menuItem.itemId) {
                    R.id.menu_view_count -> {
                        elementaryLevel.text = "조회수순"
                        "viewCount"
                    }
                    R.id.menu_likes -> {
                        elementaryLevel.text = "찜순"
                        "likes"
                    }
                    R.id.menu_latest -> {
                        elementaryLevel.text = "최신순"
                        "latest"
                    }
                    R.id.menu_cooking_time_short -> {
                        elementaryLevel.text = "요리시간 짧은순"
                        "shortTime"
                    }
                    R.id.menu_cooking_time_long -> {
                        elementaryLevel.text = "요리시간 긴순"
                        "longTime"
                    }
                    else -> null
                }

                filterByCategory(currentSelectedCategory)
                true
            }

            popup.show()
        }

        searchKeyword = intent.getStringExtra("searchKeyword") ?: ""
        val searchText: EditText = findViewById(R.id.writeSearchTxt)
        searchText.setText(searchKeyword)

        findViewById<ImageButton>(R.id.SearchResultBackIcon).setOnClickListener {
            startActivity(Intent(this, SearchMainActivity::class.java))
            finish()
        }

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
    }

    private fun setCategoryButtonStyle(selectedButton: AppCompatButton) {
        categoryButtons.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
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

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
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
}
