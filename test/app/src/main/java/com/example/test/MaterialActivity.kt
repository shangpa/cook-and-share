package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class MaterialActivity : AppCompatActivity() {

    private var isMaterialVisible = false
    private var isdistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button> // 거리 버튼 리스트
    private lateinit var selectedFilterLayout: LinearLayout

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)

        // UI 요소 초기화
        val item1: LinearLayout = findViewById(R.id.item1)
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        val myLocation: LinearLayout = findViewById(R.id.myLocation)
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        val aa: ImageView = findViewById(R.id.aa)
        val bb: ImageView = findViewById(R.id.bb)
        val plusIcon3: ImageView = findViewById(R.id.plusIcon3)

        val materialFilter = findViewById<LinearLayout>(R.id.materialFilter)
        val materialText = findViewById<TextView>(R.id.materialText)
        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val material = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val distance = findViewById<LinearLayout>(R.id.distance)

        // 재료 버튼들
        val all = findViewById<Button>(R.id.all)
        val cookware = findViewById<Button>(R.id.cookware)
        val fansPots = findViewById<Button>(R.id.fans_pots)
        val containers = findViewById<Button>(R.id.containers)
        val tableware = findViewById<Button>(R.id.tableware)
        val storage = findViewById<Button>(R.id.storageSupplies)
        val sanitary = findViewById<Button>(R.id.sanitaryProducts)
        val small = findViewById<Button>(R.id.smallAppliances)
        val disposable = findViewById<Button>(R.id.disposableProducts)
        val etc = findViewById<Button>(R.id.etc)

        val materialButtons = listOf(
            all, cookware, fansPots, containers, tableware,
            storage, sanitary, small, disposable, etc
        )

        materialButtons.forEach { button ->
            button.setOnClickListener {
                setSelectedMaterialButton(button, materialFilter, materialText)
                showSelectedFilterBadge(button.text.toString(), materialFilter, materialText)
                material.visibility = View.GONE
                isMaterialVisible = false
            }
        }

        item1.setOnClickListener {
            startActivity(Intent(this, MaterialDetailActivity::class.java))
        }

        searchIcon.setOnClickListener {
            startActivity(Intent(this, MaterialSearchActivity::class.java))
        }

        myLocation.setOnClickListener {
            startActivity(Intent(this, MaterialMyLocationActivity::class.java))
        }

        profileIcon.setOnClickListener {
            startActivity(Intent(this, MaterialMyProfileActivity::class.java))
        }

        aa.setOnClickListener {
            startActivity(Intent(this, MaterialWritingActivity::class.java))
        }

        bb.setOnClickListener {
            startActivity(Intent(this, MaterialChatActivity::class.java))
        }

        plusIcon3.setOnClickListener {
            isPlusMenuVisible = !isPlusMenuVisible
            aa.visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
            bb.visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
        }

        // 거리 버튼 리스트 초기화
        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )

        for (button in buttons) {
            button.setOnClickListener {
                setSelectedButton(button)
            }
        }

        // 재료 필터 클릭
        materialFilter.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            if (isMaterialVisible) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                materialText.setTextColor(Color.WHITE)
                materialIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                if (selectedMaterial != null) {
                    materialFilter.setBackgroundColor(Color.BLACK)
                    materialText.setTextColor(Color.WHITE)
                } else {
                    materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    materialText.setTextColor(Color.parseColor("#8A8F9C"))
                }
                materialIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        // 거리 필터 클릭
        distanceFilter.setOnClickListener {
            isdistanceVisible = !isdistanceVisible
            distance.visibility = if (isdistanceVisible) View.VISIBLE else View.GONE
            if (isdistanceVisible) {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                distanceText.setTextColor(Color.WHITE)
                distanceIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                distanceText.setTextColor(Color.parseColor("#8A8F9C"))
                distanceIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }
    }

    private fun setSelectedButton(selectedButton: Button) {
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceLayout = findViewById<LinearLayout>(R.id.distance)

        // 버튼 스타일 설정 및 selectedDistance 지정
        for (button in buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                button.setTextColor(Color.WHITE)
                selectedDistance = button
            } else {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }

        // 기존 거리 뱃지 제거
        val childCount = selectedFilterLayout.childCount
        for (i in childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            val badgeText = badge.findViewById<TextView>(R.id.filterText)
            if (badge.tag == "distance") {
                selectedFilterLayout.removeView(badge)
            }
        }

        // 뱃지 추가
        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        badge.tag = "distance"
        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = selectedButton.text.toString()
        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)
            selectedDistance = null

            // 거리 버튼 초기화
            buttons.forEach {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                it.setTextColor(Color.parseColor("#8A8F9C"))
            }

            // 거리 필터 UI 초기화
            distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
            distanceText.setTextColor(Color.parseColor("#8A8F9C"))
        }

        selectedFilterLayout.addView(badge)

        // 거리 레이아웃 닫기
        distanceLayout.visibility = View.GONE
        isdistanceVisible = false

        // 선택된 상태로 필터 배경/글자색 설정
        distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
        distanceText.setTextColor(Color.WHITE)
    }



    private fun setSelectedMaterialButton(button: Button, filterLayout: LinearLayout, textView: TextView) {
        selectedMaterial = if (selectedMaterial == button) null else button

        val allMaterialButtons = listOf(
            findViewById<Button>(R.id.all),
            findViewById(R.id.cookware),
            findViewById(R.id.fans_pots),
            findViewById(R.id.containers),
            findViewById(R.id.tableware),
            findViewById(R.id.storageSupplies),
            findViewById(R.id.sanitaryProducts),
            findViewById(R.id.smallAppliances),
            findViewById(R.id.disposableProducts),
            findViewById(R.id.etc)
        )

        allMaterialButtons.forEach {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background)
            it.setTextColor(Color.parseColor("#8A8F9C"))
        }

        selectedMaterial?.let {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            it.setTextColor(Color.WHITE)
        }
    }

    private fun showSelectedFilterBadge(text: String, materialFilter: LinearLayout, materialText: TextView) {
        selectedFilterLayout.visibility = View.VISIBLE

        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = text
        badge.tag = "material-$text" // 고유 태그 설정

        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)

            // 버튼 초기화
            val allMaterialButtons = listOf(
                findViewById<Button>(R.id.all),
                findViewById(R.id.cookware),
                findViewById(R.id.fans_pots),
                findViewById(R.id.containers),
                findViewById(R.id.tableware),
                findViewById(R.id.storageSupplies),
                findViewById(R.id.sanitaryProducts),
                findViewById(R.id.smallAppliances),
                findViewById(R.id.disposableProducts),
                findViewById(R.id.etc)
            )

            allMaterialButtons.find { it.text == text }?.let { button ->
                button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }

            // 재료 필터 색 초기화 (선택된 게 없을 때만)
            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                materialText.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }

        selectedFilterLayout.addView(badge)
    }

}
