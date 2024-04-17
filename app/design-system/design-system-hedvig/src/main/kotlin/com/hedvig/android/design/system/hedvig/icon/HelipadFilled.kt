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
val HedvigIcons.HelipadFilled: ImageVector
  get() {
    val current = _helipadFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.HelipadFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 21.5 a9.5 9.5 0 1 0 0 -19 9.5 9.5 0 0 0 0 19 m-2.25 -14 h-1.5 v9 h1.5 v-3.75 h4.5 v3.75 h1.5 v-9 h-1.5 v3.75 h-4.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 21.5
        moveTo(x = 12.0f, y = 21.5f)
        // a 9.5 9.5 0 1 0 0 -19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = -19.0f,
        )
        // a 9.5 9.5 0 0 0 0 19
        arcToRelative(
          a = 9.5f,
          b = 9.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 19.0f,
        )
        // m -2.25 -14
        moveToRelative(dx = -2.25f, dy = -14.0f)
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
    }.build().also { _helipadFilled = it }
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
        imageVector = HelipadFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _helipadFilled: ImageVector? = null
