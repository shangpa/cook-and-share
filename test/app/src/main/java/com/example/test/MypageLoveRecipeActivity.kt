package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.MypageLoveRecipeAdapter
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.model.recipt.Image

class MypageLoveRecipeActivity : AppCompatActivity() {

    private var isCategoryVisible = false
    private var isMaterialVisible = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MypageLoveRecipeAdapter
    private lateinit var arrowIcon: ImageView
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryMaterial: LinearLayout
    private lateinit var cookDropdown: LinearLayout
    private lateinit var materialDropdown: LinearLayout
    private lateinit var categoryFoodText: TextView
    private lateinit var categoryMaterialText: TextView
    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private lateinit var emptyImage: ImageView
    private lateinit var emptyMessage: TextView
    private lateinit var countText: TextView
    private var currentSort: String = "최신순"
    private val selectedCategoryButtons = mutableSetOf<Button>()
    private val selectedMaterialButtons = mutableSetOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_love_recipe)

        categoryFood = findViewById(R.id.categoryFood)
        cookDropdown = findViewById(R.id.cook)
        arrowIcon = findViewById(R.id.arrowIcon)
        categoryFoodText = categoryFood.getChildAt(0) as TextView

        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)

        emptyImage = findViewById(R.id.emptyImage)
        emptyMessage = findViewById(R.id.emptyMessage)
        countText = findViewById(R.id.RecipeResultNumber)

        val categoryButtons = listOf(
            findViewById<Button>(R.id.all),
            findViewById(R.id.filterKorean),
            findViewById(R.id.filterWestern),
            findViewById(R.id.filterJapanese),
            findViewById(R.id.filteChinese),
            findViewById(R.id.filterVegetarian),
            findViewById(R.id.filterSnackF),
            findViewById(R.id.filterSnack),
            findViewById(R.id.filterSideDish)
        )

        categoryFood.setOnClickListener {
            isCategoryVisible = !isCategoryVisible
            cookDropdown.visibility = if (isCategoryVisible) View.VISIBLE else View.GONE
            arrowIcon.setImageResource(
                if (isCategoryVisible) R.drawable.ic_arrow_up
                else R.drawable.ic_arrow_down_category_filter
            )
            if (!isCategoryVisible) {
                if (selectedCategoryButtons.isEmpty()) {
                    categoryFood.setBackgroundResource(R.drawable.btn_fridge_ct)
                    categoryFoodText.setTextColor(Color.parseColor("#8A8F9C"))
                }
            } else {
                categoryFood.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                categoryFoodText.setTextColor(Color.WHITE)
            }
        }

        categoryButtons.forEach { button ->
            button.setOnClickListener {
                if (selectedCategoryButtons.contains(button)) {
                    selectedCategoryButtons.remove(button)
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    button.setTextColor(Color.parseColor("#8A8F9C"))
                } else {
                    selectedCategoryButtons.add(button)
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    button.setTextColor(Color.WHITE)
                }

                // 전체 선택이 눌렸다면 다른 버튼 모두 해제
                if (button.id == R.id.all) {
                    selectedCategoryButtons.clear()
                    selectedCategoryButtons.add(button)
                    categoryButtons.forEach {
                        it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                        it.setTextColor(Color.parseColor("#8A8F9C"))
                    }
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    button.setTextColor(Color.WHITE)
                } else {
                    selectedCategoryButtons.remove(findViewById(R.id.all))
                    findViewById<Button>(R.id.all).setBackgroundResource(R.drawable.rounded_rectangle_background)
                    findViewById<Button>(R.id.all).setTextColor(Color.parseColor("#8A8F9C"))
                }

                filterAndDisplayRecipes()
            }
        }

        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showSortPopup(it)
        }

        recyclerView = findViewById(R.id.loveRecipeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MypageLoveRecipeAdapter { newCount ->
            countText.text = newCount.toString()
            if (newCount == 0) {
                emptyImage.visibility = View.VISIBLE
                emptyMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyImage.visibility = View.GONE
                emptyMessage.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
        recyclerView.adapter = adapter

        loadLikedRecipes()

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun toggleMaterialButton(button: Button) {
        if (selectedMaterialButtons.contains(button)) {
            selectedMaterialButtons.remove(button)
            button.setBackgroundResource(R.drawable.rounded_rectangle_background)
            button.setTextColor(Color.parseColor("#8A8F9C"))
        } else {
            selectedMaterialButtons.add(button)
            button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            button.setTextColor(Color.WHITE)
        }

        if (selectedMaterialButtons.isEmpty()) {
            categoryMaterial.setBackgroundResource(R.drawable.btn_fridge_ct)
            categoryMaterialText.setTextColor(Color.parseColor("#8A8F9C"))
        } else {
            categoryMaterial.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
            categoryMaterialText.setTextColor(Color.WHITE)
        }
    }

    private fun showSortPopup(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.add("조회수순")
        popupMenu.menu.add("최신순")
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val selectedSort = item.title.toString()
            fridgeRecipefillterText.text = selectedSort
            currentSort = selectedSort
            sortAndDisplayRecipes()
            true
        }
        popupMenu.show()

        // btnRecipeMore 클릭했을 때 RecipeActivity 이동
        val btnRecipeMore: LinearLayout = findViewById(R.id.btnRecipeMore)
        btnRecipeMore.setOnClickListener {
            val intent = Intent(this, RecipeTapActivity::class.java)
            startActivity(intent)
        }

        // backButton 클릭했을 때 MypageActivity 이동
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadLikedRecipes() {
        val token = "Bearer ${App.prefs.token}"
        val api = RetrofitInstance.apiService

        api.getLikedRecipes(token).enqueue(object : Callback<List<RecipeDetailResponse>> {
            override fun onResponse(
                call: Call<List<RecipeDetailResponse>>,
                response: Response<List<RecipeDetailResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val recipes = response.body()!!
                    allRecipes = recipes
                    sortAndDisplayRecipes()
                    countText.text = recipes.size.toString()

                    if (recipes.isEmpty()) {
                        emptyImage.visibility = View.VISIBLE
                        emptyMessage.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyImage.visibility = View.GONE
                        emptyMessage.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }

                } else {
                    Toast.makeText(this@MypageLoveRecipeActivity, "찜 목록 로딩 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<RecipeDetailResponse>>, t: Throwable) {
                Toast.makeText(this@MypageLoveRecipeActivity, "서버 통신 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var allRecipes: List<RecipeDetailResponse> = emptyList()

    private fun sortAndDisplayRecipes() {
        filterAndDisplayRecipes()
    }

    companion object {
        private const val CATEGORY_ALL = "전체"
        private const val MATERIAL_ALL = "전체"
    }

    private fun filterAndDisplayRecipes() {
        val selectedCategories = selectedCategoryButtons.map { it.text.toString() }
        val selectedMaterials = selectedMaterialButtons.map { it.text.toString() }

        val categoryMap = mapOf(
            "한식" to "koreaFood",
            "양식" to "westernFood",
            "일식" to "japaneseFood",
            "중식" to "chineseFood",
            "채식" to "vegetarianDiet",
            "간식" to "snack",
            "안주" to "alcoholSnack",
            "밑반찬" to "sideDish"
        )

        val filtered = allRecipes.filter { recipe ->
            val matchCategory = if (selectedCategories.isEmpty() || selectedCategories.contains("전체")) {
                true
            } else {
                selectedCategories.any { ct ->
                    categoryMap[ct] == recipe.category
                }
            }

            val matchMaterial = if (selectedMaterials.isEmpty() || selectedMaterials.contains("전체")) {
                true
            } else {
                selectedMaterials.any { mt ->
                    recipe.ingredients.contains(mt)
                }
            }

            matchCategory && matchMaterial
        }

        val sorted = when (currentSort) {
            "조회수순" -> filtered.sortedByDescending { it.viewCount ?: 0 }
            else -> filtered.sortedByDescending { it.createdAt }
        }

        adapter.setRecipeList(sorted)
        countText.text = sorted.size.toString()

        if (sorted.isEmpty()) {
            emptyImage.visibility = View.VISIBLE
            emptyMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyImage.visibility = View.GONE
            emptyMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
