/*비밀번호 찾기 - 메인(step 1)*/
package com.example.test

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.addTextChangedListener
import android.view.View
import com.example.test.Utils.TabBarUtils

class PantryMaterialAddDetailActivity : AppCompatActivity() {

    private lateinit var number: EditText
    private lateinit var dateEnter: EditText
    private lateinit var total: TextView            // 드롭다운 결과 표시 TextView
    private lateinit var purchaseDate: TextView     // 드롭다운 결과 표시 TextView
    private lateinit var register: AppCompatButton
    private lateinit var totalDropBox: View
    private lateinit var purchaseDateDropBox: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_material_add_detail)

        TabBarUtils.setupTabBar(this)

        // 뒤로가기
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

        total = findViewById(R.id.total)
        purchaseDate = findViewById(R.id.purchaseDate)
        number = findViewById(R.id.number)
        dateEnter = findViewById(R.id.dateEnter)
        register = findViewById(R.id.register)
        totalDropBox = findViewById(R.id.totalDropBox)
        purchaseDateDropBox = findViewById(R.id.purchaseDateDropBox)

        // 냉장고 관리로 이동
        register.setOnClickListener {
            val intent = Intent(this, PantryDetailActivity::class.java)
            startActivity(intent)
        }

        // 보관장소 드롭다운 버튼 클릭 시 열기/닫기 토글
        totalDropBox.setOnClickListener {
            val popup = PopupMenu(this, it)

            val categories = listOf("전체", "냉장", "냉동", "실외")
            categories.forEach { category ->
                popup.menu.add(category)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                total.text = menuItem.title
                total.setTextColor(Color.parseColor("#2B2B2B"))
                updateButtonState()
                true
            }

            popup.show()
        }

        // 구매일자 드롭다운 버튼 클릭 시 열기/닫기 토글
        purchaseDateDropBox.setOnClickListener {
            val popup = PopupMenu(this, it)

            val categories = listOf("구매일자", "유통기한")
            categories.forEach { category ->
                popup.menu.add(category)
            }

            popup.setOnMenuItemClickListener { menuItem ->
                purchaseDate.text = menuItem.title
                purchaseDate.setTextColor(Color.parseColor("#2B2B2B"))
                updateButtonState()
                true
            }

            popup.show()
        }

        // 3) 처음 진입 시 상태 한 번 반영
        updateButtonState()

        // 4) 입력 변화 감지해서 실시간 업데이트
        number.addTextChangedListener { updateButtonState() }
        dateEnter.addTextChangedListener { updateButtonState() }

        val imageUriStr = intent.getStringExtra("imageUri")
        val src = intent.getStringExtra("source") // "camera" / "gallery"

        imageUriStr?.let {
            val uri = Uri.parse(it)
            // 예) 미리보기
            // findViewById<ImageView>(R.id.preview).setImageURI(uri)
        }
    }

    // 등록하기 색 변함
    private fun updateButtonState() {
        val isFilled =
            number.text.toString().isNotBlank() &&
                    dateEnter.text.toString().isNotBlank()

        if (isFilled) {
            register.setBackgroundResource(R.drawable.btn_material_add)
            register.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            register.setBackgroundResource(R.drawable.btn_number_of_people)
            register.setTextColor(Color.parseColor("#A1A9AD"))
        }
    }
}