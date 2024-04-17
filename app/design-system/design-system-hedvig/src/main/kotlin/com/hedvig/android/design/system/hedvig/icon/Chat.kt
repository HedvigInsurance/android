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
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Chat: ImageVector
  get() {
    val current = _chat
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Chat",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.5 12 a9.5 9.5 0 0 1 -13.38 8.67 1 1 0 0 0 -.76 -.05 l-4.01 1.29 a1 1 0 0 1 -1.26 -1.26 l1.29 -4.01 a1 1 0 0 0 -.05 -.76 A9.5 9.5 0 1 1 21.5 12
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.5 12
        moveTo(x = 21.5f, y = 12.0f)
        // a 9.5 9.5 0 0 1 -13.38 8.67
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -13.38f,
          dy1 = 8.67f,
        )
        // a 1 1 0 0 0 -0.76 -0.05
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.76f,
          dy1 = -0.05f,
        )
        // l -4.01 1.29
        lineToRelative(dx = -4.01f, dy = 1.29f)
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
        // l 1.29 -4.01
        lineToRelative(dx = 1.29f, dy = -4.01f)
        // a 1 1 0 0 0 -0.05 -0.76
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -0.05f,
          dy1 = -0.76f,
        )
        // A 9.5 9.5 0 1 1 21.5 12
        arcTo(
          horizontalEllipseRadius = 9.5f,
          verticalEllipseRadius = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          x1 = 21.5f,
          y1 = 12.0f,
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
