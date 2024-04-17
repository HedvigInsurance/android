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
val HedvigIcons.Reload: ImageVector
  get() {
    val current = _reload
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Reload",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M12 4.25 h.69 l-1.22 -1.22 a.75 .75 0 0 1 1.06 -1.06 l2.15 2.15 a1.25 1.25 0 0 1 0 1.76 l-2.15 2.15 a.75 .75 0 1 1 -1.06 -1.06 l1.22 -1.22 H12 a6.25 6.25 0 0 0 -5.41 9.37 .75 .75 0 1 1 -1.3 .76 A7.75 7.75 0 0 1 12 4.25 M18.25 12 c0 3.45 -2.8 6.25 -6.25 6.25 h-.69 l1.22 -1.22 a.75 .75 0 1 0 -1.06 -1.06 l-2.15 2.15 a1.25 1.25 0 0 0 0 1.76 l2.15 2.15 a.75 .75 0 1 0 1.06 -1.06 l-1.22 -1.22 H12 a7.75 7.75 0 0 0 6.71 -11.63 .75 .75 0 1 0 -1.3 .76 A6 6 0 0 1 18.25 12
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 12 4.25
        moveTo(x = 12.0f, y = 4.25f)
        // h 0.69
        horizontalLineToRelative(dx = 0.69f)
        // l -1.22 -1.22
        lineToRelative(dx = -1.22f, dy = -1.22f)
        // a 0.75 0.75 0 0 1 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l 2.15 2.15
        lineToRelative(dx = 2.15f, dy = 2.15f)
        // a 1.25 1.25 0 0 1 0 1.76
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.76f,
        )
        // l -2.15 2.15
        lineToRelative(dx = -2.15f, dy = 2.15f)
        // a 0.75 0.75 0 1 1 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l 1.22 -1.22
        lineToRelative(dx = 1.22f, dy = -1.22f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // a 6.25 6.25 0 0 0 -5.41 9.37
        arcToRelative(
          a = 6.25f,
          b = 6.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -5.41f,
          dy1 = 9.37f,
        )
        // a 0.75 0.75 0 1 1 -1.3 0.76
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = true,
          dx1 = -1.3f,
          dy1 = 0.76f,
        )
        // A 7.75 7.75 0 0 1 12 4.25
        arcTo(
          horizontalEllipseRadius = 7.75f,
          verticalEllipseRadius = 7.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 12.0f,
          y1 = 4.25f,
        )
        // M 18.25 12
        moveTo(x = 18.25f, y = 12.0f)
        // c 0 3.45 -2.8 6.25 -6.25 6.25
        curveToRelative(
          dx1 = 0.0f,
          dy1 = 3.45f,
          dx2 = -2.8f,
          dy2 = 6.25f,
          dx3 = -6.25f,
          dy3 = 6.25f,
        )
        // h -0.69
        horizontalLineToRelative(dx = -0.69f)
        // l 1.22 -1.22
        lineToRelative(dx = 1.22f, dy = -1.22f)
        // a 0.75 0.75 0 1 0 -1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.06f,
          dy1 = -1.06f,
        )
        // l -2.15 2.15
        lineToRelative(dx = -2.15f, dy = 2.15f)
        // a 1.25 1.25 0 0 0 0 1.76
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.0f,
          dy1 = 1.76f,
        )
        // l 2.15 2.15
        lineToRelative(dx = 2.15f, dy = 2.15f)
        // a 0.75 0.75 0 1 0 1.06 -1.06
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = 1.06f,
          dy1 = -1.06f,
        )
        // l -1.22 -1.22
        lineToRelative(dx = -1.22f, dy = -1.22f)
        // H 12
        horizontalLineTo(x = 12.0f)
        // a 7.75 7.75 0 0 0 6.71 -11.63
        arcToRelative(
          a = 7.75f,
          b = 7.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 6.71f,
          dy1 = -11.63f,
        )
        // a 0.75 0.75 0 1 0 -1.3 0.76
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = true,
          isPositiveArc = false,
          dx1 = -1.3f,
          dy1 = 0.76f,
        )
        // A 6 6 0 0 1 18.25 12
        arcTo(
          horizontalEllipseRadius = 6.0f,
          verticalEllipseRadius = 6.0f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 18.25f,
          y1 = 12.0f,
        )
      }
    }.build().also { _reload = it }
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
        imageVector = HedvigIcons.Reload,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _reload: ImageVector? = null
