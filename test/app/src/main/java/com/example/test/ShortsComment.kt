package com.example.test

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.model.shorts.CommentRequestDTO
import com.example.test.model.shorts.ShortCommentResponse
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            Log.d("ShortsComment", "▶ 버튼 클릭됨, 입력값: '$text'")   // 버튼 눌림 로그

            if (text.isEmpty()) {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show()
                Log.d("ShortsComment", "⚠ 입력값이 비어 있음")
                return@setOnClickListener
            }

            val id = shortsId
            Log.d("ShortsComment", "▶ shortsId: $id")   // ID 확인

            if (id == null) {
                Log.e("ShortsComment", "❌ shortsId가 null")
                return@setOnClickListener
            }

            val token = App.prefs.token
            Log.d("ShortsComment", "▶ token: $token")   // 토큰 확인

            if (token == null) {
                Log.e("ShortsComment", "❌ 토큰이 null")
                return@setOnClickListener
            }

            val dto = CommentRequestDTO(text)
            Log.d("ShortsComment", "▶ 서버 호출 준비: dto=$dto")

            RetrofitInstance.apiService.addShortsComment(
                id, dto, "Bearer $token"
            ).enqueue(object : Callback<Void> {   // 👈 여기 Void로 수정
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Log.d("ShortsComment", "✅ 서버 응답: code=${response.code()}")
                    if (response.isSuccessful) {
                        et.setText("")
                        loadComments() // 성공 시 댓글 목록 다시 불러오기
                    } else {
                        Toast.makeText(this@ShortsComment, "등록 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("ShortsComment", "⛔ 서버 호출 실패: ${t.message}", t)
                    Toast.makeText(this@ShortsComment, "에러: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        loadComments()
    }
    private fun loadComments() {
        val id = shortsId ?: return
        val token = App.prefs.token
        Log.d("ShortsComment", "📥 댓글 목록 불러오기 요청 id=$id")

        RetrofitInstance.apiService.getShortsComments(id, "Bearer $token")
            .enqueue(object : Callback<List<ShortCommentResponse>> {
                override fun onResponse(
                    call: Call<List<ShortCommentResponse>>,
                    response: Response<List<ShortCommentResponse>>
                ) {
                    Log.d("ShortsComment", "📥 댓글 목록 응답: code=${response.code()}, body=${response.body()}")

                    if (response.isSuccessful) {
                        val data = response.body().orEmpty()
                        Log.d("ShortsComment", "📥 서버에서 받은 댓글 개수=${data.size}")

                        items.clear()
                        items.addAll(data.map {
                            CommentUi(
                                nickname = it.username, // 👈 바로 username 사용
                                content = it.content,
                                dateText = it.createdAt
                            )
                        })
                        adapter.submitList(items.toList())
                        updateCount()
                    } else {
                        Log.e("ShortsComment", "❌ 댓글 불러오기 실패: code=${response.code()}, errorBody=${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<ShortCommentResponse>>, t: Throwable) {
                    Log.e("ShortsComment", "⛔ 댓글 불러오기 네트워크 실패: ${t.message}", t)
                    Toast.makeText(this@ShortsComment, "댓글 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun onReportClicked(position: Int) {
        Toast.makeText(this@ShortsComment, "신고 완료", Toast.LENGTH_SHORT).show()
    }

    private fun updateCount() {
        tvCount.text = "댓글 ${items.size}"
    }
}

data class CommentUi(
    val nickname: String,
    val content: String,
    val dateText: String
)
