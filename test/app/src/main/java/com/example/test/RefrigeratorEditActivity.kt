package com.example.test.ui.fridge

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.test.R
import com.google.android.material.card.MaterialCardView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RefrigeratorEditActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etMemo: EditText
    private lateinit var img: ImageView
    private lateinit var btnSave: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: Button
    private lateinit var tvPick: TextView

    private var mode: String = "create"
    private var id: Long? = null
    private var imageFile: File? = null
    private var remoteImageUrl: String? = null
    private var cameraFile: File? = null
    private var cameraOutputUri: Uri? = null

    /** 권한 요청 (카메라 + 이미지 읽기) */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> showImagePickDialog() }

    /** 갤러리에서 이미지 가져오기 */
    private val pickGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        imageFile = copyUriToCache(uri)
        preview(imageFile)
    }

    /** 카메라 촬영 */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 미리 만든 파일 그대로 사용
            imageFile = cameraFile
            preview(imageFile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refrigerator_edit)

        etName = findViewById(R.id.etName)
        etMemo = findViewById(R.id.etMemo)
        img = findViewById(R.id.imgPreview)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
        tvPick = findViewById(R.id.tvPick)

        mode = intent.getStringExtra("mode") ?: "create"
        if (mode == "edit") {
            id = intent.getLongExtra("id", -1L)
            etName.setText(intent.getStringExtra("name") ?: "")
            etMemo.setText(intent.getStringExtra("memo") ?: "")
            remoteImageUrl = intent.getStringExtra("imageUrl")
            remoteImageUrl?.let { raw ->
                val model: Any = when {
                    raw.startsWith("http", true) || raw.startsWith("content://", true) || raw.startsWith("file://", true) -> raw
                    raw.startsWith("/") -> java.io.File(raw) // 절대경로
                    else -> raw
                }
                Glide.with(this).load(model).placeholder(R.drawable.img_kitchen1).into(img)
                tvPick.text = ""
            }

            btnDelete.visibility = Button.VISIBLE
            supportActionBar?.title = "냉장고 편집"
        } else {
            btnDelete.visibility = Button.GONE
            supportActionBar?.title = "냉장고 추가"
        }

        findViewById<MaterialCardView>(R.id.cardImage).setOnClickListener { onClickAddPhoto() }
        tvPick.setOnClickListener { onClickAddPhoto() }
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { onSave() }
        btnDelete.setOnClickListener { onDelete() }
    }

    private fun onClickAddPhoto() {
        val needs = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= 33) {
            needs += Manifest.permission.READ_MEDIA_IMAGES
        } else {
            needs += Manifest.permission.READ_EXTERNAL_STORAGE
        }
        permissionLauncher.launch(needs.toTypedArray())
    }

    /** 갤러리/카메라 선택 다이얼로그 */
    private fun showImagePickDialog() {
        val items = arrayOf("갤러리에서 선택", "카메라로 촬영")
        AlertDialog.Builder(this)
            .setItems(items) { _: DialogInterface, which: Int ->
                when (which) {
                    0 -> pickGallery.launch("image/*")
                    1 -> openCamera()
                }
            }.show()
    }

    /** 카메라 열기 */
    private fun openCamera() {
        val file = createTempImageFile()
        cameraFile = file

        val uri = androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
        cameraOutputUri = uri

        takePicture.launch(uri)
    }


    private fun preview(file: File?) {
        val model: Any = cameraOutputUri ?: file ?: return
        Glide.with(this).load(model)
            .placeholder(R.drawable.img_kitchen1)
            .into(img)
        tvPick.text = ""
    }



    private fun onSave() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val memo = etMemo.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }

        if (name.isBlank()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val result = Intent().apply {
            putExtra("result_mode", mode)
            putExtra("result_id", id ?: System.currentTimeMillis())
            putExtra("result_name", name)
            putExtra("result_memo", memo)
            putExtra("result_imageUrl", imageFile?.absolutePath ?: remoteImageUrl)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    private fun onDelete() {
        val result = Intent().apply {
            putExtra("result_mode", "delete")
            putExtra("result_id", id ?: -1L)
        }
        setResult(RESULT_OK, result)
        finish()
    }

    // ---------------------- 파일/유틸 ----------------------

    private fun createTempImageFile(): File {
        val dir = File(cacheDir, "images").apply { if (!exists()) mkdirs() }
        val name = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.KOREA).format(java.util.Date())
        return File(dir, "IMG_${name}.jpg").apply { if (!exists()) createNewFile() }
    }



    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val file = createTempImageFile()
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
