/*커뮤니티 상세*/
package com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class CommunityDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_detail)

        val inflater = LayoutInflater.from(this)
        val add = findViewById<ImageButton>(R.id.add)
        val writeReview = findViewById<EditText>(R.id.writeReview)
        val postBtn = findViewById<TextView>(R.id.post)
        val commentContainer = findViewById<LinearLayout>(R.id.commentContainer)
        val commentScroll = findViewById<NestedScrollView>(R.id.commentScroll)

        // 상단 메뉴(신고)
        add.setOnClickListener {
            val popup = PopupMenu(this, add)
            popup.menu.add("신고하기")
            popup.setOnMenuItemClickListener {
                Toast.makeText(this, "신고되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
                true
            }
            popup.show()
        }

        // 기존 댓글(고정된 댓글)의 add 버튼 처리
        val commentBlocks = mapOf(
            R.id.addTwo to R.id.commentBlock,
            R.id.addThree to R.id.commentBlockTwo,
            R.id.addFour to R.id.commentBlockThree
        )

        commentBlocks.forEach { (buttonId, blockId) ->
            val addButton = findViewById<ImageButton>(buttonId)
            val commentBlock = findViewById<LinearLayout>(blockId)
            addButton.setOnClickListener {
                val popup = PopupMenu(this, addButton)
                popup.menu.add("신고하기")
                popup.setOnMenuItemClickListener { item ->
                    if (item.title == "신고하기") {
                        Toast.makeText(this, "신고되었습니다.", Toast.LENGTH_SHORT).show()
                        commentBlock.visibility = View.GONE
                    }
                    true
                }
                popup.show()
            }
        }

        // ❤️ 좋아요 버튼 처리
        val goodButtons = listOf<ImageView>(findViewById(R.id.good))
        goodButtons.forEach { button ->
            button.setTag(R.id.good, false)
            button.setOnClickListener {
                val isLiked = it.getTag(R.id.good) as Boolean
                button.setImageResource(if (isLiked) R.drawable.ic_good else R.drawable.ic_good_fill)
                if (!isLiked) Toast.makeText(this, "해당 레시피를 추천하였습니다.", Toast.LENGTH_SHORT).show()
                it.setTag(R.id.good, !isLiked)
            }
        }

        // 📝 댓글 작성 후 추가
        postBtn.setOnClickListener {
            val inputText = writeReview.text.toString().trim()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newComment = inflater.inflate(R.layout.item_comment, null)

            val nameText = newComment.findViewById<TextView>(R.id.nameFour)
            val timeText = newComment.findViewById<TextView>(R.id.timeFour)
            val contentText = newComment.findViewById<TextView>(R.id.contentFour)
            val addButton = newComment.findViewById<ImageButton>(R.id.addFour)

            nameText.text = "나"
            timeText.text = "방금 전"
            contentText.text = inputText

            // 드롭다운 수정/삭제 기능
            addButton.setOnClickListener {
                val popup = PopupMenu(this, addButton)
                popup.menu.add(0, 0, 0, "수정")
                popup.menu.add(0, 1, 1, "삭제")
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        0 -> {
                            writeReview.setText(contentText.text.toString())
                            commentContainer.removeView(newComment)
                            true
                        }
                        1 -> {
                            commentContainer.removeView(newComment)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            // 댓글 추가 + 스크롤 + 초기화
            commentContainer.addView(newComment)
            writeReview.setText("")
            commentScroll.post {
                commentScroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }
}
