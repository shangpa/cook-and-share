package com.example.test

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

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

        week.setOnClickListener { updateSelection(week) }
        month.setOnClickListener { updateSelection(month) }
        thrMonth.setOnClickListener { updateSelection(thrMonth) }
        sixMonth.setOnClickListener { updateSelection(sixMonth) }
        year.setOnClickListener { updateSelection(year) }
    }
}
