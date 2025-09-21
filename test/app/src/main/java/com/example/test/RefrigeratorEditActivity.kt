package com.example.test.ui.fridge

import android.Manifest
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.test.App
import com.example.test.R
import com.example.test.model.refrigerator.Refrigerator
import com.example.test.repository.RefrigeratorRepository
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RefrigeratorEditActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etMemo: EditText
    private lateinit var img: ImageView
    private lateinit var btnSave: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: Button
    private lateinit var tvPick: TextView

    private var mode: String = "create" // create or edit
    private var id: Long? = null
    private var imageFile: File? = null        // ← 업로드용 파일
    private var remoteImageUrl: String? = null

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

    /** 카메라 촬영 (파일로 저장) */
    private var cameraOutputUri: Uri? = null
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // cameraOutputUri 가 가리키는 실제 파일을 imageFile 로 사용
            val file = cameraOutputUri?.let { uriToFileFromOurCache(uri = it) }
            imageFile = file
            preview(file)
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
            if (!remoteImageUrl.isNullOrBlank()) {
                Glide.with(this).load(remoteImageUrl)
                    .placeholder(R.drawable.img_kitchen1).into(img)
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
        // 필요한 권한 체크 후 선택 다이얼로그
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
        // 앱 캐시에 안전한 파일 생성
        val file = createTempImageFile()
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )
        cameraOutputUri = uri
        // TakePicture는 우리가 넘긴 uri에 촬영본을 기록
        takePicture.launch(uri)
    }

    /** 이미지 미리보기 */
    private fun preview(file: File?) {
        if (file == null) return
        Glide.with(this).load(file).into(img)
        tvPick.text = ""
    }

    private fun onSave() {
        val token = App.prefs.token ?: run {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show(); return
        }
        val name = etName.text?.toString()?.trim().orEmpty()
        val memo = etMemo.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }

        if (name.isBlank()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (mode == "create") {
            RefrigeratorRepository.create(token, name, memo, imageFile)
                .enqueue(object : Callback<Refrigerator> {
                    override fun onResponse(call: Call<Refrigerator>, response: Response<Refrigerator>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RefrigeratorEditActivity, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else Toast.makeText(this@RefrigeratorEditActivity, "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<Refrigerator>, t: Throwable) {
                        Toast.makeText(this@RefrigeratorEditActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            val targetId = id ?: return
            RefrigeratorRepository.update(token, targetId, name, memo, imageFile)
                .enqueue(object : Callback<Refrigerator> {
                    override fun onResponse(call: Call<Refrigerator>, response: Response<Refrigerator>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RefrigeratorEditActivity, "수정되었습니다.", Toast.LENGTH_SHORT).show()
                            finish()
                        } else Toast.makeText(this@RefrigeratorEditActivity, "수정 실패", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<Refrigerator>, t: Throwable) {
                        Toast.makeText(this@RefrigeratorEditActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun onDelete() {
        val token = App.prefs.token ?: return
        val targetId = id ?: return

        RefrigeratorRepository.delete(token, targetId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RefrigeratorEditActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@RefrigeratorEditActivity, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RefrigeratorEditActivity, "오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ---------------------- 파일/유틸 ----------------------

    /** 카메라 촬영 결과 저장용 임시 파일 생성 (app cache/images/) */
    private fun createTempImageFile(): File {
        val dir = File(cacheDir, "images").apply { if (!exists()) mkdirs() }
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        return File(dir, "IMG_${time}.jpg").apply { if (!exists()) createNewFile() }
    }

    /** 우리 앱 캐시에 있는 Uri → File (카메라용) */
    private fun uriToFileFromOurCache(uri: Uri): File? {
        // FileProvider로 만든 uri는 직접 File 경로를 알고 있으므로
        // createTempImageFile()로 만든 경로 그대로 사용됨. 여기선 카메라 직후라 imageFile이 이미 생성되어 있음.
        // cameraOutputUri == content://..../images/IMG_xxx.jpg 이고 실제 파일은 cache/images/IMG_xxx.jpg
        return File(cacheDir, "images").listFiles()?.maxByOrNull { it.lastModified() }
    }

    /** 갤러리 Uri 를 앱 캐시로 복사해 File 로 반환 */
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
