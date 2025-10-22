// TotalFeedAdapter.kt
package com.example.test.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.example.test.R
import com.example.test.RecipeSeeActivity
import com.example.test.model.Recipe
import com.example.test.model.recipeDetail.ShortsSearchItem

sealed interface TotalBlock
data class RecipeBlock(val items: List<Recipe>) : TotalBlock
data class ShortsBlock(val items: List<ShortsSearchItem>) : TotalBlock

class TotalFeedAdapter(
    private var blocks: List<TotalBlock>,
    private val onShortClick: (ShortsSearchItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VT_RECIPE = 1
        private const val VT_SHORTS = 2
    }

    override fun getItemViewType(position: Int): Int =
        when (blocks[position]) {
            is RecipeBlock -> VT_RECIPE
            is ShortsBlock -> VT_SHORTS
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            VT_RECIPE -> {
                val v = inf.inflate(R.layout.item_total_block_recipes, parent, false)
                RecipeBlockVH(v)
            }
            else -> {
                val v = inf.inflate(R.layout.item_total_block_shorts, parent, false)
                ShortsBlockVH(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val block = blocks[position]) {
            is RecipeBlock -> (holder as RecipeBlockVH).bind(block.items)
            is ShortsBlock -> (holder as ShortsBlockVH).bind(block.items)
        }
    }

    override fun getItemCount(): Int = blocks.size

    fun submit(newBlocks: List<TotalBlock>) {
        blocks = newBlocks
        notifyDataSetChanged()
    }

    // --- VHs ---
    inner class RecipeBlockVH(v: View) : RecyclerView.ViewHolder(v) {
        private val inner = v.findViewById<RecyclerView>(R.id.innerRecipes)
        private val innerAdapter = RecipeSearchAdapter(emptyList()) { recipe ->
            val ctx = itemView.context
            val i = Intent(ctx, RecipeSeeActivity::class.java)
            i.putExtra("recipeId", recipe.recipeId)
            ctx.startActivity(i)
        }
        init {
            inner.layoutManager = LinearLayoutManager(v.context, RecyclerView.VERTICAL, false)
            inner.adapter = innerAdapter
        }
        fun bind(items: List<Recipe>) {
            innerAdapter.updateData(items.take(5))
        }
    }

    inner class ShortsBlockVH(v: View) : RecyclerView.ViewHolder(v) {
        private val inner = v.findViewById<RecyclerView>(R.id.innerShortsGrid)
        private val innerAdapter = ShortsGridAdapter(emptyList(), onShortClick)
        init {
            inner.layoutManager = GridLayoutManager(v.context, 2)
            inner.adapter = innerAdapter
        }
        fun bind(items: List<ShortsSearchItem>) {
            innerAdapter.submit(items.take(2))
        }
    }
}
