package com.example.test

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import java.io.File
import java.io.FileOutputStream

class PhotoEditorActivity : AppCompatActivity() {

    private lateinit var photoEditor: PhotoEditor
    private lateinit var photoEditorView: PhotoEditorView

    private var currentImageUri: Uri? = null
    private var initialImageUri: Uri? = null

    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)

        photoEditorView = findViewById(R.id.photoEditorView)

        // 인텐트로 받은 원본 이미지
        val imageUriStr = intent.getStringExtra("imageUri")
        val imageUri = imageUriStr?.let { Uri.parse(it) }
        if (imageUri == null) {
            Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentImageUri = imageUri
        initialImageUri = imageUri
        Glide.with(this).load(imageUri).into(photoEditorView.source)

        photoEditor = PhotoEditor.Builder(this, photoEditorView)
            .setPinchTextScalable(true)
            .build()

        // 갤러리(레이어 이미지 추가)
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uriToBitmap(uri)?.let { photoEditor.addImage(it) }
                    ?: Toast.makeText(this, "이미지를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 자르기
        cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                result.uriContent?.let {
                    currentImageUri = it
                    Glide.with(this).load(it).into(photoEditorView.source)
                }
            } else {
                Toast.makeText(this, "자르기 실패", Toast.LENGTH_SHORT).show()
            }
        }

        // 저장
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveCompressedResult() }

        // 그리기
        findViewById<ImageView>(R.id.btnDraw).setOnClickListener {
            photoEditor.setBrushDrawingMode(true)
        }

        // 지우개
        findViewById<ImageView>(R.id.btnErase).setOnClickListener {
            photoEditor.brushEraser()
        }

        // 텍스트
        findViewById<ImageView>(R.id.btnText).setOnClickListener {
            val input = EditText(this).apply { hint = "텍스트를 입력하세요" }
            AlertDialog.Builder(this)
                .setTitle("텍스트 추가")
                .setView(input)
                .setPositiveButton("추가") { _, _ ->
                    val txt = input.text.toString()
                    if (txt.isNotBlank()) photoEditor.addText(txt, Color.BLACK)
                    else Toast.makeText(this, "텍스트가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 자르기
        findViewById<ImageView>(R.id.btnCrop).setOnClickListener {
            val uri = currentImageUri ?: return@setOnClickListener
            val opts = CropImageContractOptions(
                uri,
                CropImageOptions().apply {
                    guidelines = CropImageView.Guidelines.ON
                    fixAspectRatio = false
                    cropMenuCropButtonTitle = "완료"
                    toolbarColor = Color.DKGRAY
                    activityMenuIconColor = Color.WHITE
                    autoZoomEnabled = true
                    multiTouchEnabled = true
                }
            )
            cropImageLauncher.launch(opts)
        }

        // 레이어(갤러리에서 이미지 추가)
        findViewById<ImageView>(R.id.btnLayer).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 왼쪽 회전
        findViewById<ImageView>(R.id.btnRotateLeft).setOnClickListener {
            photoEditorView.rotation = (photoEditorView.rotation - 90f) % 360f
        }

        // 오른쪽 회전
        findViewById<ImageView>(R.id.btnRotateRight).setOnClickListener {
            photoEditorView.rotation = (photoEditorView.rotation + 90f) % 360f
        }

        // 완전 초기화(처음 이미지로)
        findViewById<ImageView>(R.id.btnTrash).setOnClickListener {
            resetToInitialImage()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? =
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }

    private fun saveCompressedResult() {
        val outputDir = File(cacheDir, "edited").apply { if (!exists()) mkdirs() }
        val outputFile = File(outputDir, "edited_${System.currentTimeMillis()}.jpg")

        photoEditor.saveAsFile(outputFile.absolutePath, object : PhotoEditor.OnSaveListener {
            override fun onSuccess(imagePath: String) {
                val originalFile = File(imagePath)
                val bitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

                val targetWidth = 1080
                val scale = targetWidth.toFloat() / bitmap.width
                val resized = Bitmap.createScaledBitmap(
                    bitmap, targetWidth, (bitmap.height * scale).toInt(), true
                )

                val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                FileOutputStream(compressedFile).use { os ->
                    resized.compress(Bitmap.CompressFormat.JPEG, 80, os)
                }

                val result = Intent().apply {
                    putExtra("editedImageUri", Uri.fromFile(compressedFile).toString())
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }

            override fun onFailure(exception: Exception) {
                Toast.makeText(this@PhotoEditorActivity, "이미지 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** 처음 불러온 이미지로 완전 초기화 */
    private fun resetToInitialImage() {
        photoEditor.clearAllViews()
        photoEditor.setBrushDrawingMode(false)

        initialImageUri?.let {
            currentImageUri = it
            Glide.with(this).load(it).into(photoEditorView.source)
            photoEditorView.rotation = 0f
        } ?: run {
            Toast.makeText(this, "처음 이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}
