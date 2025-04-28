package com.example.test.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.MaterialDetailActivity
import com.example.test.R
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.network.RetrofitInstance

class TradePostAdapter(
    private val tradePosts: List<TradePostResponse>,
    private val onItemClick: (TradePostResponse) -> Unit
    ) :RecyclerView.Adapter<TradePostAdapter.TradePostViewHolder>() {

    class TradePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val temperatureText: TextView = itemView.findViewById(R.id.temperatureText)
        val commentCount: TextView = itemView.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trade_post, parent, false)
        return TradePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradePostViewHolder, position: Int) {
        val tradePost = tradePosts[position]

        holder.itemTitle.text = tradePost.title
        holder.distanceText.text = "로그인 시 확인 가능"
        holder.itemPrice.text = if (tradePost.price == 0) {
            "나눔"
        } else {
            String.format("%,dP", tradePost.price)
        }
        holder.temperatureText.text = "70도"
        holder.commentCount.text = "1"
        val baseUrl =RetrofitInstance.BASE_URL
        if (!tradePost.imageUrls.isNullOrEmpty()) {
            val urls = tradePost.imageUrls.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .split(",")
                .map { it.trim() }

            if (urls.isNotEmpty() && urls[0].isNotBlank()) {
                val fullImageUrl = baseUrl + urls[0] // ✅ 여기서 baseUrl + 상대 경로
                Glide.with(holder.itemView.context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.img_kitchen1)
                    .into(holder.itemImage)
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(tradePost)
        }
    }

    override fun getItemCount(): Int = tradePosts.size
}
