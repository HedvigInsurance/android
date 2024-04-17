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
val HedvigIcons.Search: ImageVector
  get() {
    val current = _search
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Search",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M14 9.5 a4.5 4.5 0 1 1 -9 0 4.5 4.5 0 0 1 9 0 m-.82 4.74 a6 6 0 1 1 1.06 -1.06 l5.79 5.79 a.75 .75 0 0 1 -1.06 1.06z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 14 9.5
        moveTo(x = 14.0f, y = 9.5f)
        // a 4.5 4.5 0 1 1 -9 0
        arcToRelative(
          a = 4.5f,
          b = 4.5f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -9.0f,
          dy1 = 0.0f,
        )
        // a 4.5 4.5 0 0 1 9 0
        arcToRelative(
          a = 4.5f,
          b = 4.5f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 9.0f,
          dy1 = 0.0f,
        )
        // m -0.82 4.74
        moveToRelative(dx = -0.82f, dy = 4.74f)
        // a 6 6 0 1 1 1.06 -1.06
        arcToRelative(
          a = 6.0f,
          b = 6.0f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l 5.79 5.79
        lineToRelative(dx = 5.79f, dy = 5.79f)
        // a 0.75 0.75 0 0 1 -1.06 1.06z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = 1.06f,
        )
        close()
      }
    }.build().also { _search = it }
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
        imageVector = Search,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _search: ImageVector? = null
