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
import com.bumptech.glide.Glide
import com.example.test.App
import com.example.test.R
import com.example.test.model.pantry.PantryCreateRequest
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryUpdateRequest
import com.example.test.network.RetrofitInstance
import com.google.android.material.card.MaterialCardView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.lifecycleScope
import com.example.test.PantryMaterialAddDetailActivity
import com.example.test.Utils.TabBarUtils
import com.example.test.model.pantry.PantryStockDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PantryEditActivity : AppCompatActivity() {

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

        // 🔹 서버 업로드
        uploadImageToServer(uri) { url ->
            if (url != null) {
                remoteImageUrl = url
                val displayUrl = RetrofitInstance.toAbsoluteUrl(url) 
                Glide.with(this).load(displayUrl)
                    .placeholder(R.drawable.image_pantry)
                    .into(img)
                tvPick.text = ""
            } else {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 카메라 촬영 */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // 미리 만든 파일 → Uri
            val uri = cameraOutputUri ?: return@registerForActivityResult

            // 🔹 서버 업로드
            uploadImageToServer(uri) { url ->
                if (url != null) {
                    remoteImageUrl = url
                    val displayUrl = RetrofitInstance.toAbsoluteUrl(url)
                    Glide.with(this).load(displayUrl)
                        .placeholder(R.drawable.image_pantry)
                        .into(img)
                    tvPick.text = ""
                } else {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_edit)

        TabBarUtils.setupTabBar(this)

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
                Glide.with(this).load(model).placeholder(R.drawable.image_pantry).into(img)
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


    private fun onSave() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val memo = etMemo.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }
        if (name.isBlank()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val raw = App.prefs.token
        val token = if (!raw.isNullOrBlank()) "Bearer $raw" else null
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (mode == "create") {
            val request = PantryCreateRequest(
                name = name,
                note = memo,
                imageUrl = remoteImageUrl
            )

            lifecycleScope.launch {
                try {
                    val created = withContext(Dispatchers.IO) {
                        // suspend fun createPantry(...)
                        RetrofitInstance.pantryApi.createPantry(token, request)
                    }
                    val result = Intent().apply {
                        putExtra("result_mode", "create")
                        putExtra("result_id", created.id)
                        putExtra("result_name", created.name)
                        putExtra("result_memo", created.note)
                        putExtra("result_imageUrl", created.imageUrl)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@PantryEditActivity, "생성 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

        } else if (mode == "edit" && id != null) {
            val request = PantryUpdateRequest(
                name = name,
                note = memo,
                imageUrl = remoteImageUrl
            )

            lifecycleScope.launch {
                try {
                    val updated = withContext(Dispatchers.IO) {
                        // suspend fun updatePantry(...)
                        RetrofitInstance.pantryApi.updatePantry(token, id!!, request)
                    }
                    val result = Intent().apply {
                        putExtra("result_mode", "edit")
                        putExtra("result_id", updated.id)
                        putExtra("result_name", updated.name)
                        putExtra("result_memo", updated.note)
                        putExtra("result_imageUrl", updated.imageUrl)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@PantryEditActivity, "수정 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun onDelete() {
        if (id == null) {
            Toast.makeText(this, "삭제할 냉장고 ID가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("삭제 확인")
            .setMessage("정말 이 냉장고를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                val raw = App.prefs.token
                val token = if (!raw.isNullOrBlank()) "Bearer $raw" else null
                if (token == null) {
                    Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            // suspend fun deletePantry(...)
                            RetrofitInstance.pantryApi.deletePantry(token, id!!)
                        }
                        val result = Intent().apply {
                            putExtra("result_mode", "delete")
                            putExtra("result_id", id ?: -1L)
                        }
                        setResult(RESULT_OK, result)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@PantryEditActivity, "삭제 실패: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun createTempImageFile(): File {
        val dir = File(cacheDir, "images").apply { if (!exists()) mkdirs() }
        val name = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.KOREA).format(java.util.Date())
        return File(dir, "IMG_${name}.jpg").apply { if (!exists()) createNewFile() }
    }
    private fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = copyUriToCache(uri) ?: return callback(null)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = "Bearer " + (App.prefs.token ?: "")
        RetrofitInstance.apiService.uploadImage(token, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        callback(imageUrl)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(null)
                }
            })
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
