package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.pantry.IngredientMasterResponse
import com.example.test.network.RetrofitInstance

class IngredientGridAdapter(
    private val onClick: (IngredientMasterResponse) -> Unit
) : RecyclerView.Adapter<IngredientGridAdapter.VH>() {

    private val items = mutableListOf<IngredientMasterResponse>()

    /** 외부에서 데이터 갱신 */
    fun submit(list: List<IngredientMasterResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.ivIcon)
        val name: TextView = v.findViewById(R.id.tvName)

        init {
            v.setOnClickListener {
                val item = items[bindingAdapterPosition]
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.nameKo

        // 아이콘 URL → 절대 경로 변환 후 Glide로 로드
        val raw = item.iconUrl
        val displayUrl = RetrofitInstance.toAbsoluteUrl(raw)
        Glide.with(holder.icon.context)
            .load(displayUrl)
            .placeholder(R.drawable.image_tomato) // 기본 이미지
            .into(holder.icon)

        // 접근성 (contentDescription)
        holder.icon.contentDescription = item.nameKo
    }

    override fun getItemCount(): Int = items.size
}
