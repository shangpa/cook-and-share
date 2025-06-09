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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.test.model.review.ReviewRequestDTO
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
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

private lateinit var imageContainer: LinearLayout
private var isHeartFilled = false // 하트 상태 저장
private var isGoodFilled = false // 하트 상태 저장
private var selectedRating = 0 // 선택된 점수
private val imageUrlList = mutableListOf<String>() //이미지 리스트 저장용
class ReviewWriteActivity : AppCompatActivity() {

    // 갤러리에서 선택한 이미지를 처리하는 콜백

    // 첫 번째 pickImageLauncher (camera 버튼용)
    private val pickImageLauncherForCamera =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                uploadImageToServer(it) { imageUrl ->
                    if (imageUrl != null) {

                        imageUrlList.add(imageUrl)
                        val fullImageUrl = RetrofitInstance.BASE_URL + imageUrl.trim()
                        val imageView = ImageView(this).apply {
                            val size = 150.dpToPx()
                            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                                setMargins(12.dpToPx(), 12.dpToPx(), 0, 12.dpToPx())
                            }
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            Glide.with(this@ReviewWriteActivity).load(fullImageUrl).into(this)
                        }

                        val photoContainer = findViewById<LinearLayout>(R.id.photoContainer)
                        photoContainer.addView(imageView)

                    } else {
                        Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    private fun uploadImageToServer(uri: Uri, callback: (String?) -> Unit) {
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
                    } else callback(null)
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_write)
        //토큰
        val token = App.prefs.token.toString()
        //레시피 id 불러오기
        val recipeId = intent.getLongExtra("recipeId", -1L)
        val recipeTitle = findViewById<TextView>(R.id.foodName)
        val recipeDifficulty= findViewById<TextView>(R.id.elementary)
        val recipeTime = findViewById<TextView>(R.id.halfTime)
        val recipeWriter = findViewById<TextView>(R.id.name)
        val recipeImage = findViewById<ImageView>(R.id.Image)
        // 리뷰 작성 권유하기 선언
        val reviewWriteSuggestion = findViewById<ConstraintLayout>(R.id.reviewWriteSuggestion)
        val reviewWriteBtn = findViewById<AppCompatButton>(R.id.reviewWriteBtn)
        val searchIcon = findViewById<ImageButton>(R.id.searchIcon)
        val bellIcon = findViewById<ImageButton>(R.id.bellIcon)
        val person = findViewById<ImageButton>(R.id.person)

        // 리뷰 작성하기 선언
        val writeReview = findViewById<ConstraintLayout>(R.id.writeReview)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val reviewContentWrite = findViewById<EditText>(R.id.reviewContentWrite)
        val cameraBtn = findViewById<ImageButton>(R.id.cameraBtn)
        val registerButton = findViewById<AppCompatButton>(R.id.registerButton)

        //레시피 정보 가져오기
        RetrofitInstance.apiService.getRecipeById("Bearer $token", recipeId)
            .enqueue(object : Callback<RecipeDetailResponse> {
                override fun onResponse(call: Call<RecipeDetailResponse>, response: Response<RecipeDetailResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val recipe = response.body()!!

                        recipeTitle.text = recipe.title
                        recipeDifficulty.text = recipe.difficulty
                        recipeTime.text = "${recipe.cookingTime}분"
                        recipeWriter.text = recipe.writer

                        if (recipe.mainImageUrl.isNotBlank()) {
                            val imageUrl = RetrofitInstance.BASE_URL + recipe.mainImageUrl.trim()
                            Glide.with(this@ReviewWriteActivity).load(imageUrl).into(recipeImage)
                        }
                    } else {
                        Toast.makeText(this@ReviewWriteActivity, "레시피 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@ReviewWriteActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })

        // 리뷰 작성하러 가기 버튼 클릭 시
        reviewWriteBtn.setOnClickListener {
            reviewWriteSuggestion.visibility = View.GONE

            writeReview.visibility = View.VISIBLE
            tapBar.visibility = View.VISIBLE
        }


        // 등록하기 버튼 클릭시
        registerButton.setOnClickListener {
            val content = reviewContentWrite.text.toString().trim()

            if (content.isBlank() || selectedRating == 0) {
                Toast.makeText(this, "내용과 평점을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gson = Gson()
            val request = ReviewRequestDTO(
                recipeId = recipeId,
                content = content,
                rating = selectedRating,
                mediaUrls = gson.toJson(imageUrlList)
            )

            RetrofitInstance.apiService.submitReview("Bearer $token", request)
                .enqueue(object : Callback<ReviewResponseDTO> {
                    override fun onResponse(call: Call<ReviewResponseDTO>, response: Response<ReviewResponseDTO>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ReviewWriteActivity, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ReviewWriteActivity, RecipeSeeNoTimerActivity::class.java)
                            intent.putExtra("recipeId", recipeId)
                            intent.putExtra("selectedTab", 2) // 0: 재료, 1: 조리순서, 2: 리뷰
                            startActivity(intent)
                            finish() // 또는 작성 완료 UI로 이동
                        } else {
                            Toast.makeText(this@ReviewWriteActivity, "리뷰 등록 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ReviewResponseDTO>, t: Throwable) {
                        Toast.makeText(this@ReviewWriteActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        //내용 입력하면 버튼 색 바뀜
        reviewContentWrite.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isNotEmpty = !s.isNullOrEmpty()
                if (isNotEmpty) {
                    registerButton.setBackgroundResource(R.drawable.btn_big_green)
                    registerButton.setTextColor(Color.parseColor("#FFFFFF"))
                } else {
                    // 원래 색상으로 복구
                    registerButton.setBackgroundResource(R.drawable.btn_number_of_people)
                    registerButton.setTextColor(Color.parseColor("#A1A9AD"))
                }
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        val stars = listOf(
            findViewById<ImageButton>(R.id.star),
            findViewById<ImageButton>(R.id.starTwo),
            findViewById<ImageButton>(R.id.starThree),
            findViewById<ImageButton>(R.id.starFour),
            findViewById<ImageButton>(R.id.starFive)
        )

        // 각 별에 클릭 이벤트 추가
        for (i in stars.indices) {
            stars[i].setOnClickListener {
                updateStars(stars, i) // 클릭된 별까지 채우기
            }
        }

        // 레시피 조리순서 카메라 버튼 클릭 시 갤러리 열기
        cameraBtn.setOnClickListener {
            pickImageLauncherForCamera.launch("image/*")
        }

        findViewById<ImageButton>(R.id.backArrow).setOnClickListener {
            finish()
        }

    }

    //dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun updateStars(stars: List<ImageButton>, clickedIndex: Int) {
        selectedRating = clickedIndex + 1 // ⭐ 1부터 시작하는 평점
        for (i in stars.indices) {
            if (i <= clickedIndex) {
                stars[i].setImageResource(R.drawable.ic_star) // 채워진 별
            } else {
                stars[i].setImageResource(R.drawable.ic_star_no_fill) // 빈 별
            }
        }
    }

}