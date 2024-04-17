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
val HedvigIcons.EQ: ImageVector
  get() {
    val current = _eQ
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.EQ",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M10.5 7 a1 1 0 0 1 1 -1 h1 a1 1 0 0 1 1 1 v10 a1 1 0 0 1 -1 1 h-1 a1 1 0 0 1 -1 -1z m-5 6 a1 1 0 0 1 1 -1 h1 a1 1 0 0 1 1 1 v4 a1 1 0 0 1 -1 1 h-1 a1 1 0 0 1 -1 -1z m11 -3 a1 1 0 0 0 -1 1 v6 a1 1 0 0 0 1 1 h1 a1 1 0 0 0 1 -1 v-6 a1 1 0 0 0 -1 -1z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 10.5 7
        moveTo(x = 10.5f, y = 7.0f)
        // a 1 1 0 0 1 1 -1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.0f,
          dy1 = -1.0f,
        )
        // h 1
        horizontalLineToRelative(dx = 1.0f)
        // a 1 1 0 0 1 1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.0f,
          dy1 = 1.0f,
        )
        // v 10
        verticalLineToRelative(dy = 10.0f)
        // a 1 1 0 0 1 -1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.0f,
          dy1 = 1.0f,
        )
        // h -1
        horizontalLineToRelative(dx = -1.0f)
        // a 1 1 0 0 1 -1 -1z
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.0f,
          dy1 = -1.0f,
        )
        close()
        // m -5 6
        moveToRelative(dx = -5.0f, dy = 6.0f)
        // a 1 1 0 0 1 1 -1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.0f,
          dy1 = -1.0f,
        )
        // h 1
        horizontalLineToRelative(dx = 1.0f)
        // a 1 1 0 0 1 1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.0f,
          dy1 = 1.0f,
        )
        // v 4
        verticalLineToRelative(dy = 4.0f)
        // a 1 1 0 0 1 -1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.0f,
          dy1 = 1.0f,
        )
        // h -1
        horizontalLineToRelative(dx = -1.0f)
        // a 1 1 0 0 1 -1 -1z
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -1.0f,
          dy1 = -1.0f,
        )
        close()
        // m 11 -3
        moveToRelative(dx = 11.0f, dy = -3.0f)
        // a 1 1 0 0 0 -1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = 1.0f,
        )
        // v 6
        verticalLineToRelative(dy = 6.0f)
        // a 1 1 0 0 0 1 1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = 1.0f,
        )
        // h 1
        horizontalLineToRelative(dx = 1.0f)
        // a 1 1 0 0 0 1 -1
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.0f,
          dy1 = -1.0f,
        )
        // v -6
        verticalLineToRelative(dy = -6.0f)
        // a 1 1 0 0 0 -1 -1z
        arcToRelative(
          a = 1.0f,
          b = 1.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.0f,
          dy1 = -1.0f,
        )
        close()
      }
    }.build().also { _eQ = it }
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
        imageVector = HedvigIcons.EQ,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _eQ: ImageVector? = null
