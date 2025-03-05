package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MaterialActivity : AppCompatActivity() {

    // 변수 추가
    private var isMaterialVisible = false
    private var isdistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button> // 거리 버튼 리스트 추가

    @SuppressLint("WrongViewCast", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material) // MaterialActivity의 레이아웃 파일 연결

        // item1을 클릭했을 때 MaterialDetailActivity로 이동
        val item1: LinearLayout = findViewById(R.id.item1)
        item1.setOnClickListener {
            val intent = Intent(this, MaterialDetailActivity::class.java)
            startActivity(intent)
        }

        // searchIcon을 클릭했을 때 MaterialSearchActivity 이동
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        searchIcon.setOnClickListener {
            val intent = Intent(this, MaterialSearchActivity::class.java)
            startActivity(intent)
        }

        // myLocation을 클릭했을 때 MaterialMyLocationActivity 이동
        val myLocation: LinearLayout = findViewById(R.id.myLocation)
        myLocation.setOnClickListener {
            val intent = Intent(this, MaterialMyLocationActivity::class.java)
            startActivity(intent)
        }

        // profileIcon 클릭했을 때 MaterialMyProfileActivity 이동
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        profileIcon.setOnClickListener {
            val intent = Intent(this, MaterialMyProfileActivity::class.java)
            startActivity(intent)
        }

        // aa 클릭했을 때 MaterialWritingActivity 이동
        val aa: ImageView = findViewById(R.id.aa) // ImageView로 변경
        aa.setOnClickListener {
            val intent = Intent(this, MaterialWritingActivity::class.java)
            startActivity(intent)
        }

        // bb 클릭했을 때 MaterialChatActivity 이동
        val bb: ImageView = findViewById(R.id.bb) // ImageView로 변경
        bb.setOnClickListener {
            val intent = Intent(this, MaterialChatActivity::class.java)
            startActivity(intent)
        }

        val materialFilter = findViewById<LinearLayout>(R.id.materialFilter)
        val materialText = findViewById<TextView>(R.id.materialText) // materialFilter 안의 텍스트뷰
        val materialIcon = findViewById<ImageView>(R.id.materialIcon) // materialFilter 안의 아이콘
        val material = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText) // materialFilter 안의 텍스트뷰
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon) // materialFilter 안의 아이콘
        val distance = findViewById<LinearLayout>(R.id.distance)

        val plusIcon3 = findViewById<ImageView>(R.id.plusIcon3)

        // 재료 필터 클릭 시 material 표시/숨김 + 배경색, 글자색, 아이콘 변경
        materialFilter.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE

            if (isMaterialVisible) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected) // 선택된 배경 적용
                materialText.setTextColor(Color.WHITE) // 글자색 흰색
                materialIcon.setImageResource(R.drawable.ic_arrow_up) // 위 화살표 아이콘
            } else {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background) // 기본 배경
                materialText.setTextColor(Color.parseColor("#8A8F9C")) // 기본 글자색
                materialIcon.setImageResource(R.drawable.ic_arrow_down) // 아래 화살표 아이콘
            }
        }

        // 거리 필터 클릭 시 distance 표시/숨김
        distanceFilter.setOnClickListener {
            isdistanceVisible = !isdistanceVisible
            distance.visibility = if (isdistanceVisible) View.VISIBLE else View.GONE

            if (isdistanceVisible) {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected) // 선택된 배경 적용
                distanceText.setTextColor(Color.WHITE) // 글자색 흰색
                distanceIcon.setImageResource(R.drawable.ic_arrow_up) // 위 화살표 아이콘
            } else {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background) // 기본 배경
                distanceText.setTextColor(Color.parseColor("#8A8F9C")) // 기본 글자색
                distanceIcon.setImageResource(R.drawable.ic_arrow_down) // 아래 화살표 아이콘
            }
        }

        // 추가 버튼 클릭 시 aa, bb 표시/숨김
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

        // 각 버튼에 클릭 이벤트 추가
        for (button in buttons) {
            button.setOnClickListener { setSelectedButton(button) }
        }
    }

    private fun setSelectedButton(selectedButton: Button) {
        for (button in buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                button.setTextColor(Color.WHITE) // 선택된 버튼 글자색 흰색
            } else {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                button.setTextColor(Color.parseColor("#8A8F9C")) // 기본 글자색
            }
        }
    }
}
