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

import androidx.core.os.bundleOf
import androidx.leanback.app.PlaybackSupportFragment
import com.raywenderlich.android.netderlix.model.Video
import com.raywenderlich.android.netderlix.model.VideoItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.lifecycle.lifecycleScope
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.raywenderlich.android.netderlix.R

/** Handles video playback with media controls. */
class VideoPlaybackFragment : PlaybackSupportFragment() {

  private var playerGlue: PlaybackTransportControlGlue<EmbeddedPlayerAdapter>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val videoItem = arguments?.getSerializable(ARGUMENT_VIDEO) as VideoItem


    playerGlue= VideoPlaybackControlGlue(
      requireActivity(),
      EmbeddedPlayerAdapter(lifecycleScope)
    ).apply {

      host= PlaybackSupportFragmentGlueHost(this@VideoPlaybackFragment)


      isControlsOverlayAutoHideEnabled=false

      title=videoItem.video.title

      subtitle=videoItem.video.description
    }


  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {

    val root = super.onCreateView(inflater, container, savedInstanceState) as ViewGroup
    val videoItem = arguments?.getSerializable(ARGUMENT_VIDEO) as? VideoItem ?: return root

    val playerView = inflater.inflate(R.layout.fragment_playback_video, root,
        false) as YouTubePlayerView
    lifecycle.addObserver(playerView)

    playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
      override fun onReady(youTubePlayer: YouTubePlayer) {
        playerGlue?.playerAdapter?.setPlayer(youTubePlayer)
        playerGlue?.playerAdapter?.loadVideo(videoItem)
      }
    })

    root.addView(playerView, 0)

    backgroundType = BG_LIGHT
    return root
  }

  override fun onPause() {
    super.onPause()
    playerGlue?.pause()
  }

  companion object {

    private const val ARGUMENT_VIDEO = "movie"

    /** Creates new instance of this fragment that plays the given [Video]. */
    fun newInstance(videoItem: VideoItem) = VideoPlaybackFragment().apply {
      arguments = bundleOf(
          ARGUMENT_VIDEO to videoItem
      )
    }
  }
}