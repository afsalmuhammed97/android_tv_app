

package com.raywenderlich.android.netderlix.catalog

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.raywenderlich.android.netderlix.R
import com.raywenderlich.android.netderlix.error.ErrorFragment
import com.raywenderlich.android.netderlix.model.Playlist
import com.raywenderlich.android.netderlix.model.PlaylistPage
import com.raywenderlich.android.netderlix.model.Video
import com.raywenderlich.android.netderlix.repository.PlaylistsRepository
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.leanback.app.BackgroundManager
import android.util.DisplayMetrics
import com.raywenderlich.android.netderlix.loadDrawable
import kotlinx.coroutines.delay
import androidx.core.app.ActivityOptionsCompat
import androidx.leanback.widget.*
import com.raywenderlich.android.netderlix.details.VideoDetailsActivity
import com.raywenderlich.android.netderlix.model.VideoItem
import com.raywenderlich.android.netderlix.showVideoDetails


class CatalogFragment : BrowseSupportFragment() {

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    setUpTitleAndHeaders()
    loadAndShowPlaylists()
    initializeBackground()
    


    onItemViewClickedListener =
      OnItemViewClickedListener { itemViewHolder, item, _, _ ->
        if (item is VideoItem) {
          showVideoDetails(requireActivity(), itemViewHolder, item)
        }
      }

  }

  private fun setUpTitleAndHeaders(){
    title=getString(R.string.browse_title)

    headersState= HEADERS_ENABLED

    isHeadersTransitionOnBackEnabled=true

    brandColor=ContextCompat.getColor(
      requireContext(),R.color.fastlane_background
    )
  }

  private fun initializeBackground() {
    val backgroundManager = BackgroundManager.getInstance(activity).apply {
      attach(activity?.window)
    }

    val metrics = DisplayMetrics()
    requireActivity().windowManager.defaultDisplay.getMetrics(metrics)

    onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
      if (item is VideoItem) {
        viewLifecycleOwner.lifecycleScope.launch {
          delay(BACKGROUND_UPDATE_DELAY)

          loadDrawable(
              requireActivity(),
              item.video.backgroundImageUrl,
              R.drawable.default_background,
              metrics.widthPixels,
              metrics.heightPixels
          ) {
            backgroundManager.drawable = it
          }
        }
      }
    }
  }


  private fun loadAndShowPlaylists() {


    val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    val cardPresenter=CatalogCardPresenter()
//    val header = HeaderItem(0, "Hello! Replace me")
  //  val listRowAdapter = ArrayObjectAdapter()
   // rowsAdapter.add(ListRow(header, listRowAdapter))

    adapter = rowsAdapter

    val onPlaylistLoaded={playlist:Playlist,
      videos:ArrayList<Video>,
      lastPlayListInBatch:Boolean ->

      val listRowAdapter=ArrayObjectAdapter(cardPresenter)

      videos.forEach{video ->
      listRowAdapter.add(VideoItem(video,videos))

    }

      val header=HeaderItem(rowsAdapter.size().toLong(),playlist.title)
     rowsAdapter.add(ListRow(header,listRowAdapter))

      if (lastPlayListInBatch){
        progressBarManager.hide()
      }

    }

      progressBarManager.show()
      loadPlaylists(onPlaylistLoaded, ::showError)

      adapter=rowsAdapter
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (requestCode) {
      REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != FragmentActivity.RESULT_OK) {
        showError(getString(R.string.error_play_services_missing))
      } else {
        loadAndShowPlaylists()
      }

      REQUEST_AUTHORIZATION -> if (resultCode == FragmentActivity.RESULT_OK) {
        loadAndShowPlaylists()
      }
    }
  }

  /** Helper function that shows fullscreen error message. */
  private fun showError(message: String) {
    // Make sure progress bar is hidden
    progressBarManager.hide()

    requireActivity().supportFragmentManager
        .beginTransaction()
        .add(
            R.id.main_frame,
            ErrorFragment.newInstance(
                getString(R.string.app_name),
                message
            )
        )
        .commit()
  }

  private fun loadPlaylists(
      onPlaylistLoaded: (Playlist, ArrayList<Video>, Boolean) -> Unit,
      onError: (String) -> Unit) {

    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices()
    } else {
      viewLifecycleOwner.lifecycleScope.launch {
        val repository = PlaylistsRepository()
        var playlistsPage: PlaylistPage? = null

        do {
          try {
            playlistsPage = repository.getPlaylists(playlistsPage?.nextPageToken)

            playlistsPage.playlists.forEachIndexed { index, playlist ->
              val items = repository.getItems(playlist)
              onPlaylistLoaded(playlist, items, index == playlistsPage.playlists.size - 1)
            }
          } catch (_: IOException) {
            onError(getString(R.string.error_youtube_service))
            break
          }
        } while (playlistsPage?.nextPageToken != null)
      }
    }
  }

  private fun isGooglePlayServicesAvailable(): Boolean {
    val apiAvailability = GoogleApiAvailability.getInstance() ?: return false

    val connectionStaticCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
    return connectionStaticCode == ConnectionResult.SUCCESS
  }

  private fun acquireGooglePlayServices() {
    val apiAvailability = GoogleApiAvailability.getInstance() ?: return

    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())

    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
      progressBarManager.hide()
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
    }
  }

  private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
    val apiAvailability = GoogleApiAvailability.getInstance() ?: return

    val dialog = apiAvailability.getErrorDialog(
        requireActivity(),
        connectionStatusCode,
        REQUEST_GOOGLE_PLAY_SERVICES
    )

    dialog.show()
  }

  companion object {

    private const val BACKGROUND_UPDATE_DELAY = 300L

    const val REQUEST_AUTHORIZATION = 1001
    const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
  }
}