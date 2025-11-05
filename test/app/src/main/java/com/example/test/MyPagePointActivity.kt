package com.example.test

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.adapter.PointHistoryAdapter
import com.example.test.model.LoginInfoResponse
import com.example.test.model.PointHistoryResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MyPagePointActivity : AppCompatActivity() {

    private lateinit var all: LinearLayout
    private lateinit var week: LinearLayout
    private lateinit var month: LinearLayout
    private lateinit var thrMonth: LinearLayout
    private lateinit var sixMonth: LinearLayout
    private lateinit var year: LinearLayout
    private lateinit var adapter: PointHistoryAdapter
    private var fullData: List<PointHistoryResponse> = emptyList()
    private var selectedType = "전체"
    private var fromDate = ""
    private var toDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage_point)

        all = findViewById(R.id.all)
        week = findViewById(R.id.week)
        month = findViewById(R.id.month)
        thrMonth = findViewById(R.id.thrMonth)
        sixMonth = findViewById(R.id.sixMonth)
        year = findViewById(R.id.year)

        val allButtons = listOf(all, week, month, thrMonth, sixMonth, year)

        fun updateSelection(selected: LinearLayout) {
            for (button in allButtons) {
                val textView = button.getChildAt(0) as TextView
                if (button == selected) {
                    button.setBackgroundResource(R.drawable.rouned_ractangle_black)
                    textView.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    textView.setTextColor(resources.getColor(R.color.black, null))
                }
            }
        }

        adapter = PointHistoryAdapter(fullData)

        // 드롭다운
        val dropDownIcon = findViewById<ImageView>(R.id.myPointDropDownIcon)
        val filterText = findViewById<TextView>(R.id.myPointfillterText)

        fun updateDateFilter(monthsAgo: Int?, daysAgo: Int?) {
            val today = LocalDate.now()
            val start = when {
                monthsAgo != null -> today.minusMonths(monthsAgo.toLong())
                daysAgo != null -> today.minusDays(daysAgo.toLong())
                else -> today.minusYears(1)
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            fromDate = formatter.format(start)
            toDate = formatter.format(today)
            findViewById<TextView>(R.id.dateRange).text = "$fromDate ~ $toDate"
            adapter.filter(selectedType, fromDate, toDate)
        }

        all.setOnClickListener {
            updateSelection(all)
            fromDate = ""
            toDate = ""
            findViewById<TextView>(R.id.dateRange).text = "전체 기간"
            adapter.filter(selectedType, fromDate, toDate)
        }

        week.setOnClickListener {
            updateSelection(week)
            updateDateFilter(null, 7)
        }

        month.setOnClickListener {
            updateSelection(month)
            updateDateFilter(1, null)
        }

        thrMonth.setOnClickListener {
            updateSelection(thrMonth)
            updateDateFilter(3, null)
        }

        sixMonth.setOnClickListener {
            updateSelection(sixMonth)
            updateDateFilter(6, null)
        }

        year.setOnClickListener {
            updateSelection(year)
            updateDateFilter(12, null)
        }

        // 드롭다운 클릭 시 어댑터의 filter 메서드를 사용
        dropDownIcon.setOnClickListener {
            val popupMenu = PopupMenu(this, dropDownIcon)
            popupMenu.menu.add("전체")
            popupMenu.menu.add("적립")
            popupMenu.menu.add("사용")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                selectedType = menuItem.title.toString()
                filterText.text = selectedType
                adapter.filter(selectedType, fromDate, toDate)
                true
            }

            popupMenu.show()
        }

        updateSelection(all)

        val recycler = findViewById<RecyclerView>(R.id.recyclerPointHistory)
        recycler.layoutManager = LinearLayoutManager(this)

        val token = "Bearer ${App.prefs.token}"

        RetrofitInstance.apiService.getMyPointHistory(token)
            .enqueue(object : Callback<List<PointHistoryResponse>> {
                override fun onResponse(
                    call: Call<List<PointHistoryResponse>>,
                    response: Response<List<PointHistoryResponse>>
                ) {
                    if (response.isSuccessful) {
                        fullData = response.body() ?: emptyList()
                        adapter = PointHistoryAdapter(fullData)
                        recycler.adapter = adapter

                        // 기본값
                        updateSelection(all)
                        fromDate = ""
                        toDate = ""
                        findViewById<TextView>(R.id.dateRange).text = "전체 기간"
                        adapter.filter(selectedType, fromDate, toDate)
                    }
                }

                override fun onFailure(call: Call<List<PointHistoryResponse>>, t: Throwable) {
                    Log.e("Point", "포인트 내역 조회 실패", t)
                }
            })

        val pointOwnerText = findViewById<TextView>(R.id.pointOwnerText)
        val totalPointText = findViewById<TextView>(R.id.totalPointText)

        // 사용자 이름
        RetrofitInstance.apiService.getUserInfo(token)
            .enqueue(object : Callback<LoginInfoResponse> {
                override fun onResponse(
                    call: Call<LoginInfoResponse>,
                    response: Response<LoginInfoResponse>
                ) {
                    if (response.isSuccessful) {
                        val user = response.body()
                        pointOwnerText.text = "${user?.name}님의 보유 포인트"
                    }
                }

                override fun onFailure(call: Call<LoginInfoResponse>, t: Throwable) {
                    pointOwnerText.text = "사용자님의 보유 포인트"
                }
            })

        // 사용자 포인트 (세 자리 콤마 적용)
        RetrofitInstance.apiService.getMyPoint(token)
            .enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    val point = response.body() ?: 0
                    totalPointText.text = formatPoints(point)
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    totalPointText.text = "?"
                }
            })

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    // ====== 포인트 포맷터 ======
    private fun formatPoints(value: Number?): String {
        val n = value?.toLong() ?: 0L
        val formatted = NumberFormat.getNumberInstance(Locale.KOREA).format(n)
        return "$formatted P" // ✅ 세 자리 콤마 + P 추가
    }
}
