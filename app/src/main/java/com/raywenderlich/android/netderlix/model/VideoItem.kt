

package com.raywenderlich.android.netderlix.model

import java.io.Serializable


data class VideoItem(
    val video: Video,
    val playlist: ArrayList<Video>
) : Serializable