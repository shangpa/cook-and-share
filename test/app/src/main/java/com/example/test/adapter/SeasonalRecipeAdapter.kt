package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.Recipe

class SeasonalRecipeAdapter(
    private val recipeList: List<Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<SeasonalRecipeAdapter.SeasonalRecipeViewHolder>() {

    inner class SeasonalRecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.seasonalRecipeImage)
        val recipeTitle: TextView = itemView.findViewById(R.id.seasonalRecipeTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonalRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seasonal_recipe, parent, false)
        return SeasonalRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeasonalRecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.recipeTitle.text = recipe.title
        holder.recipeImage.setImageResource(getImageResourceByTitle(recipe.title))

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipeList.size

    private fun getImageResourceByTitle(title: String): Int {
        return when (title) {
            "삼계탕" -> R.drawable.img_seasonal_food1
            "초계국수" -> R.drawable.img_seasonal_food2
            "콩국수" -> R.drawable.img_seasonal_food3
            "물회" -> R.drawable.img_seasonal_food4
            "오이냉국" -> R.drawable.img_seasonal_food5
            else -> R.drawable.img_seasonal_food1 // 기본 이미지 지정 (없으면 새로 추가해도 됨)
        }
    }
}
