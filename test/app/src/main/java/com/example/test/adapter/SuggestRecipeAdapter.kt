// SuggestRecipeAdapter.kt
package com.example.test.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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

            val fallback = R.drawable.image_recipe_thumbnail_placeholder

            // 1) 썸네일 후보 중 첫 유효값 선택 (필요한 필드만 남겨 쓰면 됨)
            val raw = firstNonBlank(
                item.mainImageUrl,
                // item.thumbnailUrl,
                // item.imageUrl,
                // item.coverImageUrl,
                // item.firstStepImageUrl
            )

            // 2) URL 정규화 (상대경로/백슬래시/공백/따옴표 등 보정)
            val fullUrl = normalizeUrl(raw)

            Log.d(TAG, "bind: recipeId=${item.recipeId}, title=${item.title}, raw=$raw, resolved=$fullUrl, BASE_URL=${RetrofitInstance.BASE_URL}")

            Glide.with(itemView)
                .load(fullUrl ?: fallback)
                .placeholder(fallback)
                .error(fallback)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(img)

            renderHeart(item.liked)

            itemView.setOnClickListener { onClick(item) }
            heart.setOnClickListener { toggleLike(item) }
        }

        private fun toggleLike(item: Recipe) {
            val old = item.liked
            val next = !old

            // 낙관적 UI
            item.liked = next
            renderHeart(next)

            val ctx = itemView.context
            val token = App.prefs.token?.let { "Bearer $it" }
            if (token.isNullOrBlank()) {
                Toast.makeText(ctx, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                item.liked = old
                renderHeart(old)
                return
            }

            RetrofitInstance.apiService.toggleLike(item.recipeId, token)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                ctx,
                                if (next) "관심 레시피로 저장하였습니다." else "관심 레시피에서 제거하였습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(ctx, "찜 처리 실패(${response.code()})", Toast.LENGTH_SHORT).show()
                            item.liked = old
                            renderHeart(old)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(ctx, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                        item.liked = old
                        renderHeart(old)
                    }
                })
        }

        private fun renderHeart(liked: Boolean) {
            heart.setImageResource(
                if (liked) R.drawable.ic_heart_fill else R.drawable.ic_heart_list
            )
        }
    }

    // ---------- 유틸 ----------

    /** 여러 후보 중 첫 번째로 비어있지 않은 문자열 반환 */
    private fun firstNonBlank(vararg candidates: String?): String? =
        candidates.firstOrNull { !it.isNullOrBlank() }

    /**
     * 서버가 주는 URL 문자열을 안전하게 정규화:
     * - 양쪽 따옴표/공백 제거
     * - 백슬래시 → 슬래시
     * - 공백 → %20
     * - data:, blob: 스킴 제외
     * - 상대 경로면 BASE_URL 붙이기
     */
    private fun normalizeUrl(serverValue: String?): String? {
        if (serverValue.isNullOrBlank()) return null

        var v = serverValue.trim().trim('"', '\'')
        v = v.replace('\\', '/')
        v = v.replace(" ", "%20")

        val lower = v.lowercase()
        return if (lower.startsWith("http://") || lower.startsWith("https://")) {
            v
        } else if (lower.startsWith("data:") || lower.startsWith("blob:")) {
            null
        } else {
            val base = RetrofitInstance.BASE_URL.trimEnd('/')
            val path = v.trimStart('/')
            val joined = "$base/$path"
            try {
                val parsed = Uri.parse(joined)
                if (parsed.scheme.isNullOrBlank()) null else parsed.toString()
            } catch (t: Throwable) {
                Log.e(TAG, "normalizeUrl: invalid url joined=$joined from value=$serverValue", t)
                null
            }
        }
    }

    companion object {
        private const val TAG = "SuggestRecipeAdapter"
    }
}
