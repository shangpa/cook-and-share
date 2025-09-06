package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ShortsCommentAdapter(
    private val onReportClick: (Int) -> Unit
) : ListAdapter<CommentUi, ShortsCommentAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CommentUi>() {
            override fun areItemsTheSame(oldItem: CommentUi, newItem: CommentUi): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: CommentUi, newItem: CommentUi): Boolean =
                oldItem == newItem
        }
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProfile: ImageView = itemView.findViewById(R.id.img_profile)
        private val tvNickname: TextView = itemView.findViewById(R.id.tv_nickname)
        private val tvReport: TextView = itemView.findViewById(R.id.tv_report)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(item: CommentUi) {
            tvNickname.text = item.nickname
            tvContent.text = item.content
            tvDate.text = item.dateText

            if (!item.profileUrl.isNullOrBlank()) {
                Glide.with(itemView).load(item.profileUrl).circleCrop().into(imgProfile)
            } else {
                // 기본 아이콘 유지
            }

            tvReport.setOnClickListener { onReportClick(bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shorts_comment, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
