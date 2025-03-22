package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
private lateinit var steps: List<View>
private var currentStep = 0
private lateinit var hourText: TextView
private lateinit var minuteText: TextView
private lateinit var startButton: Button
private lateinit var stopButton: Button

private var timer: CountDownTimer? = null
private var timeLeftInMillis: Long = 0L // 남은 시간 (밀리초)

class RecipeSeeActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_see)

        val endFixButton: Button = findViewById(R.id.endFixButton)
        endFixButton.setOnClickListener {
            val intent = Intent(this, ReveiwWriteActivity::class.java)
            startActivity(intent)
        }

        // 리뷰 작성하기 선언
        val peopleChoice = findViewById<ConstraintLayout>(R.id.peopleChoice)
        val zero = findViewById<EditText>(R.id.zero)
        val recipeSeeMain = findViewById<ConstraintLayout>(R.id.recipeSeeMain)
        val tapBar = findViewById<ConstraintLayout>(R.id.tapBar)
        val shareButton = findViewById<ImageButton>(R.id.shareButton)
        val cookButton = findViewById<Button>(R.id.cookButton)
        val nextFixButton = findViewById<Button>(R.id.nextFixButton)

        // 다음으로 버튼 선언
        steps = listOf(
            findViewById(R.id.recipeSeeMain),
            findViewById(R.id.recipeSeeOne),
            findViewById(R.id.recipeSeeTwo),
            findViewById(R.id.recipeSeeThree),
            findViewById(R.id.recipeSeeFour),
            findViewById(R.id.recipeSeeFive),
            findViewById(R.id.recipeSeeSix),
            findViewById(R.id.recipeSeeSeven)
        )

        // 다음으로 버튼 클릭시 다음 화면으로 이동
        nextFixButton.setOnClickListener {
            if (currentStep < steps.size - 1) {
                steps[currentStep].visibility = View.GONE
                currentStep++
                steps[currentStep].visibility = View.VISIBLE
            }
        }

        // 조리하기 버튼 클릭시 상자 보이기
        cookButton.setOnClickListener {
            peopleChoice.visibility = View.GONE
            recipeSeeMain.visibility = View.VISIBLE
            tapBar.visibility = View.VISIBLE
        }

        // 하트버튼 선언
        val heartButtons = listOf(
            findViewById<ImageButton>(R.id.heartButton),
            findViewById(R.id.heartButtonTwo),
            findViewById(R.id.heartButtonThree),
            findViewById(R.id.heartButtonFour),
            findViewById(R.id.heartButtonFive),
            findViewById(R.id.heartButtonSix),
            findViewById(R.id.heartButtonSeven),
            findViewById(R.id.heartButtonEight)
        )

        // 하트버튼 클릭시 채워진 하트로 바뀜
        heartButtons.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.heartButton, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.heartButton) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_recipe_heart)
                } else {
                    button.setImageResource(R.drawable.ic_heart_fill)
                    Toast.makeText(this, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.heartButton, !isLiked)
            }
        }


        // 좋아요 버튼 선언
        val goodButtons = listOf(
            findViewById<ImageButton>(R.id.goodButton),
            findViewById(R.id.goodButtonTwo),
            findViewById(R.id.goodButtonThree),
            findViewById(R.id.goodButtonFour),
            findViewById(R.id.goodButtonFive),
            findViewById(R.id.goodButtonSix),
            findViewById(R.id.goodButtonSeven),
            findViewById(R.id.goodButtonEight)
        )

        // 좋아요 버튼 클릭시 채워진 좋아요로 바뀜
        goodButtons.forEach { button ->
            // 초기 상태를 태그로 저장
            button.setTag(R.id.goodButton, false) // false: 좋아요 안 누름

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.goodButton) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_good)
                } else {
                    button.setImageResource(R.drawable.ic_good_fill)
                    Toast.makeText(this, "해당 레시피를 추천하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반전해서 저장
                it.setTag(R.id.goodButton, !isLiked)
            }
        }

        // 공유 버튼 선언
        val shareButtons = listOf(
            findViewById<ImageButton>(R.id.shareButton),
            findViewById(R.id.shareButtonTwo),
            findViewById(R.id.shareButtonThree),
            findViewById(R.id.shareButtonFour),
            findViewById(R.id.shareButtonFive),
            findViewById(R.id.shareButtonSix),
            findViewById(R.id.shareButtonSeven),
            findViewById(R.id.shareButtonEight)
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain" // 텍스트 공유
            putExtra(Intent.EXTRA_SUBJECT, "레시피 공유") // 제목 (선택)
            putExtra(Intent.EXTRA_TEXT, "링크를 공유했어요!\n" + "어떤 링크인지 들어가서 확인해볼까요?!\nhttps://your-recipe-link.com") // 공유할 내용
        }

        val chooser = Intent.createChooser(shareIntent, "공유할 앱을 선택하세요")

        shareButtons.forEach { button ->
            button.setOnClickListener {
                startActivity(chooser)
            }
        }

        hourText = findViewById(R.id.hour)
        minuteText = findViewById(R.id.minute)
        startButton = findViewById(R.id.start)
        stopButton = findViewById(R.id.stop)

        // 텍스트뷰에서 초기 시간 불러오기 (예: 15시간 00분 → 밀리초로)
        val hour = hourText.text.toString().toInt()
        val minute = minuteText.text.toString().toInt()
        timeLeftInMillis = ((hour * 60 + minute) * 60 * 1000).toLong()

        startButton.setOnClickListener {
            startTimer()
        }

        stopButton.setOnClickListener {
            stopTimer()
        }

        }

    private fun startTimer() {
        // 매번 버튼 누를 때 시간 텍스트 기준으로 다시 계산
        val hour = hourText.text.toString().toIntOrNull() ?: 0
        val minute = minuteText.text.toString().toIntOrNull() ?: 0
        timeLeftInMillis = ((hour * 60 + minute) * 60 * 1000).toLong()

        if (timeLeftInMillis <= 0) return // 0이면 실행 X

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                // 필요 시 동작
            }
        }.start()
    }


    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun updateTimerText() {
        val totalMinutes = (timeLeftInMillis / 1000) / 60
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        hourText.text = String.format("%02d", hours)
        minuteText.text = String.format("%02d", minutes)
    }
    }