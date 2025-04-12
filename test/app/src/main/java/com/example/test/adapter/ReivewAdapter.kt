package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import java.time.format.DateTimeFormatter

class ReviewAdapter(private var reviewList: List<ReviewResponseDTO>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.reviewUserName)
        val score: TextView = itemView.findViewById(R.id.reviewScore)
        val date: TextView = itemView.findViewById(R.id.reviewDate)
        val content: TextView = itemView.findViewById(R.id.reviewContent)
        val image: ImageView = itemView.findViewById(R.id.reviewImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_card, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.userName.text = review.username
        holder.score.text = review.rating.toString()
        holder.content.text = review.content
        holder.date.text = review.createdAt.toString().substring(0, 10).replace("-", ".")

        try {
            val urls = Gson().fromJson(review.mediaUrls, Array<String>::class.java)
            val firstUrl = urls.firstOrNull()

            if (!firstUrl.isNullOrBlank()) {
                holder.image.visibility = View.VISIBLE
                val imageUrl = RetrofitInstance.BASE_URL + firstUrl
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .into(holder.image)
            } else {
                holder.image.visibility = View.GONE
            }
        } catch (e: Exception) {
            holder.image.visibility = View.GONE
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int = reviewList.size

    fun updateData(newList: List<ReviewResponseDTO>) {
        reviewList = newList
        notifyDataSetChanged()
    }
}
