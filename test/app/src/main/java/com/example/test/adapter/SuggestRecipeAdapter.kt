// SuggestRecipeAdapter.kt
package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.test.App
import com.example.test.R
import com.example.test.model.Recipe
import com.example.test.network.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SuggestRecipeAdapter(
    private val onClick: (Recipe) -> Unit
) : RecyclerView.Adapter<SuggestRecipeAdapter.VH>() {

    private val items = mutableListOf<Recipe>()

    fun submit(list: List<Recipe>) {
        items.clear()
        items.addAll(list.take(3)) // 최대 3개만
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggest_recipe, parent, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(
        itemView: View,
        private val onClick: (Recipe) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val img = itemView.findViewById<ImageView>(R.id.img)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val level = itemView.findViewById<TextView>(R.id.level)
        private val time = itemView.findViewById<TextView>(R.id.time)
        private val heart = itemView.findViewById<ImageButton>(R.id.heartBtn)

        fun bind(item: Recipe) {
            title.text = item.title
            level.text = item.difficulty.ifBlank { "초급" }
            time.text = "${item.cookingTime}분"

            val fallback = R.drawable.image_the_recipe_saw_recently_one
            Glide.with(itemView)
                .load(if (item.mainImageUrl.isNullOrBlank()) fallback else item.mainImageUrl)
                .placeholder(fallback)
                .error(fallback)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(img)

            renderHeart(item.liked)

            itemView.setOnClickListener { onClick(item) }
            heart.setOnClickListener {
                val old = item.liked
                val next = !old

                // 1) 낙관적 UI 업데이트
                item.liked = next
                renderHeart(next)

                val ctx = itemView.context

                // 2) 서버 호출
                val token = App.prefs.token?.let { "Bearer $it" }
                if (token.isNullOrBlank()) {
                    Toast.makeText(ctx, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    // 롤백
                    item.liked = old
                    renderHeart(old)
                    return@setOnClickListener
                }

                RetrofitInstance.apiService.toggleLike(item.recipeId, token)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.isSuccessful) {
                                // ✅ 성공 시 토스트
                                if (next) {
                                    Toast.makeText(ctx, "관심 레시피로 저장하였습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(ctx, "관심 레시피에서 제거하였습니다.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(ctx, "찜 처리 실패(${response.code()})", Toast.LENGTH_SHORT).show()
                                // 롤백
                                item.liked = old
                                renderHeart(old)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(ctx, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                            // 롤백
                            item.liked = old
                            renderHeart(old)
                        }
                    })
            }
        }

        private fun renderHeart(liked: Boolean) {
            heart.setImageResource(
                if (liked) R.drawable.ic_heart_fill else R.drawable.ic_heart_list
            )
        }
    }
}
