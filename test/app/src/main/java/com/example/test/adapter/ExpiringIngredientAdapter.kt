package com.example.test.adapter

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.network.RetrofitInstance
import com.example.test.ui.ExpiringItemUi

class ExpiringIngredientAdapter(
    private var items: List<ExpiringItemUi>,
    private val onRecipeClick: (Long) -> Unit // recipeId
) : RecyclerView.Adapter<ExpiringIngredientAdapter.VH>() {

    fun submit(list: List<ExpiringItemUi>) { items = list; notifyDataSetChanged() }

    inner class VH(v: View): RecyclerView.ViewHolder(v) {
        val bg: ImageView = v.findViewById(R.id.ivBg)
        val name: TextView = v.findViewById(R.id.tvName)
        val qty: TextView = v.findViewById(R.id.tvQty)
        val unit: TextView = v.findViewById(R.id.tvUnit)
        val expText: TextView = v.findViewById(R.id.tvExpText)
        val rv: RecyclerView = v.findViewById(R.id.rvRecipes)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(p.context).inflate(R.layout.item_expiring_ingredient, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]
        h.name.text = it.ingredientName
        h.qty.text = it.quantityText
        h.unit.text = it.unitName
        h.expText.text = if (!it.expiresAtText.isNullOrBlank()) "유통기한 : ${it.expiresAtText}" else "-"

        // 재료 아이콘 (ivBg)
        val iconUrl = RetrofitInstance.toIconUrl(it.iconUrl)
        Glide.with(h.bg.context)
            .load(iconUrl)
            .placeholder(R.drawable.image_recently_stored_materials_food)
            .error(R.drawable.image_recently_stored_materials_food)
            .into(h.bg)

        // 레시피 리스트 (ivRecipe는 RecipeMiniAdapter에서 처리)
        h.rv.layoutManager = LinearLayoutManager(h.itemView.context, RecyclerView.HORIZONTAL, false)
        h.rv.adapter = RecipeMiniAdapter(it.recipes) { r -> onRecipeClick(r.recipeId) }
    }


    override fun getItemCount() = items.size
}
