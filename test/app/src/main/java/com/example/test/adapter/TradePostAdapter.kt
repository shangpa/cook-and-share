package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.TradePost.TradePostResponse
import com.example.test.network.RetrofitInstance
import kotlin.math.*
import java.text.DecimalFormat

class TradePostAdapter(
    private var tradePosts: MutableList<TradePostResponse>,
    private val onItemClick: (TradePostResponse) -> Unit,
    private val userLat: Double? = null,
    private val userLng: Double? = null
) : RecyclerView.Adapter<TradePostAdapter.TradePostViewHolder>() {

    class TradePostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemImage)
        val itemTitle: TextView = itemView.findViewById(R.id.itemTitle)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
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
        val decimalFormat = DecimalFormat("#,###")
        holder.itemPrice.text = "${decimalFormat.format(tradePost.price)} P"

        // 거리 계산 후 텍스트만 출력
        if (
            userLat != null && userLng != null &&
            tradePost.latitude != null && tradePost.longitude != null &&
            userLat != 0.0 && userLng != 0.0 &&
            tradePost.latitude != 0.0 && tradePost.longitude != 0.0
        ) {
            val distance = calculateDistance(userLat, userLng, tradePost.latitude, tradePost.longitude)

            holder.distanceText.text = if (distance < 1.0) {
                String.format("%.0f m", distance * 1000)
            } else {
                String.format("%.2f km", distance)
            }
            holder.distanceText.visibility = View.VISIBLE
        } else {
            holder.distanceText.text = "위치 설정 후 거리를 확인해보세요!"
            holder.distanceText.visibility = View.VISIBLE
        }

        // 이미지 로딩
        val baseUrl = RetrofitInstance.BASE_URL
        if (!tradePost.imageUrls.isNullOrEmpty()) {
            val urls = tradePost.imageUrls.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .split(",")
                .map { it.trim() }

            if (urls.isNotEmpty() && urls[0].isNotBlank()) {
                val fullImageUrl = baseUrl + urls[0]
                holder.itemImage.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(fullImageUrl)
                    .into(holder.itemImage)
            } else {
                holder.itemImage.visibility = View.GONE
            }
        } else {
            holder.itemImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(tradePost)
        }
    }

    override fun getItemCount(): Int = tradePosts.size

    fun updateData(newPosts: List<TradePostResponse>) {
        tradePosts.clear()
        tradePosts.addAll(newPosts)
        notifyDataSetChanged()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // 지구 반지름 (km)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
