package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.community.CommunityDetailResponse
import com.example.test.network.RetrofitInstance

class BoardPostAdapter(
    private val onPostClick: (Long) -> Unit
) : ListAdapter<CommunityDetailResponse, BoardPostAdapter.BoardViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CommunityDetailResponse>() {
            override fun areItemsTheSame(a: CommunityDetailResponse, b: CommunityDetailResponse) = a.id == b.id
            override fun areContentsTheSame(a: CommunityDetailResponse, b: CommunityDetailResponse) = a == b
        }
    }

    inner class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val authorText = view.findViewById<TextView>(R.id.authorText)
        private val timeText = view.findViewById<TextView>(R.id.timeText)
        private val contentText = view.findViewById<TextView>(R.id.contentText)
        private val saveBtn = view.findViewById<ImageView>(R.id.saveBtn)
        private val goodBtn = view.findViewById<ImageView>(R.id.goodBtn)
        private val commentCount = view.findViewById<TextView>(R.id.commentCount)
        private val goodCount = view.findViewById<TextView>(R.id.goodCount)
        private val imageContainer = view.findViewById<LinearLayout>(R.id.imageContainer)
        private val imageScroll = view.findViewById<HorizontalScrollView>(R.id.imageScroll)

        fun bind(item: CommunityDetailResponse) {
            authorText.text = item.writer
            contentText.text = item.content
            timeText.text = parseTime(item.createdAt)
            commentCount.text = item.commentCount.toString()
            goodCount.text = item.likeCount.toString()
            // 저장/좋아요 상태 UI 바인딩
            saveBtn.setImageResource(if (item.liked) R.drawable.ic_store_fill else R.drawable.ic_store)
            goodBtn.setImageResource(if (item.liked) R.drawable.ic_good_fill else R.drawable.ic_good)

            // ----- 이미지 바인딩 -----
            imageContainer.removeAllViews() // 항상 초기화!
            if (item.imageUrls.isNotEmpty()) {
                imageScroll.visibility = View.VISIBLE
                val dp = { value: Int -> (value * imageContainer.resources.displayMetrics.density).toInt() }
                for (url in item.imageUrls) {
                    val imageView = ImageView(imageContainer.context).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(110), dp(110)).apply {
                            rightMargin = dp(12)
                        }
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                    Glide.with(imageContainer.context)
                        .load(RetrofitInstance.BASE_URL + url)
                        .placeholder(R.drawable.img_kitchen1)
                        .into(imageView)
                    imageView.setOnClickListener { onPostClick(item.id) }
                    imageContainer.addView(imageView)
                }
            } else {
                imageScroll.visibility = View.GONE
            }

            itemView.setOnClickListener { onPostClick(item.id) }
            // 저장/좋아요 클릭 리스너도 추가로 구현 가능
        }

        private fun parseTime(str: String): String {
            // createdAt 예시: "2024-05-16T10:30:15"
            return if (str.length >= 16) "${str.substring(5,7)}.${str.substring(8,10)} ${str.substring(11,16)}" else str
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BoardViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_board_post, parent, false)
    )
    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) = holder.bind(getItem(position))
}
