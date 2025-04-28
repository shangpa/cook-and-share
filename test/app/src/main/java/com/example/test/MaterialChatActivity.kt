package com.example.test

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MaterialChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_chat) // 다른 프로필 화면의 레이아웃 파일 연결

        // chatBack 클릭했을 때 MainActivity 이동
        val chatBack: ImageView = findViewById(R.id.chatBack)
        chatBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 채팅 선언
        val total = findViewById<LinearLayout>(R.id.total)
        val sale = findViewById<LinearLayout>(R.id.sale)
        val purchase = findViewById<LinearLayout>(R.id.purchase)
        val unread = findViewById<LinearLayout>(R.id.unread)
        val totalListContainer = findViewById<RecyclerView>(R.id.recyclerView)
        val saleListContainer = findViewById<LinearLayout>(R.id.saleListContainer)
        val purchaseListContainer = findViewById<LinearLayout>(R.id.purchaseListContainer)
        val unreadListContainer = findViewById<LinearLayout>(R.id.unreadListContainer)

        // 카테고리 LinearLayout 리스트
        val linearlayouts = listOf(
            findViewById<LinearLayout>(R.id.total),
            findViewById<LinearLayout>(R.id.sale),
            findViewById<LinearLayout>(R.id.purchase),
            findViewById<LinearLayout>(R.id.unread)
        )

        // LinearLayout 리스트 (위 LinearLayout와 1:1 매칭)
        val layouts = listOf(
            findViewById<RecyclerView>(R.id.recyclerView),
            findViewById<LinearLayout>(R.id.saleListContainer),
            findViewById<LinearLayout>(R.id.purchaseListContainer),
            findViewById<LinearLayout>(R.id.unreadListContainer)
        )

        // 카테고리 TextView 클릭 시 해당 화면으로 이동
        linearlayouts.forEachIndexed { index, layout ->
            layout.setOnClickListener {

                // 모든 LinearLayout 숨김
                layouts.forEach { it.visibility = View.GONE }

                // 클릭된 LinearLayout에 해당하는 LinearLayout 표시
                layouts[index].visibility = View.VISIBLE

                // 모든 backgroundTint 초기화 + 텍스트 색 회색
                linearlayouts.forEach {
                    it.backgroundTintList = null
                    val textView = it.getChildAt(0) as? TextView
                    textView?.setTextColor(Color.parseColor("#8A8F9C"))
                }

                // 선택된 항목은 초록색으로 강조
                layout.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#35A825"))
                val selectedTextView = layout.getChildAt(0) as? TextView
                selectedTextView?.setTextColor(Color.WHITE)
            }
        }
    }
}



