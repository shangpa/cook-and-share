package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MypageFridgeMaterialListActivity : AppCompatActivity() {

    private lateinit var categoryButtons: List<LinearLayout>
    private lateinit var categoryTexts: List<TextView>
    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private var selectedIndex: Int = 0 // 기본 선택: 전체

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_fridge_material_list)

        // 카테고리 버튼 및 텍스트 연결
        categoryButtons = listOf(
            findViewById(R.id.category_all),
            findViewById(R.id.category_cold),
            findViewById(R.id.category_freeze),
            findViewById(R.id.category_outside)
        )

        categoryTexts = listOf(
            findViewById(R.id.text_all),
            findViewById(R.id.text_cold),
            findViewById(R.id.text_freeze),
            findViewById(R.id.text_outside)
        )

        // 드롭다운 관련 뷰 연결
        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)

        // 드롭다운 메뉴 클릭 이벤트
        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showDropdownMenu()
        }

        categoryButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                updateCategorySelection(index)
            }
        }

        // 초기 상태 설정
        updateCategorySelection(selectedIndex)
    }

    private fun updateCategorySelection(selected: Int) {
        categoryButtons.forEachIndexed { index, layout ->
            if (index == selected) {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                categoryTexts[index].setTextColor(Color.WHITE)
            } else {
                layout.setBackgroundResource(R.drawable.btn_fridge_ct)
                categoryTexts[index].setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
        selectedIndex = selected
    }

    // 드롭다운 메뉴 보여주기
    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeRecipeResultDropDownIcon)
        popupMenu.menu.add("최신순")
        popupMenu.menu.add("구매")
        popupMenu.menu.add("사용")

        popupMenu.setOnMenuItemClickListener { item ->
            fridgeRecipefillterText.text = item.title
            true
        }

        popupMenu.show()

        // backButton 클릭했을 때 MypageActivity 이동
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }
}
