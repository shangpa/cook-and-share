/*레시피 작성 STEP5*/
package com.example.test

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
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


class RecipeWriteBothCookOrder : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_cook_order)

        // nextFixButton 클릭했을 때 RecipeWriteBothCookVideo 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
            startActivity(intent)
        }

        // skipFixButton 클릭했을 때 StepRecipeNoTimer 이동
        val skipFixButton: Button = findViewById(R.id.skipFixButton)
        skipFixButton.setOnClickListener {
            val intent = Intent(this, StepRecipeNoTimer::class.java)
            startActivity(intent)
        }

        // one 클릭했을 때 RecipeWriteBothTitle 이동
        val one: TextView = findViewById(R.id.one)
        one.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothTitle::class.java)
            startActivity(intent)
        }

        // two 클릭했을 때 RecipeWriteBothMaterial 이동
        val two: TextView = findViewById(R.id.two)
        two.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothMaterial::class.java)
            startActivity(intent)
        }

        // three 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
        val three: TextView = findViewById(R.id.three)
        three.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
            startActivity(intent)
        }

        // four 클릭했을 때 RecipeWriteBothHandlindMethod 이동
        val four: TextView = findViewById(R.id.four)
        four.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothHandlindMethod::class.java)
            startActivity(intent)
        }

        // six 클릭했을 때 RecipeWriteBothCookVideo 이동
        val six: TextView = findViewById(R.id.six)
        six.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
            startActivity(intent)
        }

        // seven 클릭했을 때 RecipeWriteBothDetailSettle 이동
        val seven: TextView = findViewById(R.id.seven)
        seven.setOnClickListener {
            val intent = Intent(this, RecipeWriteBothDetailSettle::class.java)
            startActivity(intent)
        }

        // 1-1. 레시피를 입력해주세요.에 입력된 텍스트 가져오기
        val recipeWrite = findViewById<EditText>(R.id.recipeWrite)

        // 선언
        val timerAdd: Button = findViewById(R.id.timerAdd)
        val contentAddButton = findViewById<Button>(R.id.contentAdd)
        val timer = findViewById<TextView>(R.id.timer)
        val hour = findViewById<EditText>(R.id.hour)
        val time = findViewById<TextView>(R.id.time)
        val minute = findViewById<EditText>(R.id.minute)
        val start = findViewById<TextView>(R.id.start)
        val middle = findViewById<TextView>(R.id.middle)
        val delete = findViewById<TextView>(R.id.delete)
        val contentAdd = findViewById<TextView>(R.id.contentAdd)
        val recipeWriteTwo = findViewById<TextView>(R.id.recipeWriteTwo)
        val deleteTwo = findViewById<ImageButton>(R.id.deleteTwo)
        val divideRectangleBarThree = findViewById<View>(R.id.divideRectangleBarThree)

        // 타이머 버튼 클릭시
        timerAdd.setOnClickListener {
            // 버튼들 사라지게 하기
            timerAdd.visibility = View.GONE
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

        // 내용 추가하기 버튼 클릭시
        contentAdd.setOnClickListener {
            // 기존 요소 보이게 하기
            recipeWriteTwo.visibility = View.VISIBLE
            deleteTwo.visibility = View.VISIBLE
            divideRectangleBarThree.visibility = View.VISIBLE

            // 타이머 165dp 이동
            val params = timer.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin += 165
            timer.layoutParams = params

            // contentAdd 165dp 이동
            val contentParams = contentAdd.layoutParams as ViewGroup.MarginLayoutParams
            contentParams.topMargin += 165
            contentAdd.layoutParams = contentParams

            // timerAdd 165dp 이동
            val timerParams = timerAdd.layoutParams as ViewGroup.MarginLayoutParams
            timerParams.topMargin += 165
            timerAdd.layoutParams = timerParams
        }

        // 삭제 버튼 클릭시
        deleteTwo.setOnClickListener {
            // 버튼들 사라지게 하기
            recipeWriteTwo.visibility = View.GONE
            deleteTwo.visibility = View.GONE
            divideRectangleBarThree.visibility = View.GONE

            // 타이머 165dp 이동
            val params = timer.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin -= 165
            timer.layoutParams = params

            // contentAdd 165dp 이동
            val contentParams = contentAdd.layoutParams as ViewGroup.MarginLayoutParams
            contentParams.topMargin -= 165
            contentAdd.layoutParams = contentParams

            // timerAdd 165dp 이동
            val timerParams = timerAdd.layoutParams as ViewGroup.MarginLayoutParams
            timerParams.topMargin -= 165
            timerAdd.layoutParams = timerParams
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
