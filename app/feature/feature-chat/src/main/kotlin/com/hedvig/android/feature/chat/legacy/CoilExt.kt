package com.hedvig.android.feature.chat.legacy

import android.widget.ImageView
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.util.CoilUtils

internal fun ImageView.dispose() {
  CoilUtils.dispose(this)
}

internal fun ImageView.load(
  data: Any?,
  imageLoader: ImageLoader,
  builder: ImageRequest.Builder.() -> Unit = {},
): Disposable {
  val request = ImageRequest.Builder(context)
    .data(data)
    .target(this)
    .apply(builder)
    .build()
  return imageLoader.enqueue(request)
}
