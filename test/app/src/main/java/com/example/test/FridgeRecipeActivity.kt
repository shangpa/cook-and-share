package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.ImageButton
import android.widget.Toast

class FridgeRecipeActivity : AppCompatActivity() {

    private var isChecked = false // 아이콘 상태 저장 변수

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fridge_recipe)

        // 카테고리 버튼들 (LinearLayout)
        val fridgeCategoryAllBtn: LinearLayout = findViewById(R.id.fridgeCategoryAllBtn)
        val fridgeCategoryColdBtn: LinearLayout = findViewById(R.id.fridgeCategoryColdBtn)
        val fridgeCategoryFreezeBtn: LinearLayout = findViewById(R.id.fridgeCategoryFreezeBtn)
        val fridgeCategoryOutBtn: LinearLayout = findViewById(R.id.fridgeCategoryOutBtn)

        // 카테고리 텍스트 (TextView)
        val fridgeCategoryAllText: TextView = findViewById(R.id.fridgeCategoryAllText)
        val fridgeCategoryColdText: TextView = findViewById(R.id.fridgeCategoryColdText)
        val fridgeCategoryFreezeText: TextView = findViewById(R.id.fridgeCategoryFreezeText)
        val fridgeCategoryOutText: TextView = findViewById(R.id.fridgeCategoryOutText)

        // 버튼 & 텍스트 리스트
        val categories = listOf(
            Pair(fridgeCategoryAllBtn, fridgeCategoryAllText),
            Pair(fridgeCategoryColdBtn, fridgeCategoryColdText),
            Pair(fridgeCategoryFreezeBtn, fridgeCategoryFreezeText),
            Pair(fridgeCategoryOutBtn, fridgeCategoryOutText)
        )

        // 카테고리 클릭 이벤트 설정
        categories.forEach { (layout, textView) ->
            layout.setOnClickListener {
                // 모든 버튼을 기본 색상으로 변경
                categories.forEach { (otherLayout, otherText) ->
                    otherLayout.setBackgroundResource(R.drawable.btn_fridge_ct) // 기본 배경
                    otherText.setTextColor(ContextCompat.getColor(this, R.color.black)) // 글자색 검정
                }

                // 선택된 버튼을 강조 색상으로 변경
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck) // 선택된 배경
                textView.setTextColor(ContextCompat.getColor(this, R.color.white)) // 글자색 흰색
            }
        }

        val fridgeAllCheckIcon: ImageView = findViewById(R.id.fridgeAllCheckIcon)
        val fridgeRecipeItem: LinearLayout = findViewById(R.id.fridegeRecipeItem) // 부모 레이아웃
        var isChecked = false // 상태를 추적하기 위한 변수

        fridgeAllCheckIcon.setOnClickListener {
            // 상태 토글 (isChecked가 true일 때와 false일 때 처리)
            isChecked = !isChecked

            // 모든 자식 뷰를 순회하여 배경 및 글자색 변경
            for (i in 0 until fridgeRecipeItem.childCount) {
                val childView = fridgeRecipeItem.getChildAt(i)

                // 배경 색 변경
                if (childView is View) {
                    if (isChecked) {
                        // 배경을 "rounded_rectangle_fridge_green"으로 변경
                        childView.setBackgroundResource(R.drawable.rounded_rectangle_fridge_green)
                    } else {
                        // 배경을 "rounded_rectangle_fridge"로 변경
                        childView.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    }
                }

                // 자식 뷰가 LinearLayout일 경우, 그 내부의 TextView들을 탐색하여 글자색 변경
                if (childView is LinearLayout) {
                    // 내부의 모든 자식 뷰를 순회
                    for (j in 0 until childView.childCount) {
                        val innerChildView = childView.getChildAt(j)

                        // TextView일 경우 글자 색 변경
                        if (innerChildView is TextView) {
                            if (isChecked) {
                                // 글자 색을 흰색으로 변경
                                innerChildView.setTextColor(Color.WHITE)
                            } else {
                                // 글자 색을 원래 색상으로 변경 (예시: #8A8F9C)
                                innerChildView.setTextColor(Color.parseColor("#8A8F9C"))
                            }
                        }
                    }
                }
            }

            // 아이콘 상태 변경
            if (isChecked) {
                fridgeAllCheckIcon.setImageResource(R.drawable.btn_fridge_checked) // 체크된 상태
            } else {
                fridgeAllCheckIcon.setImageResource(R.drawable.ic_fridge_check) // 체크 해제 상태
            }
        }

        var isDropdownOpen = false // 드롭다운 상태 변수

        // 드롭다운 버튼 & 드롭다운 영역
        val ingredientDropDownBtn: ImageButton = findViewById(R.id.IngredientDropDownBtn)
        val dropdownSelect: LinearLayout = findViewById(R.id.dropdown_select)

        // 드롭다운 버튼 클릭 이벤트 설정
        ingredientDropDownBtn.setOnClickListener {
            isDropdownOpen = !isDropdownOpen // 상태 변경

            if (isDropdownOpen) {
                ingredientDropDownBtn.setImageResource(R.drawable.ic_dropup) // 아이콘 변경
                dropdownSelect.visibility = View.GONE // 드롭다운 영역 숨기기
            } else {
                ingredientDropDownBtn.setImageResource(R.drawable.ic_dropdown) // 아이콘 원래대로
                dropdownSelect.visibility = View.VISIBLE // 드롭다운 영역 다시 보이기
            }
        }

        // 메뉴 카테고리 버튼들 (LinearLayout)
        val ingredientCategoryAllBtn: LinearLayout = findViewById(R.id.ingredientCategoryAllBtn)
        val ingredientCategoryKRBtn: LinearLayout = findViewById(R.id.ingredientCategoryKRBtn)
        val ingredientCategoryWSBtn: LinearLayout = findViewById(R.id.ingredientCategoryWSBtn)
        val ingredientCategoryJPBtn: LinearLayout = findViewById(R.id.ingredientCategoryJPBtn)
        val ingredientCategoryCNBtn: LinearLayout = findViewById(R.id.ingredientCategoryCNBtn)
        val ingredientCategoryVGBtn: LinearLayout = findViewById(R.id.ingredientCategoryVGBtn)
        val ingredientCategorySNBtn: LinearLayout = findViewById(R.id.ingredientCategorySNBtn)
        val ingredientCategoryMCBtn: LinearLayout = findViewById(R.id.ingredientCategoryMCBtn)
        val ingredientCategorySDBtn: LinearLayout = findViewById(R.id.ingredientCategorySDBtn)

        // 메뉴 카테고리 텍스트 (TextView)
        val ingredientCategoryAllText: TextView = findViewById(R.id.ingredientCategoryAllText)
        val ingredientCategoryKRText: TextView = findViewById(R.id.ingredientCategoryKRText)
        val ingredientCategoryWSText: TextView = findViewById(R.id.ingredientCategoryWSText)
        val ingredientCategoryJPText: TextView = findViewById(R.id.ingredientCategoryJPText)
        val ingredientCategoryCNText: TextView = findViewById(R.id.ingredientCategoryCNText)
        val ingredientCategoryVGText: TextView = findViewById(R.id.ingredientCategoryKRText)
        val ingredientCategorySNText: TextView = findViewById(R.id.ingredientCategorySNText)
        val ingredientCategoryMCText: TextView = findViewById(R.id.ingredientCategoryMCText)
        val ingredientCategorySDText: TextView = findViewById(R.id.ingredientCategorySDText)

        // 버튼 & 텍스트 리스트
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

        // 카테고리 클릭 이벤트 설정
        categoriesm.forEach { (layout, textView) ->
            layout.setOnClickListener {
                // 모든 버튼을 기본 색상으로 변경
                categoriesm.forEach { (otherLayout, otherText) ->
                    otherLayout.setBackgroundResource(R.drawable.btn_fridge_ct) // 기본 배경
                    otherText.setTextColor(ContextCompat.getColor(this, R.color.black)) // 글자색 검정
                }

                // 선택된 버튼을 강조 색상으로 변경
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck) // 선택된 배경
                textView.setTextColor(ContextCompat.getColor(this, R.color.white)) // 글자색 흰색
            }
        }

        // backBtn 클릭했을 때 MaterialSalesActivity 이동
        val backBtn: ImageView = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, FridgeActivity::class.java)
            startActivity(intent)
        }

        // fridgeRecipe1 클릭했을 때 RecipeMainOne 이동
        val fridgeRecipe1: LinearLayout = findViewById(R.id.fridgeRecipe1)
        fridgeRecipe1.setOnClickListener {
            val intent = Intent(this, RecipeMainOne::class.java)
            startActivity(intent)
        }

        // 드롭다운 버튼 & 필터 텍스트
        val RecipeResultDropDownBtn: ImageView = findViewById(R.id.RecipeResultDropDownBtn)
        val RecipeResultFilterText: TextView = findViewById(R.id.RecipeResultFilterText)

        // 드롭다운 버튼 클릭 리스너 설정
        RecipeResultDropDownBtn.setOnClickListener {
            // 팝업 메뉴 생성
            val popupMenu = PopupMenu(this, it)
            menuInflater.inflate(R.menu.recipe_result_menu, popupMenu.menu) // 메뉴 리소스 연결

            // 메뉴 아이템 클릭 리스너 설정
            popupMenu.setOnMenuItemClickListener { menuItem ->
                // 선택된 메뉴 아이템에 따른 필터 텍스트 변경
                when (menuItem.itemId) {
                    R.id.menu_view_count -> {
                        RecipeResultFilterText.text = "조회수순" // 선택된 텍스트 설정
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_likes -> {
                        RecipeResultFilterText.text = "찜순" // 선택된 텍스트 설정
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_latest -> {
                        RecipeResultFilterText.text = "최신순" // 선택된 텍스트 설정
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_cooking_time_short -> {
                        RecipeResultFilterText.text = "요리시간\n짧은순" // 선택된 텍스트 설정
                        return@setOnMenuItemClickListener true
                    }
                    R.id.menu_cooking_time_long -> {
                        RecipeResultFilterText.text = "요리시간\n긴순" // 선택된 텍스트 설정
                        return@setOnMenuItemClickListener true
                    }
                    else -> false
                }
            }

            popupMenu.show() // 드롭다운 메뉴 표시
        }

    }
}
