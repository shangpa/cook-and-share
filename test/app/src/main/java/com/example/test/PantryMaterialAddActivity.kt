package com.example.test

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import androidx.core.content.FileProvider
import com.example.test.Utils.TabBarUtils

class PantryMaterialAddActivity : AppCompatActivity() {

    private lateinit var buttons: List<AppCompatButton>
    private lateinit var lists: List<View>
    private lateinit var cameraPhotoUri: Uri

    // 갤러리: 이미지 1장 고르기
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                goToMaterialAdd(uri, source = "gallery")
            }
        }

    // 카메라: 지정한 Uri로 촬영 저장
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                goToMaterialAdd(cameraPhotoUri, source = "camera")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantry_material_add)

        TabBarUtils.setupTabBar(this)

        // 뒤로가기
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }

        val materialSearch = findViewById<EditText>(R.id.materialSearch)
        val addBtn = findViewById<ImageButton>(R.id.add)
        val registerAdd = findViewById<TextView>(R.id.register)
        val registerLayout = findViewById<ConstraintLayout>(R.id.registerLayout)

        // 카테고리
        buttons = listOf(
            findViewById(R.id.total),
            findViewById(R.id.vegetable),
            findViewById(R.id.meat),
            findViewById(R.id.seafood),
            findViewById(R.id.grain),
            findViewById(R.id.fruits),
            findViewById(R.id.dairyProduct),
            findViewById(R.id.season),
            findViewById(R.id.processedFood),
            findViewById(R.id.noodle),
            findViewById(R.id.etc),
            findViewById(R.id.kimchi),
            findViewById(R.id.drink)
        )

        // 레이아웃
        lists = listOf(
            findViewById(R.id.totalList),
            findViewById(R.id.vegetableList),
            findViewById(R.id.meatList),
            findViewById(R.id.seafoodList),
            findViewById(R.id.grainList),
            findViewById(R.id.fruitsList),
            findViewById(R.id.dairyProductList),
            findViewById(R.id.spiceryList),
            findViewById(R.id.processedFoodList),
            findViewById(R.id.noodlesList),
            findViewById(R.id.etcList),
            findViewById(R.id.kimchiList),
            findViewById(R.id.drinkList)
        )

        // 버튼 클릭시 해당 레이아웃으로 이동
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener { showOnly(index) }
        }

        // 3) 기본 상태 (전체만 보이게)
        showOnly(0)

        val itemLayouts = listOf(
            findViewById<ConstraintLayout>(R.id.eggplantLayout),
            findViewById<ConstraintLayout>(R.id.potatoLayout),
            findViewById<ConstraintLayout>(R.id.sweetPotatoLayout),
            findViewById<ConstraintLayout>(R.id.carrotLayout),
            findViewById<ConstraintLayout>(R.id.garlicLayout),
            findViewById<ConstraintLayout>(R.id.daikonLayout),
            findViewById<ConstraintLayout>(R.id.napaCabbageLayout),
            findViewById<ConstraintLayout>(R.id.onionLayout),
            findViewById<ConstraintLayout>(R.id.tomatoLayout),
            findViewById<ConstraintLayout>(R.id.welshOnionLayout),

            findViewById<ConstraintLayout>(R.id.eggplantLayoutTwo),
            findViewById<ConstraintLayout>(R.id.potatoLayoutTwo),
            findViewById<ConstraintLayout>(R.id.sweetPotatoLayoutTwo),
            findViewById<ConstraintLayout>(R.id.carrotLayoutTwo),
            findViewById<ConstraintLayout>(R.id.garlicLayoutTwo),
            findViewById<ConstraintLayout>(R.id.daikonLayoutTwo),
            findViewById<ConstraintLayout>(R.id.napaCabbageLayoutTwo),
            findViewById<ConstraintLayout>(R.id.onionLayoutTwo),

            findViewById<ConstraintLayout>(R.id.beefLayout),
            findViewById<ConstraintLayout>(R.id.porkLayout),
            findViewById<ConstraintLayout>(R.id.chickenLayout),
            findViewById<ConstraintLayout>(R.id.muttonLayout),

            findViewById<ConstraintLayout>(R.id.mackerelLayout),
            findViewById<ConstraintLayout>(R.id.salmonLayout),
            findViewById<ConstraintLayout>(R.id.tunaLayout),
            findViewById<ConstraintLayout>(R.id.mackerelPikeLayout),
            findViewById<ConstraintLayout>(R.id.driedPollackLayout),
            findViewById<ConstraintLayout>(R.id.pollackLayout),
            findViewById<ConstraintLayout>(R.id.cutlassfishLayout),
            findViewById<ConstraintLayout>(R.id.shrimpLayout),

            findViewById<ConstraintLayout>(R.id.riceLayout),
            findViewById<ConstraintLayout>(R.id.brownRiceLayout),
            findViewById<ConstraintLayout>(R.id.barleyLayout),
            findViewById<ConstraintLayout>(R.id.oatsLayout),
            findViewById<ConstraintLayout>(R.id.glutinousRiceLayout),
            findViewById<ConstraintLayout>(R.id.kidneyBeanLayout),
            findViewById<ConstraintLayout>(R.id.blackBeanLayout),
            findViewById<ConstraintLayout>(R.id.peaLayout),

            findViewById<ConstraintLayout>(R.id.bananaLayout),
            findViewById<ConstraintLayout>(R.id.mangoLayout),
            findViewById<ConstraintLayout>(R.id.pineappleLayout),
            findViewById<ConstraintLayout>(R.id.appleLayout),
            findViewById<ConstraintLayout>(R.id.pearLayout),
            findViewById<ConstraintLayout>(R.id.peachLayout),
            findViewById<ConstraintLayout>(R.id.plumLayout),
            findViewById<ConstraintLayout>(R.id.persimmonLayout),

            findViewById<ConstraintLayout>(R.id.milkLayout),
            findViewById<ConstraintLayout>(R.id.cheeseLayout),
            findViewById<ConstraintLayout>(R.id.freshCreamLayout),
            findViewById<ConstraintLayout>(R.id.butterLayout),
            findViewById<ConstraintLayout>(R.id.yogurtLayout),
            findViewById<ConstraintLayout>(R.id.condensedMilkLayout),

            findViewById<ConstraintLayout>(R.id.saltLayout),
            findViewById<ConstraintLayout>(R.id.sugarLayout),
            findViewById<ConstraintLayout>(R.id.soySauceLayout),
            findViewById<ConstraintLayout>(R.id.vinegarLayout),
            findViewById<ConstraintLayout>(R.id.cornSyrupLayout),
            findViewById<ConstraintLayout>(R.id.sesameOilLayout),
            findViewById<ConstraintLayout>(R.id.cookingOilLayout),
            findViewById<ConstraintLayout>(R.id.chiliPepperLayout),

            findViewById<ConstraintLayout>(R.id.fishCakeLayout),
            findViewById<ConstraintLayout>(R.id.hamLayout),
            findViewById<ConstraintLayout>(R.id.sausageLayout),
            findViewById<ConstraintLayout>(R.id.baconLayout),
            findViewById<ConstraintLayout>(R.id.tofuLayout),
            findViewById<ConstraintLayout>(R.id.cheeseBallsLayout),
            findViewById<ConstraintLayout>(R.id.friedTofuLayout),
            findViewById<ConstraintLayout>(R.id.dumplingLayout),

            findViewById<ConstraintLayout>(R.id.noodlesLayout),
            findViewById<ConstraintLayout>(R.id.ramenLayout),
            findViewById<ConstraintLayout>(R.id.udonLayout),
            findViewById<ConstraintLayout>(R.id.jjolmyeonLayout),
            findViewById<ConstraintLayout>(R.id.cellophaneNoodlesLayout),
            findViewById<ConstraintLayout>(R.id.spaghettiNoodlesLayout),
            findViewById<ConstraintLayout>(R.id.naengmyeonLayout),

            findViewById<ConstraintLayout>(R.id.eggLayout),
            findViewById<ConstraintLayout>(R.id.quailEggLayout),
            findViewById<ConstraintLayout>(R.id.laverLayout),
            findViewById<ConstraintLayout>(R.id.seaMustardLayout),
            findViewById<ConstraintLayout>(R.id.kelpLayout),
            findViewById<ConstraintLayout>(R.id.kimchiLayout),
            findViewById<ConstraintLayout>(R.id.anchovyLayout),

            findViewById<ConstraintLayout>(R.id.napaCabbageKimchiLayout),
            findViewById<ConstraintLayout>(R.id.chonggakKimchiLayout),
            findViewById<ConstraintLayout>(R.id.greenOnionKimchiLayout),
            findViewById<ConstraintLayout>(R.id.freshKimchiLayout),
            findViewById<ConstraintLayout>(R.id.gatgimchiLayout),
            findViewById<ConstraintLayout>(R.id.whiteKimchiLayout),
            findViewById<ConstraintLayout>(R.id.youngSummerRadishKimchiLayout),
            findViewById<ConstraintLayout>(R.id.dicedRadishKimchiLayout),

            findViewById<ConstraintLayout>(R.id.waterLayout),
            findViewById<ConstraintLayout>(R.id.colaLayout),
            findViewById<ConstraintLayout>(R.id.zeroColaLayout),
            findViewById<ConstraintLayout>(R.id.ciderLayout),
            findViewById<ConstraintLayout>(R.id.zeroCiderLayout),
            findViewById<ConstraintLayout>(R.id.fantaLayout),
            findViewById<ConstraintLayout>(R.id.zeroFantaLayout),
            findViewById<ConstraintLayout>(R.id.sportsDrinkLayout),

        )

        // 2) 공통 클릭 이벤트 적용
        itemLayouts.forEach { layout ->
            layout.setOnClickListener {
                val intent = Intent(this, PantryMaterialAddDetailActivity::class.java)

                startActivity(intent)
            }
        }

        // add 버튼 눌렀을 때 팝업
        addBtn.setOnClickListener { anchor ->
            showRegisterMenu(anchor)
        }

        // 글자/영역 전체 눌렀을 때도 팝업
        registerAdd.setOnClickListener { anchor ->
            showRegisterMenu(addBtn) // anchor는 addBtn 기준으로
        }
    }

    // 버튼 클릭시 색 바뀜
    private fun showOnly(selectedIndex: Int) {
        lists.forEachIndexed { i, view ->
            view.visibility = if (i == selectedIndex) View.VISIBLE else View.GONE
        }

        buttons.forEachIndexed { i, button ->
            if (i == selectedIndex) {
                button.setBackgroundResource(R.drawable.btn_fridge_ct_ck)
                button.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                button.setBackgroundResource(R.drawable.btn_recipe_add)
                button.setTextColor(Color.parseColor("#8A8F9C"))
            }
        }
    }

    private fun showRegisterMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        listOf("영수증으로 등록", "사진으로 등록").forEach { popup.menu.add(it) }

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "영수증으로 등록" -> { // 카메라
                    // 임시 파일 만들고 FileProvider로 Uri 생성
                    val photoFile = File.createTempFile("receipt_", ".jpg", cacheDir)
                    cameraPhotoUri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.provider",
                        photoFile
                    )
                    takePictureLauncher.launch(cameraPhotoUri)
                    true
                }
                "사진으로 등록" -> { // 갤러리
                    galleryLauncher.launch("image/*")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun goToMaterialAdd(uri: Uri, source: String) {
        val intent = Intent(this, PantryMaterialAddDetailActivity::class.java).apply {
            putExtra("imageUri", uri.toString()) // 안전하게 String으로 전달
            putExtra("source", source)           // "camera" 또는 "gallery"
        }
        startActivity(intent)
        // 필요하면 현재 액티비티 닫기: finish()
    }
}
