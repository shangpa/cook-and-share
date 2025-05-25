package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.Utils.TabBarUtils

class MaterialOtherProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_other_profile) // 다른 프로필 화면의 레이아웃 파일 연결

        TabBarUtils.setupTabBar(this)

        // 구매내역 선언
        val profileItem1 = findViewById<LinearLayout>(R.id.profileItem1)
        val profileItem2 = findViewById<LinearLayout>(R.id.profileItem2)
        val profileItem3 = findViewById<LinearLayout>(R.id.profileItem3)
        val profileItem4 = findViewById<LinearLayout>(R.id.profileItem4)
        val profileItem5 = findViewById<LinearLayout>(R.id.profileItem5)
        val profileItem6 = findViewById<LinearLayout>(R.id.profileItem6)
        val profileMore = findViewById<ImageView>(R.id.profileMore)
        val itemMore = findViewById<ImageView>(R.id.itemMore)
        val itemMore2 = findViewById<ImageView>(R.id.itemMore2)
        val itemMore3 = findViewById<ImageView>(R.id.itemMore3)
        val itemMore4 = findViewById<ImageView>(R.id.itemMore4)
        val itemMore5 = findViewById<ImageView>(R.id.itemMore5)
        val itemMore6 = findViewById<ImageView>(R.id.itemMore6)
        val commentIcon = findViewById<ImageView>(R.id.commentIcon)
        val commentIcon2 = findViewById<ImageView>(R.id.commentIcon2)
        val commentIcon3 = findViewById<ImageView>(R.id.commentIcon3)
        val commentIcon4 = findViewById<ImageView>(R.id.commentIcon4)
        val commentIcon5 = findViewById<ImageView>(R.id.commentIcon5)
        val commentIcon6 = findViewById<ImageView>(R.id.commentIcon6)
        val indicator = findViewById<View>(R.id.indicator)

        //채팅 아이콘 클릭시 채팅 화면으로 이동
        val commentIcons = listOf(
            findViewById<ImageView>(R.id.commentIcon),
            findViewById<ImageView>(R.id.commentIcon2),
            findViewById<ImageView>(R.id.commentIcon3),
            findViewById<ImageView>(R.id.commentIcon4),
            findViewById<ImageView>(R.id.commentIcon5),
            findViewById<ImageView>(R.id.commentIcon6)
        )

        commentIcons.forEach { icon ->
            icon.setOnClickListener {
                val intent = Intent(this, MaterialChatDetailActivity::class.java)
                startActivity(intent)
            }
        }


        // 프로필 더하기 클릭시 신고하기 나타남
        profileMore.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    // 신고 처리 로직 작성
                    finish()
                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        // 판매내역 TextView 리스트
        val textViews = listOf(
            findViewById<TextView>(R.id.total),
            findViewById<TextView>(R.id.deal),
            findViewById<TextView>(R.id.dealComplete)
        )

        // 판매내역 LinearLayout 리스트 (TextView와 1:1 매칭)
        val layouts = listOf(
            findViewById<LinearLayout>(R.id.profileItemGroup),
            findViewById<LinearLayout>(R.id.profileItemGroup2),
            findViewById<LinearLayout>(R.id.profileItemGroup3)
        )

        // 판매내역 TextView 클릭 시 해당 화면으로 이동 & 바 위치 변경
        textViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                // 모든 ConstraintLayout 숨김
                layouts.forEach { it.visibility = View.GONE }

                // 클릭된 TextView에 해당하는 ConstraintLayout만 표시
                layouts[index].visibility = View.VISIBLE

                // 모든 TextView 색상 초기화
                textViews.forEach { it.setTextColor(Color.parseColor("#B3B3B3")) }

                // 클릭된 TextView만 색상 변경 (#2B2B2B)
                textView.setTextColor(Color.parseColor("#35A825"))

                // indicator를 클릭된 TextView 아래로 이동
                val params = indicator.layoutParams as ViewGroup.MarginLayoutParams
                indicator.post {
                    val location = IntArray(2)
                    textView.getLocationOnScreen(location)
                    val textViewX = location[0]

                    // 바 위치를 TextView의 x 좌표로 이동
                    indicator.translationX = textViewX.toFloat()
                }
            }
        }

        // 더하기 버튼 클릭시 신고하기 나타남
        val reviewWriteButtons = listOf(itemMore, itemMore2, itemMore3, itemMore4, itemMore5, itemMore6)
        val reviewWriteLayouts = listOf(profileItem1, profileItem2, profileItem3, profileItem4, profileItem5, profileItem6)

        fun showReviewWritePopup(anchorView: View, targetLayout: LinearLayout) {
            val popup = PopupMenu(this, anchorView)
            popup.menu.add("신고하기")

            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title == "신고하기") {
                    targetLayout.visibility = View.GONE
                    true
                } else {
                    false
                }
            }

            popup.show()
        }

        // 신고하기 버튼 연결
        reviewWriteButtons.zip(reviewWriteLayouts).forEach { (button, layout) ->
            button.setOnClickListener {
                showReviewWritePopup(it, layout)
            }
        }

    }
}
