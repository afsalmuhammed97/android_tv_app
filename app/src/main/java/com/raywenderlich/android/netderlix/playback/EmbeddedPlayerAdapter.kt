/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.netderlix.playback

import android.util.Log
import androidx.leanback.media.PlayerAdapter
import androidx.lifecycle.LifecycleCoroutineScope
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.raywenderlich.android.netderlix.model.Video
import com.raywenderlich.android.netderlix.model.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

class EmbeddedPlayerAdapter(
    private val coroutineScope: LifecycleCoroutineScope,
    private var player: YouTubePlayer? = null
) : PlayerAdapter() {

  private var playerState: PlayerConstants.PlayerState = PlayerConstants.PlayerState.UNKNOWN

  private var duration: Float = -1f

  private var currentSecond: Float = 0f

  private var loadedFraction: Float = 0f

  private var ready = false

  private var fastForwarding = false

  private var rewinding = false

  private var playlist = mutableListOf<Video>()

  private var currentVideo = 0

  fun setPlayer(player: YouTubePlayer) {
    this.player = player

    player.addListener(object : YouTubePlayerListener {
      override fun onApiChange(youTubePlayer: YouTubePlayer) {
      }

      override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        this@EmbeddedPlayerAdapter.currentSecond = second

        callback.onCurrentPositionChanged(this@EmbeddedPlayerAdapter)
        Log.d("PlayerAdapter", "onCurrentPositionChanged($second)")
      }

      override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
        callback.onError(this@EmbeddedPlayerAdapter, 0, error.name)
        Log.d("PlayerAdapter", "onError($error)")
      }

      override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer,
          playbackQuality: PlayerConstants.PlaybackQuality) {
      }

      override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer,
          playbackRate: PlayerConstants.PlaybackRate) {
      }

      override fun onReady(youTubePlayer: YouTubePlayer) {
        this@EmbeddedPlayerAdapter.ready = true

        Log.d("PlayerAdapter", "onReady()")
        callback.onPreparedStateChanged(this@EmbeddedPlayerAdapter)
      }

      override fun onStateChange(youTubePlayer: YouTubePlayer,
          state: PlayerConstants.PlayerState) {
        this@EmbeddedPlayerAdapter.playerState = state
        Log.d("PlayerAdapter", "onStateChange($state)")

        when (state) {
          in arrayOf(PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.PAUSED) -> {
            this@EmbeddedPlayerAdapter.ready = true

            callback.onPreparedStateChanged(this@EmbeddedPlayerAdapter)
            callback.onPlayStateChanged(this@EmbeddedPlayerAdapter)


          }
          PlayerConstants.PlayerState.ENDED -> {
            callback.onPlayCompleted(this@EmbeddedPlayerAdapter)
          }
          PlayerConstants.PlayerState.VIDEO_CUED -> {
            this@EmbeddedPlayerAdapter.ready = true

            callback.onPreparedStateChanged(this@EmbeddedPlayerAdapter)
          }
        }
      }

      override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        this@EmbeddedPlayerAdapter.duration = duration

        Log.d("PlayerAdapter", "onVideoDuration($duration)")
        callback.onDurationChanged(this@EmbeddedPlayerAdapter)
        callback.onPlayStateChanged(this@EmbeddedPlayerAdapter)
      }

      override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
      }

      override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
        this@EmbeddedPlayerAdapter.loadedFraction = loadedFraction

        Log.d("PlayerAdapter", "onVideoLoadedFraction($loadedFraction)")
        callback.onBufferedPositionChanged(this@EmbeddedPlayerAdapter)
      }

    })
  }

  override fun play() {
    stopFastForwardOrRewind()

    player?.play()
  }

  override fun pause() {
    player?.pause()
  }

  override fun next() {
    stopFastForwardOrRewind()

    if (currentVideo < playlist.size - 1) {
      currentVideo++

      player?.loadVideo(playlist[currentVideo].id, 0.0f)
    }
  }

  override fun previous() {
    supportedActions

    stopFastForwardOrRewind()

    if (currentVideo > 0) {
      currentVideo--

      player?.loadVideo(playlist[currentVideo].id, 0.0f)
    }
  }

  private fun stopFastForwardOrRewind() {
    fastForwarding = false
    rewinding = false
    player?.seekTo(currentSecond)
  }

  private fun fastForwardOneSecond() {
    currentSecond = max(currentSecond + 1, 0f)
    callback.onCurrentPositionChanged(this@EmbeddedPlayerAdapter)

    if (currentSecond == duration) {
      fastForwarding = false
      player?.seekTo(currentSecond)
    }
  }

  private fun rewindOneSecond() {
    currentSecond = max(currentSecond - 1, 0f)
    callback.onCurrentPositionChanged(this@EmbeddedPlayerAdapter)

    if (currentSecond == 0f) {
      rewinding = false
      player?.seekTo(currentSecond)
    }
  }

  override fun fastForward() {
    fastForwarding = !fastForwarding
    rewinding = false

    if (fastForwarding) {
      if (isPlaying) {
        pause()
      }

      coroutineScope.launch {
        while (fastForwarding) {
          delay(SEEK_UPDATE_INTERVAL)
          fastForwardOneSecond()
        }
      }
    } else {
      stopFastForwardOrRewind()
    }
  }

  override fun rewind() {
    rewinding = !rewinding
    fastForwarding = false

    if (rewinding) {
      if (isPlaying) {
        pause()
      }

      coroutineScope.launch {
        while (rewinding) {
          delay(SEEK_UPDATE_INTERVAL)
          rewindOneSecond()
        }
      }
    } else {
      stopFastForwardOrRewind()
    }
  }

  override fun seekTo(positionInMs: Long) {
    player?.seekTo((positionInMs / MILLISECONDS).toFloat())
  }

  fun loadVideo(videoItem: VideoItem) {
    this.playlist = videoItem.playlist
    this.currentVideo = videoItem.playlist.indexOf(videoItem.video)

    player?.loadVideo(playlist[currentVideo].id, 0.0f)
  }

  override fun isPrepared() = ready

  override fun isPlaying() = playerState == PlayerConstants.PlayerState.PLAYING

  override fun getDuration() = (duration * MILLISECONDS).toLong()

  override fun getCurrentPosition() = (currentSecond * MILLISECONDS).toLong()

  override fun getBufferedPosition() = (loadedFraction * duration * MILLISECONDS).toLong()

  companion object {

    /** Number of milliseconds in a second. */
    private const val MILLISECONDS = 1000

    /** Number of milliseconds after which fast forward or rewind is moved by one second. */
    private const val SEEK_UPDATE_INTERVAL = 200L
  }
}