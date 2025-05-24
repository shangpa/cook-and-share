package com.example.test.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.model.TradePost.TradeItem
import com.bumptech.glide.Glide
import com.example.test.MaterialReviewWriteActivity

class PurchaseHistoryAdapter(
    private var tradeList: List<TradeItem>
) : RecyclerView.Adapter<PurchaseHistoryAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageItem: ImageView = itemView.findViewById(R.id.imageItem)
        val titleItem: TextView = itemView.findViewById(R.id.itemTitle)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val dateText: TextView = itemView.findViewById(R.id.purchaseDate)
        val priceItem: TextView = itemView.findViewById(R.id.itemPrice)
        val reviewWrite: LinearLayout = itemView.findViewById(R.id.reviewWrite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_purchase, parent, false)  // 너가 새로 만든 layout 파일 이름이 여기
        return PurchaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val item = tradeList[position]

        holder.titleItem.text = item.title
        holder.distanceText.text = item.distance
        holder.dateText.text = "구입 날짜 : ${item.date}"
        holder.priceItem.text = item.price

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageItem)

        holder.reviewWrite.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, MaterialReviewWriteActivity::class.java)
            intent.putExtra("tradePostId", item.id)  // 필요한 데이터 전달
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tradeList.size

    fun updateList(newList: List<TradeItem>) {
        tradeList = newList
        notifyDataSetChanged()
    }
}

