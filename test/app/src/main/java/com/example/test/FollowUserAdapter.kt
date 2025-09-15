package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.profile.FollowUserUi

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
            tvNick.text = item.name
            tvHandle.text = "@${item.username}"
            // 필요시: Glide.with(img).load(item.profileImageUrl).into(img)

            // ✅ 내 프로필이면 버튼 숨기기
            val myId = App.prefs.userId
            if (myId != null && item.userId.toLong() == myId) {
                btn.visibility = View.GONE
            } else {
                btn.visibility = View.VISIBLE
                applyButtonStyle(item)
                // 팔로우 버튼 클릭 리스너 등록
                btn.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onFollowToggle(pos, item)
                    }
                }
            }

            // ✅ 프로필 이미지/닉네임/핸들 클릭 시 OtherProfileActivity 이동
            val profileClickListener = View.OnClickListener {
                val context = itemView.context
                val intent = android.content.Intent(context, MyProfileActivity::class.java).apply {
                    putExtra("targetUserId", item.userId) // userId 전달
                }
                context.startActivity(intent)
            }
            img.setOnClickListener(profileClickListener)
            tvNick.setOnClickListener(profileClickListener)
            tvHandle.setOnClickListener(profileClickListener)
        }
        private fun applyButtonStyle(item: FollowUserUi) {
            if (item.isFollowingByMe) {
                btn.text = "취소"
                btn.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                btn.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.button_border)
            } else {
                btn.text = "팔로우"
                btn.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                btn.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.rounded_button)
            }
        }
    }
}
