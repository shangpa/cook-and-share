package com.example.test

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.test.model.community.CommunityPostRequest
import com.example.test.model.community.CommunityPostResponse
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CommunityEditPostActivity : AppCompatActivity() {

    private var isPickingRepresentImage = false
    private val imageUrlList = mutableListOf<String>()
    private var categoryKey: String = "COOKING"
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
        val postButton = findViewById<AppCompatButton>(R.id.postButton)
        val useQuestion = findViewById<ConstraintLayout>(R.id.useQuestion)
        val cancel = findViewById<Button>(R.id.cancel)
        val postCategory = findViewById<TextView>(R.id.postCategory)
        val postDropDown = findViewById<ImageView>(R.id.postDropDown)
        val content = findViewById<EditText>(R.id.content)


        register.setOnClickListener {
            startActivity(Intent(this, CommunityDetailActivity::class.java))
        }

        image.setOnClickListener {
            isPickingRepresentImage = true
            pickImageLauncherForDetailSettle.launch("image/*")
        }


        val showCategoryMenu = View.OnClickListener {
            val popup = PopupMenu(this, it)
            val categories = listOf("자유게시판", "요리게시판")

            categories.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item ->
                postCategory.text = item.title
                categoryKey = when (item.title) {
                    "자유게시판" -> "FREE"
                    "요리게시판" -> "COOKING"
                    else -> "COOKING"
                }
                true
            }

            popup.show()
        }

        postCategory.setOnClickListener(showCategoryMenu)
        postDropDown.setOnClickListener(showCategoryMenu)

        //게시하기 등록 버튼누르기
        postButton.setOnClickListener {
            useQuestion.visibility = View.VISIBLE
        }

        content.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFilled = s?.isNotBlank() == true

                if (isFilled) {
                    postButton.setBackgroundResource(R.drawable.btn_big_green)
                    postButton.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    postButton.setBackgroundResource(R.drawable.btn_number_of_people)
                    postButton.setTextColor(Color.parseColor("#A1A9AD"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        register.setOnClickListener{
            val content = findViewById<EditText>(R.id.content).text.toString().trim()

            if (content.isEmpty()) {
                Toast.makeText(this, "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gson = Gson()
            val request = CommunityPostRequest(
                content = content,
                imageUrls = imageUrlList,
                boardType = categoryKey
            )


            val token = App.prefs.token ?: ""
            println("게시글 boardType : $request.boardType")
            RetrofitInstance.communityApi.uploadPost("Bearer $token", request)
                .enqueue(object : Callback<CommunityPostResponse> {
                    override fun onResponse(call: Call<CommunityPostResponse>, response: Response<CommunityPostResponse>) {
                        if (response.isSuccessful) {
                            val postId = response.body()?.id
                            println("게시글 업로드 성공! ID: $postId")
                            finish()
                        } else {
                            println("업로드 실패 - 코드: ${response.code()}, 에러바디: ${response.errorBody()?.string()}")
                            Toast.makeText(this@CommunityEditPostActivity, "업로드 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CommunityPostResponse>, t: Throwable) {
                        Toast.makeText(this@CommunityEditPostActivity, "서버 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        cancel.setOnClickListener {
            useQuestion.visibility = View.GONE
        }
        /* //이름 가져오기
        val userNameText = findViewById<TextView>(R.id.id)
        val token = App.prefs.token.toString()
        if (token.isNotEmpty()) {
            RetrofitInstance.apiService.getUserInfo("Bearer $token")
                .enqueue(object : Callback<LoginInfoResponse> {
                    override fun onResponse(call: Call<LoginInfoResponse>, response: Response<LoginInfoResponse>) {
                        if (response.isSuccessful) {
                            val userInfo = response.body()
                            userInfo?.let {
                                userNameText.text = it.userName
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                        userNameText.text = "사용자"
                    }
                })
        }*/

    }
    fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
        val file = uriToFile(this, uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val token = App.prefs.token.toString()
        RetrofitInstance.apiService.uploadImage("Bearer $token", body)
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

    // UCrop 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                addImageToContainer(it)

                // ✅ 이미지 서버 업로드 추가
                uploadImageToServer(it) { imageUrl ->
                    if (imageUrl != null) {
                        imageUrlList.add(imageUrl)
                        println("이미지 업로드 성공: $imageUrl")
                    } else {
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

}
