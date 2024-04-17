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

val Plus: ImageVector
  get() {
    val current = _plus
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Plus",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M18 12.75 a.75 .75 0 0 0 0 -1.5 h-5.25 V6 a.75 .75 0 0 0 -1.5 0 v5.25 H6 a.75 .75 0 0 0 0 1.5 h5.25 V18 a.75 .75 0 0 0 1.5 0 v-5.25z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 18 12.75
        moveTo(x = 18.0f, y = 12.75f)
        // a 0.75 0.75 0 0 0 0 -1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        // h -5.25
        horizontalLineToRelative(dx = -5.25f)
        // V 6
        verticalLineTo(y = 6.0f)
        // a 0.75 0.75 0 0 0 -1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.5f,
          dy1 = 0.0f,
        )
        // v 5.25
        verticalLineToRelative(dy = 5.25f)
        // H 6
        horizontalLineTo(x = 6.0f)
        // a 0.75 0.75 0 0 0 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // h 5.25
        horizontalLineToRelative(dx = 5.25f)
        // V 18
        verticalLineTo(y = 18.0f)
        // a 0.75 0.75 0 0 0 1.5 0
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.5f,
          dy1 = 0.0f,
        )
        // v -5.25z
        verticalLineToRelative(dy = -5.25f)
        close()
      }
    }.build().also { _plus = it }
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
        imageVector = Plus,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _plus: ImageVector? = null
