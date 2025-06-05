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
import com.example.test.model.notification.NotificationResponseDTO
import com.example.test.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        all.setOnClickListener {
            updateSelection(all)
            filterNotificationsByCategory("all")
        }

        recipe.setOnClickListener {
            updateSelection(recipe)
            filterNotificationsByCategory("recipe")
        }

        fridege.setOnClickListener {
            updateSelection(fridege)
            filterNotificationsByCategory("fridge")
        }

        community.setOnClickListener {
            updateSelection(community)
            filterNotificationsByCategory("community")
        }

        material.setOnClickListener {
            updateSelection(material)
            filterNotificationsByCategory("material")
        }

        del.setOnClickListener {
            Log.d("checkIconTest", "\uD83D\uDC49 isDeleteMode 상태: $isDeleteMode")
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
        loadNotifications()

        findViewById<ImageView>(R.id.noticeBack).setOnClickListener {
            finish()
        }

    }

    private fun updateSelection(selected: LinearLayout) {
        val allButtons = listOf(all, recipe, fridege, material, community)
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

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode

        val delText = del.getChildAt(0) as? TextView
        delText?.text = if (isDeleteMode) "취소" else "삭제"

        deleteButtonsContainer.visibility = if (isDeleteMode) View.VISIBLE else View.GONE

        val root = findViewById<View>(R.id.scrollView)
        val allCheckIcons = findViewsWithTag(root, "checkIcon")
        Log.d("checkIconTest", "\uD83D\uDFE2 checkIcon 개수: ${allCheckIcons.size}")

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

    private fun findFirstImageView(view: View): ImageView? {
        if (view is ImageView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = findFirstImageView(view.getChildAt(i))
                if (child != null) return child
            }
        }
        return null
    }

    private fun filterNotificationsByCategory(category: String) {
        val root = findViewById<View>(R.id.scrollView)
        val allItems = findViewsWithTag(root, "rounded")

        for (item in allItems) {
            val imageView = findFirstImageView(item)
            val drawableId = imageView?.tag as? String
            item.visibility = when (category) {
                "all" -> View.VISIBLE
                "recipe" -> if (drawableId == "ic_book") View.VISIBLE else View.GONE
                "fridge" -> if (drawableId == "ic_refrigerator") View.VISIBLE else View.GONE
                "community" -> if (drawableId == "ic_chatt") View.VISIBLE else View.GONE
                "material" -> if (drawableId == "ic_bell") View.VISIBLE else View.GONE
                else -> View.VISIBLE
            }
        }
    }
    private fun loadNotifications() {
        val token = App.prefs.token ?: return
        val container = findViewById<LinearLayout>(R.id.roundedContainer)
        container.removeAllViews()

        RetrofitInstance.notificationApi.getNotifications("Bearer $token")
            .enqueue(object : Callback<List<NotificationResponseDTO>> {
                override fun onResponse(call: Call<List<NotificationResponseDTO>>, response: Response<List<NotificationResponseDTO>>) {
                    if (response.isSuccessful) {
                        val notifications = response.body() ?: return
                        Log.d("Notification", "불러온 알림 개수: ${notifications.size}")
                        for (notification in notifications) {
                            Log.d("Notification", "알림 내용: ${notification.category}, ${notification.content}")
                            val itemView = layoutInflater.inflate(R.layout.item_notification, container, false)
                            (itemView.parent as? ViewGroup)?.removeView(itemView)
                            val icon = itemView.findViewById<ImageView>(R.id.notificationIcon)
                            val content = itemView.findViewById<TextView>(R.id.notificationContent)
                            val time = itemView.findViewById<TextView>(R.id.notificationTime)

                            // 카테고리별 아이콘 지정
                            when (notification.category.uppercase()) {
                                "RECIPE" -> {
                                    icon.setImageResource(R.drawable.ic_book)
                                    icon.tag = "ic_book"
                                }
                                "FRIDGE" -> {
                                    icon.setImageResource(R.drawable.ic_refrigerator)
                                    icon.tag = "ic_refrigerator"
                                }
                                "MATERIAL" -> {
                                    icon.setImageResource(R.drawable.ic_bell)
                                    icon.tag = "ic_bell"
                                }
                                "COMMUNITY" -> {
                                    icon.setImageResource(R.drawable.ic_chatt)
                                    icon.tag = "ic_chatt"
                                }
                                else -> icon.setImageResource(R.drawable.ic_bell)
                            }

                            content.text = notification.content
                            time.text = notification.createdAt.substring(0, 16).replace("T", " ")

                            container.addView(itemView)
                            Log.d("Notification", "아이템 추가: ${notification.content}")

                        }
                        filterNotificationsByCategory("all")
                    }
                }

                override fun onFailure(call: Call<List<NotificationResponseDTO>>, t: Throwable) {
                    Log.e("Notification", "알림 불러오기 실패", t)
                }
            })
    }
}
