

package com.raywenderlich.android.netderlix.repository

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.raywenderlich.android.netderlix.BuildConfig
import com.raywenderlich.android.netderlix.model.PlaylistPage
import com.raywenderlich.android.netderlix.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PlaylistsRepository {

  private val youtubeService: YouTube by lazy {
    YouTube.Builder(
        AndroidHttp.newCompatibleTransport(),
        JacksonFactory.getDefaultInstance(),
        HttpRequestInitializer {  }
    ).setApplicationName(APP_NAME)
        .setYouTubeRequestInitializer(YouTubeRequestInitializer(BuildConfig.YOUTUBE_API_KEY))
        .build()
  }


  suspend fun getPlaylists(pageToken: String? = null) = withContext(Dispatchers.IO) {
    val playlists= youtubeService
        .playlists()
        .list("contentDetails,id,localizations,player,snippet,status")
        .setChannelId(YOUTUBE_CHANNEL_ID)
        .setPageToken(pageToken)
        .execute()

    PlaylistPage(
        playlists.items?.map { playlist ->
          com.raywenderlich.android.netderlix.model.Playlist(
              playlist.id,
              playlist.snippet.title
          )
        } ?: listOf(),
        playlists.nextPageToken
    )
  }


  suspend fun getItems( playlist: com.raywenderlich.android.netderlix.model.Playlist) = withContext(Dispatchers.IO) {
    val playlistResponse = youtubeService.playlistItems()
        .list("contentDetails,id,snippet,status")
        .setPlaylistId(playlist.id)
        .execute()

    ArrayList(playlistResponse.items.mapNotNull { item ->
      val videos = youtubeService.videos()
          .list("contentDetails,id,liveStreamingDetails,localizations,player," +
              "recordingDetails,snippet,statistics,status,topicDetails")
          .setId(item.contentDetails.videoId)
          .execute()

      videos.items.firstOrNull()?.let { video ->
        Video(
            item.contentDetails.videoId,
            video.snippet.title,
            video.snippet.description,
            video.snippet.thumbnails.high.url,
            video.snippet.thumbnails.high.url,
            video.snippet.channelTitle
        )
      }
    })
  }

  companion object {
    private const val YOUTUBE_CHANNEL_ID ="UCKNTZMRHPLXfqlbdOI7mCkg"               //"UCz3cM4qLljXcQ8oWjMPgKZA"

    private const val APP_NAME = "Netderlix"
  }
}