package com.example.test.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.R
import com.example.test.model.pantry.PantryResponse

class RefrigeratorAdapter(
    private val onEdit: (PantryResponse) -> Unit,
    private val onClick: (PantryResponse) -> Unit
) : RecyclerView.Adapter<RefrigeratorAdapter.VH>() {

    /** 아이템 리스트 */
    private val items = mutableListOf<PantryResponse>()

    /** 카드별 현재 선택 필터 상태 저장 */
    private val selectedFilterById = mutableMapOf<Long, Filter>()

    /** 임시 카운트 */
    private val dummyCounts = mapOf(
        Filter.ALL to 10,
        Filter.FRIDGE to 5,
        Filter.FREEZER to 3,
        Filter.ROOM to 2
    )

    enum class Filter(val label: String) {
        ALL("전체 재료"),
        FRIDGE("냉장 재료"),
        FREEZER("냉동 재료"),
        ROOM("상온 재료")
    }

    fun submit(list: List<PantryResponse>) {
        items.clear()
        items.addAll(list)
        // 새 목록에서는 기본값으로 ALL 지정
        list.forEach { it.id?.let { id -> if (selectedFilterById[id] == null) selectedFilterById[id] = Filter.ALL } }
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgCover)
        val name: TextView = v.findViewById(R.id.tvName)
        val memo: TextView = v.findViewById(R.id.tvMemo)
        val btnEdit: ImageButton = v.findViewById(R.id.btnEdit)

        // 드롭다운 관련 뷰
        val tvTotal: TextView = v.findViewById(R.id.total)
        val tvCount: TextView = v.findViewById(R.id.count)
        val btnDrop: ImageButton = v.findViewById(R.id.totalDropBox)

        init {
            // 카드 클릭 콜백
            v.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(items[pos])
                }
            }

            // 드롭다운 버튼 클릭 시 PopupMenu 표시
            btnDrop.setOnClickListener { anchor ->
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val item = items[pos]
                val id = item.id ?: return@setOnClickListener

                val popup = PopupMenu(anchor.context, anchor)
                popup.menuInflater.inflate(R.menu.menu_refrigerator_filter, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    val selected = when (menuItem.itemId) {
                        R.id.menu_all -> Filter.ALL
                        R.id.menu_fridge -> Filter.FRIDGE
                        R.id.menu_freezer -> Filter.FREEZER
                        R.id.menu_room -> Filter.ROOM
                        else -> Filter.ALL
                    }
                    // 상태 저장
                    selectedFilterById[id] = selected
                    // 현재 홀더 UI 갱신
                    tvTotal.text = selected.label
                    tvCount.text = dummyCounts[selected]?.toString() ?: "0"
                    true
                }
                popup.show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_refrigerator, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.memo.text = item.note ?: ""

        // 썸네일
        val raw = item.imageUrl
        if (raw.isNullOrBlank()) {
            holder.img.setImageResource(R.drawable.image_pantry)
        } else {
            val displayUrl = com.example.test.network.RetrofitInstance.toAbsoluteUrl(raw)
            Glide.with(holder.img.context)
                .load(displayUrl)
                .placeholder(R.drawable.image_pantry)
                .into(holder.img)
        }

        // 편집 버튼
        holder.btnEdit.setOnClickListener { onEdit(item) }

        // 드롭다운 현재 상태 반영
        val currentFilter = selectedFilterById[item.id ?: -1L] ?: Filter.ALL
        holder.tvTotal.text = currentFilter.label
        holder.tvCount.text = dummyCounts[currentFilter]?.toString() ?: "0"
    }

    override fun getItemCount(): Int = items.size
}
