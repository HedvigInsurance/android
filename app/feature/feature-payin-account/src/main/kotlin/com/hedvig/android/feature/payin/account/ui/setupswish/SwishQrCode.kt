package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Swish

/**
 * A QR code following Swish's guidelines: black on white, with the colour Swish symbol on a white
 * circle in the centre, the symbol a quarter of the code's width. Modules are drawn as dots and the
 * finder patterns with rounded corners. The caller provides the white background and quiet zone.
 */
@Composable
internal fun SwishQrCode(content: String, modifier: Modifier = Modifier) {
  val modules = remember(content) { encodeQrModules(content) }
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier.aspectRatio(1f),
  ) {
    Canvas(Modifier.fillMaxSize()) {
      drawQrModules(modules)
    }
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxSize(LogoBackgroundFraction)
        .background(Color.White, CircleShape),
    ) {
      Image(
        imageVector = HedvigIcons.Swish,
        contentDescription = EmptyContentDescription,
        modifier = Modifier
          .fillMaxWidth(LogoFraction / LogoBackgroundFraction)
          .aspectRatio(1f),
      )
    }
  }
}

/** Swish's guideline: the symbol is 25% of the code's width. */
private const val LogoFraction = 0.25f

/** The white circle behind the symbol, and the area cleared of modules. */
private const val LogoBackgroundFraction = 0.32f

private const val FinderSize = 7

/** The module grid without its quiet zone, `true` for dark. */
private class QrModules(private val matrix: Array<BooleanArray>) {
  val count: Int = matrix.size

  operator fun get(column: Int, row: Int): Boolean = matrix[row][column]
}

private fun encodeQrModules(content: String): QrModules {
  // High error correction, so the modules cleared under the symbol leave the code readable.
  val byteMatrix = Encoder.encode(content, ErrorCorrectionLevel.H).matrix
  return QrModules(
    Array(byteMatrix.height) { row ->
      BooleanArray(byteMatrix.width) { column -> byteMatrix.get(column, row).toInt() == 1 }
    },
  )
}

private fun DrawScope.drawQrModules(modules: QrModules) {
  val count = modules.count
  val moduleSize = size.minDimension / count
  val finderOrigins = listOf(0 to 0, count - FinderSize to 0, 0 to count - FinderSize)
  val center = Offset(size.width / 2, size.height / 2)
  val clearedRadius = size.minDimension * LogoBackgroundFraction / 2

  fun isInFinder(column: Int, row: Int): Boolean = finderOrigins.any { (originColumn, originRow) ->
    column in originColumn until originColumn + FinderSize && row in originRow until originRow + FinderSize
  }

  for (row in 0 until count) {
    for (column in 0 until count) {
      if (!modules[column, row] || isInFinder(column, row)) continue
      val moduleCenter = Offset((column + 0.5f) * moduleSize, (row + 0.5f) * moduleSize)
      // A module that pokes out from under the white circle is dropped, so no half dots show at its edge.
      if ((moduleCenter - center).getDistance() < clearedRadius + moduleSize / 2) continue
      drawCircle(Color.Black, radius = moduleSize * 0.42f, center = moduleCenter)
    }
  }
  for ((column, row) in finderOrigins) {
    drawFinder(topLeft = Offset(column * moduleSize, row * moduleSize), moduleSize = moduleSize)
  }
}

/**
 * A one-module ring around a rounded 3×3 eye. The ring's hole is rounded concentrically with its outside
 * so the ring keeps an even thickness around the corners.
 */
private fun DrawScope.drawFinder(topLeft: Offset, moduleSize: Float) {
  val outer = Rect(topLeft, Size(FinderSize * moduleSize, FinderSize * moduleSize))
  val ring = Path().apply {
    fillType = PathFillType.EvenOdd
    addRoundRect(RoundRect(outer, CornerRadius(moduleSize * 1.8f)))
    addRoundRect(RoundRect(outer.deflate(moduleSize), CornerRadius(moduleSize * 0.8f)))
  }
  drawPath(ring, Color.Black)
  val eye = outer.deflate(moduleSize * 2)
  drawRoundRect(Color.Black, eye.topLeft, eye.size, CornerRadius(moduleSize * 0.6f))
}

@HedvigPreview
@Composable
private fun PreviewSwishQrCode() {
  Box(Modifier.background(Color.White).padding(16.dp)) {
    SwishQrCode(
      content = "https://app.swish.nu/1/p/sw/?token=c28a4061470f4af48973bd2a4642b4fa",
      modifier = Modifier.size(148.dp),
    )
  }
}
