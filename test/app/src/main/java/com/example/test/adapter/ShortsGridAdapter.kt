package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.ShortsSearchItem
import com.example.test.network.RetrofitInstance

class ShortsGridAdapter(
    private var items: List<ShortsSearchItem>,
    private val onClick: (ShortsSearchItem) -> Unit
) : RecyclerView.Adapter<ShortsGridAdapter.Holder>() {

    inner class Holder(v: View): RecyclerView.ViewHolder(v) {
        val thumb: ImageView = v.findViewById(R.id.thumb)
        val title: TextView  = v.findViewById(R.id.title)
        val author: TextView = v.findViewById(R.id.author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_short_card, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(h: Holder, pos: Int) {
        val shortItem = items[pos]  // ← 이름 변경
        val url = (shortItem.thumbnailUrl ?: "")
            .let { path -> RetrofitInstance.BASE_URL + path.removePrefix("/") }

        Glide.with(h.itemView).load(url).into(h.thumb)
        h.title.text = shortItem.title
        h.author.text = shortItem.authorName ?: ""

        h.itemView.setOnClickListener {
            onClick(shortItem)     // ← View it 대신 shortItem 사용
        }
        // 또는: h.itemView.setOnClickListener { _ -> onClick(shortItem) }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<ShortsSearchItem>) {
        items = list
        notifyDataSetChanged()
    }
}
