@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.test

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultAllocator
import com.example.test.databinding.FragmentVideoPlayerBinding
import com.example.test.model.ShortObject
import com.example.test.network.RetrofitInstance
import java.io.Serializable

@UnstableApi
class VideoPlayerFragment : Fragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var chkPlay: Boolean = false
    private lateinit var shortObject: ShortObject

    companion object {
        fun newInstance(mediaUrl: ShortObject): VideoPlayerFragment {
            val args = Bundle().apply {
                putSerializable("mediaUrl", mediaUrl as Serializable)
            }
            return VideoPlayerFragment().apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortObject = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getSerializable("mediaUrl", ShortObject::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getSerializable("mediaUrl") as ShortObject
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_player, container, false)
        setContents()
        initializePlayer()

        // 탭 → play/pause 토글
        binding.playerView.setOnClickListener { playerControl(!chkPlay, false) }
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    private fun releasePlayer() {
        binding.playerView.player = null
        player?.release()
        player = null
    }

    private fun setContents() {
        // DataBinding은 id를 camelCase로 매핑함 (tv_nickname → tvNickname)
        binding.tvNickname.text = shortObject.userName
        binding.tvNicknameBottom.text = shortObject.userName
        binding.tvVideoTitle.text = shortObject.contents
        binding.tvDescription.text = shortObject.contents
        // 조회수/프로필은 실제 데이터에 맞게 갱신
        binding.tvViews.text = "조회수 ${shortObject.viewCount}회"
    }

    private fun initializePlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                /* minBufferMs = */ 2000,
                /* maxBufferMs = */ 3000,
                /* bufferForPlaybackMs = */ 1500,
                /* bufferForPlaybackAfterRebufferMs = */ 2000
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        player = ExoPlayer.Builder(requireContext())
            .setLoadControl(loadControl)
            .build().also { exoPlayer ->
                binding.playerView.player = exoPlayer
                binding.playerView.useController = false


                val base = RetrofitInstance.BASE_URL.trimEnd('/')
                val fullUrl = if (shortObject.videoUrl.startsWith("http")) {
                    shortObject.videoUrl
                } else {
                    base + shortObject.videoUrl
                }
                val mediaSource = buildMediaSource(Uri.parse(fullUrl))

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false // Activity에서 현재 페이지만 true로 바꿔줌
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                exoPlayer.addListener(playerListener)
            }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)
        val userAgent = Util.getUserAgent(requireContext(), getString(R.string.app_name))
        val httpFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)
        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        val path = uri.lastPathSegment ?: ""
        return when {
            path.contains("m3u8", ignoreCase = true) ->
                HlsMediaSource.Factory(httpFactory).createMediaSource(mediaItem)
            path.contains("mpd", ignoreCase = true) ->
                DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else ->
                ProgressiveMediaSource.Factory(httpFactory).createMediaSource(mediaItem)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            chkPlay = isPlaying
        }
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                playerControl(play = true, reset = true) // LOOP 성격
            }
        }
    }

    /** Activity가 직접 호출해서 현재 페이지만 재생/정지 */
    fun playerControl(play: Boolean, reset: Boolean) {
        player?.let { p ->
            if (play) {
                if (reset) p.seekTo(0)
                p.playWhenReady = true
                binding.playerView.onResume()
            } else {
                p.playWhenReady = false
                if (reset) p.seekTo(0)
                binding.playerView.onPause()
            }
        }
    }
}
