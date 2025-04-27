package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MypageSavePostActivity : AppCompatActivity() {

    private lateinit var fridgeRecipeResultDropDownIcon: ImageView
    private lateinit var fridgeRecipefillterText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_save_post)

        fridgeRecipeResultDropDownIcon = findViewById(R.id.fridgeRecipeResultDropDownIcon)
        fridgeRecipefillterText = findViewById(R.id.fridgeRecipefillterText)

        fridgeRecipeResultDropDownIcon.setOnClickListener {
            showDropdownMenu()
        }
    }

    private fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, fridgeRecipeResultDropDownIcon)
        popupMenu.menu.add("추천순")
        popupMenu.menu.add("댓글순")
        popupMenu.menu.add("최신순")

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
