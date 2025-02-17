/*레시피 작성 동영상 STEP1*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private lateinit var hourEditText: EditText
private lateinit var minuteEditText: EditText
private lateinit var startTextView: TextView
private lateinit var timeSeparator: TextView
private lateinit var deleteTextView: TextView
private var countDownTimer: CountDownTimer? = null
private var timeInMillis: Long = 0

class RecipeWriteImageCookOrder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_image_cook_order)

        // nextFixButton 클릭했을 때 RecipeWriteImageDetailSettle 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageDetailSettle::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteImageTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteImageMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageMaterial::class.java)
            startActivity(intent)
        }

        // three 클릭했을 때 RecipeWriteImageReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageReplaceMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteImageHandlingMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageHandlingMethod::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteImageDetailSettle 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteImageDetailSettle::class.java)
            startActivity(intent)
        }

        // 1-1. 레시피를 입력해주세요.에 입력된 텍스트 가져오기
        val recipeWrite = findViewById<EditText>(R.id.recipeWrite)

        // 타이머 추가 버튼 클릭 이벤트
        val timerAddButton = findViewById<Button>(R.id.timerAdd)
        val contentAddButton = findViewById<Button>(R.id.contentAdd)

        val timer = findViewById<TextView>(R.id.timer)
        val hour = findViewById<EditText>(R.id.hour)
        val time = findViewById<TextView>(R.id.time)
        val minute = findViewById<EditText>(R.id.minute)
        val start = findViewById<TextView>(R.id.start)
        val middle = findViewById<TextView>(R.id.middle)
        val delete = findViewById<TextView>(R.id.delete)

        timerAddButton.setOnClickListener {
            // 버튼들 사라지게 하기
            timerAddButton.visibility = View.GONE
            contentAddButton.visibility = View.GONE

            // 타이머 관련 요소들 나타나게 하기
            timer.visibility = View.VISIBLE
            hour.visibility = View.VISIBLE
            time.visibility = View.VISIBLE
            minute.visibility = View.VISIBLE
            start.visibility = View.VISIBLE
            middle.visibility = View.VISIBLE
            delete.visibility = View.VISIBLE
        }

        // 이전 화면으로 이동
        val backArrow: ImageButton = findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        hourEditText = findViewById(R.id.hour)
        minuteEditText = findViewById(R.id.minute)
        startTextView = findViewById(R.id.start)
        timeSeparator = findViewById(R.id.time)
        deleteTextView = findViewById(R.id.delete)

        // 시작 버튼 클릭 이벤트
        startTextView.setOnClickListener {
            startTimer()
        }

        // 삭제 버튼 클릭 이벤트 (타이머 초기화)
        deleteTextView.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        // 입력된 시간 가져오기
        val hours = parseEditText(hourEditText)
        val minutes = parseEditText(minuteEditText)

        // 시간을 밀리초 단위로 변환
        timeInMillis = ((hours * 60L) + minutes) * 60 * 1000

        // 타이머가 0 이하라면 실행하지 않음
        if (timeInMillis <= 0) return

        // 기존 타이머 취소 (새로 시작할 때 중복 방지)
        countDownTimer?.cancel()

        // 타이머 실행
        countDownTimer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val displayHours = totalSeconds / 3600
                val displayMinutes = (totalSeconds % 3600) / 60

                hourEditText.setText(String.format("%02d", displayHours))
                minuteEditText.setText(String.format("%02d", displayMinutes))
            }

            override fun onFinish() {
                resetTimer()
            }
        }.start()
    }

    private fun resetTimer() {
        countDownTimer?.cancel() // 실행 중인 타이머 중지
        countDownTimer = null // 타이머 객체 초기화

        hourEditText.setText("00") // 시간 초기화
        minuteEditText.setText("00") // 분 초기화
    }

    // EditText에서 숫자 가져오기 (비어있으면 0 반환)
    private fun parseEditText(editText: EditText): Int {
        return editText.text.toString().trim().toIntOrNull() ?: 0
    }
}
