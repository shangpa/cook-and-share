package com.example.test

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.transformer.TransformationException
import androidx.media3.transformer.TransformationResult
import androidx.media3.transformer.Transformer
import com.google.android.material.slider.RangeSlider
import java.io.File
import android.app.ProgressDialog
import android.view.View
import android.widget.ProgressBar

@androidx.media3.common.util.UnstableApi
class VideoTrimActivity : AppCompatActivity() {
    private var sliderInitialized = false
    private lateinit var player: ExoPlayer
    private lateinit var rangeSlider: RangeSlider
    private lateinit var btnTrim: Button
    private var videoDurationMs: Long = 0L
    private var inputUri: Uri? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_trim)

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
                Toast.makeText(this, "트리밍 구간을 두 구간으로 지정하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val startMs = (values[0] * 1000).toLong()
            val endMs = (values[1] * 1000).toLong()
            startTrim(startMs, endMs)
        }

        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val duration = player.duration
                if (duration > 0 && !sliderInitialized) {
                    sliderInitialized = true // 한 번만 초기화
                    videoDurationMs = duration
                    val seconds = (duration / 1000).toFloat()
                    rangeSlider.valueFrom = 0f
                    rangeSlider.valueTo = seconds
                    rangeSlider.setValues(0f, seconds)
                    rangeSlider.stepSize = 1f
                }
            }
        })

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
                    sliderInitialized = true // 한 번만 초기화
                    videoDurationMs = duration
                    val seconds = (duration / 1000).toFloat()
                    rangeSlider.valueFrom = 0f
                    rangeSlider.valueTo = seconds
                    rangeSlider.setValues(0f, seconds)
                    rangeSlider.stepSize = 1f
                }
            }
        })
    }

    private fun startTrim(startMs: Long, endMs: Long) {
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle("편집된 동영상")
            .build()
        val outputFile = File(cacheDir, "trimmed_output.mp4")
        val mediaItem = MediaItem.Builder()
            .setUri(inputUri!!)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMs)
                    .setEndPositionMs(endMs)
                    .build()
            )
            .setMediaMetadata(mediaMetadata) // 메타데이터 추가!
            .build()
        showProgressBar()
        val transformer = Transformer.Builder(this)
            .setVideoMimeType(MimeTypes.VIDEO_H264) // H.264(AVC)로 강제
            .build()

        transformer.setListener(object : Transformer.Listener {
            override fun onTransformationCompleted(mediaItem: MediaItem, result: TransformationResult) {
                hideProgressBar() // 완료 시 감추기!
                val resultIntent = Intent().apply {
                    putExtra("trimmedUri", Uri.fromFile(outputFile))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }

            override fun onTransformationError(mediaItem: MediaItem, exception: TransformationException) {
                hideProgressBar() // 실패 시도 감추기!
                Toast.makeText(this@VideoTrimActivity, "자르기 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        })

        transformer.startTransformation(mediaItem, outputFile.absolutePath)
    }

    private fun showProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}