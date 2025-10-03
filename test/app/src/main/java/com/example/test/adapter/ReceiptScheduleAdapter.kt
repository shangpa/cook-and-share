package com.example.test

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReceiptScheduleAdapter(
    private val items: List<ItemScheduleUi>,
    private val onPickStorage: (position: Int) -> Unit,
    private val onPickDateKind: (position: Int) -> Unit,
    private val onPickDate: (position: Int) -> Unit
) : RecyclerView.Adapter<ReceiptScheduleAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvStorage: TextView = v.findViewById(R.id.tvStorage)
        val tvDateKind: TextView = v.findViewById(R.id.tvDateKind)
        val etDate: EditText = v.findViewById(R.id.etDate)
        val tvQuantity: TextView = v.findViewById(R.id.tvQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.tvName.text = item.nameRaw
        val qInt = item.quantity.toDoubleOrNull()?.toInt() ?: 0
        h.tvQuantity.text = "수량 $qInt"

        // 보관장소 표시
        h.tvStorage.text = when (item.storage) {
            "FREEZER" -> "냉동"
            "PANTRY" -> "실온"
            else -> "냉장"
        }
        h.tvStorage.setOnClickListener { onPickStorage(position) }

        // 날짜종류 표시
        h.tvDateKind.text = item.dateKind
        h.tvDateKind.setOnClickListener { onPickDateKind(position) }

        // 날짜 표시/선택
        h.etDate.setText(item.date ?: "")
        h.etDate.inputType = InputType.TYPE_NULL
        h.etDate.keyListener = null
        h.etDate.setOnClickListener { onPickDate(position) }
    }

    override fun getItemCount(): Int = items.size
}
