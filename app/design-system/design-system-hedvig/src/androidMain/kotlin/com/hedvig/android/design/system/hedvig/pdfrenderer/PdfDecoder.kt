package com.hedvig.android.design.system.hedvig.pdfrenderer

import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PdfDecoder(
  private val source: ImageSource,
  private val options: Options,
) : Decoder {
  override suspend fun decode(): DecodeResult {
    val fileDescriptor = ParcelFileDescriptor.open(
      source.file().toFile(),
      ParcelFileDescriptor.MODE_READ_ONLY,
    )
    return fileDescriptor.use { descriptor ->
      PdfRenderer(descriptor).use { renderer ->
        renderer.openPage(0).use { page ->
          val scale = page.renderScale()
          val bitmap = createBitmap(
            width = (page.width * scale).roundToInt().coerceAtLeast(1),
            height = (page.height * scale).roundToInt().coerceAtLeast(1),
          )
          // `render` blends onto the destination, so unpainted and transparent regions of the page keep the bitmap's
          // initial fully transparent pixels unless the background is filled in first.
          bitmap.eraseColor(Color.WHITE)
          page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
          DecodeResult(
            image = bitmap.toDrawable(options.context.resources).asImage(),
            isSampled = scale < RENDER_SCALE,
          )
        }
      }
    }
  }

  /**
   * The factor to scale the page's native point size by when rendering it into a bitmap.
   *
   * [RENDER_SCALE] normally, lowered for pages whose box is large enough that rendering at that scale would exceed
   * [MAX_BITMAP_BYTES]. A bitmap above the hardware canvas' 100 MB limit throws from `Canvas#drawBitmap` during the
   * draw pass, which no amount of error handling around the image request can recover from.
   */
  private fun PdfRenderer.Page.renderScale(): Double {
    val bytesAtNativeSize = width.toDouble() * height * BYTES_PER_PIXEL
    return RENDER_SCALE.coerceAtMost(sqrt(MAX_BITMAP_BYTES / bytesAtNativeSize))
  }

  class Factory : Decoder.Factory {
    override fun create(result: SourceFetchResult, options: Options, imageLoader: ImageLoader): Decoder? {
      if (!isApplicable(result)) return null
      return PdfDecoder(result.source, options)
    }

    private fun isApplicable(result: SourceFetchResult): Boolean = result.mimeType == MIME_TYPE_PDF
  }

  companion object {
    private const val MIME_TYPE_PDF = "application/pdf"
    private const val RENDER_SCALE = 2.0
    private const val BYTES_PER_PIXEL = 4
    private const val MAX_BITMAP_BYTES = 32L * 1024 * 1024
  }
}
