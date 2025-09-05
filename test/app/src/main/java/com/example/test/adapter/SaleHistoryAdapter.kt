package com.example.test.adapter

import App.Companion.context
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.App
import com.example.test.R
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.model.UserSimpleResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import com.example.test.MaterialDetailMyProfileActivity

class SaleHistoryAdapter(
    private var tradeList: List<TradeItem>,
    private val token: String
) : RecyclerView.Adapter<SaleHistoryAdapter.SaleHistoryViewHolder>() {

    inner class SaleHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageItem: ImageView = itemView.findViewById(R.id.imageItem)
        val titleItem: TextView = itemView.findViewById(R.id.itemTitle)
        val moreButton: ImageView = itemView.findViewById(R.id.itemMore)
        val distanceText: TextView = itemView.findViewById(R.id.distanceText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val priceItem: TextView = itemView.findViewById(R.id.itemPrice)
        val completedIcon: ImageView = itemView.findViewById(R.id.completedIcon)
        val completeButtonLayout: View = itemView.findViewById(R.id.completeButtonLayout)
        val liftDropdown: View = itemView.findViewById(R.id.liftDropdown)
        val liftMenu: TextView = itemView.findViewById(R.id.liftMenu)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade, parent, false)
        return SaleHistoryViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: SaleHistoryViewHolder, position: Int) {
        val item = tradeList[position]

        // 제목
        holder.titleItem.text = item.title

        // 거리
        holder.distanceText.text = item.distance

        // 날짜
        holder.dateText.text = item.date

        // 가격
        holder.priceItem.text = item.price

        // 이미지 (Glide로 로드)
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)  // 임시 기본 이미지
            .into(holder.imageItem)

        holder.itemView.setOnClickListener {
            val ctx = it.context
            val intent = Intent(ctx, MaterialDetailMyProfileActivity::class.java)
            intent.putExtra("postId", item.id)   // postId 전달
            ctx.startActivity(intent)
        }

        // 거래완료 상태에 따라 뷰 조정
        if (item.isCompleted) {
            holder.completedIcon.visibility = View.VISIBLE
            holder.completeButtonLayout.visibility = View.GONE
        } else {
            holder.completedIcon.visibility = View.GONE
            holder.completeButtonLayout.visibility = View.VISIBLE
        }
        // completeButtonLayout 클릭 리스너 추가
        holder.completeButtonLayout.setOnClickListener {
            val context = holder.itemView.context

            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val currentItem = tradeList[currentPosition]
                RetrofitInstance.materialApi.getCompleteRequestUsers(token, currentItem.id)
                    .enqueue(object : Callback<List<UserSimpleResponse>> {
                        override fun onResponse(call: Call<List<UserSimpleResponse>>, response: Response<List<UserSimpleResponse>>) {
                            if (response.isSuccessful) {
                                val userList = response.body() ?: emptyList()
                                if (userList.isEmpty()) {
                                    Toast.makeText(context, "거래 요청한 구매자가 없습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    showBuyerSelectDialog(context, userList) { selectedUserId ->
                                        requestCompleteTradePost(context,currentItem.id, selectedUserId, currentPosition)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<UserSimpleResponse>>, t: Throwable) {
                            Log.e("TradePost", "요청자 조회 실패: ${t.message}")
                        }
                    })
            }
        }
        // 더보기 버튼 클릭 리스너 (필요하면 연결)
        holder.moreButton.setOnClickListener {
            val context = holder.itemView.context
            val popupView = LayoutInflater.from(context).inflate(R.layout.popup_lift_menu, null)
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // focusable: 바깥 터치 시 닫힘
            )

            // 끌어올리기 메뉴 클릭 처리
            val liftMenu = popupView.findViewById<TextView>(R.id.liftMenu)
            liftMenu.setOnClickListener {
                Toast.makeText(context, "끌어올리기 완료", Toast.LENGTH_SHORT).show()
                popupWindow.dismiss()

                // 서버 요청 예시
                // RetrofitInstance.materialApi.liftPost(token, item.id)...
            }

            // 버튼 아래에 띄우기
            popupWindow.elevation = 10f
            popupWindow.showAsDropDown(holder.moreButton, 0, 10) // (x, y offset)
        }

    }
    // TODO: 수정/삭제 팝업 띄우기
    private fun requestCompleteTradePost(context: Context, postId: Long, buyerId: Long, position: Int) {
        Log.d("✅ buyer_final", "postId: $postId, buyerId: $buyerId")
        RetrofitInstance.materialApi.completeTradePost(token, postId, buyerId)
            .enqueue(object : Callback<TradePostSimpleResponse> {
                override fun onResponse(
                    call: Call<TradePostSimpleResponse>,
                    response: Response<TradePostSimpleResponse>
                ) {
                    if (response.isSuccessful) {
                        (tradeList as MutableList)[position] = tradeList[position].copy(isCompleted = true)
                        notifyItemChanged(position)
                        Toast.makeText(context,"거래가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "거래완료 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TradePostSimpleResponse>, t: Throwable) {
                    Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun getItemCount(): Int = tradeList.size


    private fun showBuyerSelectDialog(
        context: Context,
        users: List<UserSimpleResponse>,
        onSelected: (Long) -> Unit
    ) {
        val names = users.map { it.nickname }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("구매자 선택")
            .setItems(names) { _, which ->
                val selectedUser = users[which]
                Log.d("✅ buyer_log", "선택된 사용자 ID: ${selectedUser.id}, 닉네임: ${selectedUser.nickname}")
                onSelected(selectedUser.id)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    // 데이터 갱신 함수 (필터링 후 사용할 것)
    fun updateList(newList: List<TradeItem>) {
        tradeList = newList
        notifyDataSetChanged()
    }
}

