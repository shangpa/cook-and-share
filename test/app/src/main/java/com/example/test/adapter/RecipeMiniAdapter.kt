package com.example.test.adapter

import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.test.R
import com.example.test.model.recipeDetail.RecipeMainSearchResponseDTO
import com.example.test.network.RetrofitInstance

class RecipeMiniAdapter(
    private val items: List<RecipeMainSearchResponseDTO>,
    private val onClick: (RecipeMainSearchResponseDTO) -> Unit
) : RecyclerView.Adapter<RecipeMiniAdapter.VH>() {

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.ivRecipe)
        val play: ImageView = v.findViewById(R.id.ivPlay)
        val title: TextView = v.findViewById(R.id.tvTitle)
        val diff: TextView = v.findViewById(R.id.tvDiff)
        val time: TextView = v.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_recipe_mini, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]

        val imageUrl = RetrofitInstance.toAbsoluteUrl(it.mainImageUrl)
        Log.d("RecipeMiniAdapter", "thumbUrl=$imageUrl")

        Glide.with(h.img.context)
            .load(imageUrl)
            .placeholder(R.drawable.image_recently_stored_materials_food)
            .error(R.drawable.image_recently_stored_materials_food)
            .into(h.img)

        h.play.visibility = if (it.videoUrl.isNullOrBlank()) View.GONE else View.VISIBLE
        h.title.text = it.title
        h.diff.text  = it.difficulty        // non-null이면 그대로
        h.time.text  = "${it.cookingTime}분" // non-null이면 그대로

        h.itemView.setOnClickListener {
            val index = h.bindingAdapterPosition
            if (index != RecyclerView.NO_POSITION) {
                onClick(items[index])
            }
        }
        // 또는 간단히: h.itemView.setOnClickListener { onClick(it) }
    }

    override fun getItemCount() = items.size
}
