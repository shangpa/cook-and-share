package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.PantryStockUi
import com.google.android.material.card.MaterialCardView

class StockAdapter(
    private val onClick: (PantryStockUi) -> Unit,
    private val onLongClick: (PantryStockUi) -> Unit,
    private val onTransferArrow: (PantryStockUi) -> Unit,
) : RecyclerView.Adapter<StockAdapter.VH>() {

    private val items = mutableListOf<PantryStockUi>()
    private val selectedIds = linkedSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], selectedIds.contains(items[position].id))
    }

    fun submit(list: List<PantryStockUi>) {
        items.clear()
        items.addAll(list)
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun toggle(id: Long) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        notifyItemChanged(items.indexOfFirst { it.id == id })
    }

    fun toggleSelectAll(all: Boolean) {
        selectedIds.clear()
        if (all) selectedIds.addAll(items.map { it.id })
        notifyDataSetChanged()
    }

    fun clearSelection() {
        if (selectedIds.isEmpty()) return
        selectedIds.clear()
        notifyDataSetChanged()
    }

    fun getSelected(): List<PantryStockUi> = items.filter { selectedIds.contains(it.id) }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val card: MaterialCardView = v.findViewById(R.id.card)
        private val img: ImageView = v.findViewById(R.id.materialImage)
        private val name: TextView = v.findViewById(R.id.material)
        private val cat: TextView = v.findViewById(R.id.greengrocery)
        private val storagePlace: TextView = v.findViewById(R.id.storagePlace)
        private val storageVal: TextView = v.findViewById(R.id.storageOutside)
        private val qty: TextView = v.findViewById(R.id.number)
        private val unit: TextView = v.findViewById(R.id.unit)
        private val arrow: ImageButton = v.findViewById(R.id.transferArrow)

        fun bind(item: PantryStockUi, selected: Boolean) {
            // 이미지
            Glide.with(img).load(item.iconUrl)
                .placeholder(R.drawable.image_tomato)
                .error(R.drawable.image_tomato)
                .into(img)

            // 텍스트
            name.text = item.name
            cat.text = item.categoryKo
            storagePlace.text = "보관장소 : "
            storageVal.text = item.storageKo
            qty.text = item.quantity
            unit.text = item.unit

            // 선택 시 초록색 테두리
            card.strokeWidth = 5
            card.strokeColor = if (selected) 0xFF35A825.toInt() else 0x00000000

            // 클릭/롱클릭
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener { onLongClick(item); true }

            // 화살표
            arrow.setOnClickListener { onTransferArrow(item) }
        }
    }
}
