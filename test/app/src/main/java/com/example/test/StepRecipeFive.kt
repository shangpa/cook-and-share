/*레시피 단계 STEP5*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Timer
import java.util.TimerTask

class StepRecipeFive : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_recipe_see5)

        // nextFixButton 클릭했을 때 StepRecipeSix 이동
        val nextFixButton: Button = findViewById(R.id.nextFixButton)
        nextFixButton.setOnClickListener {
            val intent = Intent(this, StepRecipeSix::class.java)
            startActivity(intent)
        }

        // 하트 버튼과 채워진 하트 버튼 선언
        val heartButton = findViewById<ImageButton>(R.id.heart)
        val heartFillButton = findViewById<ImageButton>(R.id.heartFill)

        // 하트 버튼 클릭 리스너 설정
        heartButton.setOnClickListener {
            // 하트 버튼을 숨기고 채워진 하트 버튼을 보이게 설정
            heartButton.visibility = View.GONE
            heartFillButton.visibility = View.VISIBLE
        }

        // 채워진 하트 클릭시 다시 그냥 하트로 돌아감
        heartFillButton.setOnClickListener {
            // 채워진 하트 버튼을 숨기고 하트 버튼을 보이게 설정
            heartFillButton.visibility = View.GONE
            heartButton.visibility = View.VISIBLE
        }

        // 좋아요 버튼과 좋아요 하트 버튼 선언
        val bellIcon = findViewById<ImageButton>(R.id.bellIcon)
        val bellFill = findViewById<ImageButton>(R.id.bellFill)

        // 좋아요 버튼 클릭 리스너 설정
        bellIcon.setOnClickListener {
            // 하트 버튼을 숨기고 채워진 하트 버튼을 보이게 설정
            bellIcon.visibility = View.GONE
            bellFill.visibility = View.VISIBLE
        }

        // 채워진 좋아요 클릭시 다시 그냥 좋아요로 돌아감
        bellFill.setOnClickListener {
            // 채워진 하트 버튼을 숨기고 하트 버튼을 보이게 설정
            bellFill.visibility = View.GONE
            bellIcon.visibility = View.VISIBLE
        }

        // 타이머 요소 선언
        val startButton = findViewById<Button>(R.id.start)
        val hourText = findViewById<TextView>(R.id.hour)
        val minuteText = findViewById<TextView>(R.id.minute)
        val colonText = findViewById<TextView>(R.id.colon)
        val stopButton = findViewById<Button>(R.id.stop)

        var hour = 15  // 시작 시간 (15시간)
        var minute = 0  // 시작 분 (0분)

        val handler = Handler(Looper.getMainLooper())  // UI 쓰레드에서 작업을 처리할 핸들러
        val timer = Timer()
        var timerTask: TimerTask? = null  // 타이머 작업을 취소할 수 있도록 저장

        // 타이머 시작 버튼 클릭
        startButton.setOnClickListener {
            // 타이머 시작
            startButton.isEnabled = false  // 시작 버튼을 비활성화하여 중복 클릭 방지
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (minute == 0 && hour == 0) {
                        timer.cancel()  // 시간이 다 되면 타이머를 멈춤
                    } else {
                        if (minute == 0) {
                            hour -= 1  // 시간이 0이면 1시간을 차감
                            minute = 59  // 분을 59로 설정
                        } else {
                            minute -= 1  // 분이 0이 아니면 분을 차감
                        }

                        // UI 업데이트 (핸들러를 통해 UI 변경)
                        handler.post {
                            hourText.text = hour.toString()
                            minuteText.text = String.format("%02d", minute)  // 두 자릿수로 표시
                        }
                    }
                }
            }, 0, 60000)  // 1분 간격으로 실행
        }

        stopButton.setOnClickListener {
            // 타이머 정지
            timerTask?.cancel()
            timer.cancel()

            // 버튼 상태 업데이트
            startButton.isEnabled = true  // 시작 버튼 활성화
            stopButton.isEnabled = false  // 정지 버튼 비활성화
        }
    }
}