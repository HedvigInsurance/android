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
val HedvigIcons.AttentionFilled: ImageVector
  get() {
    val current = _attentionFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.AttentionFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M21.5 12 a9.5 9.5 0 1 1 -19 0 9.5 9.5 0 0 1 19 0 M12 7.25 A.75 .75 0 0 1 12.75 8 v5 a.75 .75 0 0 1 -1.5 0 V8 A.75 .75 0 0 1 12 7.25 M12 17 a1 1 0 1 0 0 -2 1 1 0 0 0 0 2
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 21.5 12
        moveTo(x = 21.5f, y = 12.0f)
        // a 9.5 9.5 0 1 1 -19 0
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -19.0f,
          dy1 = 0.0f,
        )
        // a 9.5 9.5 0 0 1 19 0
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 19.0f,
          dy1 = 0.0f,
        )
        // M 12 7.25
        moveTo(x = 12.0f, y = 7.25f)
        // A 0.75 0.75 0 0 1 12.75 8
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.75f,
          y1 = 8.0f,
        )
        // v 5
        verticalLineToRelative(dy = 5.0f)
        // a 0.75 0.75 0 0 1 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // A 0.75 0.75 0 0 1 12 7.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 7.25f,
        )
        // M 12 17
        moveTo(x = 12.0f, y = 17.0f)
        // a 1 1 0 1 0 0 -2
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -2.0f,
        )
        // a 1 1 0 0 0 0 2
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 2.0f,
        )
      }
    }.build().also { _attentionFilled = it }
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
        imageVector = HedvigIcons.AttentionFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _attentionFilled: ImageVector? = null
