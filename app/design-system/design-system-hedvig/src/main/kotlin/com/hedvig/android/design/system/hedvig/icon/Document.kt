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
val HedvigIcons.Document: ImageVector
  get() {
    val current = _document
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.Document",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.5 3.75 A1.25 1.25 0 0 0 6.25 5 v14 a1.25 1.25 0 0 0 1.25 1.25 h9 A1.25 1.25 0 0 0 17.75 19 V5 a1.25 1.25 0 0 0 -1.25 -1.25z M4.75 5 A2.75 2.75 0 0 1 7.5 2.25 h9 A2.75 2.75 0 0 1 19.25 5 v14 a2.75 2.75 0 0 1 -2.75 2.75 h-9 A2.75 2.75 0 0 1 4.75 19z m11 5.5 A.75 .75 0 0 0 15 9.75 H9 a.75 .75 0 0 0 0 1.5 h6 a.75 .75 0 0 0 .75 -.75 M15 7.25 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 0 -1.5z M15.75 13 A.75 .75 0 0 0 15 12.25 H9 a.75 .75 0 0 0 0 1.5 h6 A.75 .75 0 0 0 15.75 13 m-4.25 1.75 a.75 .75 0 0 1 0 1.5 H9 a.75 .75 0 0 1 0 -1.5z
      path(
        fill = SolidColor(Color(0xFF121212)),
      ) {
        // M 7.5 3.75
        moveTo(x = 7.5f, y = 3.75f)
        // A 1.25 1.25 0 0 0 6.25 5
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 6.25f,
          y1 = 5.0f,
        )
        // v 14
        verticalLineToRelative(dy = 14.0f)
        // a 1.25 1.25 0 0 0 1.25 1.25
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 1.25f,
          dy1 = 1.25f,
        )
        // h 9
        horizontalLineToRelative(dx = 9.0f)
        // A 1.25 1.25 0 0 0 17.75 19
        arcTo(
          horizontalEllipseRadius = 1.25f,
          verticalEllipseRadius = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 17.75f,
          y1 = 19.0f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // a 1.25 1.25 0 0 0 -1.25 -1.25z
        arcToRelative(
          a = 1.25f,
          b = 1.25f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = -1.25f,
          dy1 = -1.25f,
        )
        close()
        // M 4.75 5
        moveTo(x = 4.75f, y = 5.0f)
        // A 2.75 2.75 0 0 1 7.5 2.25
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 7.5f,
          y1 = 2.25f,
        )
        // h 9
        horizontalLineToRelative(dx = 9.0f)
        // A 2.75 2.75 0 0 1 19.25 5
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 19.25f,
          y1 = 5.0f,
        )
        // v 14
        verticalLineToRelative(dy = 14.0f)
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
        // A 2.75 2.75 0 0 1 4.75 19z
        arcTo(
          horizontalEllipseRadius = 2.75f,
          verticalEllipseRadius = 2.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          x1 = 4.75f,
          y1 = 19.0f,
        )
        close()
        // m 11 5.5
        moveToRelative(dx = 11.0f, dy = 5.5f)
        // A 0.75 0.75 0 0 0 15 9.75
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 15.0f,
          y1 = 9.75f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
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
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // a 0.75 0.75 0 0 0 0.75 -0.75
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          dx1 = 0.75f,
          dy1 = -0.75f,
        )
        // M 15 7.25
        moveTo(x = 15.0f, y = 7.25f)
        // a 0.75 0.75 0 0 1 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // a 0.75 0.75 0 0 1 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
        // M 15.75 13
        moveTo(x = 15.75f, y = 13.0f)
        // A 0.75 0.75 0 0 0 15 12.25
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 15.0f,
          y1 = 12.25f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
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
        // h 6
        horizontalLineToRelative(dx = 6.0f)
        // A 0.75 0.75 0 0 0 15.75 13
        arcTo(
          horizontalEllipseRadius = 0.75f,
          verticalEllipseRadius = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = false,
          x1 = 15.75f,
          y1 = 13.0f,
        )
        // m -4.25 1.75
        moveToRelative(dx = -4.25f, dy = 1.75f)
        // a 0.75 0.75 0 0 1 0 1.5
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = 1.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // a 0.75 0.75 0 0 1 0 -1.5z
        arcToRelative(
          a = 0.75f,
          b = 0.75f,
          theta = 0.0f,
          isMoreThanHalf = false,
          isPositiveArc = true,
          dx1 = 0.0f,
          dy1 = -1.5f,
        )
        close()
      }
    }.build().also { _document = it }
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
        imageVector = Document,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _document: ImageVector? = null
