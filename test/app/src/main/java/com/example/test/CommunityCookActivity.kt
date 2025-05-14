/*요리 게시판*/
package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CommunityCookActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_cook)
        // postWrite 클릭했을 때 WritePost 이동
        val postWrite: ImageView = findViewById(R.id.postWrite)
        postWrite.setOnClickListener {
            val intent = Intent(this, CommunityWritePostActivity::class.java)
            startActivity(intent)
        }

        //chat 아이콘 클릭시 CommunityDetailActivity로 넘어감
        val chatIds = listOf(
            R.id.chat, R.id.chatTwo, R.id.chatThree,
            R.id.chatFour, R.id.chatFive
        )

        chatIds.forEach { id ->
            findViewById<ImageView>(id).setOnClickListener {
                startActivity(Intent(this, CommunityDetailActivity::class.java))
            }
        }

        val dropDown = findViewById<ImageView>(R.id.dropDown)
        val dropDownTwo = findViewById<ImageView>(R.id.dropDownTwo)
        val cookPost = findViewById<TextView>(R.id.cookPost)
        val recommend = findViewById<TextView>(R.id.recommend)

        //요리 게시판 드롭다운 버튼 클릭
        dropDown.setOnClickListener {
            val popup = PopupMenu(this, dropDown)
            val items = listOf("인기 게시판", "요리 게시판", "자유 게시판")

            items.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.title) {
                    "인기 게시판" -> {
                        val intent = Intent(this, CommunityPopularActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티 종료 (선택사항)
                    }
                    "요리 게시판" -> {
                        // 현재 화면이 요리 게시판이면 아무것도 안 해도 됨
                        cookPost.text = item.title
                    }
                    "자유 게시판" -> {
                        val intent = Intent(this, CommunityFreeActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 액티비티 종료 (선택사항)
                    }
                }
                true
            }

            popup.show()
        }

        //최신순 드롭다운 버튼 클릭
        dropDownTwo.setOnClickListener {
            val popup = PopupMenu(this, dropDownTwo)
            val items = listOf("추천순", "댓글순", "최신순")

            items.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener { item: MenuItem ->
                recommend.text = item.title // 선택된 텍스트 적용!
                true
            }

            popup.show()
        }

        // 저장버튼 선언
        val saveButtons = listOf<ImageView>(
            findViewById(R.id.save),
            findViewById(R.id.saveTwo),
            findViewById(R.id.saveThree),
            findViewById(R.id.saveFour),
            findViewById(R.id.saveFive)
        )

        // 각 버튼에 대해 저장 기능 설정
        saveButtons.forEach { button ->
            button.setTag(R.id.save, false)

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.save) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_store) // 비어 있는 저장 아이콘
                } else {
                    button.setImageResource(R.drawable.ic_store_fill) // 채워진 저장 아이콘
                    Toast.makeText(this, "레시피를 저장하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반대로 바꿔줌 (★ 이게 꼭 필요해)
                it.setTag(R.id.save, !isLiked)
            }
        }

        // 좋아요 버튼 선언
        val goodButtons = listOf<ImageView>(
            findViewById(R.id.good),
            findViewById(R.id.goodTwo),
            findViewById(R.id.goodThree),
            findViewById(R.id.goodFour),
            findViewById(R.id.goodFive)
        )

        // 각 버튼에 대해 좋아요 기능 설정
        goodButtons.forEach { button ->
            button.setTag(R.id.good, false)

            button.setOnClickListener {
                val isLiked = it.getTag(R.id.good) as Boolean

                if (isLiked) {
                    button.setImageResource(R.drawable.ic_good) // 비어 있는 좋아요 아이콘
                } else {
                    button.setImageResource(R.drawable.ic_good_fill) // 채워진 좋아요 아이콘
                    Toast.makeText(this, "해당 레시피를 추천하였습니다.", Toast.LENGTH_SHORT).show()
                }

                // 상태 반대로 바꿔줌 (★ 이게 꼭 필요해)
                it.setTag(R.id.good, !isLiked)
            }
        }
    }
}