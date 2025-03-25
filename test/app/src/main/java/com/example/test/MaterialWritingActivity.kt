package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MaterialWritingActivity : AppCompatActivity() {

    private lateinit var photoContainer: LinearLayout
    private lateinit var cameraCountText: TextView


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                photoContainer.visibility = View.VISIBLE

                val imageView = ImageView(this).apply {
                    val sizeInPx = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        150f,
                        resources.displayMetrics
                    ).toInt()

                    layoutParams = LinearLayout.LayoutParams(sizeInPx, sizeInPx).apply {
                        // 왼쪽 20dp, 위쪽 25dp margin
                        val leftMarginPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            20f,
                            resources.displayMetrics
                        ).toInt()
                        val topMarginPx = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            25f,
                            resources.displayMetrics
                        ).toInt()
                        setMargins(leftMarginPx, topMarginPx, 0, 0)
                    }

                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageURI(uri)
                }

                photoContainer.addView(imageView)

                // 사진 개수 텍스트 업데이트
                val count = photoContainer.childCount
                cameraCountText.text = "$count/10"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_writing) // 다른 프로필 화면의 레이아웃 파일 연결

        // 새로운 거래글 선언
        val newExchangeWrite = findViewById<ConstraintLayout>(R.id.newExchangeWrite)
        val photoContainer = findViewById<LinearLayout>(R.id.photoContainer)
        val cameraIcon = findViewById<ImageView>(R.id.ic_camera)
        val ic_arrow_down = findViewById<ImageView>(R.id.ic_arrow_down)
        val titleText = findViewById<EditText>(R.id.titleText)
        val quantityText = findViewById<EditText>(R.id.quantityText)
        val transactionPriceText = findViewById<EditText>(R.id.transactionPriceText)
        val purchaseDateText = findViewById<EditText>(R.id.purchaseDateText)
        val descriptionText = findViewById<EditText>(R.id.descriptionText)

        // 레시피 조리순서 카메라 버튼 클릭 시 갤러리 열기
        cameraIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


    }

    }




