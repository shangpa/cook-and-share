package com.example.test

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MaterialMySavedActivity : AppCompatActivity() {

    private lateinit var material: LinearLayout
    private lateinit var distance: LinearLayout
    private lateinit var materialContainer: LinearLayout
    private lateinit var distanceContainer: LinearLayout
    private lateinit var materialText: TextView
    private lateinit var materialIcon: ImageView
    private lateinit var distanceText: TextView
    private lateinit var distanceIcon: ImageView
    private lateinit var sortArrow: ImageView
    private lateinit var w: TextView

    private var isMaterialVisible = false
    private var isDistanceVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_my_saved)

        // 뒤로가기
        findViewById<ImageView>(R.id.savedTransactioneBack).setOnClickListener {
            finish()
        }

        // 필터 관련 뷰 초기화
        material = findViewById(R.id.material)
        distance = findViewById(R.id.distance)
        materialContainer = findViewById(R.id.materialContainer)
        distanceContainer = findViewById(R.id.distanceContainer)
        materialText = findViewById(R.id.materialText)
        materialIcon = findViewById(R.id.materialIcon)
        distanceText = findViewById(R.id.distanceText)
        distanceIcon = findViewById(R.id.distanceIcon)
        sortArrow = findViewById(R.id.sortArrow)
        w = findViewById(R.id.w)

        // 정렬 팝업
        sortArrow.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("최신순")
            popup.menu.add("거리순")
            popup.setOnMenuItemClickListener { item ->
                w.text = item.title
                true
            }
            popup.show()
        }

        // 필터 버튼 클릭 시 드롭다운
        materialContainer.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            updateFilterUI(isMaterialVisible, materialContainer, materialText, materialIcon)
        }

        distanceContainer.setOnClickListener {
            isDistanceVisible = !isDistanceVisible
            distance.visibility = if (isDistanceVisible) View.VISIBLE else View.GONE
            updateFilterUI(isDistanceVisible, distanceContainer, distanceText, distanceIcon)
        }
    }

    private fun updateFilterUI(visible: Boolean, container: LinearLayout, text: TextView, icon: ImageView) {
        if (visible) {
            container.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            text.setTextColor(Color.WHITE)
            icon.setImageResource(R.drawable.ic_arrow_up)
        } else {
            container.setBackgroundResource(R.drawable.rounded_rectangle_background)
            text.setTextColor(Color.parseColor("#8A8F9C"))
            icon.setImageResource(R.drawable.ic_arrow_down)
        }
    }
}
