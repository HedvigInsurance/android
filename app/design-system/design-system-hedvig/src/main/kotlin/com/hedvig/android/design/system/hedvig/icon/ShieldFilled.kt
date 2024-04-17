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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val HedvigIcons.ShieldFilled: ImageVector
  get() {
    val current = _shieldFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.ShieldFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M13.2971 21.2518 C12.4977 21.7543 11.5023 21.7543 10.7029 21.2518 C8.43779 19.8282 4 16.4689 4 12.0001 V7.0791 C4 5.82857 4.77572 4.70921 5.94663 4.27011 L10.9466 2.39511 C11.6258 2.14043 12.3742 2.14043 13.0534 2.39511 L18.0534 4.27011 C19.2243 4.70921 20 5.82857 20 7.0791 V12.0001 C20 16.4689 15.5622 19.8282 13.2971 21.2518Z M15.6781 10.5325 C15.9722 10.2408 15.9742 9.76597 15.6825 9.47187 C15.3908 9.17776 14.9159 9.1758 14.6218 9.46748 L11.2928 12.7691 C11.1953 12.8658 11.0382 12.8658 10.9407 12.7691 L9.67816 11.5169 C9.38406 11.2252 8.90919 11.2272 8.61751 11.5213 C8.32583 11.8154 8.32779 12.2903 8.62189 12.582 L9.88445 13.8341 C10.5667 14.5107 11.6668 14.5107 12.3491 13.8341 L15.6781 10.5325Z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 13.2971 21.2518
        moveTo(x = 13.2971f, y = 21.2518f)
        // C 12.4977 21.7543 11.5023 21.7543 10.7029 21.2518
        curveTo(
          x1 = 12.4977f,
          y1 = 21.7543f,
          x2 = 11.5023f,
          y2 = 21.7543f,
          x3 = 10.7029f,
          y3 = 21.2518f,
        )
        // C 8.43779 19.8282 4 16.4689 4 12.0001
        curveTo(
          x1 = 8.43779f,
          y1 = 19.8282f,
          x2 = 4.0f,
          y2 = 16.4689f,
          x3 = 4.0f,
          y3 = 12.0001f,
        )
        // V 7.0791
        verticalLineTo(y = 7.0791f)
        // C 4 5.82857 4.77572 4.70921 5.94663 4.27011
        curveTo(
          x1 = 4.0f,
          y1 = 5.82857f,
          x2 = 4.77572f,
          y2 = 4.70921f,
          x3 = 5.94663f,
          y3 = 4.27011f,
        )
        // L 10.9466 2.39511
        lineTo(x = 10.9466f, y = 2.39511f)
        // C 11.6258 2.14043 12.3742 2.14043 13.0534 2.39511
        curveTo(
          x1 = 11.6258f,
          y1 = 2.14043f,
          x2 = 12.3742f,
          y2 = 2.14043f,
          x3 = 13.0534f,
          y3 = 2.39511f,
        )
        // L 18.0534 4.27011
        lineTo(x = 18.0534f, y = 4.27011f)
        // C 19.2243 4.70921 20 5.82857 20 7.0791
        curveTo(
          x1 = 19.2243f,
          y1 = 4.70921f,
          x2 = 20.0f,
          y2 = 5.82857f,
          x3 = 20.0f,
          y3 = 7.0791f,
        )
        // V 12.0001
        verticalLineTo(y = 12.0001f)
        // C 20 16.4689 15.5622 19.8282 13.2971 21.2518z
        curveTo(
          x1 = 20.0f,
          y1 = 16.4689f,
          x2 = 15.5622f,
          y2 = 19.8282f,
          x3 = 13.2971f,
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
    }.build().also { _shieldFilled = it }
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
        imageVector = HedvigIcons.ShieldFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _shieldFilled: ImageVector? = null
