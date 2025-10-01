package com.example.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.test.model.pantry.PantryStatsResponse
import com.example.test.network.RetrofitInstance
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class PantryStatsActivity : AppCompatActivity() {

    private lateinit var expirationDateEatValue: TextView
    private lateinit var expirationDateNotValue: TextView
    private lateinit var topConsumedValue: TextView
    private lateinit var backBtn: ImageView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pantry_stats)

        backBtn = findViewById(R.id.backBtn)
        topConsumedValue = findViewById(R.id.topConsumedValue)
        expirationDateEatValue = findViewById(R.id.expirationDateEatValue)
        expirationDateNotValue = findViewById(R.id.expirationDateNotValue)
        pieChart = findViewById(R.id.pieChart)

        backBtn.setOnClickListener { finish() }

        com.example.test.Utils.TabBarUtils.setupTabBar(this)

        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val stats: PantryStatsResponse =
                    RetrofitInstance.pantryApi.getPantryOverview("Bearer $token")

                withContext(Dispatchers.Main) { updateUI(stats) }

            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    if (e.code() == 401) {
                        startActivity(Intent(this@PantryStatsActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@PantryStatsActivity,
                            "통계 불러오기 실패(${e.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PantryStatsActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(stats: com.example.test.model.pantry.PantryStatsResponse) {
        // 카드 값 바인딩 (레이아웃과 정확히 매칭)
        expirationDateEatValue.text = stats.eatenCount.toString()
        expirationDateNotValue.text = stats.expiredCount.toString()
        topConsumedValue.text = stats.topConsumedName ?: "데이터 없음"

        // 카테고리 도넛
        setupCategoryPieChart(stats.categoryCounts ?: emptyMap())
    }

    private fun setupCategoryPieChart(categoryCounts: Map<String, Long>) {
        if (categoryCounts.isEmpty() || categoryCounts.values.sum() == 0L) {
            pieChart.clear()
            pieChart.setNoDataText("데이터가 없습니다")
            pieChart.invalidate()
            return
        }

        // ✅ 영문 → 한글로 키 변환
        val localizedCounts: Map<String, Long> = categoryCounts
            .map { localizeCategory(it.key) to it.value }
            .groupBy({ it.first }, { it.second })  // 같은 한글 키가 겹칠 수 있으니 합산
            .mapValues { it.value.sum() }

        val entries = localizedCounts
            .filter { it.value > 0 }
            .map { PieEntry(it.value.toFloat(), it.key) }

        val palette = listOf(
            Color.parseColor("#99D89F"),
            Color.parseColor("#9CCAFF"),
            Color.parseColor("#FFD48A"),
            Color.parseColor("#FF9FA2"),
            Color.parseColor("#BFA5FF"),
            Color.parseColor("#8ED1FC"),
            Color.parseColor("#C5E1A5"),
            Color.parseColor("#FFCC80")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = if (entries.size <= palette.size) palette.take(entries.size) else palette
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            // ✅ 1.00 → 1 로 표시
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false

            legend.isEnabled = true
            legend.isWordWrapEnabled = true
            legend.textSize = 12f
            legend.form = com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment   = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.orientation         = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL

            // 범례 아이템 간격 (dp → px 변환 필요)
            val density = resources.displayMetrics.density
            legend.xEntrySpace = 10f * density   // 가로 간격 10dp
            legend.yEntrySpace = 5f * density    // 세로 간격 (멀티 라인 시)
            legend.xOffset = -10f * density         // 맨 앞 10dp 빼서 전체를 왼쪽으로 당김
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            holeRadius = 60f
            transparentCircleRadius = 65f
            animateY(800)
            invalidate()
        }
    }

    private fun setupPieChart(refrigerated: Long, frozen: Long, room: Long) {
        val total = refrigerated + frozen + room
        if (total == 0L) {
            pieChart.clear()
            pieChart.setNoDataText("데이터가 없습니다")
            pieChart.invalidate()
            return
        }

        val entries = listOf(
            PieEntry(refrigerated.toFloat(), "냉장"),
            PieEntry(frozen.toFloat(), "냉동"),
            PieEntry(room.toFloat(), "실온")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#99D89F"),
                Color.parseColor("#9CCAFF"),
                Color.parseColor("#FFD48A")
            )
            sliceSpace = 3f
            valueTextSize = 12f
            valueTextColor = Color.BLACK
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setUsePercentValues(false)
            setDrawEntryLabels(false)
            isDrawHoleEnabled = true
            holeRadius = 60f
            transparentCircleRadius = 65f
            animateY(800)
            invalidate()
        }
    }

    private val CATEGORY_LABELS = mapOf(
        // 서버 Enum 이름 → 한글
        "Vegetables" to "채소류",
        "Meats" to "육류",
        "Seafood" to "해산물",
        "Grains" to "곡류",
        "Fruits" to "과일류",
        "Dairy" to "유제품",
        "Products" to "가공식품",          // 서버에서 이렇게 오는 경우 대비
        "ProcessedFoods" to "가공식품",     // enum에 s 있는 버전
        "ProcessedFood" to "가공식품",      // s 없는 버전까지 안전 대처
        "Seasonings" to "조미료",
        "Noodles" to "면류",
        "Kimchi" to "김치",
        "Beverages" to "음료",
        "Others" to "기타"
    )

    private fun localizeCategory(key: String): String =
        CATEGORY_LABELS[key] ?: CATEGORY_LABELS[key.replaceFirstChar { it.uppercase() }] ?: key

}
