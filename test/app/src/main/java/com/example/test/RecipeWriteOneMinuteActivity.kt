package com.example.test

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
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
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class RecipeWriteOneMinuteActivity : AppCompatActivity() {
    private var selectedVideoUri: Uri? = null
    private var isVideoUploading = false // 업로드 중 여부 체크
    private var recipeVideoUrl: String? = null  // 서버에 업로드된 영상 URL 저장용
    private var targetContainer: LinearLayout? = null  // 선택한 이미지가 추가될 컨테이너 저장
    private var isPublicFlag: Boolean = true
    private lateinit var videoCameraLauncher: ActivityResultLauncher<Intent>
    private var isPickingRepresentImage = false
    private lateinit var imageContainerTwo: LinearLayout
    private var mainImageUrl: String = "" // 대표 이미지 저장용 변수
    private var selectedContainer: LinearLayout? = null
    private val stepImages  =  mutableMapOf<Int, String>()
    private val videoTrimLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val trimmedUri = result.data?.getParcelableExtra<Uri>("trimmedUri")
            trimmedUri?.let {
                selectedVideoUri = it
                showVideoInfo(it)
                uploadVideoFileOnly(it)  // ✅ 변경: 파일만 업로드
            }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, ShortsTrimActivity::class.java)
            intent.putExtra("videoUri", it)
            videoTrimLauncher.launch(intent)
        }
    }

    private val pickImageLauncherForDetailSettle =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val destinationUri = Uri.fromFile(File(cacheDir, "cropped_represent_${System.currentTimeMillis()}.jpg"))
                val intent = Intent(this, PhotoEditorActivity::class.java).apply {
                    putExtra("imageUri", it.toString())
                }
                startActivityForResult(intent, EDIT_IMAGE_REQUEST_CODE)
            }
        }

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

        // 숏츠로 이동 (플로팅)
        findViewById<AppCompatButton>(R.id.register).setOnClickListener {
            startActivity(Intent(this, ShortsActivity::class.java))
        }

        videoCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val videoUri = result.data?.data
                if (videoUri != null) {
                    selectedVideoUri = videoUri
                    showVideoInfo(videoUri)
                    uploadVideoFileOnly(videoUri)  // ✅ 변경
                }
            }
        }

        // 카메라 클릭시
        camera.setOnClickListener {
            targetContainer = imageContainer
            // AlertDialog로 두 가지 선택지 제공
            AlertDialog.Builder(this)
                .setTitle("동영상 가져오기")
                .setItems(arrayOf("카메라 촬영", "앨범에서 선택")) { _, which ->
                    when (which) {
                        0 -> {
                            launchVideoCamera() // 위에서 만든 함수 그대로 사용!
                        }
                        1 -> {
                            // 🔵 갤러리에서 동영상 선택
                            videoPickerLauncher.launch("video/*")
                        }
                    }
                }.show()
        }

        // 대표 사진 카메라 버튼 클릭 시 갤러리 열기
        cameraTwo.setOnClickListener {
            isPickingRepresentImage = true
            pickImageLauncherForDetailSettle.launch("image/*")
        }

        // 제목 입력되면 등록하기 색 바뀜
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
            val body = ShortsCreateRequest(title, videoUrl, isPublicFlag)

            RetrofitInstance.apiService.registerShorts(body, "Bearer $token")
                .enqueue(object : Callback<Long> {
                    override fun onResponse(call: Call<Long>, response: Response<Long>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RecipeWriteOneMinuteActivity, "등록 성공!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@RecipeWriteOneMinuteActivity, ShortsActivity::class.java).apply {
                            }
                            startActivity(intent)
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

        // 임시저장 버튼 클릭시 여부 나타남
        temporaryStorageBtn.setOnClickListener {
            transientStorageLayout.visibility = View.VISIBLE
        }

        // 임시저장 취소 클릭시 임시저장 여부 없어짐
        btnCancel.setOnClickListener {
            transientStorage.visibility = View.GONE
        }

        // 임시저장 저장 클릭시 홈으로 이동
        btnStore.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            finish()
        }

        // 공개설정 클릭시 공개설정 박스 나타남
        shareFixButton.setOnClickListener {
            shareSettle.visibility = View.VISIBLE
        }

        // 공개설정에서 체크 클릭시
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

        //공개설정에서 취소 클릭시 공개설정 박스 없어짐
        cancelButton.setOnClickListener {
            shareSettle.visibility = View.GONE
        }

        // 공개설정에서 설정 누르면 레시피 등록 여부 박스 나타남
        settleButton.setOnClickListener {
            shareSettle.visibility = View.GONE
            recipeRegister.visibility = View.VISIBLE
        }

        // 레시피 등록 여부에서 취소 누르면 없어짐
        cancelTwoButton.setOnClickListener {
            recipeRegister.visibility = View.GONE
        }


        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
            finish()
        }
        updateRegisterButtonState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val editedUriStr = data?.getStringExtra("editedImageUri")
            val editedUri = editedUriStr?.let { Uri.parse(it) }

            editedUri?.let {
                if (isPickingRepresentImage) {
                    // imageContainerTwo는 이제 초기화되어 있음
                    displaySelectedImage(it, imageContainerTwo)
                    uploadImageToServer(it) { imageUrl ->
                        if (imageUrl != null) {
                            mainImageUrl = imageUrl
                        }
                    }
                    isPickingRepresentImage = false
                }
            }
        }
    }

    private fun updateRegisterButtonState() {
        val btn = findViewById<AppCompatButton>(R.id.registerFixButton)
        val titleFilled = findViewById<EditText>(R.id.recipeTitleWrite).text.toString().isNotBlank()
        val videoReady = !recipeVideoUrl.isNullOrBlank()
        val enabled = titleFilled && videoReady && !isVideoUploading

        // 디자인만 바꾸고 클릭은 유지(지금 UX 유지)
        if (enabled) {
            btn.setBackgroundResource(R.drawable.btn_big_green)
        } else {
            btn.setBackgroundResource(R.drawable.btn_number_of_people)
        }
        btn.isEnabled = true
    }

    private fun launchVideoCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)
    }

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

        // 캐시에 mp4 임시 파일 생성
        val file = File(cacheDir, "upload_video.mp4")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", file.name, requestFile)

        val token = App.prefs.token.orEmpty()
        if (token.isBlank()) {
            isVideoUploading = false
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ RecipeWriteVideoActivity에서 쓰던 것처럼 동일 엔드포인트 사용
        RetrofitInstance.apiService.uploadVideo(body, "Bearer $token")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    isVideoUploading = false
                    if (response.isSuccessful && response.body() != null) {
                        val raw = response.body()!!.string().trim()  // 서버가 돌려준 URL(상대/절대 모두 가능)
                        recipeVideoUrl = raw

                        Toast.makeText(
                            this@RecipeWriteOneMinuteActivity,
                            "영상 업로드 성공!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ 프리뷰는 절대 URL로 변환해서 보여주기
                        showVideoPreview(resolveFullUrl(raw))
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
    // 서버가 '/uploads/..' 같은 상대경로를 주면 BASE_URL 붙여서 절대 URL로 변환
    private fun resolveFullUrl(serverValue: String): String {
        return if (serverValue.startsWith("http://") || serverValue.startsWith("https://")) {
            serverValue
        } else {
            "${RetrofitInstance.BASE_URL}$serverValue"
        }
    }
    private fun showVideoPreview(videoUrl: String) {
        val container = findViewById<LinearLayout>(R.id.imageContainer)
        container.removeAllViews()

        val videoView = android.widget.VideoView(this).apply {
            setVideoURI(Uri.parse(videoUrl)) // ← 그대로 사용 (BASE_URL 더하지 않음)
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

    //이미지선택
    private fun displaySelectedImage(uri: Uri, targetContainer: LinearLayout) {
        val imageView = ImageView(this)
        imageView.setImageURI(uri)
        val layoutParams = LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx())
        imageView.layoutParams = layoutParams
        targetContainer.addView(imageView) // 선택한 컨테이너에 이미지 추가
        Log.d("RecipeWriteImageActivity", "이미지 추가 완료! 대상 컨테이너: ${targetContainer.id}")
    }

    //백엔드 서버에 이미지 업로드
    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = uriToFile(this, uri) ?: return
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token ?: ""
        if (token.isEmpty()) {
            Log.e("Upload", "토큰이 없음!")
            callback(null) // 실패 시 null 반환
            return
        }

        Log.d("Upload", "이미지 업로드 시작 - 파일명: ${file.name}, 크기: ${file.length()} 바이트")

        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.string()
                        Log.d("Upload", "이미지 업로드 성공! URL: $imageUrl")
                        callback(imageUrl) // ✅ 성공 시 URL 반환
                    } else {
                        Log.e("Upload", "이미지 업로드 실패: 응답 코드 ${response.code()}, 오류 메시지: ${response.errorBody()?.string()}")
                        callback(null) // 실패 시 null 반환
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "네트워크 요청 실패: ${t.message}")
                    callback(null) // 실패 시 null 반환
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

        // 파일명이 비어있으면 기본 파일명 설정
        if (fileName.isNullOrEmpty()) {
            fileName = "temp_image_${System.currentTimeMillis()}.jpg"
        }

        val file = File(context.cacheDir, fileName)

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

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }


}