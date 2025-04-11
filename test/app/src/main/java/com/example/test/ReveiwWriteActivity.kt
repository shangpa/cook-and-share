package com.example.test

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private lateinit var imageContainer: LinearLayout
private lateinit var representImageContainer: LinearLayout
private var isHeartFilled = false // 하트 상태 저장
private var isGoodFilled = false // 하트 상태 저장

class ReveiwWriteActivity : AppCompatActivity() {

    // 갤러리에서 선택한 이미지를 처리하는 콜백

    // 첫 번째 pickImageLauncher (camera 버튼용)
    private val pickImageLauncherForCamera =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                // 이미지를 동적으로 추가
                val imageView = ImageView(this)
                imageView.setImageURI(it) // 이미지 URI 설정
                val layoutParams =
                    LinearLayout.LayoutParams(336.dpToPx(), 261.dpToPx()) // 이미지 크기 설정
                imageView.layoutParams = layoutParams
                imageContainer.addView(imageView) // LinearLayout에 이미지 추가
                representImageContainer.addView(imageView) // LinearLayout에 이미지 추가
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
        val reviewWriteBtn = findViewById<Button>(R.id.reviewWriteBtn)
        val searchIcon = findViewById<ImageButton>(R.id.searchIcon)
        val bellIcon = findViewById<ImageButton>(R.id.bellIcon)
        val person = findViewById<ImageButton>(R.id.person)

        // 리뷰 작성하기 선언
        val writeReview = findViewById<ConstraintLayout>(R.id.writeReview)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val reviewContentWrite = findViewById<EditText>(R.id.reviewContentWrite)
        val cameraBtn = findViewById<ImageButton>(R.id.cameraBtn)
        val registerButton = findViewById<Button>(R.id.registerButton)

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
                            Glide.with(this@ReveiwWriteActivity).load(imageUrl).into(recipeImage)
                        }
                    } else {
                        Toast.makeText(this@ReveiwWriteActivity, "레시피 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RecipeDetailResponse>, t: Throwable) {
                    Toast.makeText(this@ReveiwWriteActivity, "서버 연결 실패", Toast.LENGTH_SHORT).show()
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
            writeReview.visibility = View.GONE
            tapBar.visibility = View.GONE

        }

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


    }

    //dp를 px로 변환하는 확장 함수
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun updateStars(stars: List<ImageButton>, clickedIndex: Int) {
        for (i in stars.indices) {
            if (i <= clickedIndex) {
                stars[i].setImageResource(R.drawable.ic_star) // 채워진 별
            } else {
                stars[i].setImageResource(R.drawable.ic_star_no_fill) // 빈 별
            }
        }
    }

}