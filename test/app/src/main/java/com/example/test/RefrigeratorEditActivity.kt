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
import com.example.test.App
import com.example.test.R
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

        // 🔹 서버 업로드
        uploadImageToServer(uri) { url ->
            if (url != null) {
                remoteImageUrl = url
                // 미리보기
                Glide.with(this).load(url)
                    .placeholder(R.drawable.img_kitchen1)
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
                    Glide.with(this).load(url)
                        .placeholder(R.drawable.img_kitchen1)
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


    private fun onSave() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val memo = etMemo.text?.toString()?.trim().takeIf { !it.isNullOrBlank() }

        if (name.isBlank()) {
            Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val token = App.prefs.token.toString()
        // 서버에 보낼 body
        val body = com.example.test.model.pantry.PantryCreateRequest(
            name = name,
            note = memo,
            imageUrl = remoteImageUrl, // 🔹 여기서 서버에 업로드된 URL 전달
            isDefault = false,
            sortOrder = 0
        )

        RetrofitInstance.pantryApi.createPantry("Bearer $token", body)

            .enqueue(object : retrofit2.Callback<com.example.test.model.pantry.PantryResponse> {
                override fun onResponse(
                    call: Call<com.example.test.model.pantry.PantryResponse>,
                    response: Response<com.example.test.model.pantry.PantryResponse>
                ) {
                    if (response.isSuccessful) {
                        val created = response.body()
                        Toast.makeText(this@RefrigeratorEditActivity, "저장 완료", Toast.LENGTH_SHORT).show()

                        val result = Intent().apply {
                            putExtra("result_mode", mode)
                            putExtra("created_id", created?.id ?: -1L)
                            putExtra("created_name", created?.name ?: name)
                            putExtra("created_memo", created?.note ?: memo)
                            putExtra("created_imageUrl", created?.imageUrl ?: remoteImageUrl)
                        }
                        setResult(RESULT_OK, result)
                        finish()
                    } else {
                        val msg = response.errorBody()?.string() ?: "저장 실패(${response.code()})"
                        Toast.makeText(this@RefrigeratorEditActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(
                    call: Call<com.example.test.model.pantry.PantryResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(this@RefrigeratorEditActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun onDelete() {
        val result = Intent().apply {
            putExtra("result_mode", "delete")
            putExtra("result_id", id ?: -1L)
        }
        setResult(RESULT_OK, result)
        finish()
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
