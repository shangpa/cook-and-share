package com.example.test

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.RecipeSearchAdapter
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<AppCompatButton>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeSearchAdapter
    private lateinit var numberTextView: TextView
    private lateinit var sortTextView: TextView
    private var currentSelectedCategory: String? = null
    private var sortOrder: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)

        // 카테고리 버튼 세팅
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

        allBtn.setOnClickListener { setCategoryButtonStyle(allBtn); filterByCategory(null) }
        krBtn.setOnClickListener { setCategoryButtonStyle(krBtn); filterByCategory("koreaFood") }
        wsBtn.setOnClickListener { setCategoryButtonStyle(wsBtn); filterByCategory("westernFood") }
        jpBtn.setOnClickListener { setCategoryButtonStyle(jpBtn); filterByCategory("japaneseFood") }
        cnBtn.setOnClickListener { setCategoryButtonStyle(cnBtn); filterByCategory("chineseFood") }
        vgBtn.setOnClickListener { setCategoryButtonStyle(vgBtn); filterByCategory("vegetarianDiet") }
        snBtn.setOnClickListener { setCategoryButtonStyle(snBtn); filterByCategory("snack") }
        asBtn.setOnClickListener { setCategoryButtonStyle(asBtn); filterByCategory("alcoholSnack") }
        sdBtn.setOnClickListener { setCategoryButtonStyle(sdBtn); filterByCategory("sideDish") }

        // 리사이클러뷰 세팅
        recyclerView = findViewById(R.id.searchResultRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecipeSearchAdapter(emptyList()) { recipe ->
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            intent.putExtra("recipeId", recipe.recipeId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // 갯수 텍스트뷰
        numberTextView = findViewById(R.id.number)

        // 정렬 텍스트뷰 & 드롭다운
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

        // 초기 검색 - 전체 레시피 표시
        setCategoryButtonStyle(allBtn)
        filterByCategory(null)

        val pencil = findViewById<ImageButton>(R.id.pencil)
        val recipeWrite = findViewById<TextView>(R.id.recipeWrite)

        val intent = Intent(this, RecipeWriteMain::class.java)

        pencil.setOnClickListener {
            startActivity(intent)
        }

        recipeWrite.setOnClickListener {
            startActivity(intent)
        }

    }

    private fun filterByCategory(category: String?) {
        currentSelectedCategory = category
        RetrofitInstance.apiService.getAllPublicRecipes(sort = sortOrder)
            .enqueue(object : Callback<List<Recipe>> {
                override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                    if (response.isSuccessful) {
                        val recipeList = response.body()?.filter {
                            category == null || it.category == category
                        } ?: emptyList()

                        adapter.updateData(recipeList)
                        numberTextView.text = recipeList.size.toString()
                    } else {
                        Toast.makeText(this@RecipeActivity, "레시피 불러오기 실패", Toast.LENGTH_SHORT).show()
                        numberTextView.text = "0"
                    }
                }

                override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                    Toast.makeText(this@RecipeActivity, "서버 통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    numberTextView.text = "0"
                }
            })
    }

    private fun setCategoryButtonStyle(selectedButton: AppCompatButton) {
        categoryButtons.forEach { button ->
            if (button == selectedButton) {
                val selectedDrawable = GradientDrawable().apply {
                    cornerRadius = dpToPx(40f)
                    setColor(ContextCompat.getColor(this@RecipeActivity, R.color.black))
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
