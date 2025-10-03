package com.example.test.model.recipt

import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R

class OcrItemVH(
    v: View,
    private val onSearch: (Int, String) -> Unit,
    private val onCheckedChanged: (Int, Boolean) -> Unit
) : RecyclerView.ViewHolder(v) {
    private val cb = v.findViewById<CheckBox>(R.id.cb)
    private val etName = v.findViewById<EditText>(R.id.etName)
    private val etQty = v.findViewById<EditText>(R.id.etQty)
    private val tvMatch = v.findViewById<TextView>(R.id.tvMatch)
    private val btnSearch = v.findViewById<ImageButton>(R.id.btnSearch)

    fun bind(m: OcrItemUi) {
        cb.isChecked = m.checked
        etName.setText(m.nameRaw)
        etQty.setText(m.quantityStr)
        tvMatch.text = m.matchedName?.let { "매칭: $it" } ?: "미매칭"

        cb.setOnCheckedChangeListener { _, b -> onCheckedChanged(bindingAdapterPosition, b) }
        btnSearch.setOnClickListener {
            onSearch(bindingAdapterPosition, etName.text.toString())
        }

        // 수량/이름 변경 시 내부 리스트 갱신은 submit 전에 뷰홀더에서 값을 읽도록 처리해도 OK
    }
}