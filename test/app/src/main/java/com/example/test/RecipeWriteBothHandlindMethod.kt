/*레시피 작성 STEP4*/
package com.example.test

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

private lateinit var materialContainer: LinearLayout
private lateinit var addFixButton: Button
private var itemCount = 0 // 추가된 개수 추적
private val maxItems = 10 // 최대 10개 제한
private val buttonMarginIncrease = 130 // 버튼을 아래로 내릴 거리 (px)


class RecipeWriteBothHandlindMethod : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_write_both_handling_method)

        materialContainer = findViewById(R.id.materialContainer)
        addFixButton = findViewById(R.id.addFixButton)

        addFixButton.setOnClickListener {
            if (itemCount < maxItems) {
                addNewItem()
                moveButtonDown() // 버튼 아래로 이동
            }
        }

            // nextFixButton 클릭했을 때 RecipeWriteBothCookOrder 이동
            val nextFixButton: Button = findViewById(R.id.nextFixButton)
            nextFixButton.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothCookOrder::class.java)
                startActivity(intent)
            }

            // skipFixButton 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
            val skipFixButton: Button = findViewById(R.id.skipFixButton)
            skipFixButton.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
                startActivity(intent)
            }

            // one 클릭했을 때 RecipeWriteBothTitle 이동
            val one: TextView = findViewById(R.id.one)
            one.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothTitle::class.java)
                startActivity(intent)
            }

            // two 클릭했을 때 RecipeWriteBothMaterial 이동
            val two: TextView = findViewById(R.id.two)
            two.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothMaterial::class.java)
                startActivity(intent)
            }

            // three 클릭했을 때 RecipeWriteBothReplaceMaterial 이동
            val three: TextView = findViewById(R.id.three)
            three.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothReplaceMaterial::class.java)
                startActivity(intent)
            }

            // five 클릭했을 때 RecipeWriteBothCookOrder 이동
            val five: TextView = findViewById(R.id.five)
            five.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothCookOrder::class.java)
                startActivity(intent)
            }

            // six 클릭했을 때 RecipeWriteBothCookVideo 이동
            val six: TextView = findViewById(R.id.six)
            six.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothCookVideo::class.java)
                startActivity(intent)
            }

            // seven 클릭했을 때 RecipeWriteBothDetailSettle 이동
            val seven: TextView = findViewById(R.id.seven)
            seven.setOnClickListener {
                val intent = Intent(this, RecipeWriteBothDetailSettle::class.java)
                startActivity(intent)
            }

            // 재료명1 입력된 텍스트 가져오기
            val material = findViewById<EditText>(R.id.material)

            // 처리방법1 입력된 텍스트 가져오기
            val handlingMethod = findViewById<EditText>(R.id.handlingMethod)

            val deleteTwo = findViewById<ImageButton>(R.id.deleteTwo)
            val materialTwo = findViewById<EditText>(R.id.materialTwo)
            val handlingMethodTwo = findViewById<EditText>(R.id.handlingMethodTwo)
            val divideRectangleBarFour = findViewById<View>(R.id.divideRectangleBarFour)

            // 삭제하기 눌렀을때 재료명, 계량, 바, 삭제 버튼 삭제
            deleteTwo.setOnClickListener {
                materialTwo.visibility = View.GONE
                handlingMethodTwo.visibility = View.GONE
                deleteTwo.visibility = ImageButton.GONE
                divideRectangleBarFour.visibility = View.GONE
            }

            // 이전 화면으로 이동
            val backArrow: ImageButton = findViewById(R.id.backArrow)
            backArrow.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        }

        private fun addNewItem() {
            // 새로운 ConstraintLayout 생성
            val newItemLayout = ConstraintLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // 재료명 EditText 생성
            val materialEditText = EditText(this).apply {
                id = View.generateViewId()  // ID 생성하여 ConstraintLayout에서 사용할 수 있도록 함
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(47, 10, 0, 0) // 위쪽 여백 설정
                }
                hint = "재료명"
                textSize = 13f
                setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
            }

            // 처리 방법 EditText 생성
            val handlingEditText = EditText(this).apply {
                id = View.generateViewId()  // ID 생성
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(532, 10, 0, 0) // 위쪽 여백 설정
                }
                hint = "처리 방법"
                textSize = 13f
                setBackgroundResource(android.R.color.transparent)  // 배경을 투명으로 설정
            }

            // 삭제 버튼 생성
            val deleteButton = ImageButton(this).apply {
                id = View.generateViewId()  // ID 생성
                layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    // 오른쪽 끝에 위치하도록 설정
                    endToEnd = materialEditText.id  // materialEditText의 오른쪽 끝에 맞추기
                    topToTop = materialEditText.id  // materialEditText의 위쪽 끝에 맞추기

                    // 오른쪽 마진을 5dp로 설정하여 왼쪽으로 이동
                    setMargins(0, 0, dpToPx(13), 0) // dpToPx를 사용하여 픽셀로 변환한 후 오른쪽 마진 설정
                }
                setImageResource(R.drawable.ic_delete) // 삭제 아이콘 설정
                setBackgroundResource(android.R.color.transparent) // 배경 투명
            }

            // 삭제 버튼 클릭 시 해당 레이아웃 삭제 & 버튼 위치 조정
            deleteButton.setOnClickListener {
                materialContainer.removeView(newItemLayout)
                itemCount--
                moveButtonUp() // 아이템 삭제 시 버튼을 위로 이동
            }

            // 새로운 바 생성
            val divideRectangleBarFour = View(this).apply {
                id = View.generateViewId()  // ID 생성

                // LayoutParams 설정
                val layoutParams = ConstraintLayout.LayoutParams(
                    0,  // width를 0으로 설정하여 부모의 width를 채우도록 함
                    dpToPx(2)  // 2dp 높이를 px로 변환하여 설정
                ).apply {
                    // 바를 materialEditText 아래로 배치
                    topToBottom = materialEditText.id  // materialEditText 아래에 위치
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID  // 왼쪽 끝에 붙임
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID  // 오른쪽 끝에 붙임

                    // 좌우 마진 5dp 설정
                    setMargins(dpToPx(3), 0, dpToPx(3), 0)
                }

                this.layoutParams = layoutParams
                setBackgroundResource(R.drawable.bar_recipe_see_material)  // 배경 설정
            }


            // LinearLayout에 요소 추가
            newItemLayout.apply {
                addView(materialEditText)
                addView(handlingEditText)
                addView(deleteButton)
                addView(divideRectangleBarFour)
            }

            // 부모 레이아웃에 추가
            materialContainer.addView(newItemLayout)
            itemCount++
        }

        // dpToPx 함수를 사용하여 dp 단위를 px로 변환
        private fun dpToPx(dp: Int): Int {
            return (dp * resources.displayMetrics.density).toInt()
        }

        private fun moveButtonDown() {
            val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams
            params.topMargin += buttonMarginIncrease // 버튼을 50px 아래로 이동
            addFixButton.layoutParams = params
        }

        private fun moveButtonUp() {
            val params = addFixButton.layoutParams as ConstraintLayout.LayoutParams
            if (params.topMargin > 0) {
                params.topMargin -= buttonMarginIncrease // 버튼을 50px 위로 이동
                addFixButton.layoutParams = params
            }
        }
    }
