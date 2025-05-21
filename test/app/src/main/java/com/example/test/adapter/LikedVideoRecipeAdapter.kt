package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO

class LikedVideoRecipeAdapter(
    private val recipeList: List<RecipeMainSearchResponseDTO>,
    private val onItemClick: (RecipeMainSearchResponseDTO) -> Unit
) : RecyclerView.Adapter<LikedVideoRecipeAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.recipeImage)
        val title: TextView = view.findViewById(R.id.recipeTitle)
        val difficulty: TextView = view.findViewById(R.id.recipeDifficulty)
        val time: TextView = view.findViewById(R.id.recipeTime)
        val playBtn: ImageView = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interest_video_recipe, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val recipe = recipeList[position]
        Glide.with(holder.itemView.context)
            .load(recipe.mainImageUrl)
            .placeholder(R.drawable.image_recently_stored_materials_food)
            .into(holder.image)

        holder.title.text = recipe.title
        holder.difficulty.text = recipe.difficulty
        holder.time.text = "${recipe.cookingTime}분"
        holder.playBtn.visibility = View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int = recipeList.size
}

