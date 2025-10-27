package com.example.test

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test.model.shorts.ShortVideoDto
import com.example.test.model.shorts.ShortVideoListResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoTabFragment : Fragment() {

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        fun newInstance(userId: Int) = VideoTabFragment().apply {
            arguments = Bundle().apply { putInt(ARG_USER_ID, userId) }
        }
    }

    private var targetUserId: Int = -1

    private lateinit var rv: RecyclerView
    private lateinit var adapter: VideoGridAdapter
    private val videos: MutableList<ShortVideoDto> = mutableListOf()

    private lateinit var latestLayout: LinearLayout
    private lateinit var popularityLayout: LinearLayout
    private lateinit var dateLayout: LinearLayout
    private lateinit var latestText: TextView
    private lateinit var popularityText: TextView
    private lateinit var dateText: TextView

    private enum class Sort { LATEST, POPULARITY, DATE }
    private var currentSort = Sort.LATEST

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_video_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        targetUserId = arguments?.getInt(ARG_USER_ID, -1) ?: -1

        rv = view.findViewById(R.id.videoRecycler)
        val spanCount = 3
        rv.layoutManager = GridLayoutManager(requireContext(), spanCount)
        val spacingPx = (resources.displayMetrics.density * 8).toInt()
        rv.addItemDecoration(GridSpacingItemDecoration(spanCount, spacingPx))

        adapter = VideoGridAdapter(videos, spanCount, spacingPx) { position, item ->
            // 바로 쇼츠 재생 화면으로 이동
            val intent = Intent(requireContext(), ShortsActivity::class.java).apply {
                putExtra("mode", "user")                // ← 유저 쇼츠 모드
                putExtra("userId", targetUserId)        // ← 현재 프로필 주인 ID
                putExtra("sort", currentSortKey())      // ← latest|views|date
                putExtra("startIndex", position)        // ← 사용자가 클릭한 인덱스부터 재생
            }
            startActivity(intent)
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

        applySortUI(Sort.LATEST)
        fetchVideosFromServer("latest")
    }

    private fun onSortSelected(sort: Sort) {
        if (currentSort == sort) return
        currentSort = sort
        val key = when (sort) {
            Sort.LATEST -> "latest"
            Sort.POPULARITY -> "views"
            Sort.DATE -> "date"
        }
        applySortUI(sort)
        fetchVideosFromServer(key)
    }

    private fun fetchVideosFromServer(sort: String, page: Int = 0) {
        val token = App.prefs.token
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (targetUserId == -1) {
            Toast.makeText(requireContext(), "대상 유저 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitInstance.apiService.getUserShorts("Bearer $token", targetUserId, sort, page, 30)
            .enqueue(object : Callback<ShortVideoListResponse> {
                override fun onResponse(
                    call: Call<ShortVideoListResponse>,
                    response: Response<ShortVideoListResponse>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val list = response.body()?.videos ?: emptyList()
                        videos.clear()
                        videos.addAll(list)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ShortVideoListResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /** 정렬 UI (✅ VideoTabFragment.Sort 사용) */
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

    /** 3열 그리드 간격 */
    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            outRect.top = spacing
            outRect.bottom = spacing
        }
    }

    /** 썸네일 카드 어댑터 */
    private class VideoGridAdapter(
        private val items: List<ShortVideoDto>,
        private val spanCount: Int,
        private val spacingPx: Int,
        private val onClick: (Int, ShortVideoDto) -> Unit
    ) : RecyclerView.Adapter<VideoGridAdapter.VH>() {

        class VH(
            val root: FrameLayout,
            val thumb: ImageView,
            val title: TextView,
            val meta: TextView
        ) : RecyclerView.ViewHolder(root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val parentWidth = parent.measuredWidth.takeIf { it > 0 }
                ?: parent.resources.displayMetrics.widthPixels
            val available = parentWidth - parent.paddingLeft - parent.paddingRight - spacingPx * (spanCount - 1)
            val width = available / spanCount
            val height = (width * 16f / 9f).toInt()

            val root = FrameLayout(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(width, height)
            }
            val img = ImageView(parent.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
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
            holder.meta.text = "조회수 ${formatViews(item.viewCount)}"

            // ✅ 썸네일 URL 절대경로 변환
            val thumbUrl = item.thumbnailUrl?.trim()
            val fullUrl = RetrofitInstance.toAbsoluteUrl(thumbUrl)

            if (!fullUrl.isNullOrEmpty()) {
                Glide.with(holder.thumb.context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_writer_badge)
                    .centerCrop()
                    .into(holder.thumb)
            } else if (!item.videoUrl.isNullOrEmpty()) {
                // ✅ 썸네일이 없으면 비디오 첫 프레임 표시
                Glide.with(holder.thumb.context)
                    .asBitmap()
                    .load(item.videoUrl)
                    .frame(1_000_000) // 1초 시점 프레임
                    .centerCrop()
                    .placeholder(R.drawable.ic_writer_badge)
                    .into(holder.thumb)
            } else {
                holder.thumb.setImageResource(R.drawable.ic_writer_badge)
            }

            holder.root.setOnClickListener { onClick(position, item) }
        }

        override fun getItemCount() = items.size

        private fun formatViews(v: Int): String =
            if (v >= 10_000) String.format("%.1f만", v / 10_000f) else v.toString()
    }
    private fun currentSortKey(): String = when (currentSort) {
        Sort.LATEST -> "latest"
        Sort.POPULARITY -> "views"
        Sort.DATE -> "date"
    }
}
