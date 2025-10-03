// com.example.test.adapter.RefrigeratorAdapter
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
import com.example.test.model.pantry.PantryResponse
import com.example.test.model.pantry.PantryStockDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RefrigeratorAdapter(
    private val onEdit: (PantryResponse) -> Unit,
    private val onClick: (PantryResponse) -> Unit
) : RecyclerView.Adapter<RefrigeratorAdapter.VH>() {

    private val items = mutableListOf<PantryResponse>()

    /** pantryId -> (이미 "14일 이내"로 필터된 재고들) */
    private var expiringMap: Map<Long, List<PantryStockDto>> = emptyMap()

    fun submit(list: List<PantryResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun updateExpiringMap(map: Map<Long, List<PantryStockDto>>) {
        expiringMap = map
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val name: TextView = v.findViewById(R.id.tvName)
        val memo: TextView = v.findViewById(R.id.tvMemo)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)

        // 임박 2줄
        val rowExpire1: View? = v.findViewById(R.id.rowExpire1)
        val rowExpire2: View? = v.findViewById(R.id.rowExpire2)
        val total1: TextView? = v.findViewById(R.id.total1)
        val date1: TextView? = v.findViewById(R.id.pantryMainExpirationDate1)
        val total2: TextView? = v.findViewById(R.id.total2)
        val date2: TextView? = v.findViewById(R.id.pantryMainExpirationDate2)

        init {
            v.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onClick(items[pos])
            }
            btnEdit.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onEdit(items[pos])
            }
        }
    }

    private val isoDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private fun String.toLocalDateOrNull(): LocalDate? =
        runCatching { LocalDate.parse(this, isoDate) }.getOrNull()

    /** 전달받은 목록(이미 14일 이내 필터됨)에서 랜덤 2개만 뽑기 */
    private fun pickTwoRandom(list: List<PantryStockDto>?): List<Pair<String, LocalDate>> {
        if (list.isNullOrEmpty()) return emptyList()
        return list.shuffled()
            .take(2)
            .mapNotNull { s ->
                val d = s.expiresAt?.toLocalDateOrNull() ?: return@mapNotNull null
                s.ingredientName to d
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_refrigerator, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.memo.text = item.note ?: ""

        // 썸네일
        val raw = item.imageUrl
        if (raw.isNullOrBlank()) {
            holder.img.setImageResource(R.drawable.image_pantry)
        } else {
            val url = com.example.test.network.RetrofitInstance.toAbsoluteUrl(raw)
            Glide.with(holder.img.context)
                .load(url)
                .placeholder(R.drawable.image_pantry)
                .into(holder.img)
        }

        // 임박 2개 랜덤 표시
        val two = pickTwoRandom(expiringMap[item.id])

        when (two.size) {
            0 -> {
                holder.rowExpire1?.visibility = View.GONE
                holder.rowExpire2?.visibility = View.GONE
            }
            1 -> {
                holder.rowExpire1?.visibility = View.VISIBLE
                holder.rowExpire2?.visibility = View.GONE
                holder.total1?.text = two[0].first
                holder.date1?.text = "${two[0].second.format(isoDate)} 까지"
            }
            else -> {
                holder.rowExpire1?.visibility = View.VISIBLE
                holder.rowExpire2?.visibility = View.VISIBLE
                holder.total1?.text = two[0].first
                holder.date1?.text = "${two[0].second.format(isoDate)} 까지"
                holder.total2?.text = two[1].first
                holder.date2?.text = "${two[1].second.format(isoDate)} 까지"
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
