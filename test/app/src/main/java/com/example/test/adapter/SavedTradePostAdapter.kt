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
import com.google.gson.Gson

class SavedTradePostAdapter(
    private val items: List<TradePostResponse>
) : RecyclerView.Adapter<SavedTradePostAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.saveItemTitle)
        val price: TextView = view.findViewById(R.id.saveItemPrice)
        val image: ImageView = view.findViewById(R.id.saveImage)
        val date: TextView = view.findViewById(R.id.dateText)
        val commentCount: TextView = view.findViewById(R.id.commentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_trade_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.price.text = if (item.price == 0) "나눔" else "${item.price} P"
        //todo 채팅갯수 표시해야함
        // holder.commentCount.text = item.commentCount.toString()
        holder.date.text = item.purchaseDate.substring(5) // MM-DD 포맷

        val images = Gson().fromJson(item.imageUrls, Array<String>::class.java)
        Glide.with(holder.itemView.context)
            .load(RetrofitInstance.BASE_URL + images.firstOrNull())
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
