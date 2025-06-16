package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.adapter.MyWriteRecipeAdapter
import com.example.test.model.recipeDetail.MyWriteRecipe
import com.example.test.model.recipeDetail.MyWriteRecipeResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageWriteRecipeActivity : AppCompatActivity() {

    private var isCategoryVisible = false
    private var isMaterialVisible = false

    private lateinit var arrowIcon: ImageView
    private lateinit var categoryFood: LinearLayout
    private lateinit var cookDropdown: LinearLayout
    private lateinit var materialDropdown: LinearLayout
    private lateinit var categoryFoodText: TextView
    private lateinit var categoryMaterialText: TextView
    private lateinit var writeRecipeDropDownIcon: ImageView
    private lateinit var writeRecipefillterText: TextView
    private lateinit var writeRecipeNumber: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: MyWriteRecipeAdapter
    private var recipeList = listOf<MyWriteRecipe>()
    private val selectedCategoryButtons = mutableSetOf<Button>()
    private val selectedMaterialButtons = mutableSetOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_write_recipe)

        // UI 요소 연결
        arrowIcon = findViewById(R.id.writeRecipeDropDownIcon)
        categoryFood = findViewById(R.id.categoryFood)
        cookDropdown = findViewById(R.id.cook)
        categoryFoodText = categoryFood.getChildAt(0) as TextView
        writeRecipeDropDownIcon = findViewById(R.id.writeRecipeDropDownIcon)
        writeRecipefillterText = findViewById(R.id.writeRecipefillterText)
        writeRecipeNumber = findViewById(R.id.WriteRecipeNumber)
        recyclerView = findViewById(R.id.writeRecipeRecyclerView)

        // 리사이클러뷰 세팅
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyWriteRecipeAdapter(recipeList) { item -> showMorePopup(item) }
        recyclerView.adapter = adapter

        // 드롭다운(정렬)
        writeRecipeDropDownIcon.setOnClickListener {
            showSortPopup(it)
        }

        // 필터 드롭다운 열기
        categoryFood.setOnClickListener {
            isCategoryVisible = !isCategoryVisible
            cookDropdown.visibility = if (isCategoryVisible) View.VISIBLE else View.GONE
            arrowIcon.setImageResource(if (isCategoryVisible) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down_category_filter)
            updateCategoryStyle()
        }

        // 필터 버튼 초기화
        setupCategoryButtons()

        // 더 많은 레시피 작성하러 가기
        findViewById<LinearLayout>(R.id.btnRecipeMore).setOnClickListener {
            startActivity(Intent(this, RecipeWriteMain::class.java))
        }

        // 뒤로가기
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }

        // 서버에서 레시피 가져오기
        fetchMyRecipes()
    }

    private fun updateCategoryStyle() {
        if (!isCategoryVisible) {
            if (selectedCategoryButtons.isNotEmpty()) {
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

    private fun setupCategoryButtons() {
        val buttons = listOf(
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

        buttons.forEach { button ->
            button.setOnClickListener {
                if (button.id == R.id.all) {
                    // 전체 버튼 → 전체 선택 해제 + 스타일 초기화
                    selectedCategoryButtons.clear()
                    buttons.forEach {
                        it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                        it.setTextColor(Color.parseColor("#8A8F9C"))
                    }
                    // 전체 버튼 강조
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                    button.setTextColor(Color.WHITE)
                } else {
                    // 전체 버튼 스타일 비활성화
                    findViewById<Button>(R.id.all).apply {
                        setBackgroundResource(R.drawable.rounded_rectangle_background)
                        setTextColor(Color.parseColor("#8A8F9C"))
                    }

                    // 다중 선택 토글
                    if (selectedCategoryButtons.contains(button)) {
                        selectedCategoryButtons.remove(button)
                        button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                        button.setTextColor(Color.parseColor("#8A8F9C"))
                    } else {
                        selectedCategoryButtons.add(button)
                        button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                        button.setTextColor(Color.WHITE)
                    }
                }

                updateCategoryStyle()
                fetchMyRecipes()
            }
        }
    }

    private fun getSelectedFilters(): List<String> {
        val result = mutableListOf<String>()

        // 카테고리 다중 선택
        selectedCategoryButtons.forEach {
            if (it.text.toString() != "전체") {
                result.add(it.text.toString())
            }
        }

        // 재료 다중 선택
        selectedMaterialButtons.forEach {
            if (it.text.toString() != "전체") {
                result.add(it.text.toString())
            }
        }

        return result
    }

    private fun fetchMyRecipes() {
        val token = App.prefs.token ?: return
        val sort = when (writeRecipefillterText.text.toString()) {
            "조회수순" -> "views"
            "최신순" -> "latest"
            else -> "latest"
        }
        val filters = getSelectedFilters()

        RetrofitInstance.apiService.getMyRecipes("Bearer $token", sort, filters)
            .enqueue(object : Callback<MyWriteRecipeResponse> {
                override fun onResponse(call: Call<MyWriteRecipeResponse>, response: Response<MyWriteRecipeResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            recipeList = it.recipes
                            writeRecipeNumber.text = "${it.count}"
                            adapter = MyWriteRecipeAdapter(recipeList) { item -> showMorePopup(item) }
                            recyclerView.adapter = adapter
                        }
                    } else {
                        Log.e("작성레시피", "서버 응답 오류 ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MyWriteRecipeResponse>, t: Throwable) {
                    Log.e("작성레시피", "통신 실패: ${t.message}")
                }
            })
    }

    @OptIn(UnstableApi::class) private fun showMorePopup(item: MyWriteRecipe) {
        val popup = PopupMenu(this, writeRecipeDropDownIcon)
        popup.menu.add("수정")
        popup.menu.add("삭제")

        popup.setOnMenuItemClickListener {
            when (it.title) {
                "수정" -> {
                    val intent = Intent(this, RecipeWriteBothActivity::class.java)
                    intent.putExtra("recipeId", item.id) // 필요 시 ID 전달
                    startActivity(intent)
                }
                "삭제" -> {
                    val token = App.prefs.token ?: return@setOnMenuItemClickListener true
                    RetrofitInstance.apiService.deleteRecipe("Bearer $token", item.id)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@MypageWriteRecipeActivity, "삭제되었습니다", Toast.LENGTH_SHORT).show()
                                    fetchMyRecipes() // 목록 다시 불러오기
                                } else {
                                    Toast.makeText(this@MypageWriteRecipeActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(this@MypageWriteRecipeActivity, "에러: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
            true
        }
        popup.show()
    }

    private fun showSortPopup(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menu.add("조회수순")
        popupMenu.menu.add("최신순")
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            writeRecipefillterText.text = item.title
            fetchMyRecipes()
            true
        }
        popupMenu.show()
    }
}
