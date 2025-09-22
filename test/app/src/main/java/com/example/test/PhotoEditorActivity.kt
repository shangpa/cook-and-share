package com.example.test

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.*
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import java.io.File
import java.io.FileOutputStream

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var photoEditor: PhotoEditor
    private lateinit var photoEditorView: PhotoEditorView
    private var currentImageUri: Uri? = null
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)

        photoEditorView = findViewById(R.id.photoEditorView)
        val imageUriStr = intent.getStringExtra("imageUri")
        val imageUri = imageUriStr?.let { Uri.parse(it) }

        if (imageUri == null) {
            Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentImageUri = imageUri
        Glide.with(this).load(imageUri).into(photoEditorView.source)

        photoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(true)
            .build()

        // 자르기 런처 등록
        cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val uri = result.uriContent
                uri?.let {
                    currentImageUri = it
                    Glide.with(this).load(it).into(photoEditorView.source)
                }
            } else {
                Toast.makeText(this, "자르기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 저장
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val outputDir = File(cacheDir, "edited")
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "edited_${System.currentTimeMillis()}.jpg")

            photoEditor.saveAsFile(outputFile.absolutePath, object : PhotoEditor.OnSaveListener {
                override fun onSuccess(imagePath: String) {
                    val originalFile = File(imagePath)

                    // 1. Bitmap으로 불러오기
                    val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

                    // 2. 크기 줄이기 (예: 가로 1080 유지, 비율 따라 세로 자동 계산)
                    val targetWidth = 1080
                    val scale = targetWidth.toFloat() / bitmap.width
                    val resizedBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        targetWidth,
                        (bitmap.height * scale).toInt(),
                        true
                    )

                    // 3. 압축 후 새 파일로 저장
                    val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(compressedFile)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // 품질 80%
                    outputStream.close()

                    // 4. 결과 전달
                    val resultIntent = Intent().apply {
                        putExtra("editedImageUri", Uri.fromFile(compressedFile).toString())
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

                override fun onFailure(exception: java.lang.Exception) {
                    Toast.makeText(this@PhotoEditorActivity, "이미지 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 그리기
        findViewById<ImageView>(R.id.btnDraw).setOnClickListener {
            photoEditor.setBrushDrawingMode(true)
        }

        // 지우개
        findViewById<ImageView>(R.id.btnErase).setOnClickListener {
            photoEditor.brushEraser()
        }

        // 텍스트 추가
        findViewById<ImageView>(R.id.btnText).setOnClickListener {
            val editText = EditText(this)
            editText.hint = "텍스트를 입력하세요"

            AlertDialog.Builder(this)
                .setTitle("텍스트 추가")
                .setView(editText)
                .setPositiveButton("추가") { _, _ ->
                    val inputText = editText.text.toString()
                    if (inputText.isNotBlank()) {
                        photoEditor.addText(inputText, Color.BLACK)
                    } else {
                        Toast.makeText(this, "텍스트가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 자르기
        findViewById<ImageView>(R.id.btnCrop).setOnClickListener {
            val uri = currentImageUri ?: return@setOnClickListener
            val options = CropImageContractOptions(
                uri,
                CropImageOptions().apply {
                    guidelines = CropImageView.Guidelines.ON
                    fixAspectRatio = false // ✅ 비율 자유롭게
                    cropMenuCropButtonTitle = "완료" // ✅ 완료 버튼 텍스트
                    toolbarColor = Color.DKGRAY
                    activityMenuIconColor = Color.WHITE
                    autoZoomEnabled = true
                    multiTouchEnabled = true
                }
            )
            cropImageLauncher.launch(options)
        }
    }
}
