package com.example.test

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VideoTabFragment : Fragment() {

    companion object { fun newInstance() = VideoTabFragment() }

    // 개발 중: false(더미), 서버 붙일 때: true (fetch 주석 해제)
    private val USE_REMOTE = false

    private lateinit var rv: RecyclerView
    private lateinit var adapter: VideoGridAdapter
    private val videos: MutableList<LocalVideo> = mutableListOf()

    // 정렬 버튼
    private lateinit var latestLayout: LinearLayout
    private lateinit var popularityLayout: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var latestText: TextView
    private lateinit var popularityText: TextView
    private lateinit var dateText: TextView

    private enum class Sort { LATEST, POPULARITY, DATE }
    private var currentSort = Sort.LATEST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_video_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // RecyclerView
        rv = view.findViewById(R.id.videoRecycler)
        val spanCount = 3
        rv.layoutManager = GridLayoutManager(requireContext(), spanCount)

        // ⬇아이템 간격(좌우/상하) 균일하게
        val spacingPx = (resources.displayMetrics.density * 8).toInt()
        rv.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx))

        adapter = VideoGridAdapter(videos, spanCount, spacingPx) { item ->
            Toast.makeText(requireContext(), "${item.title} 클릭", Toast.LENGTH_SHORT).show()
        }
        rv.adapter = adapter

        // 정렬 UI 바인딩
        latestLayout = view.findViewById(R.id.latest)
        popularityLayout = view.findViewById(R.id.popularity)
        dateLayout = view.findViewById(R.id.date)
        latestText = view.findViewById(R.id.latest_text)
        popularityText = view.findViewById(R.id.popularity_text)
        dateText = view.findViewById(R.id.date_text)

        latestLayout.setOnClickListener { onSortSelected(Sort.LATEST) }
        popularityLayout.setOnClickListener { onSortSelected(Sort.POPULARITY) }
        dateLayout.setOnClickListener { onSortSelected(Sort.DATE) }

        // 초기 상태
        applySortUI(Sort.LATEST)

        if (USE_REMOTE) {
            // fetchVideosFromServer("latest")  // ← 백엔드 붙일 때 주석 해제
            Toast.makeText(requireContext(), "백엔드 연결 코드 주석 해제 필요", Toast.LENGTH_SHORT).show()
        } else {
            loadDummy(count = 8) // 더미 개수만 바꾸면 자동 반영
        }
    }

    private fun onSortSelected(sort: Sort) {
        if (currentSort == sort) return
        currentSort = sort

        val key = when (sort) {
            Sort.LATEST -> "latest"
            Sort.POPULARITY -> "views"
            Sort.DATE -> "date"
        }

        when (key) {
            "latest" -> videos.sortByDescending { it.createdAt }
            "views"  -> videos.sortByDescending { it.views }
            "date"   -> videos.sortBy { it.createdAt }
        }
        adapter.notifyDataSetChanged()
        applySortUI(sort)

        if (USE_REMOTE) {
            // fetchVideosFromServer(key)  // ← 백엔드 붙일 때 주석 해제
        }
    }

    private fun applySortUI(sort: Sort) {
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = 0xFF8A8F9C.toInt()
        val bgSelected = ContextCompat.getDrawable(requireContext(), R.drawable.btn_fridge_ct_ck)
        val bgUnselected = ContextCompat.getDrawable(requireContext(), R.drawable.btn_fridge_ct)

        fun setSel(container: LinearLayout, tv: TextView, selected: Boolean) {
            container.background = if (selected) bgSelected else bgUnselected
            tv.setTextColor(if (selected) selectedTextColor else unselectedTextColor)
        }
        setSel(latestLayout, latestText, sort == Sort.LATEST)
        setSel(popularityLayout, popularityText, sort == Sort.POPULARITY)
        setSel(dateLayout, dateText, sort == Sort.DATE)
    }

    // ───────────── 더미/백엔드 전환 유틸 ─────────────

    private fun loadDummy(count: Int) {
        videos.clear()
        videos.addAll(generateDummyVideos(count))
        videos.sortByDescending { it.createdAt }  // 기본 최신순
        adapter.notifyDataSetChanged()
    }

    private fun generateDummyVideos(count: Int): List<LocalVideo> {
        val base = 20250821
        return (1..count).map { i ->
            LocalVideo(
                id = i.toLong(),
                title = "영상 $i",
                createdAt = base - i,
                views = (500..15000).random()
            )
        }
    }

    @Suppress("unused")
    private fun fetchVideosFromServer(sort: String) {
        // Retrofit 사용 시 여기 주석 해제해 연결 (현재는 더미만 사용)
        Toast.makeText(requireContext(), "백엔드 연결 준비 완료(주석 해제 필요)", Toast.LENGTH_SHORT).show()
    }

    // ───────────── 내부 모델 & 어댑터 & 데코레이션 ─────────────

    private data class LocalVideo(
        val id: Long,
        val title: String,
        val createdAt: Int,   // YYYYMMDD
        val views: Int
    )

    /** 아이템 간격용 데코레이션(좌우/상하 균일) */
    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    /** 9:16 세로 카드, 하단 오버레이(제목/조회수), 3열 */
    private class VideoGridAdapter(
        private val items: List<LocalVideo>,
        private val spanCount: Int,
        private val spacingPx: Int,
        private val onClick: (LocalVideo) -> Unit
    ) : RecyclerView.Adapter<VideoGridAdapter.VH>() {

        class VH(
            val root: FrameLayout,
            val thumb: View,
            val title: TextView,
            val meta: TextView
        ) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            // 가용 너비 = 부모너비 - 패딩 - (간격*(열-1))
            val parentWidth = parent.measuredWidth.takeIf { it > 0 }
                ?: parent.resources.displayMetrics.widthPixels
            val available = parentWidth - parent.paddingLeft - parent.paddingRight - spacingPx * (spanCount - 1)
            val width = available / spanCount
            val height = (width * 16f / 9f).toInt() // 9:16 세로 카드
            val root = FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(width, height)
            }

            val img = View(parent.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(0xFFEFEFEF.toInt())
            }

            val overlay = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM
                )
                setPadding(12, 8, 12, 8)
                setBackgroundColor(0x66000000)
            }

            val title = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(0xFFFFFFFF.toInt())
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            val meta = TextView(parent.context).apply {
                textSize = 10f
                setTextColor(0xFFEFEFEF.toInt())
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            overlay.addView(title)
            overlay.addView(meta)

            root.addView(img)
            root.addView(overlay)
            return VH(root, img, title, meta)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            holder.meta.text = "조회수 ${formatViews(item.views)}"
            holder.root.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size

        private fun formatViews(v: Int): String {
            return when {
                v >= 1_000_000 -> String.format("%.1f만", v / 10_000f)
                v >= 10_000    -> String.format("%.1f만", v / 10_000f)
                else           -> v.toString()
            }
        }
    }
}
