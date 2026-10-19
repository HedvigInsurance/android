package com.hedvig.android.ui.bankid

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import hedvig.resources.Res
import hedvig.resources.TALKBACK_QR_CODE_DESCRIPTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

/**
 * The BankID QR code for signing or logging in from another device. [liveQrCodeData] changes every second while
 * the BankID order is pending, so pass the latest value from each poll.
 */
@SuppressLint("ProduceStateDoesNotAssignValue") // https://issuetracker.google.com/issues/379337777
@Composable
fun BankIdQrCode(liveQrCodeData: String?, modifier: Modifier = Modifier) {
  var intSize: IntSize? by remember { mutableStateOf(null) }
  val painter by produceState<Painter>(ColorPainter(Color.Transparent), intSize, liveQrCodeData) {
    val size = intSize
    if (size == null || liveQrCodeData == null) {
      value = ColorPainter(Color.Transparent)
      return@produceState
    }
    val bitmapPainter: BitmapPainter = withContext(Dispatchers.Default) {
      val bitMatrix: BitMatrix = QRCodeWriter().encode(
        liveQrCodeData,
        BarcodeFormat.QR_CODE,
        size.width,
        size.height,
      )
      val bitmap = createBitmap(size.width, size.height, Bitmap.Config.RGB_565)
      for (x in 0 until size.width) {
        for (y in 0 until size.height) {
          val color = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
          bitmap[x, y] = color
        }
      }
      BitmapPainter(bitmap.asImageBitmap())
    }
    value = bitmapPainter
  }
  val qrCodeDescription = stringResource(Res.string.TALKBACK_QR_CODE_DESCRIPTION)
  Image(
    painter,
    null,
    modifier
      .semantics {
        role = Role.Image
        contentDescription = qrCodeDescription
      }
      .onSizeChanged { intSize = it },
  )
}
