package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LikedRecipeAdapter(
    private val recipes: List<RecipeMainSearchResponseDTO>,
    private val onItemClick: (RecipeMainSearchResponseDTO) -> Unit
) : RecyclerView.Adapter<LikedRecipeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.recipeImage)
        val title: TextView = view.findViewById(R.id.recipeTitle)
        val time: TextView = view.findViewById(R.id.recipeTime)
        val difficulty: TextView = view.findViewById(R.id.recipeDifficulty)
        val heartIcon: ImageView = view.findViewById(R.id.recipeHeartIcon)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(recipes[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_main_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = recipes.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        val context = holder.itemView.context

        holder.title.text = recipe.title
        holder.time.text = "${recipe.cookingTime}분"
        holder.difficulty.text = recipe.difficulty

        val imageUrl = recipe.mainImageUrl?.let {
            if (it.startsWith("http")) it
            else RetrofitInstance.BASE_URL.trimEnd('/') + "/" + it.trimStart('/')
        }

        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.image_recently_stored_materials_food)
            .error(R.drawable.image_recently_stored_materials_food)
            .into(holder.image)

        holder.heartIcon.setImageResource(
            if (recipe.liked) R.drawable.ic_heart_fill else R.drawable.ic_heart_list
        )
    }
}



