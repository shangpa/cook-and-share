package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test.model.Fridge.FridgeStatsResponse
import com.example.test.network.RetrofitInstance
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
// import kotlinx.coroutines.CoroutineScope // ⚠️ 사용하지 않으므로 제거 권장
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FridgeStatsActivity : AppCompatActivity() {

    // 상단 카드의 수치 텍스트뷰들
    private lateinit var nowMaterialValue: TextView
    private lateinit var expirationDateEatValue: TextView
    private lateinit var expirationDateNotValue: TextView

    // 상단바 뒤로가기 아이콘
    private lateinit var backBtn: ImageView

    // MPAndroidChart의 파이 차트(도넛형으로 표시)
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 이 액티비티가 사용할 레이아웃(xml) 지정
        setContentView(R.layout.activity_fridge_stats)

        // ---------- View 바인딩(findViewById) ----------
        backBtn = findViewById(R.id.backBtn)
        nowMaterialValue = findViewById(R.id.nowMaterialValue)
        expirationDateEatValue = findViewById(R.id.expirationDateEatValue)
        expirationDateNotValue = findViewById(R.id.expirationDateNotValue)
        // fridge_graph.xml 안에 있는 PieChart(id: pieChart). include로 합쳐져 있으므로 여기서 바로 findViewById 가능
        pieChart = findViewById(R.id.pieChart)

        // ---------- 뒤로가기 버튼 ----------
        backBtn.setOnClickListener { finish() }

        // ---------- 로그인 토큰 체크 ----------
        // App.prefs.token 은 SharedPreferences 등에 저장된 인증 토큰이라고 가정
        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            // 미로그인: 안내 후 로그인 화면으로 이동하고 현재 액티비티 종료
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // ---------- 서버에서 통계 데이터 불러오기 ----------
        // lifecycleScope: 액티비티 생명주기와 함께 코루틴 관리(액티비티 종료 시 자동 취소)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Retrofit 호출: Authorization 헤더에 Bearer 토큰 포함
                val res = RetrofitInstance.apiService.getFridgeStats("Bearer $token")

                // UI 갱신은 Main 스레드에서
                withContext(Dispatchers.Main) {
                    when {
                        // 200 OK
                        res.isSuccessful -> res.body()?.let { updateUI(it) }

                        // 인증 만료/무효 토큰
                        res.code() == 401 -> {
                            startActivity(Intent(this@FridgeStatsActivity, LoginActivity::class.java))
                            finish()
                        }

                        // 그 외 실패
                        else -> {
                            Toast.makeText(
                                this@FridgeStatsActivity,
                                "통계 불러오기 실패(${res.code()})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (_: Exception) {
                // 네트워크 예외(타임아웃/연결 실패 등)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FridgeStatsActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 서버에서 받은 통계 응답을 UI에 반영
     */
    private fun updateUI(stats: FridgeStatsResponse) {
        // ---------- 카드 수치 표시(조건 2~4) ----------
        nowMaterialValue.text = stats.nowCount.toString()          // 현재 보관 재료 수
        expirationDateEatValue.text = stats.eatenCount.toString()  // 유통기한 내에 먹은 수
        expirationDateNotValue.text = stats.expiredCount.toString()// 유통기한 못 지킨 수

        // ---------- 도넛 차트 갱신 ----------
        setupPieChart(
            refrigerated = stats.refrigeratedCount, // 냉장
            frozen = stats.frozenCount,             // 냉동
            room = stats.roomCount                  // 실외/실온
        )
    }

    /**
     * 도넛형 파이차트 구성
     * @param refrigerated 냉장 개수
     * @param frozen 냉동 개수
     * @param room 실외(실온) 개수
     */
    private fun setupPieChart(refrigerated: Long, frozen: Long, room: Long) {
        val total = refrigerated + frozen + room

        // 모든 값이 0이면 "데이터 없음" 표시
        if (total == 0L) {
            pieChart.clear()
            pieChart.setNoDataText("데이터가 없습니다")
            pieChart.invalidate()
            return
        }

        // 파이 차트 엔트리(라벨은 XML의 legendRow 라벨과 동일하게 맞춤)
        val entries = listOf(
            PieEntry(refrigerated.toFloat(), "냉장"),
            PieEntry(frozen.toFloat(), "냉동"),
            PieEntry(room.toFloat(), "실외")
        )

        // 데이터셋 스타일 설정
        val dataSet = PieDataSet(entries, "").apply {
            // 범례 색상: fridge_graph.xml의 색상 점과 동일하게 매칭
            colors = listOf(
                Color.parseColor("#99D89F"), // 냉장
                Color.parseColor("#9CCAFF"), // 냉동
                Color.parseColor("#FFD48A")  // 실외
            )
            sliceSpace = 3f          // 조각 사이 간격
            valueTextSize = 12f      // 값 텍스트 크기
            valueTextColor = Color.BLACK
        }

        // 파이차트 공통 설정
        pieChart.apply {
            data = PieData(dataSet)      // 데이터 바인딩
            description.isEnabled = false// 우하단 디스크립션 텍스트 숨김
            legend.isEnabled = false     // 하단 커스텀 legendRow 사용하므로 기본 범례 끔
            setUsePercentValues(false)   // 퍼센트 표시 비활성화(정수 개수 그대로)
            setDrawEntryLabels(false)    // 조각 라벨 텍스트 숨김(시각 혼잡도↓)
            isDrawHoleEnabled = true     // 도넛 형태
            holeRadius = 60f             // 내부 원홀 반지름
            transparentCircleRadius = 65f// 투명 원 반지름(도넛 외곽)
            animateY(800)                // Y축 방향 애니메이션(부드러운 등장)
            invalidate()                 // 다시 그리기
        }
    }
}
