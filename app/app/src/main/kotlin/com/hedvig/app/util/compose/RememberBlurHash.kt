package com.hedvig.app.util.compose

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import com.hedvig.app.util.BlurHashDecoder

fun blurHash(blurHash: String, width: Int, height: Int, context: Context): BitmapDrawable? {
  return BlurHashDecoder.decode(blurHash, width, height)?.let { decodedBitmap ->
    BitmapDrawable(
      context.resources,
      decodedBitmap,
    )
  }
}
