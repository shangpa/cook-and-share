package com.example.test.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance

class LikeRecipeAdapter(
    private val context: Context,
    private var recipeList: List<RecipeDetailResponse>,
    private val onItemClick: (Long) -> Unit
) : RecyclerView.Adapter<LikeRecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipeImage)
        val recipeTitle: TextView = itemView.findViewById(R.id.recipeTitle)
        val recipeLevel: TextView = itemView.findViewById(R.id.recipeLevel)
        val recipeTime: TextView = itemView.findViewById(R.id.recipeTime)
        val recipeRating: TextView = itemView.findViewById(R.id.recipeRating)
        val recipeRatingCount: TextView = itemView.findViewById(R.id.recipeRatingCount)
        val recipeWriter: TextView = itemView.findViewById(R.id.recipeWriter)
        val heartButton: ImageButton = itemView.findViewById(R.id.heartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_like_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int = recipeList.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipeList[position]

        holder.recipeTitle.text = recipe.title
        holder.recipeLevel.text = recipe.difficulty
        holder.recipeTime.text = recipe.cookingTime.toString()
        holder.recipeRating.text = "5.0" // ⭐️ 서버에 평점 필드 없으면 임의로 고정
        holder.recipeRatingCount.text = "(0)" // 리뷰 수가 없으니 기본값
        holder.recipeWriter.text = recipe.writer

        val imageUrl = RetrofitInstance.BASE_URL + recipe.mainImageUrl

        Glide.with(context)
            .load(imageUrl)
            .into(holder.recipeImage)
        holder.itemView.setOnClickListener {
            onItemClick(recipe.recipeId)
        }
        holder.heartButton.setImageResource(R.drawable.ic_heart_recipe_fill_list)
    }

    fun updateData(newList: List<RecipeDetailResponse>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}
