package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.SeasonalRecipe
import com.example.test.network.RetrofitInstance
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil


class SeasonalRecipeAdapter(
    private val onItemClick: (SeasonalRecipe) -> Unit
) : ListAdapter<SeasonalRecipe, SeasonalRecipeAdapter.VH>(diff) {

    object diff : DiffUtil.ItemCallback<SeasonalRecipe>() {
        override fun areItemsTheSame(a: SeasonalRecipe, b: SeasonalRecipe) = a.recipeId == b.recipeId
        override fun areContentsTheSame(a: SeasonalRecipe, b: SeasonalRecipe) = a == b
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val img: ImageView = view.findViewById(R.id.seasonalRecipeImage)
        private val title: TextView = view.findViewById(R.id.seasonalRecipeTitle)
        fun bind(item: SeasonalRecipe) {
            title.text = item.title
            val fallback = getFallbackResByTitle(item.title)
            val absUrl = item.mainImageUrl?.trim()?.takeIf { it.isNotEmpty() }
                ?.let { RetrofitInstance.toAbsoluteUrl(it) }
            if (absUrl == null) {
                img.setImageResource(fallback)
            } else {
                Glide.with(itemView).load(absUrl)
                    .placeholder(fallback).error(fallback)
                    .into(img)
            }
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_seasonal_recipe, p, false)
        return VH(v)
    }
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    private fun getFallbackResByTitle(title: String) = when(title.trim()) {
        "삼계탕" -> R.drawable.img_seasonal_food1
        "초계국수" -> R.drawable.img_seasonal_food2
        "콩국수" -> R.drawable.img_seasonal_food3
        "물회" -> R.drawable.img_seasonal_food4
        "오이냉국" -> R.drawable.img_seasonal_food5
        else -> R.drawable.img_seasonal_food1
    }
}
