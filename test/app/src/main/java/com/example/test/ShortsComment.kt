package com.example.test

import android.os.Bundle
import android.text.format.DateFormat
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShortsComment : AppCompatActivity() {

    private lateinit var tvCount: TextView
    private lateinit var btnClose: ImageButton
    private lateinit var rv: RecyclerView
    private lateinit var et: EditText
    private lateinit var btnSend: Button

    private val adapter by lazy { ShortsCommentAdapter { pos -> onReportClicked(pos) } }
    private val items = mutableListOf<CommentUi>()

    private var shortsId: Long? = null // VideoPlayerFragment에서 putExtra로 전달 가능

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shorts_comment)

        shortsId = intent.getLongExtra("shortsId", -1).takeIf { it != -1L }

        tvCount = findViewById(R.id.tv_comment_count)
        btnClose = findViewById(R.id.btn_close)
        rv = findViewById(R.id.rv_comments)
        et = findViewById(R.id.et_comment)
        btnSend = findViewById(R.id.btn_send)
        btnClose = findViewById(R.id.btn_close)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // 더미 데이터 예시 (서버 연동 시 삭제)
        // items.add(CommentUi(...))

        adapter.submitList(items.toList())
        updateCount()

        btnClose.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            val text = et.text?.toString()?.trim().orEmpty()
            if (text.isEmpty()) {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val now = System.currentTimeMillis()
            val dateStr = DateFormat.format("yyyy.MM.dd HH:mm", now).toString()

            val newItem = CommentUi(
                profileUrl = null, // 서버에서 URL 내려주면 사용
                nickname = "나",
                content = text,
                dateText = dateStr
            )
            items.add(newItem)
            adapter.submitList(items.toList())
            rv.scrollToPosition(items.lastIndex)
            et.setText("")
            updateCount()

            // TODO: 서버 연동 시 여기서 POST 호출 후 성공 시 목록 갱신
        }

    }

    private fun onReportClicked(position: Int) {
        Toast.makeText(this@ShortsComment, "신고 완료", Toast.LENGTH_SHORT).show()
    }

    private fun updateCount() {
        tvCount.text = "댓글 ${items.size}"
    }
}

data class CommentUi(
    val profileUrl: String?,
    val nickname: String,
    val content: String,
    val dateText: String
)
