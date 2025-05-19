package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.board.CommunityDetailResponse
import com.example.test.network.RetrofitInstance

class MyCommunityPostAdapter(
    private val postList: List<CommunityDetailResponse>,
    private val onMoreClick: (CommunityDetailResponse, View) -> Unit
) : RecyclerView.Adapter<MyCommunityPostAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postDate: TextView = view.findViewById(R.id.postDate)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postLikeCount: TextView = view.findViewById(R.id.postLikeCount)
        val postCommentCount: TextView = view.findViewById(R.id.postCommentCount)
        val postMoreButton: ImageButton = view.findViewById(R.id.postMoreButton)

        fun bind(post: CommunityDetailResponse) {
            // 날짜 표시
            postDate.text = post.createdAt.take(10) // "yyyy-MM-dd"

            // 본문 내용
            postContent.text = post.content

            // 좋아요/댓글 수
            postLikeCount.text = post.likeCount.toString()
            postCommentCount.text = post.commentCount.toString()

            // 이미지 처리
            if (post.imageUrls.isNotEmpty()) {
                postImage.visibility = View.VISIBLE
                Glide.with(postImage.context)
                    .load(RetrofitInstance.BASE_URL + post.imageUrls[0])
                    .into(postImage)
            } else {
                postImage.visibility = View.GONE
            }

            // 더보기 버튼 클릭
            postMoreButton.setOnClickListener {
                onMoreClick(post, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int = postList.size
}
