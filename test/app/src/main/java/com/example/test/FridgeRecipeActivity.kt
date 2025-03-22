package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.Toast

class FridgeRecipeActivity : AppCompatActivity() {

    // 재귀 함수를 통해 모든 하위 TextView의 글자색을 변경하는 함수
    private fun setTextColorRecursively(view: View, color: Int) {
        if (view is TextView) {
            view.setTextColor(color)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setTextColorRecursively(view.getChildAt(i), color)
            }
        }
    }

    // XML의 onClick 속성에서 호출될 toggleHeart 메서드
    // 이 메서드는 public이어야 하며, 인자로 View를 받습니다.
    fun toggleHeart(view: View) {
        if (view is ImageView) {
            val isFilled = view.tag as? Boolean ?: false
            if (isFilled) {
                view.setImageResource(R.drawable.ic_heart_recipe_list)
                view.tag = false
            } else {
                view.setImageResource(R.drawable.ic_favorite)
                view.tag = true
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_recipe)

        // --- 카테고리 버튼 처리 ---
        val fridgeCategoryAllBtn: LinearLayout = findViewById(R.id.fridgeCategoryAllBtn)
        val fridgeCategoryColdBtn: LinearLayout = findViewById(R.id.fridgeCategoryColdBtn)
        val fridgeCategoryFreezeBtn: LinearLayout = findViewById(R.id.fridgeCategoryFreezeBtn)
        val fridgeCategoryOutBtn: LinearLayout = findViewById(R.id.fridgeCategoryOutBtn)

        val fridgeCategoryAllText: TextView = findViewById(R.id.fridgeCategoryAllText)
        val fridgeCategoryColdText: TextView = findViewById(R.id.fridgeCategoryColdText)
        val fridgeCategoryFreezeText: TextView = findViewById(R.id.fridgeCategoryFreezeText)
        val fridgeCategoryOutText: TextView = findViewById(R.id.fridgeCategoryOutText)

        val categories = listOf(
            Pair(fridgeCategoryAllBtn, fridgeCategoryAllText),
            Pair(fridgeCategoryColdBtn, fridgeCategoryColdText),
            Pair(fridgeCategoryFreezeBtn, fridgeCategoryFreezeText),
            Pair(fridgeCategoryOutBtn, fridgeCategoryOutText)
        )

        categories.forEach { (layout, textView) ->
            layout.setOnClickListener {
                categories.forEach { (otherLayout, otherText) ->
                    otherLayout.setBackgroundResource(R.drawable.btn_fridge_ct)
                    otherText.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }

        // --- 개별 아이템 및 전체 선택 처리 ---
        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        val fridgeRecipeItem: LinearLayout = findViewById(R.id.fridegeRecipeItem)

        // 각 자식 뷰에 대해 초기 선택 상태(false) 및 onClickListener 등록
        for (i in 0 until fridgeRecipeItem.childCount) {
            val child = fridgeRecipeItem.getChildAt(i)
            child.tag = false // 초기 선택 상태: false (미선택)
            child.setOnClickListener {
                val selected = child.tag as Boolean
                if (!selected) {
                    child.setBackgroundResource(R.drawable.rounded_rectangle_fridge_green)
                    setTextColorRecursively(child, Color.WHITE)
                    child.tag = true
                } else {
                    child.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    setTextColorRecursively(child, Color.parseColor("#8A8F9C"))
                    child.tag = false
                }
            }
        }

        // 전체 선택 아이콘 클릭 시 전체 토글 처리
        var isAllChecked = false
        fridgeAllCheckIcon.setOnClickListener {
            isAllChecked = !isAllChecked
            for (i in 0 until fridgeRecipeItem.childCount) {
                val child = fridgeRecipeItem.getChildAt(i)
                if (isAllChecked) {
                    child.setBackgroundResource(R.drawable.rounded_rectangle_fridge_green)
                    setTextColorRecursively(child, Color.WHITE)
                    child.tag = true
                } else {
                    child.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    setTextColorRecursively(child, Color.parseColor("#8A8F9C"))
                    child.tag = false
                }
            }
            if (isAllChecked) {
                fridgeAllCheckIcon.setImageResource(R.drawable.btn_fridge_checked)
            } else {
                fridgeAllCheckIcon.setImageResource(R.drawable.ic_fridge_check)
            }
        }

        // --- 드롭다운 버튼 및 기타 메뉴 처리 ---

        fun toggleHeart(view: View) {
            if (view is ImageView) {
                val isFilled = view.tag as? Boolean ?: false
                if (isFilled) {
                    view.setImageResource(R.drawable.ic_heart_recipe_list)
                    view.tag = false
                } else {
                    view.setImageResource(R.drawable.ic_favorite)
                    view.tag = true
                }
            }
        }

        val ingredientDropDownBtn: ImageButton = findViewById(R.id.IngredientDropDownBtn)
        val dropdownSelect: LinearLayout = findViewById(R.id.dropdown_select)
        var isDropdownOpen = false
        ingredientDropDownBtn.setOnClickListener {
            isDropdownOpen = !isDropdownOpen
            if (isDropdownOpen) {
                ingredientDropDownBtn.setImageResource(R.drawable.ic_dropup)
                dropdownSelect.visibility = View.GONE
            } else {
                ingredientDropDownBtn.setImageResource(R.drawable.ic_dropdown)
                dropdownSelect.visibility = View.VISIBLE
            }
        }

        // 메뉴 카테고리 버튼들
        val ingredientCategoryAllBtn: LinearLayout = findViewById(R.id.ingredientCategoryAllBtn)
        val ingredientCategoryKRBtn: LinearLayout = findViewById(R.id.ingredientCategoryKRBtn)
        val ingredientCategoryWSBtn: LinearLayout = findViewById(R.id.ingredientCategoryWSBtn)
        val ingredientCategoryJPBtn: LinearLayout = findViewById(R.id.ingredientCategoryJPBtn)
        val ingredientCategoryCNBtn: LinearLayout = findViewById(R.id.ingredientCategoryCNBtn)
        val ingredientCategoryVGBtn: LinearLayout = findViewById(R.id.ingredientCategoryVGBtn)
        val ingredientCategorySNBtn: LinearLayout = findViewById(R.id.ingredientCategorySNBtn)
        val ingredientCategoryMCBtn: LinearLayout = findViewById(R.id.ingredientCategoryMCBtn)
        val ingredientCategorySDBtn: LinearLayout = findViewById(R.id.ingredientCategorySDBtn)

        val ingredientCategoryAllText: TextView = findViewById(R.id.ingredientCategoryAllText)
        val ingredientCategoryKRText: TextView = findViewById(R.id.ingredientCategoryKRText)
        val ingredientCategoryWSText: TextView = findViewById(R.id.ingredientCategoryWSText)
        val ingredientCategoryJPText: TextView = findViewById(R.id.ingredientCategoryJPText)
        val ingredientCategoryCNText: TextView = findViewById(R.id.ingredientCategoryCNText)
        val ingredientCategoryVGText: TextView = findViewById(R.id.ingredientCategoryKRText)
        val ingredientCategorySNText: TextView = findViewById(R.id.ingredientCategorySNText)
        val ingredientCategoryMCText: TextView = findViewById(R.id.ingredientCategoryMCText)
        val ingredientCategorySDText: TextView = findViewById(R.id.ingredientCategorySDText)

        val categoriesm = listOf(
            Pair(ingredientCategoryAllBtn, ingredientCategoryAllText),
            Pair(ingredientCategoryKRBtn, ingredientCategoryKRText),
            Pair(ingredientCategoryWSBtn, ingredientCategoryWSText),
            Pair(ingredientCategoryJPBtn, ingredientCategoryJPText),
            Pair(ingredientCategoryCNBtn, ingredientCategoryCNText),
            Pair(ingredientCategoryVGBtn, ingredientCategoryKRText),
            Pair(ingredientCategorySNBtn, ingredientCategorySNText),
            Pair(ingredientCategoryMCBtn, ingredientCategoryMCText),
            Pair(ingredientCategorySDBtn, ingredientCategorySDText)
        )

        categoriesm.forEach { (layout, textView) ->
            layout.setOnClickListener {
                categoriesm.forEach { (otherLayout, otherText) ->
                    otherLayout.setBackgroundResource(R.drawable.btn_fridge_ct)
                    otherText.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }

        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        val fridgeRecipe1: LinearLayout = findViewById(R.id.fridgeRecipe1)
        fridgeRecipe1.setOnClickListener {
            val intent = Intent(this, RecipeSeeMainActivity::class.java)
            startActivity(intent)
        }

        val RecipeResultDropDownBtn: ImageView = findViewById(R.id.RecipeResultDropDownBtn)
        val RecipeResultFilterText: TextView = findViewById(R.id.RecipeResultFilterText)
        RecipeResultDropDownBtn.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            menuInflater.inflate(R.menu.recipe_result_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_view_count -> {
                        RecipeResultFilterText.text = "조회수순"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_likes -> {
                        RecipeResultFilterText.text = "찜순"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_latest -> {
                        RecipeResultFilterText.text = "최신순"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_cooking_time_short -> {
                        RecipeResultFilterText.text = "요리시간\n짧은순"
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_cooking_time_long -> {
                        RecipeResultFilterText.text = "요리시간\n긴순"
                        return@setOnMenuItemClickListener true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}