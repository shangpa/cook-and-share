package com.example.test.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.PointHistoryResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PointHistoryAdapter(private val fullList: List<PointHistoryResponse>) :
    RecyclerView.Adapter<PointHistoryAdapter.ViewHolder>() {

    private var filteredList: List<PointHistoryResponse> = fullList.toList()

    fun filter(type: String, fromDate: String, toDate: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        filteredList = fullList.filter { item ->
            val matchesType = when (type) {
                "전체" -> true
                "적립" -> item.pointChange >= 0
                "사용" -> item.pointChange < 0
                else -> true
            }

            val matchesDate = if (fromDate.isBlank() || toDate.isBlank()) {
                true
            } else {
                val createdDate = LocalDate.parse(item.createdAt.substring(0, 10), formatter)
                createdDate >= LocalDate.parse(fromDate, formatter) &&
                        createdDate <= LocalDate.parse(toDate, formatter)
            }

            matchesType && matchesDate
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.tvDescription)
        val point: TextView = view.findViewById(R.id.tvPoint)
        val date: TextView = view.findViewById(R.id.tvDate)
        val type: TextView = view.findViewById(R.id.tvType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_point_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        holder.description.text = item.description
        holder.point.text = if (item.pointChange >= 0) "+ ${item.pointChange}P" else "${item.pointChange}P"
        holder.point.setTextColor(
            if (item.pointChange >= 0) Color.parseColor("#FF6961") else Color.parseColor("#409CFF")
        )
        holder.date.text = item.createdAt.substring(0, 16)
        holder.type.text = if (item.pointChange >= 0) "적립" else "사용"
    }

    override fun getItemCount(): Int = filteredList.size
}
