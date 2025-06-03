package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.Recipe

class SeasonalRecipeAdapter(
    private val recipes: List<Recipe>,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<SeasonalRecipeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imageRecipe)
        val title: TextView = view.findViewById(R.id.textRecipeTitle)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(recipes[position].recipeId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seasonal_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.title.text = recipe.title
        Glide.with(holder.itemView.context)
            .load(recipe.mainImageUrl)
            .placeholder(R.drawable.image_the_recipe_saw_recently_one) // 기본 이미지
            .into(holder.image)
    }

    override fun getItemCount() = recipes.size
}
