package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MypageWritePostActivity : AppCompatActivity() {

    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_write_post)

        // 뷰 초기화
        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)
        backButton = findViewById(R.id.backButton)

        // 드롭다운 아이콘 클릭 시 팝업 메뉴 표시
        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showDropdownMenu()
        }

        // 뒤로가기 버튼 클릭 시 이전 화면으로 이동
        backButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 모든 ic_more 버튼에 클릭 리스너 설정
        val moreIconIds = listOf(R.id.moreIcon1, R.id.moreIcon2, R.id.moreIcon3)
        for (id in moreIconIds) {
            val moreIcon = findViewById<ImageButton>(id)
            moreIcon.setOnClickListener { view ->
                showMoreOptions(view)
            }
        }
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeRecipeResultDropDownIcon)
        popupMenu.menu.add("추천순")
        popupMenu.menu.add("댓글순")
        popupMenu.menu.add("최신순")

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            fridgeRecipefillterText.text = item.title
            true
        }

        popupMenu.show()
    }

    private fun showMoreOptions(anchor: View) {
        val popupMenu = PopupMenu(this, anchor)
        popupMenu.menu.add("수정")
        popupMenu.menu.add("삭제")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title) {
                "수정" -> {
                    // 수정 로직 구현
                    true
                }
                "삭제" -> {
                    // 삭제 로직 구현
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
}
