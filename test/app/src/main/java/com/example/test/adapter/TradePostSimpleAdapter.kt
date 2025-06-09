package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.network.RetrofitInstance
import java.text.DecimalFormat

class TradePostSimpleAdapter(
    private var tradePosts: MutableList<TradePostSimpleResponse>,
    private val onItemClick: (TradePostSimpleResponse) -> Unit
) : RecyclerView.Adapter<TradePostSimpleAdapter.TradePostViewHolder>() {

    class TradePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trade_post, parent, false)
        return TradePostViewHolder(view)
    }

    override fun onBindViewHolder(holder: TradePostViewHolder, position: Int) {
        val tradePost = tradePosts[position]

        holder.itemTitle.text = tradePost.title
        holder.itemPrice.text = "${DecimalFormat("#,###").format(tradePost.price)} P"

        // 거리 텍스트만 문구 변경, 아이콘은 그대로 보여줌
        holder.itemView.findViewById<TextView>(R.id.distanceText).apply {
            text = "내 위치와의 거리 확인하기"
            visibility = View.VISIBLE
        }

        holder.itemView.findViewById<View>(R.id.distanceIcon).visibility = View.VISIBLE
        holder.itemView.findViewById<View>(R.id.itemMore).visibility = View.GONE

        if (!tradePost.firstImageUrl.isNullOrBlank()) {
            val fullImageUrl = RetrofitInstance.BASE_URL + tradePost.firstImageUrl
            holder.itemImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(fullImageUrl)
                .into(holder.itemImage)
        } else {
            holder.itemImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(tradePost)
        }
    }


    override fun getItemCount(): Int = tradePosts.size

    fun updateData(newPosts: List<TradePostSimpleResponse>) {
        tradePosts.clear()
        tradePosts.addAll(newPosts)
        notifyDataSetChanged()
    }
}
