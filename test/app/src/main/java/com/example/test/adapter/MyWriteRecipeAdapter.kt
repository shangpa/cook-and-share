package com.example.test.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.recipeDetail.MyWriteRecipe
import com.example.test.network.RetrofitInstance

class MyWriteRecipeAdapter(
    private val recipeList: List<MyWriteRecipe>,
    private val onMoreClick: (MyWriteRecipe) -> Unit
) : RecyclerView.Adapter<MyWriteRecipeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.imageThumbnail)
        val title: TextView = view.findViewById(R.id.titleText)
        val heartCount: TextView = view.findViewById(R.id.heartCountText)
        val goodCount: TextView = view.findViewById(R.id.goodCountText)
        val date: TextView = view.findViewById(R.id.dateText)
        val moreIcon: ImageButton = view.findViewById(R.id.moreIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_write_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = recipeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recipeList[position]
        holder.title.text = item.title
        holder.heartCount.text = item.heartCount.toString()
        holder.goodCount.text = item.likeCount.toString()
        holder.date.text = "${item.createdAt}"
        val imageUrl = item.mainImageUrl?.let {
            if (it.startsWith("http")) it
            else RetrofitInstance.BASE_URL.trimEnd('/') + "/" + it.trimStart('/')
        }
        if (imageUrl != null) {
            Log.d("ImageUrl", imageUrl)
        }
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.thumbnail)

        holder.moreIcon.setOnClickListener { onMoreClick(item) }
    }
}
