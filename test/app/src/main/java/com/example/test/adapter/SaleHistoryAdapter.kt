package com.example.test.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleHistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade, parent, false)
        return SaleHistoryViewHolder(view)
    }

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

                RetrofitInstance.apiService.completeTradePost(token, currentItem.id)
                    .enqueue(object : Callback<TradePostSimpleResponse> {
                        override fun onResponse(
                            call: Call<TradePostSimpleResponse>,
                            response: Response<TradePostSimpleResponse>
                        ) {
                            if (response.isSuccessful) {
                                // 서버 응답 성공하면 해당 아이템 상태 변경
                                (tradeList as MutableList)[currentPosition] = currentItem.copy(isCompleted = true)
                                notifyItemChanged(currentPosition)
                            } else {
                                Log.e("TradePost", "거래완료 실패: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<TradePostSimpleResponse>, t: Throwable) {
                            Log.e("TradePost", "거래완료 에러: ${t.message}")
                        }
                    })
            }
        }
        // 더보기 버튼 클릭 리스너 (필요하면 연결)
        holder.moreButton.setOnClickListener {
            // TODO: 수정/삭제 팝업 띄우기
        }
    }

    override fun getItemCount(): Int = tradeList.size

    // 데이터 갱신 함수 (필터링 후 사용할 것)
    fun updateList(newList: List<TradeItem>) {
        tradeList = newList
        notifyDataSetChanged()
    }
}