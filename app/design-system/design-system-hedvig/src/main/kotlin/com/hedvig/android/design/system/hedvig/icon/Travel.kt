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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme

@Suppress("UnusedReceiverParameter")
val HedvigIcons.Travel: ImageVector
  get() {
    val current = _travel
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Travel",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9.25 4 C9.25 3.0335 10.0335 2.25 11 2.25 H13 C13.9665 2.25 14.75 3.0335 14.75 4 V5.25 H16.5 C18.0188 5.25 19.25 6.48122 19.25 8 V18 C19.25 19.5188 18.0188 20.75 16.5 20.75 H16 C16 21.3023 15.5523 21.75 15 21.75 C14.4477 21.75 14 21.3023 14 20.75 H10 C10 21.3023 9.55229 21.75 9 21.75 C8.44771 21.75 8 21.3023 8 20.75 H7.5 C5.98122 20.75 4.75 19.5188 4.75 18 V8 C4.75 6.48122 5.98122 5.25 7.5 5.25 H9.25 V4Z M13.25 4 V5.25 H10.75 V4 C10.75 3.86193 10.8619 3.75 11 3.75 H13 C13.1381 3.75 13.25 3.86193 13.25 4Z M7.5 6.75 C6.80964 6.75 6.25 7.30964 6.25 8 V18 C6.25 18.6904 6.80964 19.25 7.5 19.25 H16.5 C17.1904 19.25 17.75 18.6904 17.75 18 V8 C17.75 7.30964 17.1904 6.75 16.5 6.75 H7.5Z M11.25 9 C11.25 8.58579 11.5858 8.25 12 8.25 C12.4142 8.25 12.75 8.58579 12.75 9 V17 C12.75 17.4142 12.4142 17.75 12 17.75 C11.5858 17.75 11.25 17.4142 11.25 17 V9Z M9 8.25 C8.58579 8.25 8.25 8.58579 8.25 9 V17 C8.25 17.4142 8.58579 17.75 9 17.75 C9.41421 17.75 9.75 17.4142 9.75 17 V9 C9.75 8.58579 9.41421 8.25 9 8.25Z M14.25 9 C14.25 8.58579 14.5858 8.25 15 8.25 C15.4142 8.25 15.75 8.58579 15.75 9 V17 C15.75 17.4142 15.4142 17.75 15 17.75 C14.5858 17.75 14.25 17.4142 14.25 17 V9Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 9.25 4
        moveTo(x = 9.25f, y = 4.0f)
        // C 9.25 3.0335 10.0335 2.25 11 2.25
        curveTo(
          x1 = 9.25f,
          y1 = 3.0335f,
          x2 = 10.0335f,
          y2 = 2.25f,
          x3 = 11.0f,
          y3 = 2.25f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.9665 2.25 14.75 3.0335 14.75 4
        curveTo(
          x1 = 13.9665f,
          y1 = 2.25f,
          x2 = 14.75f,
          y2 = 3.0335f,
          x3 = 14.75f,
          y3 = 4.0f,
        )
        // V 5.25
        verticalLineTo(y = 5.25f)
        // H 16.5
        horizontalLineTo(x = 16.5f)
        // C 18.0188 5.25 19.25 6.48122 19.25 8
        curveTo(
          x1 = 18.0188f,
          y1 = 5.25f,
          x2 = 19.25f,
          y2 = 6.48122f,
          x3 = 19.25f,
          y3 = 8.0f,
        )
        // V 18
        verticalLineTo(y = 18.0f)
        // C 19.25 19.5188 18.0188 20.75 16.5 20.75
        curveTo(
          x1 = 19.25f,
          y1 = 19.5188f,
          x2 = 18.0188f,
          y2 = 20.75f,
          x3 = 16.5f,
          y3 = 20.75f,
        )
        // H 16
        horizontalLineTo(x = 16.0f)
        // C 16 21.3023 15.5523 21.75 15 21.75
        curveTo(
          x1 = 16.0f,
          y1 = 21.3023f,
          x2 = 15.5523f,
          y2 = 21.75f,
          x3 = 15.0f,
          y3 = 21.75f,
        )
        // C 14.4477 21.75 14 21.3023 14 20.75
        curveTo(
          x1 = 14.4477f,
          y1 = 21.75f,
          x2 = 14.0f,
          y2 = 21.3023f,
          x3 = 14.0f,
          y3 = 20.75f,
        )
        // H 10
        horizontalLineTo(x = 10.0f)
        // C 10 21.3023 9.55229 21.75 9 21.75
        curveTo(
          x1 = 10.0f,
          y1 = 21.3023f,
          x2 = 9.55229f,
          y2 = 21.75f,
          x3 = 9.0f,
          y3 = 21.75f,
        )
        // C 8.44771 21.75 8 21.3023 8 20.75
        curveTo(
          x1 = 8.44771f,
          y1 = 21.75f,
          x2 = 8.0f,
          y2 = 21.3023f,
          x3 = 8.0f,
          y3 = 20.75f,
        )
        // H 7.5
        horizontalLineTo(x = 7.5f)
        // C 5.98122 20.75 4.75 19.5188 4.75 18
        curveTo(
          x1 = 5.98122f,
          y1 = 20.75f,
          x2 = 4.75f,
          y2 = 19.5188f,
          x3 = 4.75f,
          y3 = 18.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // C 4.75 6.48122 5.98122 5.25 7.5 5.25
        curveTo(
          x1 = 4.75f,
          y1 = 6.48122f,
          x2 = 5.98122f,
          y2 = 5.25f,
          x3 = 7.5f,
          y3 = 5.25f,
        )
        // H 9.25
        horizontalLineTo(x = 9.25f)
        // V 4z
        verticalLineTo(y = 4.0f)
        close()
        // M 13.25 4
        moveTo(x = 13.25f, y = 4.0f)
        // V 5.25
        verticalLineTo(y = 5.25f)
        // H 10.75
        horizontalLineTo(x = 10.75f)
        // V 4
        verticalLineTo(y = 4.0f)
        // C 10.75 3.86193 10.8619 3.75 11 3.75
        curveTo(
          x1 = 10.75f,
          y1 = 3.86193f,
          x2 = 10.8619f,
          y2 = 3.75f,
          x3 = 11.0f,
          y3 = 3.75f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.1381 3.75 13.25 3.86193 13.25 4z
        curveTo(
          x1 = 13.1381f,
          y1 = 3.75f,
          x2 = 13.25f,
          y2 = 3.86193f,
          x3 = 13.25f,
          y3 = 4.0f,
        )
        close()
        // M 7.5 6.75
        moveTo(x = 7.5f, y = 6.75f)
        // C 6.80964 6.75 6.25 7.30964 6.25 8
        curveTo(
          x1 = 6.80964f,
          y1 = 6.75f,
          x2 = 6.25f,
          y2 = 7.30964f,
          x3 = 6.25f,
          y3 = 8.0f,
        )
        // V 18
        verticalLineTo(y = 18.0f)
        // C 6.25 18.6904 6.80964 19.25 7.5 19.25
        curveTo(
          x1 = 6.25f,
          y1 = 18.6904f,
          x2 = 6.80964f,
          y2 = 19.25f,
          x3 = 7.5f,
          y3 = 19.25f,
        )
        // H 16.5
        horizontalLineTo(x = 16.5f)
        // C 17.1904 19.25 17.75 18.6904 17.75 18
        curveTo(
          x1 = 17.1904f,
          y1 = 19.25f,
          x2 = 17.75f,
          y2 = 18.6904f,
          x3 = 17.75f,
          y3 = 18.0f,
        )
        // V 8
        verticalLineTo(y = 8.0f)
        // C 17.75 7.30964 17.1904 6.75 16.5 6.75
        curveTo(
          x1 = 17.75f,
          y1 = 7.30964f,
          x2 = 17.1904f,
          y2 = 6.75f,
          x3 = 16.5f,
          y3 = 6.75f,
        )
        // H 7.5z
        horizontalLineTo(x = 7.5f)
        close()
        // M 11.25 9
        moveTo(x = 11.25f, y = 9.0f)
        // C 11.25 8.58579 11.5858 8.25 12 8.25
        curveTo(
          x1 = 11.25f,
          y1 = 8.58579f,
          x2 = 11.5858f,
          y2 = 8.25f,
          x3 = 12.0f,
          y3 = 8.25f,
        )
        // C 12.4142 8.25 12.75 8.58579 12.75 9
        curveTo(
          x1 = 12.4142f,
          y1 = 8.25f,
          x2 = 12.75f,
          y2 = 8.58579f,
          x3 = 12.75f,
          y3 = 9.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 12.75 17.4142 12.4142 17.75 12 17.75
        curveTo(
          x1 = 12.75f,
          y1 = 17.4142f,
          x2 = 12.4142f,
          y2 = 17.75f,
          x3 = 12.0f,
          y3 = 17.75f,
        )
        // C 11.5858 17.75 11.25 17.4142 11.25 17
        curveTo(
          x1 = 11.5858f,
          y1 = 17.75f,
          x2 = 11.25f,
          y2 = 17.4142f,
          x3 = 11.25f,
          y3 = 17.0f,
        )
        // V 9z
        verticalLineTo(y = 9.0f)
        close()
        // M 9 8.25
        moveTo(x = 9.0f, y = 8.25f)
        // C 8.58579 8.25 8.25 8.58579 8.25 9
        curveTo(
          x1 = 8.58579f,
          y1 = 8.25f,
          x2 = 8.25f,
          y2 = 8.58579f,
          x3 = 8.25f,
          y3 = 9.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 8.25 17.4142 8.58579 17.75 9 17.75
        curveTo(
          x1 = 8.25f,
          y1 = 17.4142f,
          x2 = 8.58579f,
          y2 = 17.75f,
          x3 = 9.0f,
          y3 = 17.75f,
        )
        // C 9.41421 17.75 9.75 17.4142 9.75 17
        curveTo(
          x1 = 9.41421f,
          y1 = 17.75f,
          x2 = 9.75f,
          y2 = 17.4142f,
          x3 = 9.75f,
          y3 = 17.0f,
        )
        // V 9
        verticalLineTo(y = 9.0f)
        // C 9.75 8.58579 9.41421 8.25 9 8.25z
        curveTo(
          x1 = 9.75f,
          y1 = 8.58579f,
          x2 = 9.41421f,
          y2 = 8.25f,
          x3 = 9.0f,
          y3 = 8.25f,
        )
        close()
        // M 14.25 9
        moveTo(x = 14.25f, y = 9.0f)
        // C 14.25 8.58579 14.5858 8.25 15 8.25
        curveTo(
          x1 = 14.25f,
          y1 = 8.58579f,
          x2 = 14.5858f,
          y2 = 8.25f,
          x3 = 15.0f,
          y3 = 8.25f,
        )
        // C 15.4142 8.25 15.75 8.58579 15.75 9
        curveTo(
          x1 = 15.4142f,
          y1 = 8.25f,
          x2 = 15.75f,
          y2 = 8.58579f,
          x3 = 15.75f,
          y3 = 9.0f,
        )
        // V 17
        verticalLineTo(y = 17.0f)
        // C 15.75 17.4142 15.4142 17.75 15 17.75
        curveTo(
          x1 = 15.75f,
          y1 = 17.4142f,
          x2 = 15.4142f,
          y2 = 17.75f,
          x3 = 15.0f,
          y3 = 17.75f,
        )
        // C 14.5858 17.75 14.25 17.4142 14.25 17
        curveTo(
          x1 = 14.5858f,
          y1 = 17.75f,
          x2 = 14.25f,
          y2 = 17.4142f,
          x3 = 14.25f,
          y3 = 17.0f,
        )
        // V 9z
        verticalLineTo(y = 9.0f)
        close()
      }
    }.build().also { _travel = it }
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
        imageVector = HedvigIcons.Travel,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _travel: ImageVector? = null
