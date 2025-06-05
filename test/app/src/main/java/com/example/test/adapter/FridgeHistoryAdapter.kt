package com.example.test.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.Fridge.FridgeHistoryResponse

class FridgeHistoryAdapter(private var historyList: List<FridgeHistoryResponse>) :
    RecyclerView.Adapter<FridgeHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val historyDate: TextView = view.findViewById(R.id.historyDate)
        val ingredientNameText: TextView = view.findViewById(R.id.ingredientNameText)
        val amountSign: TextView = view.findViewById(R.id.amountSign)
        val quantityText: TextView = view.findViewById(R.id.quantityText)
        val unitText: TextView = view.findViewById(R.id.unitText)
        val purchaseDateText: TextView = view.findViewById(R.id.purchaseDateText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fridge_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]

        val label = if (item.actionType == "ADD") "구매날짜" else "소비날짜"
        holder.historyDate.text = "$label : ${item.actionDate.substring(0, 10)}"
        holder.ingredientNameText.text = item.ingredientName
        holder.amountSign.text = if (item.actionType == "ADD") "+" else "-"
        val formattedQuantity = if (item.quantity % 1.0 == 0.0) {
            item.quantity.toInt().toString()
        } else {
            String.format("%.1f", item.quantity)
        }
        holder.quantityText.text = formattedQuantity

        holder.unitText.text = item.unit

        val color = if (item.actionType == "ADD") Color.parseColor("#409CFF") else Color.parseColor("#FF6961")
        holder.amountSign.setTextColor(color)
        holder.quantityText.setTextColor(color)
        holder.unitText.setTextColor(color)

        val optionLabel = item.dateOption ?: "날짜"
        val displayDate = item.fridgeDate?.substring(0, 10) ?: "정보 없음"
        holder.purchaseDateText.text = "$optionLabel : $displayDate"
    }

    override fun getItemCount(): Int = historyList.size

    fun updateList(newList: List<FridgeHistoryResponse>) {
        historyList = newList
        notifyDataSetChanged()
    }
}
