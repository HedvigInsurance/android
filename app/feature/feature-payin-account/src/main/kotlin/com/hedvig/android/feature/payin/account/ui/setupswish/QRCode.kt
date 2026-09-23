package com.hedvig.android.feature.payin.account.ui.setupswish

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("ProduceStateDoesNotAssignValue")
@Composable
internal fun QRCode(token: String, modifier: Modifier = Modifier) {
  var intSize: IntSize? by remember { mutableStateOf(null) }
  val painter by produceState<Painter>(ColorPainter(Color.Transparent), intSize, token) {
    val size = intSize
    if (size == null) {
      value = ColorPainter(Color.Transparent)
      return@produceState
    }
    val bitmapPainter: BitmapPainter = withContext(Dispatchers.Default) {
      val bitMatrix: BitMatrix = QRCodeWriter().encode(
        token,
        BarcodeFormat.QR_CODE,
        size.width,
        size.height,
      )
      val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565)
      for (x in 0 until size.width) {
        for (y in 0 until size.height) {
          val color = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          bitmap.setPixel(x, y, color)
        }
      }
      BitmapPainter(bitmap.asImageBitmap())
    }
    value = bitmapPainter
  }
  Image(
    painter,
    null,
    modifier.onSizeChanged { intSize = it },
  )
}
