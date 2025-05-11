package com.example.test

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        all = findViewById(R.id.all)
        recipe = findViewById(R.id.recipe)
        fridege = findViewById(R.id.fridege)
        material = findViewById(R.id.material)
        community = findViewById(R.id.community)

        val allButtons = listOf(all, recipe, fridege, material, community)
        val iconSize = (24 * resources.displayMetrics.density).toInt()

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

        fun updateDelIcons() {
            val root = findViewById<View>(android.R.id.content)

            fun findRoundedFrames(view: View): List<ViewGroup> {
                val result = mutableListOf<ViewGroup>()
                if (view is ViewGroup && view.tag == "rounded") {
                    result.add(view)
                }
                if (view is ViewGroup) {
                    for (i in 0 until view.childCount) {
                        result.addAll(findRoundedFrames(view.getChildAt(i)))
                    }
                }
                return result
            }

            val roundedFrames = findRoundedFrames(root)

            for (frame in roundedFrames) {
                val icon = frame.findViewById<ImageView>(R.id.checkIcon)
                icon?.visibility = View.VISIBLE
            }
        }


        findViewById<LinearLayout>(R.id.del).setOnClickListener {
            updateDelIcons()
        }

        findViewById<ImageView>(R.id.noticeBack).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        all.setOnClickListener { updateSelection(all) }
        recipe.setOnClickListener { updateSelection(recipe) }
        fridege.setOnClickListener { updateSelection(fridege) }
        material.setOnClickListener { updateSelection(material) }
        community.setOnClickListener { updateSelection(community) }

        updateSelection(all)
    }
}
