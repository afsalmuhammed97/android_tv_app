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

package com.raywenderlich.android.netderlix.catalog

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.raywenderlich.android.netderlix.R
import com.raywenderlich.android.netderlix.loadBitmapIntoImageView
import com.raywenderlich.android.netderlix.model.VideoItem

class CatalogCardPresenter : Presenter() {

  private var defaultCardImage: Drawable? = null
  private var selectedBackgroundColor: Int = 0
  private var defaultBackgroundColor: Int = 0

  override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
    parent?.context?.let { context ->
      defaultBackgroundColor = ContextCompat.getColor(context, R.color.default_background)
      selectedBackgroundColor = ContextCompat.getColor(context, R.color.selected_background)
      defaultCardImage = ContextCompat.getDrawable(context, R.drawable.movie)
    }

    val cardView = object : ImageCardView(parent?.context) {
      override fun setSelected(selected: Boolean) {
        updateCardBackgroundColor(this, selected)
        super.setSelected(selected)
      }
    }

    cardView.isFocusable = true
    cardView.isFocusableInTouchMode = true
    updateCardBackgroundColor(cardView, false)
    return ViewHolder(cardView)
  }

  override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {

    val videoItem=item as VideoItem
    val video=videoItem.video

    val cardView=viewHolder?.view as ImageCardView
    cardView.titleText=video.title
    cardView.contentText=video.channel
    cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)

    loadBitmapIntoImageView(
      cardView.context,
      video.cardImageUrl,
      R.drawable.movie,
      cardView.mainImageView
    )


  }

  override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
       val cardView=viewHolder?.view as ImageCardView

       cardView.badgeImage=null
    cardView.mainImage=null


  }

  private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
    val color = if (selected) selectedBackgroundColor else defaultBackgroundColor
    // Both background colors should be set because the view"s background is temporarily visible
    // during animations.
    view.setBackgroundColor(color)
    view.setInfoAreaBackgroundColor(color)
  }

  companion object {
    private const val CARD_WIDTH = 313
    private const val CARD_HEIGHT = 176
  }
}