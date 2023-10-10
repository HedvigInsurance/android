package com.hedvig.app.ui.coil

import android.widget.ImageView
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest

fun ImageView.load(
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
