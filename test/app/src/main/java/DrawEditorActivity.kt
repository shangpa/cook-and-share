//package com.example.test
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.widget.*
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import com.example.test.view.DrawView
//
//class DrawEditorActivity : AppCompatActivity() {
//
//    private lateinit var imageView: ImageView
//    private lateinit var drawView: DrawView
//    private var selectedColor = 0xFF000000.toInt() // 기본: 검정
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_draw_editor)
//
//        imageView = findViewById(R.id.imageView)
//        drawView = findViewById(R.id.drawView)
//
//        val uri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
//        imageView.setImageURI(uri)
//
//        // 버튼들 설정
//        findViewById<Button>(R.id.btnColorRed).setOnClickListener {
//            selectedColor = 0xFFFF0000.toInt()
//            drawView.setPaintColor(selectedColor)
//        }
//
//        findViewById<Button>(R.id.btnColorBlue).setOnClickListener {
//            selectedColor = 0xFF0000FF.toInt()
//            drawView.setPaintColor(selectedColor)
//        }
//
//        findViewById<Button>(R.id.btnColorBlack).setOnClickListener {
//            selectedColor = 0xFF000000.toInt()
//            drawView.setPaintColor(selectedColor)
//        }
//
//        findViewById<Button>(R.id.btnEraser).setOnClickListener {
//            drawView.enableEraser()
//        }
//
//        findViewById<Button>(R.id.btnUndo).setOnClickListener {
//            drawView.undo()
//        }
//
//        findViewById<Button>(R.id.btnText).setOnClickListener {
//            val input = EditText(this)
//            AlertDialog.Builder(this)
//                .setTitle("글씨 입력")
//                .setView(input)
//                .setPositiveButton("확인") { _, _ ->
//                    val text = input.text.toString()
//                    drawView.enableTextMode(text)
//                    Toast.makeText(this, "그림판 위 원하는 위치를 터치하세요", Toast.LENGTH_SHORT).show()
//                }
//                .setNegativeButton("취소", null)
//                .show()
//        }
//
//        findViewById<Button>(R.id.btnDone).setOnClickListener {
//            val bitmap = createFinalBitmap()
//            val uri = saveBitmapToGallery(bitmap)
//            val resultIntent = Intent().apply {
//                putExtra("editedImageUri", uri.toString())
//            }
//            setResult(RESULT_OK, resultIntent)
//            finish()
//        }
//    }
//
//    // 배경 이미지 + 그림을 합쳐 비트맵으로 만들기
//    private fun createFinalBitmap(): Bitmap {
//        val finalBitmap = Bitmap.createBitmap(drawView.width, drawView.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(finalBitmap)
//        imageView.draw(canvas)
//        drawView.draw(canvas)
//        return finalBitmap
//    }
//
//    // 비트맵을 MediaStore에 저장하고 URI 반환
//    private fun saveBitmapToGallery(bitmap: Bitmap): Uri {
//        val uri = MediaStore.Images.Media.insertImage(
//            contentResolver,
//            bitmap,
//            "edited_image_${System.currentTimeMillis()}",
//            "그림판에서 저장된 이미지"
//        )
//        return Uri.parse(uri)
//    }
//}
