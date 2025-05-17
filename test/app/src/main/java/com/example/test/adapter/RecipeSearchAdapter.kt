package com.example.test.adapter

import Prefs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

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
        holder.recipeRating.text = String.format("%.1f", recipe.averageRating)
        holder.reviewCount.text = "(${recipe.reviewCount})"
        holder.recipeAuthor.text = recipe.user.name ?: "작성자"
        holder.heartIcon.setImageResource(
            if (recipe.liked) R.drawable.ic_heart_fill
            else R.drawable.image_search_result_list_heart
        )

        // base URL을 붙여서 이미지 로드
        val imageUrl = RetrofitInstance.BASE_URL + recipe.mainImageUrl // mainImageUrl에 base URL 붙여서

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.recipeImage)

        // 하트 클릭 리스너
        holder.heartIcon.setOnClickListener {
            val token = "Bearer " + (Prefs(holder.itemView.context).token ?: "")
            println("토큰왜안되냐"+token)
            RetrofitInstance.apiService.toggleLike(recipe.recipeId, token)
                .enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val newLiked = !recipe.liked
                            recipe.liked = newLiked
                            holder.heartIcon.setImageResource(
                                if (newLiked) R.drawable.ic_heart_fill
                                else R.drawable.image_search_result_list_heart
                            )
                            val msg = if (newLiked) "관심 레시피로 저장되었습니다." else "관심 해제되었습니다."
                            showToast(holder, msg)
                        } else {
                            showToast(holder, "서버 오류: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        showToast(holder, "서버 요청 실패: ${t.localizedMessage}")
                    }
                })
        }
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    private fun showToast(holder: RecipeViewHolder, message: String) {
        Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount(): Int = recipeList.size

    fun updateData(newList: List<Recipe>) {
        recipeList = newList
        notifyDataSetChanged()
    }
}
