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
val HedvigIcons.Lock: ImageVector
  get() {
    val current = _lock
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Lock",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 5.25 c1.8 0 3.25 1.46 3.25 3.25 v1.25 h-6.5 V8.5 c0 -1.8 1.46 -3.25 3.25 -3.25 M7.25 9.76 V8.5 a4.75 4.75 0 0 1 9.5 0 v1.26 a2.75 2.75 0 0 1 2.5 2.74 v5 a2.75 2.75 0 0 1 -2.75 2.75 h-9 a2.75 2.75 0 0 1 -2.75 -2.75 v-5 c0 -1.43 1.1 -2.61 2.5 -2.74
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 5.25
        moveTo(x = 12.0f, y = 5.25f)
        // c 1.8 0 3.25 1.46 3.25 3.25
        curveToRelative(
          dx1 = 1.8f,
          dy1 = 0.0f,
          dx2 = 3.25f,
          dy2 = 1.46f,
          dx3 = 3.25f,
          dy3 = 3.25f,
        )
        // v 1.25
        verticalLineToRelative(dy = 1.25f)
        // h -6.5
        horizontalLineToRelative(dx = -6.5f)
        // V 8.5
        verticalLineTo(y = 8.5f)
        // c 0 -1.8 1.46 -3.25 3.25 -3.25
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -1.8f,
          dx2 = 1.46f,
          dy2 = -3.25f,
          dx3 = 3.25f,
          dy3 = -3.25f,
        )
        // M 7.25 9.76
        moveTo(x = 7.25f, y = 9.76f)
        // V 8.5
        verticalLineTo(y = 8.5f)
        // a 4.75 4.75 0 0 1 9.5 0
        arcToRelative(
          a = 4.75f,
          b = 4.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 9.5f,
          dy1 = 0.0f,
        )
        // v 1.26
        verticalLineToRelative(dy = 1.26f)
        // a 2.75 2.75 0 0 1 2.5 2.74
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 2.5f,
          dy1 = 2.74f,
        )
        // v 5
        verticalLineToRelative(dy = 5.0f)
        // a 2.75 2.75 0 0 1 -2.75 2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.75f,
          dy1 = 2.75f,
        )
        // h -9
        horizontalLineToRelative(dx = -9.0f)
        // a 2.75 2.75 0 0 1 -2.75 -2.75
        arcToRelative(
          a = 2.75f,
          b = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = -2.75f,
          dy1 = -2.75f,
        )
        // v -5
        verticalLineToRelative(dy = -5.0f)
        // c 0 -1.43 1.1 -2.61 2.5 -2.74
        curveToRelative(
          dx1 = 0.0f,
          dy1 = -1.43f,
          dx2 = 1.1f,
          dy2 = -2.61f,
          dx3 = 2.5f,
          dy3 = -2.74f,
        )
      }
    }.build().also { _lock = it }
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
        imageVector = HedvigIcons.Lock,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _lock: ImageVector? = null
