package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MaterialMyProfilePurchaseActivity : AppCompatActivity() {

    private lateinit var profileItemSeven: LinearLayout
    private lateinit var profileItemEight: LinearLayout
    private lateinit var itemMore7: ImageView
    private lateinit var itemMore8: ImageView
    private lateinit var reviewWrite: LinearLayout
    private lateinit var reviewWriteTwo: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_purchase)

        // 뒤로가기
        findViewById<ImageView>(R.id.myPurchaseBack).setOnClickListener {
            finish()
        }

        profileItemSeven = findViewById(R.id.profileItemSeven)
        profileItemEight = findViewById(R.id.profileItemEight)
        itemMore7 = findViewById(R.id.itemMore7)
        itemMore8 = findViewById(R.id.itemMore8)
        reviewWrite = findViewById(R.id.reviewWrite)
        reviewWriteTwo = findViewById(R.id.reviewWriteTwo)

        // 후기 작성 이동
        reviewWrite.setOnClickListener {
            val intent = Intent(this, MaterialReviewWriteActivity::class.java)
            intent.putExtra("targetItem", "profileItemSeven")
            startActivity(intent)
        }

        reviewWriteTwo.setOnClickListener {
            val intent = Intent(this, MaterialReviewWriteActivity::class.java)
            intent.putExtra("targetItem", "profileItemEight")
            startActivity(intent)
        }

        // 더보기 클릭 시 신고하기 팝업 (기능 연결할 수 있음)
        itemMore7.setOnClickListener {
            showReportPopup(it, profileItemSeven)
        }

        itemMore8.setOnClickListener {
            showReportPopup(it, profileItemEight)
        }
    }

    private fun showReportPopup(anchorView: View, targetLayout: LinearLayout) {
        val popup = android.widget.PopupMenu(this, anchorView)
        popup.menu.add("신고하기")
        popup.setOnMenuItemClickListener { item ->
            if (item.title == "신고하기") {
                targetLayout.visibility = View.GONE
                true
            } else {
                false
            }
        }
        popup.show()
    }
}
