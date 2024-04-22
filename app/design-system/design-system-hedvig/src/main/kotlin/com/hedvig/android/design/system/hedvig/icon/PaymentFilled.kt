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
val HedvigIcons.PaymentFilled: ImageVector
  get() {
    val current = _paymentFilled
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.PaymentFilled",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M7.5 2.25 C5.98122 2.25 4.75 3.48122 4.75 5 V18.8484 C4.75 20.8642 6.84702 22.195 8.67094 21.3367 L9.24153 21.0682 C9.57008 20.9136 9.94963 20.9095 10.2814 21.0569 L10.8831 21.3243 C11.5942 21.6404 12.4058 21.6404 13.1169 21.3243 L13.7186 21.0569 C14.0504 20.9095 14.4299 20.9136 14.7585 21.0682 L15.3291 21.3367 C17.153 22.195 19.25 20.8642 19.25 18.8484 V5 C19.25 3.48122 18.0188 2.25 16.5 2.25 H7.5Z M15.75 8.25 C15.75 7.83579 15.4142 7.5 15 7.5 H9 C8.58579 7.5 8.25 7.83579 8.25 8.25 C8.25 8.66421 8.58579 9 9 9 H15 C15.4142 9 15.75 8.66421 15.75 8.25Z M13.75 10.75 C13.75 10.3358 13.4142 10 13 10 H9 C8.58579 10 8.25 10.3358 8.25 10.75 C8.25 11.1642 8.58579 11.5 9 11.5 H13 C13.4142 11.5 13.75 11.1642 13.75 10.75Z M15 12.5 C15.4142 12.5 15.75 12.8358 15.75 13.25 C15.75 13.6642 15.4142 14 15 14 H9 C8.58579 14 8.25 13.6642 8.25 13.25 C8.25 12.8358 8.58579 12.5 9 12.5 H15Z M13.75 15.75 C13.75 15.3358 13.4142 15 13 15 H9 C8.58579 15 8.25 15.3358 8.25 15.75 C8.25 16.1642 8.58579 16.5 9 16.5 H13 C13.4142 16.5 13.75 16.1642 13.75 15.75Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 7.5 2.25
        moveTo(x = 7.5f, y = 2.25f)
        // C 5.98122 2.25 4.75 3.48122 4.75 5
        curveTo(
          x1 = 5.98122f,
          y1 = 2.25f,
          x2 = 4.75f,
          y2 = 3.48122f,
          x3 = 4.75f,
          y3 = 5.0f,
        )
        // V 18.8484
        verticalLineTo(y = 18.8484f)
        // C 4.75 20.8642 6.84702 22.195 8.67094 21.3367
        curveTo(
          x1 = 4.75f,
          y1 = 20.8642f,
          x2 = 6.84702f,
          y2 = 22.195f,
          x3 = 8.67094f,
          y3 = 21.3367f,
        )
        // L 9.24153 21.0682
        lineTo(x = 9.24153f, y = 21.0682f)
        // C 9.57008 20.9136 9.94963 20.9095 10.2814 21.0569
        curveTo(
          x1 = 9.57008f,
          y1 = 20.9136f,
          x2 = 9.94963f,
          y2 = 20.9095f,
          x3 = 10.2814f,
          y3 = 21.0569f,
        )
        // L 10.8831 21.3243
        lineTo(x = 10.8831f, y = 21.3243f)
        // C 11.5942 21.6404 12.4058 21.6404 13.1169 21.3243
        curveTo(
          x1 = 11.5942f,
          y1 = 21.6404f,
          x2 = 12.4058f,
          y2 = 21.6404f,
          x3 = 13.1169f,
          y3 = 21.3243f,
        )
        // L 13.7186 21.0569
        lineTo(x = 13.7186f, y = 21.0569f)
        // C 14.0504 20.9095 14.4299 20.9136 14.7585 21.0682
        curveTo(
          x1 = 14.0504f,
          y1 = 20.9095f,
          x2 = 14.4299f,
          y2 = 20.9136f,
          x3 = 14.7585f,
          y3 = 21.0682f,
        )
        // L 15.3291 21.3367
        lineTo(x = 15.3291f, y = 21.3367f)
        // C 17.153 22.195 19.25 20.8642 19.25 18.8484
        curveTo(
          x1 = 17.153f,
          y1 = 22.195f,
          x2 = 19.25f,
          y2 = 20.8642f,
          x3 = 19.25f,
          y3 = 18.8484f,
        )
        // V 5
        verticalLineTo(y = 5.0f)
        // C 19.25 3.48122 18.0188 2.25 16.5 2.25
        curveTo(
          x1 = 19.25f,
          y1 = 3.48122f,
          x2 = 18.0188f,
          y2 = 2.25f,
          x3 = 16.5f,
          y3 = 2.25f,
        )
        // H 7.5z
        horizontalLineTo(x = 7.5f)
        close()
        // M 15.75 8.25
        moveTo(x = 15.75f, y = 8.25f)
        // C 15.75 7.83579 15.4142 7.5 15 7.5
        curveTo(
          x1 = 15.75f,
          y1 = 7.83579f,
          x2 = 15.4142f,
          y2 = 7.5f,
          x3 = 15.0f,
          y3 = 7.5f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 7.5 8.25 7.83579 8.25 8.25
        curveTo(
          x1 = 8.58579f,
          y1 = 7.5f,
          x2 = 8.25f,
          y2 = 7.83579f,
          x3 = 8.25f,
          y3 = 8.25f,
        )
        // C 8.25 8.66421 8.58579 9 9 9
        curveTo(
          x1 = 8.25f,
          y1 = 8.66421f,
          x2 = 8.58579f,
          y2 = 9.0f,
          x3 = 9.0f,
          y3 = 9.0f,
        )
        // H 15
        horizontalLineTo(x = 15.0f)
        // C 15.4142 9 15.75 8.66421 15.75 8.25z
        curveTo(
          x1 = 15.4142f,
          y1 = 9.0f,
          x2 = 15.75f,
          y2 = 8.66421f,
          x3 = 15.75f,
          y3 = 8.25f,
        )
        close()
        // M 13.75 10.75
        moveTo(x = 13.75f, y = 10.75f)
        // C 13.75 10.3358 13.4142 10 13 10
        curveTo(
          x1 = 13.75f,
          y1 = 10.3358f,
          x2 = 13.4142f,
          y2 = 10.0f,
          x3 = 13.0f,
          y3 = 10.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 10 8.25 10.3358 8.25 10.75
        curveTo(
          x1 = 8.58579f,
          y1 = 10.0f,
          x2 = 8.25f,
          y2 = 10.3358f,
          x3 = 8.25f,
          y3 = 10.75f,
        )
        // C 8.25 11.1642 8.58579 11.5 9 11.5
        curveTo(
          x1 = 8.25f,
          y1 = 11.1642f,
          x2 = 8.58579f,
          y2 = 11.5f,
          x3 = 9.0f,
          y3 = 11.5f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.4142 11.5 13.75 11.1642 13.75 10.75z
        curveTo(
          x1 = 13.4142f,
          y1 = 11.5f,
          x2 = 13.75f,
          y2 = 11.1642f,
          x3 = 13.75f,
          y3 = 10.75f,
        )
        close()
        // M 15 12.5
        moveTo(x = 15.0f, y = 12.5f)
        // C 15.4142 12.5 15.75 12.8358 15.75 13.25
        curveTo(
          x1 = 15.4142f,
          y1 = 12.5f,
          x2 = 15.75f,
          y2 = 12.8358f,
          x3 = 15.75f,
          y3 = 13.25f,
        )
        // C 15.75 13.6642 15.4142 14 15 14
        curveTo(
          x1 = 15.75f,
          y1 = 13.6642f,
          x2 = 15.4142f,
          y2 = 14.0f,
          x3 = 15.0f,
          y3 = 14.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 14 8.25 13.6642 8.25 13.25
        curveTo(
          x1 = 8.58579f,
          y1 = 14.0f,
          x2 = 8.25f,
          y2 = 13.6642f,
          x3 = 8.25f,
          y3 = 13.25f,
        )
        // C 8.25 12.8358 8.58579 12.5 9 12.5
        curveTo(
          x1 = 8.25f,
          y1 = 12.8358f,
          x2 = 8.58579f,
          y2 = 12.5f,
          x3 = 9.0f,
          y3 = 12.5f,
        )
        // H 15z
        horizontalLineTo(x = 15.0f)
        close()
        // M 13.75 15.75
        moveTo(x = 13.75f, y = 15.75f)
        // C 13.75 15.3358 13.4142 15 13 15
        curveTo(
          x1 = 13.75f,
          y1 = 15.3358f,
          x2 = 13.4142f,
          y2 = 15.0f,
          x3 = 13.0f,
          y3 = 15.0f,
        )
        // H 9
        horizontalLineTo(x = 9.0f)
        // C 8.58579 15 8.25 15.3358 8.25 15.75
        curveTo(
          x1 = 8.58579f,
          y1 = 15.0f,
          x2 = 8.25f,
          y2 = 15.3358f,
          x3 = 8.25f,
          y3 = 15.75f,
        )
        // C 8.25 16.1642 8.58579 16.5 9 16.5
        curveTo(
          x1 = 8.25f,
          y1 = 16.1642f,
          x2 = 8.58579f,
          y2 = 16.5f,
          x3 = 9.0f,
          y3 = 16.5f,
        )
        // H 13
        horizontalLineTo(x = 13.0f)
        // C 13.4142 16.5 13.75 16.1642 13.75 15.75z
        curveTo(
          x1 = 13.4142f,
          y1 = 16.5f,
          x2 = 13.75f,
          y2 = 16.1642f,
          x3 = 13.75f,
          y3 = 15.75f,
        )
        close()
      }
    }.build().also { _paymentFilled = it }
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
        imageVector = HedvigIcons.PaymentFilled,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _paymentFilled: ImageVector? = null
