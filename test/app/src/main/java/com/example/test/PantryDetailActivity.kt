package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.Utils.TabBarUtils

class PantryDetailActivity : AppCompatActivity() {

    private var selectedItem: ConstraintLayout? = null
    private val selectedLists = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_detail)

        TabBarUtils.setupTabBar(this)

        // 냉장고 분석으로 이동
        val analysisIcon: ImageButton = findViewById(R.id.analysisIcon)
        analysisIcon.setOnClickListener {
            val intent = Intent(this, FridgeStatsActivity::class.java)
            startActivity(intent)
        }

        // 재료 추가로 이동
        val plusIcon: ImageButton = findViewById(R.id.plusIcon)
        plusIcon.setOnClickListener {
            val intent = Intent(this, PantryMaterialAddActivity::class.java)
            startActivity(intent)
        }

        // 뒤로가기
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

        val totalChoice = findViewById<ConstraintLayout>(R.id.totalChoice)
        val checkBtn = findViewById<ImageButton>(R.id.check)
        val listOne = findViewById<ConstraintLayout>(R.id.listOne)
        val listTwo = findViewById<ConstraintLayout>(R.id.listTwo)
        val listThree = findViewById<ConstraintLayout>(R.id.listThree)
        val listFour = findViewById<ConstraintLayout>(R.id.listFour)
        val coldStorageListOne = findViewById<ConstraintLayout>(R.id.coldStorageListOne)
        val coldStorageListTwo = findViewById<ConstraintLayout>(R.id.coldStorageListTwo)
        val coldStorageListThree = findViewById<ConstraintLayout>(R.id.coldStorageListThree)
        val coldStorageListFour = findViewById<ConstraintLayout>(R.id.coldStorageListFour)
        val freezeListOne = findViewById<ConstraintLayout>(R.id.freezeListOne)
        val freezeListTwo = findViewById<ConstraintLayout>(R.id.freezeListTwo)
        val freezeListThree = findViewById<ConstraintLayout>(R.id.freezeListThree)
        val freezeListFour = findViewById<ConstraintLayout>(R.id.freezeListFour)
        val outsideListOne = findViewById<ConstraintLayout>(R.id.outsideListOne)
        val outsideListTwo = findViewById<ConstraintLayout>(R.id.outsideListTwo)
        val outsideListThree = findViewById<ConstraintLayout>(R.id.outsideListThree)
        val outsideListFour = findViewById<ConstraintLayout>(R.id.outsideListFour)
        val rectOne = findViewById<View>(R.id.rectOne)
        val rectTwo = findViewById<View>(R.id.rectTwo)
        val rectThree = findViewById<View>(R.id.rectThree)
        val rectFour = findViewById<View>(R.id.rectFour)
        val coldStorageRectOne = findViewById<View>(R.id.coldStorageRectOne)
        val coldStorageRectTwo = findViewById<View>(R.id.coldStorageRectTwo)
        val coldStorageRectThree = findViewById<View>(R.id.coldStorageRectThree)
        val coldStorageRectFour = findViewById<View>(R.id.coldStorageRectFour)
        val freezeRectOne = findViewById<View>(R.id.freezeRectOne)
        val freezeRectTwo = findViewById<View>(R.id.freezeRectTwo)
        val freezeRectThree = findViewById<View>(R.id.freezeRectThree)
        val freezeRectFour = findViewById<View>(R.id.freezeRectFour)
        val outsideRectOne = findViewById<View>(R.id.outsideRectOne)
        val outsideRectTwo = findViewById<View>(R.id.outsideRectTwo)
        val outsideRectThree = findViewById<View>(R.id.outsideRectThree)
        val outsideRectFour = findViewById<View>(R.id.outsideRectFour)
        val btnTotal = findViewById<AppCompatButton>(R.id.total)
        val btnRefrigeration = findViewById<AppCompatButton>(R.id.refrigeration)
        val btnFreeze = findViewById<AppCompatButton>(R.id.freeze)
        val btnOutside = findViewById<AppCompatButton>(R.id.outside)
        val totalList = findViewById<ConstraintLayout>(R.id.totalList)
        val coldStorageList = findViewById<ConstraintLayout>(R.id.coldStorageList)
        val freezeList = findViewById<ConstraintLayout>(R.id.freezeList)
        val outsideList = findViewById<ConstraintLayout>(R.id.outsideList)
        val modification = findViewById<TextView>(R.id.modification)
        val deleteBtn = findViewById<TextView>(R.id.delete)

        // 탭바 리스트와 해당 리스트 묶음
        val tabs = listOf(
            btnTotal to totalList,
            btnRefrigeration to coldStorageList,
            btnFreeze to freezeList,
            btnOutside to outsideList
        )

        // 탭바 버튼 클릭시 색 바뀜
        fun setTabSelected(button: AppCompatButton, selected: Boolean) {
            button.setBackgroundResource(
                if (selected) R.drawable.btn_fridge_ct_ck else R.drawable.btn_fridge_ct
            )
            button.setTextColor(if (selected) Color.parseColor("#FFFFFF") else Color.parseColor("#8A8F9C"))
            button.isSelected = selected
        }

        // 탭바 클릭시 해당 리스트 나타남
        tabs.forEach { (btn, list) ->
            btn.setOnClickListener {
                tabs.forEach { (b, l) ->
                    val selected = (b == btn)
                    setTabSelected(b, selected)
                    l.visibility = if (selected) View.VISIBLE else View.GONE
                }
            }
        }

        // 초기 상태 ("전체" 선택 + totalList 보이기)
        tabs.forEach { (btn, list) ->
            val selected = (btn == btnTotal)
            setTabSelected(btn, selected)
            list.visibility = if (selected) View.VISIBLE else View.GONE
        }

        // 리스트
        val buttonGroups = listOf(
            listOf(listOne, listTwo, listThree, listFour),
            listOf(coldStorageListOne, coldStorageListTwo, coldStorageListThree, coldStorageListFour),
            listOf(freezeListOne, freezeListTwo, freezeListThree, freezeListFour),
            listOf(outsideListOne, outsideListTwo, outsideListThree, outsideListFour)
        )

        // 리스트 상자
        val rectGroups = listOf(
            listOf(rectOne, rectTwo, rectThree, rectFour),
            listOf(coldStorageRectOne, coldStorageRectTwo, coldStorageRectThree, coldStorageRectFour),
            listOf(freezeRectOne, freezeRectTwo, freezeRectThree, freezeRectFour),
            listOf(outsideRectOne, outsideRectTwo, outsideRectThree, outsideRectFour)
        )

        // 리스트 상자 하나로 합치고 싶을 때
        val allRects = rectGroups.flatten()

        // 전체 선택 상태
        var allChecked = false

        // 전체 선택 눌렀을때 리스트 눌리고 체크 바뀜
        fun applyAll(checked: Boolean) {
            // 체크 아이콘 바꾸기
            checkBtn.setImageResource(
                if (checked) R.drawable.ic_fridge_checked else R.drawable.ic_fridge_check
            )
            checkBtn.contentDescription = if (checked) "전체 선택됨" else "전체 선택 해제"

            // 모든 rect 배경 바꾸기
            val bgRes = if (checked) R.drawable.rounded_rectangle_fridge_ck
            else R.drawable.rounded_rectangle_fridge

            allRects.forEach { it.setBackgroundResource(bgRes) }
            allRects.forEach { it.tag = checked } // 상태 저장
        }

        // totalChoice 클릭 시 토글
        totalChoice.setOnClickListener {
            allChecked = !allChecked
            applyAll(allChecked)
        }

        // 체크 버튼 클릭 시도 같은 동작 (아이콘만 눌러도 되게)
        checkBtn.setOnClickListener {
            totalChoice.performClick()
        }

        // 전체 선택 초기 상태
        applyAll(false)

        // 리스트 클릭시 테두리 바뀜
        fun setToggleBackground(list: ConstraintLayout, rect: View) {
            list.setOnClickListener {
                val selected = rect.tag as? Boolean ?: false
                if (selected) {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge)
                    selectedItem = null
                } else {
                    rect.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck)
                    selectedItem = list
                }
                rect.tag = !selected
            }
        }

        val btnToRect = mutableMapOf<ConstraintLayout, View>()

        // === 매핑 연결 ===
        buttonGroups.zip(rectGroups).forEach { (btnList, rectList) ->
            btnList.zip(rectList).forEach { (btn, rect) ->
                setToggleBackground(btn, rect)
                btnToRect[btn] = rect
            }
        }

        fun getSelectedItems(): List<ConstraintLayout> {
            val result = mutableListOf<ConstraintLayout>()
            buttonGroups.zip(rectGroups).forEach { (btnList, rectList) ->
                btnList.zip(rectList).forEach { (btn, rect) ->
                    val selected = (rect.tag as? Boolean) == true
                    if (selected) result.add(btn)
                }
            }
            return result
        }

        // 수정 버튼 클릭 시
        modification.setOnClickListener {
            val selected = getSelectedItems()
            when (selected.size) {
                1 -> {
                    val item = selected.first()
                    val intent = Intent(this, FridgeIngredientActivity::class.java)
                    intent.putExtra("selectedId", item.id)
                    startActivity(intent)
                }
                0 -> {
                    Toast.makeText(this, "편집할 항목을 하나 선택해 주세요.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "수정은 1개만 가능합니다. (현재 ${selected.size}개 선택됨)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        deleteBtn.setOnClickListener {
            val selected = getSelectedItems()  // rect.tag == true 인 것들
            if (selected.isEmpty()) {
                Toast.makeText(this, "삭제할 항목을 선택해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selected.forEach { btn ->
                // 컨테이너 숨김
                btn.visibility = View.GONE

                // 대응하는 사각형 박스도 함께 숨김 + 태그 초기화
                btnToRect[btn]?.apply {
                    visibility = View.GONE
                    tag = false
                }
            }
        }
    }
}
