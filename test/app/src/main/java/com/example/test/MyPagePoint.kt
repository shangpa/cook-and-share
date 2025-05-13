package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R
import android.widget.ImageView
import android.widget.PopupMenu
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

class MyPagePoint : AppCompatActivity() {

    private lateinit var week: LinearLayout
    private lateinit var month: LinearLayout
    private lateinit var thrMonth: LinearLayout
    private lateinit var sixMonth: LinearLayout
    private lateinit var year: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_point)

        week = findViewById(R.id.week)
        month = findViewById(R.id.month)
        thrMonth = findViewById(R.id.thrMonth)
        sixMonth = findViewById(R.id.sixMonth)
        year = findViewById(R.id.year)

        val allButtons = listOf(week, month, thrMonth, sixMonth, year)

        fun updateSelection(selected: LinearLayout) {
            for (button in allButtons) {
                val textView = button.getChildAt(0) as TextView
                if (button == selected) {
                    button.setBackgroundResource(R.drawable.rouned_ractangle_black)
                    textView.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    textView.setTextColor(resources.getColor(R.color.black, null)) // 예: #8A8F9C
                }
            }
        }

        //날짜
        fun updateDateRangeText(monthsAgo: Int?, daysAgo: Int?) {
            val today = Calendar.getInstance()
            val start = Calendar.getInstance()

            // 날짜 차이 적용
            when {
                monthsAgo != null -> start.add(Calendar.MONTH, -monthsAgo)
                daysAgo != null -> start.add(Calendar.DAY_OF_YEAR, -daysAgo)
            }

            // yyyy-MM-dd 형식으로 포맷
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateRange = "${formatter.format(start.time)} ~ ${formatter.format(today.time)}"

            val dateRangeView = findViewById<TextView>(R.id.dateRange)
            dateRangeView.text = dateRange
        }
        week.setOnClickListener {
            updateSelection(week)
            updateDateRangeText(monthsAgo = null, daysAgo = 7)
        }

        month.setOnClickListener {
            updateSelection(month)
            updateDateRangeText(monthsAgo = 1, daysAgo = null)
        }

        thrMonth.setOnClickListener {
            updateSelection(thrMonth)
            updateDateRangeText(monthsAgo = 3, daysAgo = null)
        }

        sixMonth.setOnClickListener {
            updateSelection(sixMonth)
            updateDateRangeText(monthsAgo = 6, daysAgo = null)
        }

        year.setOnClickListener {
            updateSelection(year)
            updateDateRangeText(monthsAgo = 12, daysAgo = null)
        }



        //드롭다운
        val dropDownIcon = findViewById<ImageView>(R.id.fridgeRecipeResultDropDownIcon)
        val filterText = findViewById<TextView>(R.id.fridgeRecipefillterText)
        val pointHistoryList = findViewById<LinearLayout>(R.id.pointHistoryList)

        dropDownIcon.setOnClickListener {
            val popupMenu = PopupMenu(this, dropDownIcon)
            popupMenu.menu.add("전체")
            popupMenu.menu.add("적립")
            popupMenu.menu.add("사용")

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val selected = menuItem.title.toString()
                filterText.text = selected

                var i = 0
                while (i < pointHistoryList.childCount) {
                    val item = pointHistoryList.getChildAt(i)

                    // 기본값: 안보이게
                    var matched = false

                    if (item is LinearLayout) {
                        // 내부에서 "적립"/"사용" 텍스트 포함 여부 확인
                        for (j in 0 until item.childCount) {
                            val subChild = item.getChildAt(j)
                            if (subChild is LinearLayout) {
                                for (k in 0 until subChild.childCount) {
                                    val textView = subChild.getChildAt(k)
                                    if (textView is TextView) {
                                        val text = textView.text.toString()
                                        if (selected == "전체" ||
                                            (selected == "적립" && text.contains("적립")) ||
                                            (selected == "사용" && text.contains("사용"))) {
                                            matched = true
                                        }
                                    }
                                }
                            }
                        }

                        item.visibility = if (matched || selected == "전체") View.VISIBLE else View.GONE

                        // 다음에 오는 구분선(View)도 처리 (예: i+1이 View이면 같이 숨기기)
                        if (i + 1 < pointHistoryList.childCount) {
                            val next = pointHistoryList.getChildAt(i + 1)
                            if (next is View) {
                                next.visibility = item.visibility
                            }
                        }
                    }

                    i++
                }

                true
            }
            popupMenu.show()
        }
        updateSelection(week)
    }
}
