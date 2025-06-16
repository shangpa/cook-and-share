package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.App
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeDetailResponse
import com.example.test.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageLoveRecipeAdapter(private val onUnliked: (newCount: Int) -> Unit) :
    RecyclerView.Adapter<MypageLoveRecipeAdapter.ViewHolder>() {

    private var recipeList: MutableList<RecipeDetailResponse> = mutableListOf()

    fun setRecipeList(recipes: List<RecipeDetailResponse>) {
        recipeList = recipes.toMutableList()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnail: ImageView = itemView.findViewById(R.id.recipeImage)
        val title: TextView = itemView.findViewById(R.id.recipeTitle)
        val difficulty: TextView = itemView.findViewById(R.id.recipeLevel)
        val time: TextView = itemView.findViewById(R.id.recipeTime)
        val rating: TextView = itemView.findViewById(R.id.recipeRating)
        val reviewCount: TextView = itemView.findViewById(R.id.reviewCount)
        val writer: TextView = itemView.findViewById(R.id.recipeAuthor)
        val heart: ImageView = itemView.findViewById(R.id.heartIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipeList[position]
        val context = holder.itemView.context

        holder.title.text = recipe.title
        holder.difficulty.text = recipe.difficulty
        holder.time.text = "${recipe.cookingTime}분"
        holder.rating.text = "5.0"
        holder.reviewCount.text = "(0)"
        holder.writer.text = recipe.writer
        val imageUrl = recipe.mainImageUrl?.let {
            if (it.startsWith("http")) it
            else RetrofitInstance.BASE_URL.trimEnd('/') + "/" + it.trimStart('/')
        }
        if (!imageUrl.isNullOrBlank()) {
            Glide.with(context)
                .load(imageUrl)
                .into(holder.thumbnail)
        }else {
            holder.thumbnail.visibility = View.GONE
        }
        holder.heart.setImageResource(R.drawable.image_search_result_list_heart_fill)

        // 하트 클릭 시 찜 해제 요청
        holder.heart.setOnClickListener {
            val token = "Bearer ${App.prefs.token}"
            RetrofitInstance.apiService.toggleLike(recipe.recipeId, token)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val currentPosition = holder.adapterPosition
                            if (currentPosition != RecyclerView.NO_POSITION) {
                                recipeList.removeAt(currentPosition)
                                notifyItemRemoved(currentPosition)
                                onUnliked(recipeList.size) // 🔥 남은 개수 콜백 전달
                            }
                        } else {
                            Toast.makeText(context, "찜 해제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(context, "서버 통신 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    override fun getItemCount(): Int = recipeList.size
}
