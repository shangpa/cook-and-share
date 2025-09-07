package com.example.test.adapter

import App.Companion.context
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.example.test.R
import com.example.test.model.TradePost.TradeItem
import com.example.test.model.TradePost.TradePostSimpleResponse
import com.example.test.model.TradePost.TradePostUpResult
import com.example.test.model.UserSimpleResponse
import com.example.test.network.RetrofitInstance
import com.example.test.MaterialDetailMyProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SaleHistoryAdapter(
    private var tradeList: List<TradeItem>,
    private val token: String,                                // "Bearer xxx"
    private val onUpSuccess: ((postId: Long) -> Unit)? = null // 선택: 끌올 후 부모에서 새로고침하고 싶을 때
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

        // 제목/거리/날짜/가격
        holder.titleItem.text = item.title
        holder.distanceText.text = item.distance
        holder.dateText.text = item.date
        holder.priceItem.text = item.price

        // 썸네일
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageItem)

        // 상세 화면 이동 (인텐트 키를 "tradePostId"로 통일)
        holder.itemView.setOnClickListener {
            val ctx = it.context
            val intent = Intent(ctx, MaterialDetailMyProfileActivity::class.java)
            intent.putExtra("tradePostId", item.id)   // ★ 통일
            ctx.startActivity(intent)
        }

        // 거래완료 상태 표시
        if (item.isCompleted) {
            holder.completedIcon.visibility = View.VISIBLE
            holder.completeButtonLayout.visibility = View.GONE
        } else {
            holder.completedIcon.visibility = View.GONE
            holder.completeButtonLayout.visibility = View.VISIBLE
        }

        // [거래완료] 버튼 → 구매자 선택 → 완료 요청
        holder.completeButtonLayout.setOnClickListener {
            val context = holder.itemView.context
            val currentPosition = holder.adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val currentItem = tradeList[currentPosition]
            RetrofitInstance.materialApi.getCompleteRequestUsers(token, currentItem.id)
                .enqueue(object : Callback<List<UserSimpleResponse>> {
                    override fun onResponse(
                        call: Call<List<UserSimpleResponse>>,
                        response: Response<List<UserSimpleResponse>>
                    ) {
                        if (response.isSuccessful) {
                            val userList = response.body().orEmpty()
                            if (userList.isEmpty()) {
                                Toast.makeText(context, "거래 요청한 구매자가 없습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                showBuyerSelectDialog(context, userList) { selectedUserId ->
                                    requestCompleteTradePost(context, currentItem.id, selectedUserId, currentPosition)
                                }
                            }
                        } else {
                            Toast.makeText(context, "요청자 조회 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<UserSimpleResponse>>, t: Throwable) {
                        Log.e("TradePost", "요청자 조회 실패: ${t.message}")
                        Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // [더보기] 팝업 → 끌어올리기
        holder.moreButton.setOnClickListener {
            showLiftPopup(holder, item.id)
        }
    }

    private fun showLiftPopup(holder: SaleHistoryViewHolder, postId: Long) {
        val context = holder.itemView.context
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_lift_menu, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val liftMenu = popupView.findViewById<TextView>(R.id.liftMenu)
        liftMenu.setOnClickListener {
            // 중복 클릭 방지
            liftMenu.isEnabled = false

            RetrofitInstance.apiService
                .upTradePost(token, postId)
                .enqueue(object : Callback<TradePostUpResult> {
                    override fun onResponse(
                        call: Call<TradePostUpResult>,
                        response: Response<TradePostUpResult>
                    ) {
                        liftMenu.isEnabled = true
                        popupWindow.dismiss()

                        if (response.isSuccessful) {
                            // 서버가 사용 포인트를 내려주면 그 값을 쓰고, 없으면 500P로 표시
                            val used = response.body()?.usedPoints ?: 500
                            Toast.makeText(context, "끌어올렸어요! ${used}P 차감", Toast.LENGTH_SHORT).show()

                            // (선택) 끌어올리기 후 상위로 정렬된 피드를 보고 싶다면 부모에서 새로고침
                            onUpSuccess?.invoke(postId)
                            return
                        }

                        // --- 실패 처리: 포인트 부족 메시지 분기 ---
                        val code = response.code()
                        val raw = try { response.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                        val lower = raw.lowercase()

                        // 서버가 402/403/409 등으로 내려주거나, 에러 메시지에 포인트 부족 뉘앙스가 있으면
                        val insufficientByCode = code == 402 || code == 403 || code == 409
                        val insufficientByBody =
                            lower.contains("insufficient_point") ||
                                    lower.contains("insufficient") ||
                                    lower.contains("not enough") ||
                                    lower.contains("포인트") ||
                                    lower.contains("point")

                        if (insufficientByCode || insufficientByBody) {
                            Toast.makeText(context, "포인트가 부족합니다", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "업 실패 ($code)", Toast.LENGTH_SHORT).show()
                            Log.w("TradeUp", "fail code=$code body=$raw")
                        }
                    }

                    override fun onFailure(call: Call<TradePostUpResult>, t: Throwable) {
                        liftMenu.isEnabled = true
                        popupWindow.dismiss()
                        Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        popupWindow.elevation = 10f
        popupWindow.showAsDropDown(holder.moreButton, 0, 10)
    }


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
                        Toast.makeText(context, "거래가 완료되었습니다.", Toast.LENGTH_SHORT).show()
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

    fun updateList(newList: List<TradeItem>) {
        tradeList = newList
        notifyDataSetChanged()
    }
}
