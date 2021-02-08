package com.hedvig.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.annotation.Dimension
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.hedvig.app.R

class QR private constructor(
    context: Context
) {
    private val defaultQRSize = context.resources.getDimensionPixelSize(R.dimen.default_qr_code_size)

    fun load(text: String, @Dimension width: Int = defaultQRSize, @Dimension height: Int = defaultQRSize): QRBitmap {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (matrix.get(x, y)) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }
                )
            }
        }
        return QRBitmap(bitmap)
    }

    companion object {
        fun with(context: Context) = QR(context)
    }

    class QRBitmap(
        private val bitmap: Bitmap
    ) {
        fun into(imageView: ImageView) = imageView.setImageBitmap(bitmap)
    }
}
