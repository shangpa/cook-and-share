package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MaterialDetailActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_detail) // MaterialDetailActivity의 레이아웃 파일 연결

        // detailViewIcon 클릭했을 때 MaterialOtherProfileActivity 이동
        val detailViewIcon: ImageView = findViewById(R.id.detailViewIcon)
        detailViewIcon.setOnClickListener {
            val intent = Intent(this, MaterialOtherProfileActivity::class.java)
            startActivity(intent)
        }

        // chatButton 클릭했을 때 MaterialChatDetailActivity 이동
        val chatButton: Button = findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            val intent = Intent(this, MaterialChatDetailActivity::class.java)
            startActivity(intent)
        }

        // imageSearch 클릭했을 때 MaterialSearchActivity 이동
        val imageSearch: ImageView = findViewById(R.id.imageSearch)
        imageSearch.setOnClickListener {
            val intent = Intent(this, MaterialSearchActivity::class.java)
            startActivity(intent)
        }

        //뒤로가기 클릭시 이전화면으로 화면 이동
        val imageBack = findViewById<ImageView>(R.id.imageBack)
        imageBack.setOnClickListener {
            finish()  // 현재 액티비티 종료 = 이전 화면으로 이동
        }

        // 등록한 거래글 보기 더하기 버튼 클릭시 수정, 삭제 나타남
        // 더하기 버튼 클릭시 신고하기 나타남
        val itemMore = findViewById<ImageView>(R.id.itemMore)

        itemMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {

                    finish() // 현재 액티비티 종료 → 이전 화면으로 돌아감
                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        // 등록한 거래글 보기 하트버튼 선언
        val heartIcon = listOf(
            findViewById<ImageView>(R.id.heartIcon)
        )

        // 등록한 거래글 보기 하트버튼 클릭시 채워진 하트로 바뀜
        heartIcon.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.heartIcon, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.heartIcon) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_heart)
                } else {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.heartIcon, !isLiked)
            }
        }
    }
}