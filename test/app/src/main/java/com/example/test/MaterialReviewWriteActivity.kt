package com.example.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MaterialReviewWriteActivity : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var postBtn: Button
    private lateinit var descriptionText: EditText
    private lateinit var itemTitle: TextView
    private lateinit var itemSubTitle: TextView
    private lateinit var itemImage: ImageView
    private lateinit var moreBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_review_write)

        // XML 바인딩
        backBtn = findViewById(R.id.reviewWriteBack)
        postBtn = findViewById(R.id.postBtn)
        descriptionText = findViewById(R.id.descriptionText)
        itemTitle = findViewById(R.id.reviewItemTitle)
        itemSubTitle = findViewById(R.id.reviewItemSubTitle)
        itemImage = findViewById(R.id.reviewimage)
        moreBtn = findViewById(R.id.reviewItemMore)

        // TODO: 필요 시 intent로 데이터 받기 및 설정
        itemTitle.text = "거래한 주방용품"
        itemSubTitle.text = "미개봉 브리타 팔아요"
        itemImage.setImageResource(R.drawable.image_britta)

        // 뒤로가기
        backBtn.setOnClickListener {
            finish()
        }

        // 게시하기
        postBtn.setOnClickListener {
            val content = descriptionText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "후기 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: 서버 업로드 API 연동 필요
                Toast.makeText(this, "후기가 게시되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 더보기 버튼 (옵션 메뉴 필요 시 구현)
        moreBtn.setOnClickListener {
            Toast.makeText(this, "더보기 메뉴 클릭됨", Toast.LENGTH_SHORT).show()
        }
    }
}
