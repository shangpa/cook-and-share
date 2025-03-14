package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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

        // 작성한 리뷰 보기 선언
        val writeReviewCheck = findViewById<ConstraintLayout>(R.id.writeReviewCheck)
        val heartIcon: ImageButton = findViewById(R.id.heartIcon)
        val goodButton: ImageButton = findViewById(R.id.goodButton)
        val shareButton: ImageButton = findViewById(R.id.shareButton)

        // 리뷰 작성하러 가기 버튼 클릭 시
        reviewWriteBtn.setOnClickListener {
            reviewWriteSuggestion.visibility = View.GONE

            writeReview.visibility = View.VISIBLE
            tapBar.visibility = View.VISIBLE
        }


        // 리뷰 작성하러 가기 버튼 클릭시
        registerButton.setOnClickListener {
            writeReview.visibility = View.GONE
            tapBar.visibility = View.GONE

            writeReviewCheck.visibility = View.VISIBLE

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

        heartIcon.setOnClickListener {
            com.example.test.isHeartFilled = !com.example.test.isHeartFilled // 상태 변경
            if (com.example.test.isHeartFilled) {
                heartIcon.setImageResource(R.drawable.ic_heart_fill) // 채워진 하트
            } else {
                heartIcon.setImageResource(R.drawable.ic_recipe_heart) // 빈 하트
            }
        }

        goodButton.setOnClickListener {
            com.example.test.isGoodFilled = !com.example.test.isGoodFilled // 상태 변경
            if (com.example.test.isGoodFilled) {
                goodButton.setImageResource(R.drawable.ic_good_fill) // 채워진 좋아요
            } else {
                goodButton.setImageResource(R.drawable.ic_good) // 빈 좋아요
            }
        }

        // 공유 버튼 클릭 시 공유 인텐트 실행
        shareButton.setOnClickListener {
            val blogUrl = "https://tekken5953.tistory.com/" // 공유할 링크
            val content = "링크를 공유했어요!\n어떤 링크인지 들어가서 확인해볼까요?" // 공유할 메시지

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND // 공유 액션 설정
                putExtra(Intent.EXTRA_TEXT, "$content\n\n$blogUrl") // 공유할 텍스트 & 링크
                type = "text/plain" // 공유할 데이터 유형 (텍스트)
            }

            startActivity(Intent.createChooser(shareIntent, "공유하기")) // 공유 다이얼로그 띄우기
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