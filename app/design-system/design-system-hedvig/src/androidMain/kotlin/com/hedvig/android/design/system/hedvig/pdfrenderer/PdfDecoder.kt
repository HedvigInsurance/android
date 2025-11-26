package com.hedvig.android.design.system.hedvig.pdfrenderer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options

class PdfDecoder(
  private val source: ImageSource,
  private val options: Options,
) : Decoder {
  override suspend fun decode(): DecodeResult {
    val context = options.context
    val pdfRenderer = PdfRenderer(
      ParcelFileDescriptor.open(
        source.file().toFile(),
        ParcelFileDescriptor.MODE_READ_ONLY,
      ),
    )
    val page = pdfRenderer.openPage(0)

    val bitmap = Bitmap.createBitmap(
      page.width * 2,
      page.height * 2,
      Bitmap.Config.ARGB_8888,
    )
    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    page.close()
    pdfRenderer.close()

    return DecodeResult(
      drawable = bitmap.toDrawable(context.resources),
      isSampled = false,
    )
  }

  class Factory : Decoder.Factory {
    override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
      if (!isApplicable(result)) return null
      return PdfDecoder(result.source, options)
    }

    private fun isApplicable(result: SourceResult): Boolean = result.mimeType == MIME_TYPE_PDF
  }

  companion object {
    private const val MIME_TYPE_PDF = "application/pdf"
  }
}
