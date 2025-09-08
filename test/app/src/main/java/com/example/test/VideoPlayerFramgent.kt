@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.test

import android.content.Intent
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
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.HttpDataSource

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

        // 팔로우 버튼 토글
        binding.btnFollow.setOnClickListener {
            val btn = binding.btnFollow
            if (btn.text == "팔로우") {
                btn.text = "팔로잉"
                btn.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            } else {
                btn.text = "팔로우"
                btn.setBackgroundColor(resources.getColor(R.color.green_35A825, null))
            }
        }

        // 좋아요 버튼 토글
        binding.btnLike.setOnClickListener {
            val btn = binding.btnLike
            val tag = btn.tag as? Boolean ?: false
            if (!tag) {
                btn.setImageResource(R.drawable.ic_heart_fill)
            } else {
                btn.setImageResource(R.drawable.ic_heart)
            }
            btn.tag = !tag
        }

        binding.btnComment.setOnClickListener {
            val intent = Intent(requireContext(), ShortsComment::class.java).apply {
                putExtra("shortsId", shortObject.id)
            }
            startActivity(intent)
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
        binding.playerView.player = null
        player?.release()
        player = null
    }

    private fun setContents() {
        // DataBinding은 id를 camelCase로 매핑함 (tv_nickname → tvNickname)
        binding.tvNicknameBottom.text = shortObject.userName
        binding.tvVideoTitle.text = shortObject.title
        //유저네임 클릭 시 프로필 화면으로 이동
        val goProfile = View.OnClickListener {
            val intent = Intent(requireContext(), MyProfileActivity::class.java).apply {
                putExtra("targetUserId", shortObject.userId) // ← 반드시 userId
            }
            startActivity(intent)
        }

        // ✅ 실제로 달아주기
        binding.tvNicknameBottom.setOnClickListener(goProfile)
        binding.imgProfile.setOnClickListener(goProfile)

        // 조회수/프로필은 실제 데이터에 맞게 갱신
        binding.tvViews.text = "조회수 ${shortObject.viewCount}회"
    }

    private fun initializePlayer() {
        val url = resolveFullUrl(shortObject.videoUrl)
        if (url.isBlank()) {
            Log.e("ShortsPlay", "❌ video url is blank (id=${shortObject.id})")
            return
        }
        Log.d("ShortsPlay", "▶ will play: $url")
        val mediaSource = buildMediaSource(Uri.parse(url))

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

                exoPlayer.setMediaSource(mediaSource)   // ✅ 여기서 한 번만
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false
                exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                exoPlayer.addListener(playerListener)
            }
    }


    private fun resolveFullUrl(relativeOrAbsolute: String?): String {
        if (relativeOrAbsolute.isNullOrBlank()) return ""
        return if (relativeOrAbsolute.startsWith("http://") || relativeOrAbsolute.startsWith("https://")) {
            relativeOrAbsolute
        } else {
            val base = RetrofitInstance.BASE_URL.trimEnd('/')
            val path = relativeOrAbsolute.trimStart('/')
            "$base/$path"
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val mediaItem = MediaItem.fromUri(uri)

        // 모든 프로토콜에 같은 HTTP 팩토리 사용
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(requireContext(), getString(R.string.app_name)))
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)

        val path = (uri.lastPathSegment ?: "").lowercase()

        return when {
            path.endsWith(".m3u8") || uri.toString().contains("m3u8", true) ->
                HlsMediaSource.Factory(httpFactory).createMediaSource(mediaItem)

            path.endsWith(".mpd") || uri.toString().contains("mpd", true) ->
                DashMediaSource.Factory(httpFactory).createMediaSource(mediaItem)

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
                playerControl(play = true, reset = true)
            }
        }
        override fun onPlayerError(error: PlaybackException) {
            Log.e("ShortsPlay", "⛔ player error: ${error.errorCodeName}", error)
            val cause = error.cause
            if (cause is HttpDataSource.InvalidResponseCodeException) {
                Log.e("ShortsPlay", "HTTP ${cause.responseCode}, url=${cause.dataSpec?.uri}")
                Log.e("ShortsPlay", "headers=${cause.headerFields}")   // ✅ 여기!
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
