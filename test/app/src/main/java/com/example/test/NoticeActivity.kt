package com.example.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NoticeActivity : AppCompatActivity() {

    private lateinit var all: LinearLayout
    private lateinit var recipe: LinearLayout
    private lateinit var fridege: LinearLayout
    private lateinit var material: LinearLayout
    private lateinit var community: LinearLayout
    private lateinit var del: LinearLayout
    private lateinit var deleteButtonsContainer: LinearLayout
    private lateinit var deleteAllButton: TextView
    private lateinit var deleteSelectedButton: TextView
    private lateinit var noticeBack: ImageView

    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        // View 초기화
        all = findViewById(R.id.all)
        recipe = findViewById(R.id.recipe)
        fridege = findViewById(R.id.fridege)
        material = findViewById(R.id.material)
        community = findViewById(R.id.community)
        del = findViewById(R.id.del)
        deleteButtonsContainer = findViewById(R.id.deleteButtonsContainer)
        deleteAllButton = findViewById(R.id.deleteAllBtn)
        deleteSelectedButton = findViewById(R.id.deleteSelectedBtn)
        noticeBack = findViewById(R.id.noticeBack)

        val allButtons = listOf(all, recipe, fridege, material, community)

        // 카테고리 선택 UI 업데이트
        fun updateSelection(selected: LinearLayout) {
            for (button in allButtons) {
                val textView = button.getChildAt(0) as TextView
                if (button == selected) {
                    button.setBackgroundResource(R.drawable.rouned_ractangle_black)
                    textView.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    textView.setTextColor(resources.getColor(R.color.black, null))
                }
            }
        }

        del.setOnClickListener {
            Log.d("checkIconTest", "👉 isDeleteMode 상태: $isDeleteMode")
            toggleDeleteMode()
        }

        deleteAllButton.setOnClickListener {
            val roundedViews = findViewsWithTag(findViewById(R.id.scrollView), "rounded")
            for (view in roundedViews) {
                (view.parent as? ViewGroup)?.removeView(view)
            }
        }

        deleteSelectedButton.setOnClickListener {
            val roundedViews = findViewsWithTag(findViewById(R.id.scrollView), "rounded")
            for (view in roundedViews) {
                val icon = findViewsWithTag(view, "checkIcon").firstOrNull() as? ImageView
                val state = icon?.getTag(R.id.check_state_tag)
                if (state == "checked") {
                    (view.parent as? ViewGroup)?.removeView(view)
                }
            }
        }

        noticeBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        all.setOnClickListener { updateSelection(all) }
        recipe.setOnClickListener { updateSelection(recipe) }
        fridege.setOnClickListener { updateSelection(fridege) }
        material.setOnClickListener { updateSelection(material) }
        community.setOnClickListener { updateSelection(community) }

        updateSelection(all)
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode

        val delText = del.getChildAt(0) as? TextView
        delText?.text = if (isDeleteMode) "취소" else "삭제"

        deleteButtonsContainer.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

        val root = findViewById<View>(R.id.scrollView) // 스크롤뷰 기준으로 탐색
        val allCheckIcons = findViewsWithTag(root, "checkIcon")
        Log.d("checkIconTest", "🟢 checkIcon 개수: ${allCheckIcons.size}")

        for (icon in allCheckIcons) {
            if (icon is ImageView) {
                if (isDeleteMode) {
                    icon.visibility = View.VISIBLE
                    icon.setImageResource(R.drawable.ic_no_check)
                    icon.setTag(R.id.check_state_tag, "unchecked")
                    icon.setOnClickListener {
                        val isChecked = icon.getTag(R.id.check_state_tag) == "checked"
                        icon.setImageResource(if (isChecked) R.drawable.ic_no_check else R.drawable.ic_check)
                        icon.setTag(R.id.check_state_tag, if (isChecked) "unchecked" else "checked")
                    }
                } else {
                    icon.visibility = View.GONE
                    icon.setImageResource(R.drawable.ic_no_check)
                    icon.setTag(R.id.check_state_tag, "unchecked")
                    icon.setOnClickListener(null)
                }
            }
        }
    }

    // 태그로 뷰 찾기 (재귀 탐색)
    private fun findViewsWithTag(view: View, tag: String): List<View> {
        val result = mutableListOf<View>()
        if (tag == view.tag) result.add(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                result.addAll(findViewsWithTag(view.getChildAt(i), tag))
            }
        }
        return result
    }
}
