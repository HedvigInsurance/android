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
val HedvigIcons.Chat: ImageVector
  get() {
    val current = _chat
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Chat",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.5 12 C21.5 17.2467 17.2467 21.5 12 21.5 C10.6182 21.5 9.30527 21.205 8.12082 20.6745 C7.88245 20.5677 7.61343 20.5445 7.36467 20.6241 L3.34833 21.9088 C2.5741 22.1564 1.84356 21.4259 2.09121 20.6517 L3.37588 16.6353 C3.45545 16.3866 3.43227 16.1175 3.32551 15.8792 C2.79502 14.6947 2.5 13.3818 2.5 12 C2.5 6.75329 6.7533 2.5 12 2.5 C17.2467 2.5 21.5 6.75329 21.5 12Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 21.5 12
        moveTo(x = 21.5f, y = 12.0f)
        // C 21.5 17.2467 17.2467 21.5 12 21.5
        curveTo(
          x1 = 21.5f,
          y1 = 17.2467f,
          x2 = 17.2467f,
          y2 = 21.5f,
          x3 = 12.0f,
          y3 = 21.5f,
        )
        // C 10.6182 21.5 9.30527 21.205 8.12082 20.6745
        curveTo(
          x1 = 10.6182f,
          y1 = 21.5f,
          x2 = 9.30527f,
          y2 = 21.205f,
          x3 = 8.12082f,
          y3 = 20.6745f,
        )
        // C 7.88245 20.5677 7.61343 20.5445 7.36467 20.6241
        curveTo(
          x1 = 7.88245f,
          y1 = 20.5677f,
          x2 = 7.61343f,
          y2 = 20.5445f,
          x3 = 7.36467f,
          y3 = 20.6241f,
        )
        // L 3.34833 21.9088
        lineTo(x = 3.34833f, y = 21.9088f)
        // C 2.5741 22.1564 1.84356 21.4259 2.09121 20.6517
        curveTo(
          x1 = 2.5741f,
          y1 = 22.1564f,
          x2 = 1.84356f,
          y2 = 21.4259f,
          x3 = 2.09121f,
          y3 = 20.6517f,
        )
        // L 3.37588 16.6353
        lineTo(x = 3.37588f, y = 16.6353f)
        // C 3.45545 16.3866 3.43227 16.1175 3.32551 15.8792
        curveTo(
          x1 = 3.45545f,
          y1 = 16.3866f,
          x2 = 3.43227f,
          y2 = 16.1175f,
          x3 = 3.32551f,
          y3 = 15.8792f,
        )
        // C 2.79502 14.6947 2.5 13.3818 2.5 12
        curveTo(
          x1 = 2.79502f,
          y1 = 14.6947f,
          x2 = 2.5f,
          y2 = 13.3818f,
          x3 = 2.5f,
          y3 = 12.0f,
        )
        // C 2.5 6.75329 6.7533 2.5 12 2.5
        curveTo(
          x1 = 2.5f,
          y1 = 6.75329f,
          x2 = 6.7533f,
          y2 = 2.5f,
          x3 = 12.0f,
          y3 = 2.5f,
        )
        // C 17.2467 2.5 21.5 6.75329 21.5 12z
        curveTo(
          x1 = 17.2467f,
          y1 = 2.5f,
          x2 = 21.5f,
          y2 = 6.75329f,
          x3 = 21.5f,
          y3 = 12.0f,
        )
        close()
      }
    }.build().also { _chat = it }
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
        imageVector = HedvigIcons.Chat,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chat: ImageVector? = null
