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
val HedvigIcons.Play: ImageVector
  get() {
    val current = _play
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.icon.Play",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M9 16.155 V7.84503 C9 7.59812 9.08247 7.39555 9.24742 7.23733 C9.41236 7.07911 9.6048 7 9.82473 7 C9.89344 7 9.96551 7.01019 10.0409 7.03057 C10.1164 7.05095 10.1883 7.08152 10.2569 7.12227 L16.6254 11.2862 C16.7503 11.3737 16.8439 11.4786 16.9064 11.6009 C16.9688 11.7231 17 11.8562 17 12 C17 12.1438 16.9688 12.2769 16.9064 12.3991 C16.8439 12.5214 16.7503 12.6263 16.6254 12.7138 L10.2569 16.8777 C10.1882 16.9185 10.116 16.949 10.0403 16.9694 C9.96468 16.9898 9.89248 17 9.82372 17 C9.60369 17 9.4114 16.9209 9.24685 16.7627 C9.08228 16.6044 9 16.4019 9 16.155Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 9 16.155
        moveTo(x = 9.0f, y = 16.155f)
        // V 7.84503
        verticalLineTo(y = 7.84503f)
        // C 9 7.59812 9.08247 7.39555 9.24742 7.23733
        curveTo(
          x1 = 9.0f,
          y1 = 7.59812f,
          x2 = 9.08247f,
          y2 = 7.39555f,
          x3 = 9.24742f,
          y3 = 7.23733f,
        )
        // C 9.41236 7.07911 9.6048 7 9.82473 7
        curveTo(
          x1 = 9.41236f,
          y1 = 7.07911f,
          x2 = 9.6048f,
          y2 = 7.0f,
          x3 = 9.82473f,
          y3 = 7.0f,
        )
        // C 9.89344 7 9.96551 7.01019 10.0409 7.03057
        curveTo(
          x1 = 9.89344f,
          y1 = 7.0f,
          x2 = 9.96551f,
          y2 = 7.01019f,
          x3 = 10.0409f,
          y3 = 7.03057f,
        )
        // C 10.1164 7.05095 10.1883 7.08152 10.2569 7.12227
        curveTo(
          x1 = 10.1164f,
          y1 = 7.05095f,
          x2 = 10.1883f,
          y2 = 7.08152f,
          x3 = 10.2569f,
          y3 = 7.12227f,
        )
        // L 16.6254 11.2862
        lineTo(x = 16.6254f, y = 11.2862f)
        // C 16.7503 11.3737 16.8439 11.4786 16.9064 11.6009
        curveTo(
          x1 = 16.7503f,
          y1 = 11.3737f,
          x2 = 16.8439f,
          y2 = 11.4786f,
          x3 = 16.9064f,
          y3 = 11.6009f,
        )
        // C 16.9688 11.7231 17 11.8562 17 12
        curveTo(
          x1 = 16.9688f,
          y1 = 11.7231f,
          x2 = 17.0f,
          y2 = 11.8562f,
          x3 = 17.0f,
          y3 = 12.0f,
        )
        // C 17 12.1438 16.9688 12.2769 16.9064 12.3991
        curveTo(
          x1 = 17.0f,
          y1 = 12.1438f,
          x2 = 16.9688f,
          y2 = 12.2769f,
          x3 = 16.9064f,
          y3 = 12.3991f,
        )
        // C 16.8439 12.5214 16.7503 12.6263 16.6254 12.7138
        curveTo(
          x1 = 16.8439f,
          y1 = 12.5214f,
          x2 = 16.7503f,
          y2 = 12.6263f,
          x3 = 16.6254f,
          y3 = 12.7138f,
        )
        // L 10.2569 16.8777
        lineTo(x = 10.2569f, y = 16.8777f)
        // C 10.1882 16.9185 10.116 16.949 10.0403 16.9694
        curveTo(
          x1 = 10.1882f,
          y1 = 16.9185f,
          x2 = 10.116f,
          y2 = 16.949f,
          x3 = 10.0403f,
          y3 = 16.9694f,
        )
        // C 9.96468 16.9898 9.89248 17 9.82372 17
        curveTo(
          x1 = 9.96468f,
          y1 = 16.9898f,
          x2 = 9.89248f,
          y2 = 17.0f,
          x3 = 9.82372f,
          y3 = 17.0f,
        )
        // C 9.60369 17 9.4114 16.9209 9.24685 16.7627
        curveTo(
          x1 = 9.60369f,
          y1 = 17.0f,
          x2 = 9.4114f,
          y2 = 16.9209f,
          x3 = 9.24685f,
          y3 = 16.7627f,
        )
        // C 9.08228 16.6044 9 16.4019 9 16.155z
        curveTo(
          x1 = 9.08228f,
          y1 = 16.6044f,
          x2 = 9.0f,
          y2 = 16.4019f,
          x3 = 9.0f,
          y3 = 16.155f,
        )
        close()
      }
    }.build().also { _play = it }
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
        imageVector = HedvigIcons.Play,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _play: ImageVector? = null
