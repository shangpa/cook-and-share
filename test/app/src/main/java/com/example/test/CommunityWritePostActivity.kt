package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.yalantis.ucrop.UCrop
import java.io.File

class CommunityWritePostActivity : AppCompatActivity() {

    private var isPickingRepresentImage = false

    // 이미지 업로드 (갤러리에서 가져와 crop)
    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg"))
                UCrop.of(it, destinationUri)
                    .withMaxResultSize(800, 800)
                    .start(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_write_post)

        val register: Button = findViewById(R.id.register)
        val image = findViewById<ImageButton>(R.id.image)
        val postButton = findViewById<Button>(R.id.postButton)
        val useQuestion = findViewById<ConstraintLayout>(R.id.useQuestion)
        val cancel = findViewById<Button>(R.id.cancel)

        register.setOnClickListener {
            startActivity(Intent(this, CommunityDetailActivity::class.java))
        }

        image.setOnClickListener {
            isPickingRepresentImage = true
            pickImageLauncherForDetailSettle.launch("image/*")
        }

        postButton.setOnClickListener {
            useQuestion.visibility = View.VISIBLE
        }

        cancel.setOnClickListener {
            useQuestion.visibility = View.GONE
        }
    }

    // UCrop 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let { addImageToContainer(it) }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "이미지 자르기 실패: $cropError", Toast.LENGTH_SHORT).show()
        }
    }

    // 선택된 이미지 추가 함수
    private fun addImageToContainer(uri: Uri) {
        val container = findViewById<LinearLayout>(R.id.imageContainer)

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(217), dpToPx(246)).apply {
                topMargin = dpToPx(20)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }

        container.addView(imageView)
    }

    // dp → px 변환 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
}
