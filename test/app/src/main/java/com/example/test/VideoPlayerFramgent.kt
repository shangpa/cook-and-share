// VideoPlayerFragment.kt
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

@UnstableApi
class VideoPlayerFragment : Fragment() {
    private lateinit var binding: FragmentVideoPlayerBinding
    private var player: ExoPlayer? = null
    private var mIsVisibleToUser = false
    private var chkPlay: Boolean = true
    private lateinit var shortObject: ShortObject

    companion object {
        fun newInstance(mediaUrl: ShortObject?): VideoPlayerFragment {
            val args = Bundle().apply {
                putSerializable("mediaUrl", mediaUrl)
            }
            val fragment = VideoPlayerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        mIsVisibleToUser = menuVisible
        if (menuVisible) {
            playerControl(play = true, reset = false)
        } else {
            playerControl(play = false, reset = true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shortObject = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getSerializable("mediaUrl", ShortObject::class.java)!!
        } else {
            requireArguments().getSerializable("mediaUrl") as ShortObject
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_video_player, container, false)
        setContents()
        initializePlayer()
        binding.playerView.setOnClickListener {
            playerControl(!chkPlay, false)
        }
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
        player?.release()
        player = null
    }

    private fun setContents() {
        binding.tvNickname.text = shortObject.userName
        binding.tvTextShort.text = shortObject.contents
    }

    private fun initializePlayer() {
        val loadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(2000, 3000, 1500, 2000)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        player = ExoPlayer.Builder(requireContext())
            .setLoadControl(loadControl)
            .build().also { exoPlayer ->
                binding.playerView.player = exoPlayer
                binding.playerView.useController = false
                val mediaSource = buildMediaSource(Uri.parse(shortObject.url))
                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
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
            path.contains("m3u8") -> HlsMediaSource.Factory(httpFactory).createMediaSource(mediaItem)
            path.contains("mpd") -> DashMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(httpFactory).createMediaSource(mediaItem)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            chkPlay = isPlaying
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                playerControl(true, true)
            }
        }
    }

    fun playerControl(play: Boolean, reset: Boolean) {
        player?.let {
            if (play) {
                if (mIsVisibleToUser) {
                    if (reset) it.seekTo(0)
                    it.playWhenReady = true
                    binding.playerView.onResume()
                }
            } else {
                it.playWhenReady = false
                if (reset) it.seekTo(0)
                binding.playerView.onPause()
            }
        }
    }
}
