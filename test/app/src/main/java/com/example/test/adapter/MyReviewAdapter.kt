package com.example.test.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.review.ReviewResponseDTO
import com.example.test.network.RetrofitInstance
import com.google.gson.Gson
import android.view.View

class MyReviewAdapter(
    private var reviewList: List<ReviewResponseDTO>,
    private val onEditClick: (ReviewResponseDTO) -> Unit,
    private val onDeleteClick: (ReviewResponseDTO) -> Unit
) : RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder>() {

    inner class MyReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.reviewRecipeTitle)
        val date: TextView = view.findViewById(R.id.reviewDateText)
        val rating: TextView = view.findViewById(R.id.reviewStarText)
        val content: TextView = view.findViewById(R.id.reviewContentText)
        val image: ImageView = view.findViewById(R.id.reviewRecipeImage)
        val deleteBtn: TextView = view.findViewById(R.id.reviewDeleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return MyReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.title.text = review.recipeTitle
        holder.date.text = review.createdAt.substring(0, 10)
        holder.rating.text = review.rating.toString()
        holder.content.text = review.content

        try {
            val urls = Gson().fromJson(review.mediaUrls, Array<String>::class.java)
            val firstUrl = urls.firstOrNull()
            if (!firstUrl.isNullOrBlank()) {
                val imageUrl = RetrofitInstance.BASE_URL + firstUrl
                Glide.with(holder.itemView.context).load(imageUrl).into(holder.image)
            } else {
                holder.image.setImageResource(R.drawable.image_review_list_food)
            }
        } catch (e: Exception) {
            holder.image.setImageResource(R.drawable.image_review_list_food)
        }

        holder.deleteBtn.setOnClickListener { onDeleteClick(review) }
    }

    override fun getItemCount(): Int = reviewList.size

    fun updateData(newList: List<ReviewResponseDTO>) {
        reviewList = newList
        notifyDataSetChanged()
    }
}
