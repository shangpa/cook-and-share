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
import com.example.test.network.RetrofitInstance

class RecipeSearchAdapter(private var recipeList: List<Recipe>,
                          private val onItemClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeSearchAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitle)
        val heartIcon: ImageView = itemView.findViewById(R.id.heartIcon)
        val recipeLevel: TextView = itemView.findViewById(R.id.recipeLevel)
        val recipeTime: TextView = itemView.findViewById(R.id.recipeTime)
        val recipeRating: TextView = itemView.findViewById(R.id.recipeRating)
        val reviewCount: TextView = itemView.findViewById(R.id.reviewCount)
        val recipeAuthor: TextView = itemView.findViewById(R.id.recipeAuthor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.recipeTitle.text = recipe.title
        holder.recipeLevel.text = recipe.difficulty
        holder.recipeTime.text = "${recipe.cookingTime}분"
        holder.recipeRating.text = "5.0"
        holder.reviewCount.text = "(6)"
        holder.recipeAuthor.text = recipe.user.name ?: "작성자"

        // base URL을 붙여서 이미지 로드
        val imageUrl = RetrofitInstance.BASE_URL + recipe.mainImageUrl // mainImageUrl에 base URL 붙여서

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.recipeImage)
        holder.heartIcon.setImageResource(R.drawable.image_search_result_list_heart)
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}
