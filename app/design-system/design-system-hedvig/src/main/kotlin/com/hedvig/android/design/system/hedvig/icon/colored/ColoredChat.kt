package com.hedvig.android.design.system.hedvig.icon.colored

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
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.ColoredChat: ImageVector
  get() {
    val current = _chat
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.colored.Chat",
      defaultWidth = 40.0.dp,
      defaultHeight = 40.0.dp,
      viewportWidth = 40.0f,
      viewportHeight = 40.0f,
    ).apply {
      // M20 4 A16 16 0 1 0 20 36 16 16 0 1 0 20 4z
      path(
        fill = SolidColor(Color(0xFF51BFFB)),
      ) {
        // M 20 4
        moveTo(x = 20.0f, y = 4.0f)
        // A 16 16 0 1 0 20 36
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 36.0f,
        )
        // A 16 16 0 1 0 20 4z
        arcTo(
          horizontalEllipseRadius = 16.0f,
          verticalEllipseRadius = 16.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          x1 = 20.0f,
          y1 = 4.0f,
        )
        close()
      }
      // M28.5 20 a8.5 8.5 0 0 1 -11.93 7.78 1 1 0 0 0 -.76 -.04 l-3.42 1.13 a1 1 0 0 1 -1.26 -1.26 l1.13 -3.42 a1 1 0 0 0 -.04 -.76 A8.5 8.5 0 1 1 28.5 20
      path(
        fill = SolidColor(Color(0xFFFAFAFA)),
      ) {
        // M 28.5 20
        moveTo(x = 28.5f, y = 20.0f)
        // a 8.5 8.5 0 0 1 -11.93 7.78
        arcToRelative(
          a = 8.5f,
          b = 8.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -11.93f,
          dy1 = 7.78f,
        )
        // a 1 1 0 0 0 -0.76 -0.04
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.76f,
          dy1 = -0.04f,
        )
        // l -3.42 1.13
        lineToRelative(dx = -3.42f, dy = 1.13f)
        // a 1 1 0 0 1 -1.26 -1.26
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.26f,
          dy1 = -1.26f,
        )
        // l 1.13 -3.42
        lineToRelative(dx = 1.13f, dy = -3.42f)
        // a 1 1 0 0 0 -0.04 -0.76
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.04f,
          dy1 = -0.76f,
        )
        // A 8.5 8.5 0 1 1 28.5 20
        arcTo(
          horizontalEllipseRadius = 8.5f,
          verticalEllipseRadius = 8.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          x1 = 28.5f,
          y1 = 20.0f,
        )
      }
    }.build().also { _chat = it }
  }

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.ColoredChat,
        contentDescription = null,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _chat: ImageVector? = null
