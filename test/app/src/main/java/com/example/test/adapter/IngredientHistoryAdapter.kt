package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.pantry.IngredientHistoryUi
import com.example.test.model.pantry.toDot
import com.example.test.network.RetrofitInstance
import java.time.format.DateTimeFormatter

class IngredientHistoryAdapter :
    ListAdapter<IngredientHistoryUi, IngredientHistoryAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<IngredientHistoryUi>() {
            override fun areItemsTheSame(o: IngredientHistoryUi, n: IngredientHistoryUi) = o.id == n.id
            override fun areContentsTheSame(o: IngredientHistoryUi, n: IngredientHistoryUi) = o == n
        }
        private val DATE_ONLY = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val historyDate: TextView = v.findViewById(R.id.historyDate)
        val material: TextView = v.findViewById(R.id.material)
        val purchaseDateText: TextView = v.findViewById(R.id.purchaseDateText)
        val amountSign: TextView = v.findViewById(R.id.amountSign)
        val number: TextView = v.findViewById(R.id.number)
        val unit: TextView = v.findViewById(R.id.unit)
        val materialImage: ImageView = v.findViewById(R.id.materialImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)

        // 1. + / - 및 색상
        val sign = if (item.isIncrease) "+" else "-"
        val colorRes = if (item.isIncrease) R.color.pantryHistoryBlue else R.color.pantryHistoryRed
        val color = ContextCompat.getColor(h.itemView.context, colorRes)

        h.amountSign.text = sign
        h.amountSign.setTextColor(color)

        // 2. 수량/단위 (부호 제거 후 표시)
        val parts = item.amountText.split("\\s+".toRegex(), limit = 2)
        val amountStr = parts.getOrNull(0).orEmpty()
            .removePrefix("+")
            .removePrefix("-")
        val unitStr = parts.getOrNull(1).orEmpty()

        h.number.text = amountStr
        h.number.setTextColor(color)
        h.unit.text = unitStr
        h.unit.setTextColor(color)

        // 3. 카드 바깥 날짜 = createdAt(yyyy.MM.dd)
        h.historyDate.text = item.createdAt.format(DATE_ONLY)

        // 4. 재료명
        h.material.text = item.ingredientName

        // 5. 구매일자 > 유통기한
        val p = item.purchasedAt.toDot()
        val e = item.expiresAt.toDot()
        h.purchaseDateText.text = when {
            p != null -> "구매일자 : $p"
            e != null -> "유통기한 : $e"
            else -> "구매/유통 정보 없음"
        }

        // 6. 아이콘
        val displayUrl = RetrofitInstance.toIconUrl(item.iconUrl)
        Glide.with(h.itemView)
            .load(displayUrl)
            .placeholder(R.drawable.image_tomato)
            .error(R.drawable.image_tomato)
            .into(h.materialImage)

    }
}
