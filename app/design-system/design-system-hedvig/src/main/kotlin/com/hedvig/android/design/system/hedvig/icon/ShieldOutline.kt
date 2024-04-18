package com.hedvig.android.design.system.hedvig.icon

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ShieldOutline: ImageVector
  get() {
    val current = _shieldOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.ShieldOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M18.5 12.0001 C18.5 13.7234 17.6417 15.3395 16.3738 16.7748 C15.1138 18.2011 13.5703 19.3085 12.499 19.9818 C12.1874 20.1776 11.8126 20.1776 11.501 19.9818 C10.4297 19.3085 8.8862 18.2011 7.6262 16.7748 C6.35828 15.3395 5.5 13.7234 5.5 12.0001 V7.0791 C5.5 6.45383 5.88786 5.89415 6.47331 5.67461 L11.4733 3.79961 C11.8129 3.67227 12.1871 3.67227 12.5267 3.79961 L17.5267 5.67461 C18.1121 5.89415 18.5 6.45384 18.5 7.0791 V12.0001Z M10.7029 21.2518 C11.5023 21.7543 12.4977 21.7543 13.2971 21.2518 C15.5622 19.8282 20 16.4689 20 12.0001 V7.0791 C20 5.82857 19.2243 4.70921 18.0534 4.27011 L13.0534 2.39511 C12.3742 2.14043 11.6258 2.14043 10.9466 2.39511 L5.94663 4.27011 C4.77572 4.70921 4 5.82857 4 7.0791 V12.0001 C4 16.4689 8.43779 19.8282 10.7029 21.2518Z M15.6781 10.5325 C15.9722 10.2408 15.9742 9.76597 15.6825 9.47187 C15.3908 9.17776 14.9159 9.1758 14.6218 9.46748 L11.2928 12.7691 C11.1953 12.8658 11.0382 12.8658 10.9407 12.7691 L9.67816 11.5169 C9.38406 11.2252 8.90919 11.2272 8.61751 11.5213 C8.32583 11.8154 8.32779 12.2903 8.62189 12.582 L9.88445 13.8341 C10.5667 14.5107 11.6668 14.5107 12.3491 13.8341 L15.6781 10.5325Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 18.5 12.0001
        moveTo(x = 18.5f, y = 12.0001f)
        // C 18.5 13.7234 17.6417 15.3395 16.3738 16.7748
        curveTo(
          x1 = 18.5f,
          y1 = 13.7234f,
          x2 = 17.6417f,
          y2 = 15.3395f,
          x3 = 16.3738f,
          y3 = 16.7748f,
        )
        // C 15.1138 18.2011 13.5703 19.3085 12.499 19.9818
        curveTo(
          x1 = 15.1138f,
          y1 = 18.2011f,
          x2 = 13.5703f,
          y2 = 19.3085f,
          x3 = 12.499f,
          y3 = 19.9818f,
        )
        // C 12.1874 20.1776 11.8126 20.1776 11.501 19.9818
        curveTo(
          x1 = 12.1874f,
          y1 = 20.1776f,
          x2 = 11.8126f,
          y2 = 20.1776f,
          x3 = 11.501f,
          y3 = 19.9818f,
        )
        // C 10.4297 19.3085 8.8862 18.2011 7.6262 16.7748
        curveTo(
          x1 = 10.4297f,
          y1 = 19.3085f,
          x2 = 8.8862f,
          y2 = 18.2011f,
          x3 = 7.6262f,
          y3 = 16.7748f,
        )
        // C 6.35828 15.3395 5.5 13.7234 5.5 12.0001
        curveTo(
          x1 = 6.35828f,
          y1 = 15.3395f,
          x2 = 5.5f,
          y2 = 13.7234f,
          x3 = 5.5f,
          y3 = 12.0001f,
        )
        // V 7.0791
        verticalLineTo(y = 7.0791f)
        // C 5.5 6.45383 5.88786 5.89415 6.47331 5.67461
        curveTo(
          x1 = 5.5f,
          y1 = 6.45383f,
          x2 = 5.88786f,
          y2 = 5.89415f,
          x3 = 6.47331f,
          y3 = 5.67461f,
        )
        // L 11.4733 3.79961
        lineTo(x = 11.4733f, y = 3.79961f)
        // C 11.8129 3.67227 12.1871 3.67227 12.5267 3.79961
        curveTo(
          x1 = 11.8129f,
          y1 = 3.67227f,
          x2 = 12.1871f,
          y2 = 3.67227f,
          x3 = 12.5267f,
          y3 = 3.79961f,
        )
        // L 17.5267 5.67461
        lineTo(x = 17.5267f, y = 5.67461f)
        // C 18.1121 5.89415 18.5 6.45384 18.5 7.0791
        curveTo(
          x1 = 18.1121f,
          y1 = 5.89415f,
          x2 = 18.5f,
          y2 = 6.45384f,
          x3 = 18.5f,
          y3 = 7.0791f,
        )
        // V 12.0001z
        verticalLineTo(y = 12.0001f)
        close()
        // M 10.7029 21.2518
        moveTo(x = 10.7029f, y = 21.2518f)
        // C 11.5023 21.7543 12.4977 21.7543 13.2971 21.2518
        curveTo(
          x1 = 11.5023f,
          y1 = 21.7543f,
          x2 = 12.4977f,
          y2 = 21.7543f,
          x3 = 13.2971f,
          y3 = 21.2518f,
        )
        // C 15.5622 19.8282 20 16.4689 20 12.0001
        curveTo(
          x1 = 15.5622f,
          y1 = 19.8282f,
          x2 = 20.0f,
          y2 = 16.4689f,
          x3 = 20.0f,
          y3 = 12.0001f,
        )
        // V 7.0791
        verticalLineTo(y = 7.0791f)
        // C 20 5.82857 19.2243 4.70921 18.0534 4.27011
        curveTo(
          x1 = 20.0f,
          y1 = 5.82857f,
          x2 = 19.2243f,
          y2 = 4.70921f,
          x3 = 18.0534f,
          y3 = 4.27011f,
        )
        // L 13.0534 2.39511
        lineTo(x = 13.0534f, y = 2.39511f)
        // C 12.3742 2.14043 11.6258 2.14043 10.9466 2.39511
        curveTo(
          x1 = 12.3742f,
          y1 = 2.14043f,
          x2 = 11.6258f,
          y2 = 2.14043f,
          x3 = 10.9466f,
          y3 = 2.39511f,
        )
        // L 5.94663 4.27011
        lineTo(x = 5.94663f, y = 4.27011f)
        // C 4.77572 4.70921 4 5.82857 4 7.0791
        curveTo(
          x1 = 4.77572f,
          y1 = 4.70921f,
          x2 = 4.0f,
          y2 = 5.82857f,
          x3 = 4.0f,
          y3 = 7.0791f,
        )
        // V 12.0001
        verticalLineTo(y = 12.0001f)
        // C 4 16.4689 8.43779 19.8282 10.7029 21.2518z
        curveTo(
          x1 = 4.0f,
          y1 = 16.4689f,
          x2 = 8.43779f,
          y2 = 19.8282f,
          x3 = 10.7029f,
          y3 = 21.2518f,
        )
        close()
        // M 15.6781 10.5325
        moveTo(x = 15.6781f, y = 10.5325f)
        // C 15.9722 10.2408 15.9742 9.76597 15.6825 9.47187
        curveTo(
          x1 = 15.9722f,
          y1 = 10.2408f,
          x2 = 15.9742f,
          y2 = 9.76597f,
          x3 = 15.6825f,
          y3 = 9.47187f,
        )
        // C 15.3908 9.17776 14.9159 9.1758 14.6218 9.46748
        curveTo(
          x1 = 15.3908f,
          y1 = 9.17776f,
          x2 = 14.9159f,
          y2 = 9.1758f,
          x3 = 14.6218f,
          y3 = 9.46748f,
        )
        // L 11.2928 12.7691
        lineTo(x = 11.2928f, y = 12.7691f)
        // C 11.1953 12.8658 11.0382 12.8658 10.9407 12.7691
        curveTo(
          x1 = 11.1953f,
          y1 = 12.8658f,
          x2 = 11.0382f,
          y2 = 12.8658f,
          x3 = 10.9407f,
          y3 = 12.7691f,
        )
        // L 9.67816 11.5169
        lineTo(x = 9.67816f, y = 11.5169f)
        // C 9.38406 11.2252 8.90919 11.2272 8.61751 11.5213
        curveTo(
          x1 = 9.38406f,
          y1 = 11.2252f,
          x2 = 8.90919f,
          y2 = 11.2272f,
          x3 = 8.61751f,
          y3 = 11.5213f,
        )
        // C 8.32583 11.8154 8.32779 12.2903 8.62189 12.582
        curveTo(
          x1 = 8.32583f,
          y1 = 11.8154f,
          x2 = 8.32779f,
          y2 = 12.2903f,
          x3 = 8.62189f,
          y3 = 12.582f,
        )
        // L 9.88445 13.8341
        lineTo(x = 9.88445f, y = 13.8341f)
        // C 10.5667 14.5107 11.6668 14.5107 12.3491 13.8341
        curveTo(
          x1 = 10.5667f,
          y1 = 14.5107f,
          x2 = 11.6668f,
          y2 = 14.5107f,
          x3 = 12.3491f,
          y3 = 13.8341f,
        )
        // L 15.6781 10.5325z
        lineTo(x = 15.6781f, y = 10.5325f)
        close()
      }
    }.build().also { _shieldOutline = it }
  }

@Preview
@Composable
private fun IconPreview() {
  com.hedvig.android.design.system.hedvig.HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ShieldOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _shieldOutline: ImageVector? = null
