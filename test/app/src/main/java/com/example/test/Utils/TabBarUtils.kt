// 파일 위치: com.example.test.Utils.TabBarUtils.kt
package com.example.test.Utils

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.example.test.*

object TabBarUtils {

    fun setupTabBar(activity: Activity) {
        activity.findViewById<ImageView>(R.id.tapRecipeIcon)?.setOnClickListener {
            activity.startActivity(Intent(activity, RecipeActivity::class.java))
            activity.finish()
        }

        activity.findViewById<TextView>(R.id.tapRecipeText)?.setOnClickListener {
            activity.startActivity(Intent(activity, RecipeActivity::class.java))
            activity.finish()
        }

        activity.findViewById<ImageView>(R.id.tapVillageKitchenIcon)?.setOnClickListener {
            activity.startActivity(Intent(activity, MaterialActivity::class.java))
            activity.finish()
        }

        activity.findViewById<TextView>(R.id.tapVillageKitchenText)?.setOnClickListener {
            activity.startActivity(Intent(activity, MaterialActivity::class.java))
            activity.finish()
        }

        activity.findViewById<ImageView>(R.id.tapFridgeIcon)?.setOnClickListener {
            activity.startActivity(Intent(activity, FridgeActivity::class.java))
            activity.finish()
        }

        activity.findViewById<TextView>(R.id.tapFridgeText)?.setOnClickListener {
            activity.startActivity(Intent(activity, FridgeActivity::class.java))
            activity.finish()
        }

        activity.findViewById<ImageView>(R.id.tapCommunityIcon)?.setOnClickListener {
            activity.startActivity(Intent(activity, CommunityMainActivity::class.java))
            activity.finish()
        }

        activity.findViewById<TextView>(R.id.tapCommunityText)?.setOnClickListener {
            activity.startActivity(Intent(activity, CommunityMainActivity::class.java))
            activity.finish()
        }

        activity.findViewById<ImageView>(R.id.tapHomeIcon)?.setOnClickListener {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }
}
