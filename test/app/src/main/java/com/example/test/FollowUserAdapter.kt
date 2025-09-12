package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class FollowUserAdapter(
    private val onFollowToggle: (pos: Int, item: FollowUserUi) -> Unit
) : RecyclerView.Adapter<FollowUserAdapter.VH>() {

    private val items = mutableListOf<FollowUserUi>()

    fun submit(list: List<FollowUserUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getItem(position: Int) = items[position]

    fun updateItem(position: Int, newItem: FollowUserUi) {
        items[position] = newItem
        notifyItemChanged(position)
    }

    fun addIfAbsentForFollowings(newItem: FollowUserUi) {
        if (items.none { it.userId == newItem.userId }) {
            items.add(0, newItem)
            notifyItemInserted(0)
        }
    }

    fun removeByUserId(userId: Int) {
        val idx = items.indexOfFirst { it.userId == userId }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_follow_user, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img = itemView.findViewById<ImageView>(R.id.imgProfile)
        private val tvNick = itemView.findViewById<TextView>(R.id.tvNickname)
        private val tvHandle = itemView.findViewById<TextView>(R.id.tvHandle)
        private val btn = itemView.findViewById<TextView>(R.id.btnFollow)

        fun bind(item: FollowUserUi) {
            tvNick.text = item.nickname
            tvHandle.text = "@${item.handle}"
            // 필요시: Glide.with(img).load(item.profileImageUrl).into(img)

            applyButtonStyle(item)

            btn.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onFollowToggle(pos, item)
                }
            }
        }

        private fun applyButtonStyle(item: FollowUserUi) {
            if (item.isFollowingByMe) {
                // 취소(언팔)
                btn.text = "취소"
                btn.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                btn.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.button_border)
            } else {
                // 팔로우
                btn.text = "팔로우"
                btn.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                btn.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.rounded_button)
            }
        }
    }
}
