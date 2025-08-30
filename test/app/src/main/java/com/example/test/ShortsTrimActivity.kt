package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.ui.PlayerView
import com.google.android.material.slider.RangeSlider
import java.io.File
import androidx.media3.common.Effect
import androidx.media3.effect.Crop
import androidx.media3.transformer.ExportException
@androidx.media3.common.util.UnstableApi
class ShortsTrimActivity : AppCompatActivity() {
    private var sliderInitialized = false
    private lateinit var player: ExoPlayer
    private lateinit var rangeSlider: RangeSlider
    private lateinit var btnTrim: Button
    private var videoDurationMs: Long = 0L
    private var inputUri: Uri? = null
    private var cropOffsetPercent: Float = 50f // 0~100 (세로 위치)
    private var cropZoomPercent: Float = 100f  // 100~300 (%)
    private lateinit var cropGuide: com.example.test.ui.CropGuideView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shorts_trim) // ⚠️ 새로운 레이아웃 파일 사용

        cropGuide = findViewById(R.id.cropGuide)

        val offsetSlider = findViewById<com.google.android.material.slider.Slider>(R.id.cropOffsetSlider)
        val zoomSlider   = findViewById<com.google.android.material.slider.Slider>(R.id.cropZoomSlider)

        offsetSlider.addOnChangeListener { _, v, _ ->
            cropOffsetPercent = v // 0~100
            updateGuideBox()
        }
        zoomSlider.addOnChangeListener { _, v, _ ->
            cropZoomPercent = v   // 100~300
            updateGuideBox()
        }

        inputUri = intent.getParcelableExtra("videoUri")
        if (inputUri == null) {
            Toast.makeText(this, "영상 없음", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPlayer(inputUri!!)
        rangeSlider = findViewById(R.id.rangeSlider)
        btnTrim = findViewById(R.id.btnTrim)

        btnTrim.setOnClickListener {
            val values = rangeSlider.values
            if (values.size < 2) {
                Toast.makeText(this, "트리밍 구간을 지정하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val startMs = (values[0] * 1000).toLong()
            val endMs = (values[1] * 1000).toLong()
            startTrimAndCrop(startMs, endMs)
        }
        updateGuideBox()
    }
    private fun updateGuideBox() {
        if (inputUri == null) return
        val (w, h) = getVideoSize(inputUri!!)
        val rect = computeNormalizedCropRect(w, h, cropOffsetPercent, cropZoomPercent)
        cropGuide.setCropNormalized(rect.left, rect.top, rect.right, rect.bottom)
    }
    private fun computeNormalizedCropRect(
        inputWidth: Int,
        inputHeight: Int,
        offsetPercent: Float, // 0~100
        zoomPercent: Float    // 100~300
    ): android.graphics.RectF {
        val inputRatio = inputWidth.toFloat() / inputHeight
        val targetRatio = 16f / 9f

        var cropW: Float
        var cropH: Float
        if (inputRatio > targetRatio) {
            cropH = 1f
            cropW = targetRatio / inputRatio
        } else {
            cropW = 1f
            cropH = inputRatio / targetRatio
        }

        val zoom = (zoomPercent / 100f).coerceAtLeast(1f)
        cropW /= zoom
        cropH /= zoom

        val left = (1f - cropW) / 2f
        val maxTop = 1f - cropH
        val top = (offsetPercent.coerceIn(0f, 100f) / 100f) * maxTop

        return android.graphics.RectF(left, top, left + cropW, top + cropH)
    }
    private fun setupPlayer(uri: Uri) {
        player = ExoPlayer.Builder(this).build()
        val playerView = findViewById<PlayerView>(R.id.playerView)
        playerView.player = player

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val duration = player.duration
                if (duration > 0 && !sliderInitialized) {
                    sliderInitialized = true
                    videoDurationMs = duration

                    // ⚠️ Shorts 전용: 60초 시간 제한 적용
                    val maxDurationMs = 60000L
                    val clippedDurationMs = minOf(videoDurationMs, maxDurationMs)

                    val seconds = (clippedDurationMs / 1000).toFloat()
                    rangeSlider.valueFrom = 0f
                    rangeSlider.valueTo = seconds
                    rangeSlider.setValues(0f, seconds)
                    rangeSlider.stepSize = 1f
                    updateGuideBox()
                    if (videoDurationMs > maxDurationMs) {
                        Toast.makeText(this@ShortsTrimActivity, "Shorts는 최대 60초까지만 편집할 수 있습니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    // ⚠️ 16:9 비율 크롭 기능 추가
    @androidx.media3.common.util.UnstableApi
    private fun startTrimAndCrop(startMs: Long, endMs: Long) {
        val outputFile = File(cacheDir, "shorts_output.mp4")
        showProgressBar()

        val (w, h) = getVideoSize(inputUri!!)
        // ✅ 여기만 교체!
        val cropEffect = buildAdjustableCrop16by9Effect(w, h, cropOffsetPercent, cropZoomPercent)

        // 1) MediaItem에 트리밍 설정 (1.3.1 방식)
        val clipping = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(startMs)
            .setEndPositionMs(endMs)
            .build()

        val mediaItemForEdit = MediaItem.Builder()
            .setUri(inputUri!!)
            .setClippingConfiguration(clipping)   // ✅ 트리밍은 여기!
            .build()

        // 2) 이펙트는 EditedMediaItem 쪽에
        val editedMediaItem = EditedMediaItem.Builder(mediaItemForEdit)
            .setEffects(
                androidx.media3.transformer.Effects( // 충돌 방지용 완전수식명
                    emptyList(),
                    listOf(cropEffect)
                )
            )
            .build()

        // 3) Composition 경유해서 start 호출 (모호성 제거)
        val sequence = EditedMediaItemSequence(editedMediaItem)
        val composition = Composition.Builder(listOf(sequence)).build() // 리스트 형태 권장 :contentReference[oaicite:2]{index=2}

        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .build()

        transformer.addListener(object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                hideProgressBar()
                val resultIntent = Intent().apply {
                    putExtra("trimmedUri", Uri.fromFile(outputFile))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            override fun onError( // ✅ 1.3.1 시그니처
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException
            ) {
                hideProgressBar()
                Toast.makeText(this@ShortsTrimActivity, "편집 실패: ${exportException.message}", Toast.LENGTH_SHORT).show()
            }
        })

        transformer.start(composition, outputFile.absolutePath)
    }
    private fun showProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }
    private fun getVideoSize(uri: Uri): Pair<Int, Int> {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(this, uri)
        val w = retriever.extractMetadata(
            android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
        )?.toInt() ?: 0
        val h = retriever.extractMetadata(
            android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
        )?.toInt() ?: 0
        retriever.release()
        return w to h
    }

    private fun buildAdjustableCrop16by9Effect(
        inputWidth: Int,
        inputHeight: Int,
        offsetPercent: Float, // 0~100 (세로 위치)
        zoomPercent: Float    // 100~300 (%)
    ): Effect {
        val inputRatio = inputWidth.toFloat() / inputHeight
        val targetRatio = 16f / 9f

        var cropWidth: Float
        var cropHeight: Float

        if (inputRatio > targetRatio) {
            // 가로가 더 넓음 → 좌우 크롭 (세로는 꽉 채움)
            cropHeight = 1f
            cropWidth = targetRatio / inputRatio
        } else {
            // 세로가 더 큼/같음 → 상하 크롭 (가로는 꽉 채움)
            cropWidth = 1f
            cropHeight = inputRatio / targetRatio
        }

        // 줌 인(값이 클수록 더 확대 → 크롭 박스 더 작게)
        val zoom = (zoomPercent / 100f).coerceAtLeast(1f)
        cropWidth /= zoom
        cropHeight /= zoom

        // 중앙 기준 기본 위치
        var left = (1f - cropWidth) / 2f
        var top  = (1f - cropHeight) / 2f

        // 세로 오프셋 적용 (0~100 → 0~maxTop)
        val maxTop = 1f - cropHeight
        top = (offsetPercent.coerceIn(0f, 100f) / 100f) * maxTop

        val right = left + cropWidth
        val bottom = top + cropHeight

        // ✅ 보정: 항상 좌표 정상화 + 최소 크기 보장
        val finalLeft = minOf(left, right).coerceIn(0f, 1f)
        val finalRight = maxOf(left, right).coerceIn(0f, 1f)
        val finalTop = minOf(top, bottom).coerceIn(0f, 1f)
        val finalBottom = maxOf(top, bottom).coerceIn(0f, 1f)

        if (finalRight - finalLeft < 0.01f || finalBottom - finalTop < 0.01f) {
            return Crop(0f, 0f, 1f, 1f) // fallback: 전체 화면
        }

        return Crop(finalLeft, finalRight, finalTop, finalBottom)
    }
}