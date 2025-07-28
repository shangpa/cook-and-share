package com.example.test

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
import com.example.test.Utils.TabBarUtils
import com.example.test.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private lateinit var videoCameraLauncher: ActivityResultLauncher<Intent>

class RecipeWriteOneMinuteActivity : AppCompatActivity() {
    private var selectedVideoUri: Uri? = null
    private var isVideoUploading = false // 업로드 중 여부 체크
    private var recipeVideoUrl: String? = null  // 서버에 업로드된 영상 URL 저장용
    private var targetContainer: LinearLayout? = null  // 선택한 이미지가 추가될 컨테이너 저장

    private val videoTrimLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val trimmedUri = result.data?.getParcelableExtra<Uri>("trimmedUri")
            trimmedUri?.let {
                selectedVideoUri = it
                showVideoInfo(it)
                uploadVideoToServer(it)  // ✅ 트리밍된 영상 업로드
            }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val intent = Intent(this, VideoTrimActivity::class.java)
            intent.putExtra("videoUri", it)
            videoTrimLauncher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_one_minute)

        TabBarUtils.setupTabBar(this)

        val recipeTitleWrite = findViewById<EditText>(R.id.recipeTitleWrite)
        val camera = findViewById<ImageButton>(R.id.camera)
        val imageContainer = findViewById<LinearLayout>(R.id.imageContainer)
        val registerFixButton = findViewById<AppCompatButton>(R.id.registerFixButton)
        val shareFixButton = findViewById<AppCompatButton>(R.id.shareFixButton)
        val shareSettle = findViewById<ConstraintLayout>(R.id.shareSettle)
        val uncheck = findViewById<ImageButton>(R.id.uncheck)
        val uncheckTwo = findViewById<ImageButton>(R.id.uncheckTwo)
        val cancelButton = findViewById<AppCompatButton>(R.id.cancel)
        val settleButton = findViewById<AppCompatButton>(R.id.settle)
        val cancelTwoButton = findViewById<AppCompatButton>(R.id.cancelTwo)
        val recipeRegister = findViewById<ConstraintLayout>(R.id.recipeRegister)

        videoCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val videoUri = result.data?.data
                if (videoUri != null) {
                    selectedVideoUri = videoUri
                    showVideoInfo(videoUri)
                    uploadVideoToServer(videoUri)
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

        // 제목 입력되면 등록하기 색 바뀜
        recipeTitleWrite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 비어있지 않으면 색상 변경
                if (!s.isNullOrBlank()) {
                    registerFixButton.setBackgroundResource(R.drawable.btn_big_green)
                } else {
                    // 필요하다면 원래 색상으로 되돌리기
                    registerFixButton.setBackgroundResource(R.drawable.btn_number_of_people)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 공개설정 클릭시 공개설정 박스 나타남
        shareFixButton.setOnClickListener {
            shareSettle.visibility = View.VISIBLE
        }

        // 공개설정에서 체크 클릭시
        val checkButtons = listOf(uncheck, uncheckTwo)

        for (button in checkButtons) {
            button.setOnClickListener {
                // 모든 버튼을 uncheck 상태로
                checkButtons.forEach { it.setImageResource(R.drawable.ic_uncheck) }
                // 현재 누른 버튼만 check 상태로
                button.setImageResource(R.drawable.ic_check)
            }
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

    private fun uploadVideoToServer(uri: Uri) {
        Log.d("Upload", "영상 업로드 시작")
        isVideoUploading = true
        val inputStream = contentResolver.openInputStream(uri) ?: return
        val file = File(cacheDir, "upload_video.mp4")
        file.outputStream().use { inputStream.copyTo(it) }

        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("video", file.name, requestFile)

        val token = App.prefs.token ?: ""
        Log.d("JWT", "보내는 토큰: Bearer $token")

        RetrofitInstance.apiService.uploadVideo(body, "Bearer $token")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    isVideoUploading = false
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        val videoUrl = responseBody.string()
                        Log.d("Upload", "영상 업로드 성공: $videoUrl")
                        recipeVideoUrl = videoUrl
                        Log.d("Upload", "recipeVideoUrl 저장됨: $recipeVideoUrl")
                        Toast.makeText(this@RecipeWriteOneMinuteActivity, "동영상 업로드 성공!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("Upload", "업로드 실패 - 응답 없음 또는 실패 응답: ${response.code()}")
                        Toast.makeText(this@RecipeWriteOneMinuteActivity, "동영상 업로드 실패!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload", "업로드 실패: ${t.message}")
                    Toast.makeText(this@RecipeWriteOneMinuteActivity, "동영상 업로드 실패!", Toast.LENGTH_SHORT).show()
                }
            })
    }
}