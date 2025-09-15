package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeDraftDto
import com.bumptech.glide.request.RequestOptions
import android.content.Context
import kotlin.math.roundToInt

class DraftAdapter(
    private val onClick: (RecipeDraftDto) -> Unit,
    private val onDelete: (RecipeDraftDto) -> Unit
) : ListAdapter<RecipeDraftDto, DraftAdapter.VH>(diff) {

    companion object {
        val diff = object : DiffUtil.ItemCallback<RecipeDraftDto>() {
            override fun areItemsTheSame(o: RecipeDraftDto, n: RecipeDraftDto) =
                o.recipeId == n.recipeId
            override fun areContentsTheSame(o: RecipeDraftDto, n: RecipeDraftDto) = o == n
        }
    }

    init { setHasStableIds(true) }
    override fun getItemId(position: Int) = getItem(position).recipeId ?: RecyclerView.NO_ID

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivThumb)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val date: TextView = v.findViewById(R.id.tvDate)
        val delete: ImageButton = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_draft, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)

        h.title.text = item.title?.takeIf { it.isNotBlank() } ?: "(제목 없음)"
        h.date.text = "작성일자 : ${formatDate(item.createdAt)}"

        val fullUrl = normalizeUrl(item.mainImageUrl)

        if (fullUrl == null) {
            h.iv.setImageResource(R.drawable.image_recipe_thumbnail_placeholder)
        } else {
            val radiusPx = 5.dp(h.iv.context)      // ← 12dp를 px로
            Glide.with(h.iv)
                .load(fullUrl)
                .apply(
                    RequestOptions()
                        .transform(
                            CenterCrop(),
                            RoundedCorners(radiusPx)
                        )
                        .placeholder(R.drawable.image_recipe_thumbnail_placeholder)
                        .error(R.drawable.image_recipe_thumbnail_placeholder)
                )
                .into(h.iv)
        }

        h.v.setOnClickListener {
            val p = h.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) onClick(getItem(p))
        }
        h.delete.setOnClickListener {
            val p = h.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) onDelete(getItem(p))
        }
    }

    private fun Int.dp(context: Context): Int {
        return (this * context.resources.displayMetrics.density).roundToInt()
    }

    /**
     * URL 정규화:
     * - null/빈문자/"null" → null 로 취급
     * - 절대경로(http/https)면 그대로
     * - 상대경로면 BASE_URL 붙여서 절대경로로 변환
     */
    private fun normalizeUrl(maybeUrl: String?): String? {
        val cleaned = maybeUrl?.trim()
        if (cleaned.isNullOrEmpty() || cleaned.equals("null", ignoreCase = true)) return null

        return if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned
        } else {
            val base = com.example.test.network.RetrofitInstance.BASE_URL.trimEnd('/')
            val path = cleaned.trimStart('/')
            "$base/$path"
        }
    }

    private fun formatDate(createdAt: String?): String {
        // 서버가 "2025-02-03T12:34:56" 형식이라고 가정
        return try {
            // 간단히 날짜부분만
            createdAt?.take(10) ?: "-"
        } catch (_: Exception) {
            "-"
        }
    }
}

