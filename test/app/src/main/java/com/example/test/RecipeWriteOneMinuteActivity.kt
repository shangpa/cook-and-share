@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.test

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.media3.common.util.UnstableApi
import com.example.test.RecipeWriteImageActivity.Companion.EDIT_IMAGE_REQUEST_CODE
import com.example.test.Utils.TabBarUtils
import com.example.test.model.ShortsCreateRequest
import com.example.test.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeWriteOneMinuteActivity : AppCompatActivity() {

    // ----------------------------------------------------
    // 권한/카메라 흐름 제어용
    // ----------------------------------------------------
    private enum class NextAction { NONE, CAPTURE }
    private var nextAction: NextAction = NextAction.NONE
    private var autoThumbUploading = false

    private val permsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            val cam = granted[Manifest.permission.CAMERA] == true
            val mic = granted[Manifest.permission.RECORD_AUDIO] == true
            if (cam && mic) {
                if (nextAction == NextAction.CAPTURE) launchVideoCamera()
            } else {
                Toast.makeText(this, "카메라/마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
            nextAction = NextAction.NONE
        }

    private lateinit var captureVideoLauncher: ActivityResultLauncher<Uri>
    private var pendingVideoUri: Uri? = null

    // ----------------------------------------------------
    // 상태/뷰 참조
    // ----------------------------------------------------
    private var selectedVideoUri: Uri? = null
    private var isVideoUploading = false
    private var recipeVideoUrl: String? = null
    private var targetContainer: LinearLayout? = null
    private var isPublicFlag: Boolean = true
    private var isPickingRepresentImage = false
    private lateinit var imageContainerTwo: LinearLayout
    private var mainImageUrl: String = ""
    private var selectedContainer: LinearLayout? = null
    private val stepImages = mutableMapOf<Int, String>()

    // ----------------------------------------------------
    // 결과 수신 런처들
    // ----------------------------------------------------
    private val videoTrimLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val trimmedUri = result.data?.getParcelableExtra<Uri>("trimmedUri")
                trimmedUri?.let {
                    selectedVideoUri = it
                    showVideoInfo(it)
                    uploadVideoFileOnly(it)
                }
            }
        }

    private val videoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(this, ShortsTrimActivity::class.java)
                intent.putExtra("videoUri", it)
                videoTrimLauncher.launch(intent)
            }
        }

    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                    putExtra("imageUri", it.toString())
                }
                startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
            }
        }

    // ----------------------------------------------------
    // 생명주기
    // ----------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_one_minute)

        TabBarUtils.setupTabBar(this)

        val recipeTitleWrite = findViewById<EditText>(R.id.recipeTitleWrite)
        val camera = findViewById<ImageButton>(R.id.camera)
        val cameraTwo = findViewById<ImageButton>(R.id.cameraTwo)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        imageContainerTwo = findViewById(R.id.imageContainerTwo)
        val registerFixButton = findViewById<AppCompatButton>(R.id.registerFixButton)
        val shareFixButton = findViewById<AppCompatButton>(R.id.shareFixButton)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val cancelButton = findViewById<AppCompatButton>(R.id.cancel)
        val settleButton = findViewById<AppCompatButton>(R.id.settle)
        val cancelTwoButton = findViewById<AppCompatButton>(R.id.cancelTwo)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)
        val temporaryStorageBtn = findViewById<AppCompatButton>(R.id.temporaryStorage)
        val transientStorageLayout = findViewById<ConstraintLayout>(R.id.transientStorage)
        val transientStorage = findViewById<ConstraintLayout>(R.id.transientStorage)
        val btnCancel = findViewById<AppCompatButton>(R.id.cancelThree)
        val btnStore = findViewById<AppCompatButton>(R.id.store)

        findViewById<AppCompatButton>(R.id.register).setOnClickListener {
            startActivity(Intent(this, ShortsActivity::class.java))
        }

        // 촬영/앨범 다이얼로그 (유지)
        camera.setOnClickListener {
            targetContainer = imageContainer
            AlertDialog.Builder(this)
                .setTitle("동영상 가져오기")
                .setItems(arrayOf("카메라 촬영", "앨범에서 선택")) { _, which ->
                    when (which) {
                        0 -> { // 촬영
                            val need = mutableListOf<String>()
                            if (checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED
                            ) need += Manifest.permission.CAMERA
                            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                                != PackageManager.PERMISSION_GRANTED
                            ) need += Manifest.permission.RECORD_AUDIO

                            if (need.isNotEmpty()) {
                                nextAction = NextAction.CAPTURE
                                permsLauncher.launch(need.toTypedArray())
                            } else {
                                launchVideoCamera()
                            }
                        }
                        1 -> { // 앨범
                            videoPickerLauncher.launch("video/*")
                        }
                    }
                }
                .show()
        }

        // 촬영 콜백
        captureVideoLauncher =
            registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
                val uri = pendingVideoUri
                pendingVideoUri = null
                if (success && uri != null) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        contentResolver.update(
                            uri,
                            ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) },
                            null,
                            null
                        )
                    }
                    selectedVideoUri = uri
                    showVideoInfo(uri)
                    uploadVideoFileOnly(uri)
                } else {
                    Toast.makeText(this, "촬영이 취소되었거나 실패했어요.", Toast.LENGTH_SHORT).show()
                }
            }

        // 대표 이미지 선택
        cameraTwo.setOnClickListener {
            isPickingRepresentImage = true
            pickImageLauncherForDetailSettle.launch("image/*")
        }

        recipeTitleWrite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRegisterButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        registerFixButton.setOnClickListener {
            if (isVideoUploading) {
                Toast.makeText(this, "영상 업로드 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val title = findViewById<EditText>(R.id.recipeTitleWrite).text.toString().trim()
            val videoUrl = recipeVideoUrl
            if (title.isBlank() || videoUrl.isNullOrBlank()) {
                Toast.makeText(this, "제목과 영상을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = App.prefs.token.orEmpty()
            val body = com.example.test.model.ShortsCreateRequest(
                title = title,
                videoUrl = videoUrl,
                thumbnailUrl = mainImageUrl.ifBlank { null }, // ✅ 대표사진 포함
                isPublic = isPublicFlag
            )

            RetrofitInstance.apiService.registerShorts(body, "Bearer $token")
                .enqueue(object : Callback<Long> {
                    override fun onResponse(call: Call<Long>, response: Response<Long>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RecipeWriteOneMinuteActivity, "등록 성공!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RecipeWriteOneMinuteActivity, ShortsActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@RecipeWriteOneMinuteActivity, "등록 실패(${response.code()})", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Long>, t: Throwable) {
                        Toast.makeText(this@RecipeWriteOneMinuteActivity, "등록 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 임시 저장 플로우
        temporaryStorageBtn.setOnClickListener {
            transientStorageLayout.visibility = View.VISIBLE
        }
        btnCancel.setOnClickListener {
            transientStorage.visibility = View.GONE
        }
        btnStore.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }

        // 공개 설정 플로우
        shareFixButton.setOnClickListener { shareSettle.visibility = View.VISIBLE }
        val checkButtons = listOf(uncheck, uncheckTwo)
        uncheck.setOnClickListener {
            checkButtons.forEach { it.setImageResource(R.drawable.ic_uncheck) }
            uncheck.setImageResource(R.drawable.ic_check)
            isPublicFlag = true
        }
        uncheckTwo.setOnClickListener {
            checkButtons.forEach { it.setImageResource(R.drawable.ic_uncheck) }
            uncheckTwo.setImageResource(R.drawable.ic_check)
            isPublicFlag = false
        }
        cancelButton.setOnClickListener { shareSettle.visibility = View.GONE }
        settleButton.setOnClickListener {
            shareSettle.visibility = View.GONE
            recipeRegister.visibility = View.VISIBLE
        }
        cancelTwoButton.setOnClickListener { recipeRegister.visibility = View.GONE }

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener { finish() }

        updateRegisterButtonState()
    }

    // ----------------------------------------------------
    // Activity Result (PhotoEditor)
    // ----------------------------------------------------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val editedUriStr = data?.getStringExtra("editedImageUri")
            val editedUri = editedUriStr?.let { Uri.parse(it) }
            editedUri?.let {
                if (isPickingRepresentImage) {
                    displaySelectedImage(it, imageContainerTwo)
                    uploadImageToServer(it) { imageUrl ->
                        if (imageUrl != null) mainImageUrl = imageUrl
                    }
                    isPickingRepresentImage = false
                }
            }
        }
    }

    // ----------------------------------------------------
    // UI 상태
    // ----------------------------------------------------
    private fun updateRegisterButtonState() {
        val btn = findViewById<AppCompatButton>(R.id.registerFixButton)
        val titleFilled = findViewById<EditText>(R.id.recipeTitleWrite).text.toString().isNotBlank()
        val videoReady = !recipeVideoUrl.isNullOrBlank()
        val enabled = titleFilled && videoReady && !isVideoUploading
        btn.setBackgroundResource(
            if (enabled) R.drawable.btn_big_green else R.drawable.btn_number_of_people
        )
        btn.isEnabled = true // 클릭 유지 (기존 UX)
    }

    // ----------------------------------------------------
    // 촬영/파일 생성
    // ----------------------------------------------------
    private fun createVideoUriViaMediaStore(): Uri? {
        val name = "VID_${System.currentTimeMillis()}.mp4"
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/MyApp")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        return contentResolver.insert(collection, values)
    }

    private fun launchVideoCamera() {
        val outputUri = createVideoUriViaMediaStore() ?: run {
            Toast.makeText(this, "임시 파일 생성 실패", Toast.LENGTH_SHORT).show()
            return
        }
        pendingVideoUri = outputUri
        try {
            captureVideoLauncher.launch(outputUri)
        } catch (e: Exception) {
            pendingVideoUri = null
            Toast.makeText(this, "카메라를 열 수 없어요: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ----------------------------------------------------
    // 미리보기/업로드
    // ----------------------------------------------------
    private fun showVideoInfo(uri: Uri) {
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "이름 없음"

        val container = findViewById<LinearLayout>(R.id.imageContainer)
        container.removeAllViews()
        val textView = TextView(this).apply {
            text = "선택한 동영상: $fileName"
            textSize = 16f
            setTextColor(Color.BLACK)
        }
        container.addView(textView)
    }

    private fun uploadVideoFileOnly(uri: Uri) {
        Log.d("Upload", "숏츠 파일 업로드 시작")
        isVideoUploading = true

        val inputStream = contentResolver.openInputStream(uri) ?: run {
            isVideoUploading = false
            Toast.makeText(this, "영상 파일을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(cacheDir, "upload_${System.currentTimeMillis()}.mp4")
        file.outputStream().use { output -> inputStream.copyTo(output) }

        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", file.name, requestFile)

        val token = App.prefs.token.orEmpty()
        if (token.isBlank()) {
            isVideoUploading = false
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.apiService.uploadVideo(body, "Bearer $token")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    isVideoUploading = false
                    if (response.isSuccessful && response.body() != null) {
                        val raw = response.body()!!.string().trim()

                        // 절대 URL로 정규화해서 보관
                        val fullUrl = RetrofitInstance.toVideoUrl(raw) ?: raw
                        recipeVideoUrl = fullUrl

                        Toast.makeText(
                            this@RecipeWriteOneMinuteActivity,
                            "영상 업로드 성공!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 미리보기 재생
                        showVideoPreview(fullUrl)

                        // 대표 이미지가 아직 없으면, 업로드된 영상의 1초 프레임을 임시 썸네일로 보여줌
                        if (mainImageUrl.isBlank()) {
                            showFallbackThumbnailFromVideo(fullUrl)
                        }

                        updateRegisterButtonState()
                    } else {
                        Log.e(
                            "Upload",
                            "실패 code=${response.code()} msg=${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            this@RecipeWriteOneMinuteActivity,
                            "영상 업로드 실패!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isVideoUploading = false
                    Log.e("Upload", "업로드 실패: ${t.message}")
                    Toast.makeText(
                        this@RecipeWriteOneMinuteActivity,
                        "업로드 실패: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun resolveFullUrl(serverValue: String): String =
        RetrofitInstance.toVideoUrl(serverValue) ?: serverValue

    private fun showVideoPreview(videoUrl: String) {
        val container = findViewById<LinearLayout>(R.id.imageContainer)
        container.removeAllViews()
        val videoView = android.widget.VideoView(this).apply {
            setVideoURI(Uri.parse(videoUrl))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600
            )
            setOnPreparedListener { mp ->
                mp.isLooping = true
                start()
            }
        }
        container.addView(videoView)
    }

    // REPLACE ENTIRE METHOD
    private fun showFallbackThumbnailFromVideo(uriOrUrl: String) {
        lifecycleScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                try {
                    val retriever = android.media.MediaMetadataRetriever()
                    if (uriOrUrl.startsWith("http", ignoreCase = true)) {
                        retriever.setDataSource(uriOrUrl, HashMap())
                    } else {
                        retriever.setDataSource(this@RecipeWriteOneMinuteActivity, Uri.parse(uriOrUrl))
                    }
                    val frame = retriever.getFrameAtTime(1_000_000) // 1초 프레임
                    retriever.release()
                    frame
                } catch (_: Exception) { null }
            }

            if (bmp != null) {
                // 1) 화면에 1장만 보여주기
                val container = findViewById<LinearLayout>(R.id.imageContainerTwo)
                container.removeAllViews()
                val iv = ImageView(this@RecipeWriteOneMinuteActivity).apply {
                    setImageBitmap(bmp)
                    layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
                container.addView(iv)

                // 2) 대표사진 자동 업로드(한 번만)
                if (mainImageUrl.isBlank() && !autoThumbUploading) {
                    autoThumbUploading = true
                    val uri = bitmapToTempJpegUri(bmp)
                    if (uri != null) {
                        uploadImageToServer(uri) { imageUrl ->
                            autoThumbUploading = false
                            if (!imageUrl.isNullOrBlank() && mainImageUrl.isBlank()) {
                                mainImageUrl = imageUrl
                            }
                        }
                    } else {
                        autoThumbUploading = false
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // 이미지 선택/업로드
    // ----------------------------------------------------
    private fun displaySelectedImage(uri: Uri, targetContainer: LinearLayout) {
        // ✅ 먼저 모두 지우고
        targetContainer.removeAllViews()

        val imageView = ImageView(this).apply {
            setImageURI(uri)
            layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        targetContainer.addView(imageView)
        Log.d("RecipeWriteImageActivity", "이미지 추가 완료! 대상 컨테이너: ${targetContainer.id}")
    }

    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = uriToFile(this, uri) ?: return
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token ?: ""
        if (token.isEmpty()) {
            Log.e("Upload", "토큰이 없음!")
            callback(null)
            return
        }

        Log.d("Upload", "이미지 업로드 시작 - 파일명: ${file.name}, 크기: ${file.length()} 바이트")

        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        Log.d("Upload", "이미지 업로드 성공! URL: $imageUrl")
                        callback(imageUrl)
                    } else {
                        Log.e("Upload", "이미지 업로드 실패: code ${response.code()}, msg ${response.errorBody()?.string()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "네트워크 요청 실패: ${t.message}")
                    callback(null)
                }
            })
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        var fileName: String? = null
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                it.moveToFirst()
                fileName = it.getString(nameIndex)
            }
        }
        if (fileName.isNullOrEmpty()) {
            fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        }

        val file = File(context.cacheDir, fileName!!)
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToTempJpegUri(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val file = File(cacheDir, "auto_thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density).toInt()
}
