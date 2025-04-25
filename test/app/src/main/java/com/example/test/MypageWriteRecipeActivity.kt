package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu

class MypageWriteRecipeActivity : AppCompatActivity() {

    private var isCategoryVisible = false
    private var isMaterialVisible = false

    private lateinit var arrowIcon: ImageView
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryMaterial: LinearLayout
    private lateinit var cookDropdown: LinearLayout
    private lateinit var materialDropdown: LinearLayout
    private lateinit var categoryFoodText: TextView
    private lateinit var categoryMaterialText: TextView
    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView

    private var selectedButton: Button? = null
    private val selectedMaterialButtons = mutableSetOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_write_recipe)

        categoryFood = findViewById(R.id.categoryFood)
        cookDropdown = findViewById(R.id.cook)
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

        findViewById<LinearLayout>(R.id.btnRecipeMore).setOnClickListener {
            startActivity(Intent(this, RecipeActivity::class.java))
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        val moreIconIds = listOf(
            R.id.moreIcon1, R.id.moreIcon2, R.id.moreIcon3,
            R.id.moreIcon4, R.id.moreIcon5
        )

        moreIconIds.forEach { id ->
            findViewById<ImageButton>(id).setOnClickListener { view ->
                showMorePopup(view)
            }
        }

    }

    private fun showMorePopup(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("수정")
        popup.menu.add("삭제")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "수정" -> {
                    Toast.makeText(this, "수정 클릭됨", Toast.LENGTH_SHORT).show()
                    // TODO: 수정 로직 연결
                }
                "삭제" -> {
                    Toast.makeText(this, "삭제 클릭됨", Toast.LENGTH_SHORT).show()
                    // TODO: 삭제 로직 연결
                }
            }
            true
        }
        popup.show()
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

        // btnRecipeMore 클릭했을 때 RecipeWriteMain 이동
        val btnRecipeMore: LinearLayout = findViewById(R.id.btnRecipeMore)
        btnRecipeMore.setOnClickListener {
            val intent = Intent(this, RecipeWriteMain::class.java)
            startActivity(intent)
        }
    }
}
