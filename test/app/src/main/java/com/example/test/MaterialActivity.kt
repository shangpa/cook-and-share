package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.children

class MaterialActivity : AppCompatActivity() {

    private var isMaterialVisible = false
    private var isdistanceVisible = false
    private var isPlusMenuVisible = false

    private lateinit var buttons: List<Button>
    private lateinit var selectedFilterLayout: LinearLayout

    private var selectedMaterial: Button? = null
    private var selectedDistance: Button? = null

    private lateinit var sortText: TextView
    private lateinit var sortArrow: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)

        selectedFilterLayout = findViewById(R.id.selectedFilterLayout)

        //테스트 용 하드코딩
        val testPost = findViewById<LinearLayout>(R.id.testPost)
        testPost.setOnClickListener{ startActivity(Intent(this, MaterialDetailActivity::class.java)) }

        val item1: LinearLayout = findViewById(R.id.item1)
        val searchIcon: ImageView = findViewById(R.id.searchIcon)
        val myLocation: LinearLayout = findViewById(R.id.myLocation)
        val profileIcon: ImageView = findViewById(R.id.profileIcon)
        val aa: ImageView = findViewById(R.id.aa)
        val bb: ImageView = findViewById(R.id.bb)
        val plusIcon3: ImageView = findViewById(R.id.plusIcon3)

        val materialFilter = findViewById<LinearLayout>(R.id.materialFilter)
        val materialText = findViewById<TextView>(R.id.materialText)
        val materialIcon = findViewById<ImageView>(R.id.materialIcon)
        val material = findViewById<LinearLayout>(R.id.material)

        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceIcon = findViewById<ImageView>(R.id.distanceIcon)
        val distance = findViewById<LinearLayout>(R.id.distance)

        sortText = findViewById(R.id.w)
        sortArrow = findViewById(R.id.sortArrow)

        sortArrow.setOnClickListener {
            val popupMenu = PopupMenu(this, sortArrow)
            popupMenu.menu.add("거리순")
            popupMenu.menu.add("최신순")
            popupMenu.menu.add("가격순")
            popupMenu.menu.add("구입 날짜순")

            popupMenu.setOnMenuItemClickListener { item ->
                sortText.text = item.title
                Toast.makeText(this, "${item.title} 정렬 적용됨", Toast.LENGTH_SHORT).show()
                true
            }

            popupMenu.show()
        }

        val materialButtons = listOf(
            findViewById<Button>(R.id.all),
            findViewById(R.id.cookware),
            findViewById(R.id.fans_pots),
            findViewById(R.id.containers),
            findViewById(R.id.tableware),
            findViewById(R.id.storageSupplies),
            findViewById(R.id.sanitaryProducts),
            findViewById(R.id.smallAppliances),
            findViewById(R.id.disposableProducts),
            findViewById(R.id.etc)
        )

        materialButtons.forEach { button ->
            button.setOnClickListener {
                setSelectedMaterialButton(button, materialFilter, materialText)
                showSelectedFilterBadge(button.text.toString(), materialFilter, materialText)
                material.visibility = View.GONE
                isMaterialVisible = false
            }
        }

        item1.setOnClickListener { startActivity(Intent(this, MaterialDetailActivity::class.java)) }
        searchIcon.setOnClickListener { startActivity(Intent(this, MaterialSearchActivity::class.java)) }
        myLocation.setOnClickListener { startActivity(Intent(this, MaterialMyLocationActivity::class.java)) }
        profileIcon.setOnClickListener { startActivity(Intent(this, MaterialMyProfileActivity::class.java)) }
        aa.setOnClickListener { startActivity(Intent(this, MaterialWritingActivity::class.java)) }
        bb.setOnClickListener { startActivity(Intent(this, MaterialChatActivity::class.java)) }

        plusIcon3.setOnClickListener {
            isPlusMenuVisible = !isPlusMenuVisible
            aa.visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
            bb.visibility = if (isPlusMenuVisible) View.VISIBLE else View.GONE
        }

        buttons = listOf(
            findViewById(R.id.alll),
            findViewById(R.id.threeHundred),
            findViewById(R.id.fiveHundred),
            findViewById(R.id.oneThousand),
            findViewById(R.id.onefiveThousand),
            findViewById(R.id.twoThousand)
        )

        buttons.forEach {
            it.setOnClickListener { button -> setSelectedButton(button as Button) }
        }

        materialFilter.setOnClickListener {
            isMaterialVisible = !isMaterialVisible
            material.visibility = if (isMaterialVisible) View.VISIBLE else View.GONE
            if (isMaterialVisible) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                materialText.setTextColor(Color.WHITE)
                materialIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                if (selectedMaterial != null) {
                    materialFilter.setBackgroundColor(Color.BLACK)
                    materialText.setTextColor(Color.WHITE)
                } else {
                    materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                    materialText.setTextColor(Color.parseColor("#8A8F9C"))
                }
                materialIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }

        distanceFilter.setOnClickListener {
            isdistanceVisible = !isdistanceVisible
            distance.visibility = if (isdistanceVisible) View.VISIBLE else View.GONE
            if (isdistanceVisible) {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                distanceText.setTextColor(Color.WHITE)
                distanceIcon.setImageResource(R.drawable.ic_arrow_up)
            } else {
                distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                distanceText.setTextColor(Color.parseColor("#8A8F9C"))
                distanceIcon.setImageResource(R.drawable.ic_arrow_down)
            }
        }
    }

    private fun setSelectedButton(selectedButton: Button) {
        val distanceText = findViewById<TextView>(R.id.distanceText)
        val distanceFilter = findViewById<LinearLayout>(R.id.distanceFilter)
        val distanceLayout = findViewById<LinearLayout>(R.id.distance)

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
                button.setTextColor(Color.WHITE)
                selectedDistance = button
            } else {
                button.setBackgroundResource(R.drawable.rounded_rectangle_background)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }

        for (i in selectedFilterLayout.childCount - 1 downTo 0) {
            val badge = selectedFilterLayout.getChildAt(i)
            if (badge.tag == "distance") selectedFilterLayout.removeView(badge)
        }

        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        badge.tag = "distance"
        badge.findViewById<TextView>(R.id.filterText).text = selectedButton.text.toString()
        badge.findViewById<ImageView>(R.id.filterClose).setOnClickListener {
            selectedFilterLayout.removeView(badge)
            selectedDistance = null
            buttons.forEach {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                it.setTextColor(Color.parseColor("#8A8F9C"))
            }
            distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
            distanceText.setTextColor(Color.parseColor("#8A8F9C"))
        }

        selectedFilterLayout.addView(badge)
        distanceLayout.visibility = View.GONE
        isdistanceVisible = false
        distanceFilter.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
        distanceText.setTextColor(Color.WHITE)
    }

    private fun setSelectedMaterialButton(button: Button, filterLayout: LinearLayout, textView: TextView) {
        selectedMaterial = if (selectedMaterial == button) null else button

        val materialButtons = listOf(
            findViewById<Button>(R.id.all),
            findViewById(R.id.cookware),
            findViewById(R.id.fans_pots),
            findViewById(R.id.containers),
            findViewById(R.id.tableware),
            findViewById(R.id.storageSupplies),
            findViewById(R.id.sanitaryProducts),
            findViewById(R.id.smallAppliances),
            findViewById(R.id.disposableProducts),
            findViewById(R.id.etc)
        )

        materialButtons.forEach {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background)
            it.setTextColor(Color.parseColor("#8A8F9C"))
        }

        selectedMaterial?.let {
            it.setBackgroundResource(R.drawable.rounded_rectangle_background_selected)
            it.setTextColor(Color.WHITE)
        }
    }

    private fun showSelectedFilterBadge(text: String, materialFilter: LinearLayout, materialText: TextView) {
        selectedFilterLayout.visibility = View.VISIBLE

        val badge = layoutInflater.inflate(R.layout.filter_badge, null)
        val badgeText = badge.findViewById<TextView>(R.id.filterText)
        val badgeClose = badge.findViewById<ImageView>(R.id.filterClose)

        badgeText.text = text
        badge.tag = "material-$text"

        badgeClose.setOnClickListener {
            selectedFilterLayout.removeView(badge)

            val materialButtons = listOf(
                findViewById<Button>(R.id.all),
                findViewById(R.id.cookware),
                findViewById(R.id.fans_pots),
                findViewById(R.id.containers),
                findViewById(R.id.tableware),
                findViewById(R.id.storageSupplies),
                findViewById(R.id.sanitaryProducts),
                findViewById(R.id.smallAppliances),
                findViewById(R.id.disposableProducts),
                findViewById(R.id.etc)
            )

            materialButtons.find { it.text == text }?.let {
                it.setBackgroundResource(R.drawable.rounded_rectangle_background)
                it.setTextColor(Color.parseColor("#8A8F9C"))
            }

            if (selectedFilterLayout.children.none { it.tag.toString().startsWith("material-") }) {
                materialFilter.setBackgroundResource(R.drawable.rounded_rectangle_background)
                materialText.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }

        selectedFilterLayout.addView(badge)
    }
}
