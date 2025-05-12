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
import com.example.test.adapter.LikeRecipeAdapter
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageLoveRecipeActivity : AppCompatActivity() {

    private var isCategoryVisible = false
    private var isMaterialVisible = false
    private val categoryMap = mapOf(
        "전체" to "",
        "한식" to "koreaFood",
        "양식" to "westernFood",
        "일식" to "japaneseFood",
        "중식" to "chineseFood",
        "채식" to "vegetarianFood",
        "간식" to "snack",
        "안주" to "alcoholFood",
        "밑반찬" to "sideDish"
    )

    private lateinit var arrowIcon: ImageView
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryMaterial: LinearLayout
    private lateinit var cookDropdown: LinearLayout
    private lateinit var materialDropdown: LinearLayout
    private lateinit var categoryFoodText: TextView
    private lateinit var categoryMaterialText: TextView
    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private lateinit var loveRecipeAdapter: LikeRecipeAdapter
    private lateinit var loveRecipeRecyclerView: RecyclerView
    private var allLikedRecipes: List<RecipeDetailResponse> = emptyList()

    private var selectedButton: Button? = null
    private val selectedMaterialButtons = mutableSetOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_love_recipe)

        categoryFood = findViewById(R.id.categoryFood)
        cookDropdown = findViewById(R.id.cook)
        arrowIcon = findViewById(R.id.arrowIcon)
        categoryFoodText = categoryFood.getChildAt(0) as TextView

        categoryMaterial = findViewById(R.id.categoryMaterial)
        materialDropdown = findViewById(R.id.material)
        categoryMaterialText = categoryMaterial.getChildAt(0) as TextView

        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)

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

        val materialButtons = listOf(
            findViewById<Button>(R.id.alll),
            findViewById(R.id.grain),
            findViewById(R.id.fruit),
            findViewById(R.id.vegetable),
            findViewById(R.id.meat),
            findViewById(R.id.dairy),
            findViewById(R.id.seafood),
            findViewById(R.id.condiment),
            findViewById(R.id.mushroom),
            findViewById(R.id.additive),
            findViewById(R.id.processed),
            findViewById(R.id.favorite)
        )

        categoryFood.setOnClickListener {
            isCategoryVisible = !isCategoryVisible
            cookDropdown.visibility = if (isCategoryVisible) View.VISIBLE else View.GONE
            arrowIcon.setImageResource(
                if (isCategoryVisible) R.drawable.ic_arrow_up
                else R.drawable.ic_arrow_down_category_filter
            )
            if (!isCategoryVisible) {
                if (selectedButton != null) {
                    categoryFood.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                    categoryFoodText.setTextColor(Color.WHITE)
                } else {
                    categoryFood.setBackgroundResource(R.drawable.btn_fridge_ct)
                    categoryFoodText.setTextColor(Color.parseColor("#8A8F9C"))
                }
            } else {
                categoryFood.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                categoryFoodText.setTextColor(Color.WHITE)
            }
        }

        categoryMaterial.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            materialDropdown.bringToFront()
            materialDropdown.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            categoryMaterial.getChildAt(1).apply {
                if (this is ImageView) {
                    setImageResource(
                        if (isMaterialVisible) R.drawable.ic_arrow_up
                        else R.drawable.ic_arrow_down_category_filter
                    )
                }
            }
            if (!isMaterialVisible) {
                if (selectedMaterialButtons.isEmpty()) {
                    categoryMaterial.setBackgroundResource(R.drawable.btn_fridge_ct)
                    categoryMaterialText.setTextColor(Color.parseColor("#8A8F9C"))
                }
            } else {
                categoryMaterial.setBackgroundResource(R.drawable.btn_fridge_ct_selected)
                categoryMaterialText.setTextColor(Color.WHITE)
            }
        }

        categoryButtons.forEach { button ->
            button.setOnClickListener {
                selectedButton = if (selectedButton == button) null else button
                categoryButtons.forEach {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    it.setTextColor(Color.parseColor("#8A8F9C"))
                }
                selectedButton?.let {
                    it.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    it.setTextColor(Color.WHITE)
                }
                //필터 적용
                val selectedCategory = button.text.toString()
                filterRecipesByCategory(selectedCategory)
            }

        }

        materialButtons.forEach { button ->
            button.setOnClickListener {
                toggleMaterialButton(button)
            }
        }

        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showSortPopup(it)
        }
        // RecyclerView 연결
        loveRecipeRecyclerView = findViewById(R.id.loveRecipeRecyclerView)
        loveRecipeRecyclerView.layoutManager = LinearLayoutManager(this)
        loveRecipeAdapter = LikeRecipeAdapter(this, emptyList()) { recipeId ->
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            startActivity(intent)
        }
        loveRecipeRecyclerView.adapter = loveRecipeAdapter

        // 서버에서 찜한 레시피 불러오기
        loadLikedRecipes()
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
        popupMenu.menu.add("별점순")
        popupMenu.menu.add("최신순")
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            fridgeRecipefillterText.text = item.title
            true
        }
        popupMenu.show()

        // btnRecipeMore 클릭했을 때 RecipeActivity 이동
        val btnRecipeMore: LinearLayout = findViewById(R.id.btnRecipeMore)
        btnRecipeMore.setOnClickListener {
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        // backButton 클릭했을 때 MypageActivity 이동
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
    //좋아요한 레시피 조회
    private fun loadLikedRecipes() {
        val token = "Bearer ${App.prefs.token.toString()}"
        val api = RetrofitInstance.apiService

        api.getLikedRecipes(token).enqueue(object : Callback<List<RecipeDetailResponse>> {
            override fun onResponse(
                call: Call<List<RecipeDetailResponse>>,
                response: Response<List<RecipeDetailResponse>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipes ->
                        allLikedRecipes = recipes
                        loveRecipeAdapter.updateData(recipes)
                        findViewById<TextView>(R.id.fridgeRecipeResultNumber).text = recipes.size.toString()
                    }
                } else {
                    Toast.makeText(this@MypageLoveRecipeActivity, "레시피 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<RecipeDetailResponse>>, t: Throwable) {
                Toast.makeText(this@MypageLoveRecipeActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }
    //카테고리 필터
    private fun filterRecipesByCategory(category: String?) {
        val categoryCode = categoryMap[category] ?: ""

        val filteredList = if (categoryCode.isEmpty()) {
            allLikedRecipes
        } else {
            allLikedRecipes.filter { it.category == categoryCode }
        }

        loveRecipeAdapter.updateData(filteredList)
        findViewById<TextView>(R.id.fridgeRecipeResultNumber).text = filteredList.size.toString()
    }

}
