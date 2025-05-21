package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.Fridge.FridgeResponse

class FridgeAdapter(
    private val fridgeList: List<FridgeResponse>,
    private val selectedIds: Set<Long>,
    private val onItemClick: (FridgeResponse) -> Unit,
    private val onIconClick: (FridgeResponse) -> Unit
) : RecyclerView.Adapter<FridgeAdapter.FridgeViewHolder>() {

    inner class FridgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val box: LinearLayout = itemView.findViewById(R.id.fridgeIngredientBox)
        private val nameText: TextView = itemView.findViewById(R.id.ingredientName)
        private val quantityText: TextView = itemView.findViewById(R.id.ingredientQuantity)
        private val unitText: TextView = itemView.findViewById(R.id.ingredientUnit)
        private val storageText: TextView = itemView.findViewById(R.id.storageAreaText)
        private val icon: ImageView = itemView.findViewById(R.id.fridgeArrowIcon)

        fun bind(fridge: FridgeResponse) {
            nameText.text = fridge.ingredientName
            quantityText.text = fridge.quantity.toInt().toString()
            unitText.text = fridge.unitDetail
            storageText.text = fridge.storageArea

            // 선택된 경우 배경 강조
            if (selectedIds.contains(fridge.id)) {
                box.setBackgroundResource(R.drawable.rounded_rectangle_fridge_ck) // 예: 연회색 배경
            } else {
                box.setBackgroundResource(R.drawable.rounded_rectangle_fridge) // 예: 흰 배경
            }

            box.setOnClickListener { onItemClick(fridge) }
            icon.setOnClickListener { onIconClick(fridge) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FridgeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fridge, parent, false)
        return FridgeViewHolder(view)
    }

    override fun onBindViewHolder(holder: FridgeViewHolder, position: Int) {
        holder.bind(fridgeList[position])
    }

    override fun getItemCount(): Int = fridgeList.size
}

