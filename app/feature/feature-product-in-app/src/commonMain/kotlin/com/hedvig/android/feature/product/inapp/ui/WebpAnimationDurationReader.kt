package com.hedvig.android.feature.product.inapp.ui

import coil3.ImageLoader
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlin.concurrent.Volatile
import okio.BufferedSource
import okio.IOException

/**
 * Records how long one play of an animated WebP lasts, then declines to decode so the image loader's own
 * decoders still handle the image.
 */
internal class WebpAnimationDurationReader : Decoder.Factory {
  @Volatile
  var durationMillis: Int? = null
    private set

  override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
    durationMillis = animatedWebpDurationMillis(result.source.source().peek())
    return null
  }
}

/**
 * The sum of the frame durations of an animated WebP, or null when [source] is not one.
 */
internal fun animatedWebpDurationMillis(source: BufferedSource): Int? {
  return try {
    if (source.readUtf8(4) != "RIFF") return null
    source.skip(4)
    if (source.readUtf8(4) != "WEBP") return null
    var totalMillis = 0
    var frameCount = 0
    while (!source.exhausted()) {
      val fourCc = source.readUtf8(4)
      val size = source.readIntLe().toLong() and 0xFFFFFFFFL
      val paddedSize = size + (size and 1L)
      if (fourCc == "ANMF") {
        // Frame X, Y, width and height (3 bytes each) come before the 3-byte duration.
        source.skip(12)
        totalMillis += source.readUInt24Le()
        frameCount++
        source.skip(paddedSize - 15)
      } else {
        source.skip(paddedSize)
      }
    }
    if (frameCount > 0) totalMillis else null
  } catch (_: IOException) {
    null
  }
}

private fun BufferedSource.readUInt24Le(): Int {
  val b0 = readByte().toInt() and 0xFF
  val b1 = readByte().toInt() and 0xFF
  val b2 = readByte().toInt() and 0xFF
  return b0 or (b1 shl 8) or (b2 shl 16)
}
