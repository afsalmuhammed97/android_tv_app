/*
 * Copyright (c) 2021 Razeware LLC
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

package com.raywenderlich.android.netderlix

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityOptionsCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.raywenderlich.android.netderlix.details.VideoDetailsActivity
import com.raywenderlich.android.netderlix.model.VideoItem

/** Utility method for loading images from URL via Glide image library. */
fun loadDrawable(
    activity: Activity,
    imageUrl: String?,
    @DrawableRes defaultImage: Int,
    width: Int,
    height: Int,
    onLoaded: (image: Drawable) -> Unit) {

  Glide.with(activity)
      .load(imageUrl)
      .centerCrop()
      .error(defaultImage)
      .into<CustomTarget<Drawable>>(object : CustomTarget<Drawable>(width, height) {

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
          onLoaded(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
          placeholder?.let { onLoaded(it) }
        }
      })
}

/** Utility method for loading Bitmap images from URL via Glide image library. */
fun loadBitmap(
    activity: Activity,
    imageUrl: String?,
    @DrawableRes defaultImage: Int,
    onLoaded: (bitmap: Bitmap) -> Unit) {
  Glide.with(activity)
      .asBitmap()
      .load(imageUrl)
      .centerCrop()
      .error(defaultImage)
      .into(object: CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          onLoaded(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
          placeholder?.let {
            onLoaded(drawableToBitmap(it))
          }
        }

      })
}

/** Taken from https://stackoverflow.com/a/10600736/2914696 */
private fun drawableToBitmap (drawable: Drawable): Bitmap  {
  if (drawable is BitmapDrawable) {
    if(drawable.bitmap != null) {
      return drawable.bitmap
    }
  }

  val bitmap = if(drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
    // Single color bitmap will be created of 1x1 pixel
    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
  } else {
    Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888);
  }

  val canvas = Canvas(bitmap)
  drawable.setBounds(0, 0, canvas.width, canvas.height)
  drawable.draw(canvas)
  return bitmap
}

fun loadBitmapIntoImageView(
    context: Context,
    imageUrl: String?,
    @DrawableRes defaultImage: Int,
    imageView: ImageView
) {
  Glide.with(context)
      .load(imageUrl)
      .centerCrop()
      .error(defaultImage)
      .into(imageView)
}

fun showVideoDetails(activity: Activity, itemViewHolder: Presenter.ViewHolder?, item: VideoItem) {
  val intent = VideoDetailsActivity.newIntent(activity, item)

  val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
      activity,
      (itemViewHolder?.view as ImageCardView).mainImageView,
      VideoDetailsActivity.SHARED_ELEMENT_NAME)
      .toBundle()
  activity.startActivity(intent, bundle)
}