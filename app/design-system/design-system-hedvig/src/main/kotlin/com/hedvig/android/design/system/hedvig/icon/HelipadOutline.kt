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
val HedvigIcons.HelipadOutline: ImageVector
  get() {
    val current = _helipadOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.HelipadOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M20 12 a8 8 0 1 1 -16 0 8 8 0 0 1 16 0 m1.5 0 a9.5 9.5 0 1 1 -19 0 9.5 9.5 0 0 1 19 0 M9.75 7.5 h-1.5 v9 h1.5 v-3.75 h4.5 v3.75 h1.5 v-9 h-1.5 v3.75 h-4.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 20 12
        moveTo(x = 20.0f, y = 12.0f)
        // a 8 8 0 1 1 -16 0
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -16.0f,
          dy1 = 0.0f,
        )
        // a 8 8 0 0 1 16 0
        arcToRelative(
          a = 8.0f,
          b = 8.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 16.0f,
          dy1 = 0.0f,
        )
        // m 1.5 0
        moveToRelative(dx = 1.5f, dy = 0.0f)
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
        // M 9.75 7.5
        moveTo(x = 9.75f, y = 7.5f)
        // h -1.5
        horizontalLineToRelative(dx = -1.5f)
        // v 9
        verticalLineToRelative(dy = 9.0f)
        // h 1.5
        horizontalLineToRelative(dx = 1.5f)
        // v -3.75
        verticalLineToRelative(dy = -3.75f)
        // h 4.5
        horizontalLineToRelative(dx = 4.5f)
        // v 3.75
        verticalLineToRelative(dy = 3.75f)
        // h 1.5
        horizontalLineToRelative(dx = 1.5f)
        // v -9
        verticalLineToRelative(dy = -9.0f)
        // h -1.5
        horizontalLineToRelative(dx = -1.5f)
        // v 3.75
        verticalLineToRelative(dy = 3.75f)
        // h -4.5z
        horizontalLineToRelative(dx = -4.5f)
        close()
      }
    }.build().also { _helipadOutline = it }
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
        imageVector = HedvigIcons.HelipadOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _helipadOutline: ImageVector? = null
