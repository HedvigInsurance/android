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

@Suppress("UnusedReceiverParameter")
val HedvigIcons.WarningOutline: ImageVector
  get() {
    val current = _warningOutline
    if (current != null) return current

    return ImageVector.Builder(
      name = "com.hedvig.android.design.system.hedvig.HedvigTheme.WarningOutline",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    ).apply {
      // M19.7267 16.3103 L13.8368 5.59646 C13.0331 4.13451 10.9669 4.13451 10.1632 5.59646 L4.27331 16.3103 C3.47505 17.7624 4.5289 19.5 6.11015 19.5 H17.8899 C19.4711 19.5 20.5249 17.7624 19.7267 16.3103Z M15.1513 4.87384 C13.7778 2.37538 10.2222 2.37539 8.8487 4.87385 L2.95884 15.5877 C1.62394 18.0159 3.36142 21 6.11015 21 H17.8899 C20.6386 21 22.3761 18.0159 21.0412 15.5877 L15.1513 4.87384Z M12 7.75 C12.4142 7.75 12.75 8.08579 12.75 8.5 V13.5 C12.75 13.9142 12.4142 14.25 12 14.25 C11.5858 14.25 11.25 13.9142 11.25 13.5 V8.5 C11.25 8.08579 11.5858 7.75 12 7.75Z M12 17.5 C12.5523 17.5 13 17.0523 13 16.5 C13 15.9477 12.5523 15.5 12 15.5 C11.4477 15.5 11 15.9477 11 16.5 C11 17.0523 11.4477 17.5 12 17.5Z
      path(
        fill = SolidColor(Color(0xFF121212)),
        pathFillType = PathFillType.EvenOdd,
      ) {
        // M 19.7267 16.3103
        moveTo(x = 19.7267f, y = 16.3103f)
        // L 13.8368 5.59646
        lineTo(x = 13.8368f, y = 5.59646f)
        // C 13.0331 4.13451 10.9669 4.13451 10.1632 5.59646
        curveTo(
          x1 = 13.0331f,
          y1 = 4.13451f,
          x2 = 10.9669f,
          y2 = 4.13451f,
          x3 = 10.1632f,
          y3 = 5.59646f,
        )
        // L 4.27331 16.3103
        lineTo(x = 4.27331f, y = 16.3103f)
        // C 3.47505 17.7624 4.5289 19.5 6.11015 19.5
        curveTo(
          x1 = 3.47505f,
          y1 = 17.7624f,
          x2 = 4.5289f,
          y2 = 19.5f,
          x3 = 6.11015f,
          y3 = 19.5f,
        )
        // H 17.8899
        horizontalLineTo(x = 17.8899f)
        // C 19.4711 19.5 20.5249 17.7624 19.7267 16.3103z
        curveTo(
          x1 = 19.4711f,
          y1 = 19.5f,
          x2 = 20.5249f,
          y2 = 17.7624f,
          x3 = 19.7267f,
          y3 = 16.3103f,
        )
        close()
        // M 15.1513 4.87384
        moveTo(x = 15.1513f, y = 4.87384f)
        // C 13.7778 2.37538 10.2222 2.37539 8.8487 4.87385
        curveTo(
          x1 = 13.7778f,
          y1 = 2.37538f,
          x2 = 10.2222f,
          y2 = 2.37539f,
          x3 = 8.8487f,
          y3 = 4.87385f,
        )
        // L 2.95884 15.5877
        lineTo(x = 2.95884f, y = 15.5877f)
        // C 1.62394 18.0159 3.36142 21 6.11015 21
        curveTo(
          x1 = 1.62394f,
          y1 = 18.0159f,
          x2 = 3.36142f,
          y2 = 21.0f,
          x3 = 6.11015f,
          y3 = 21.0f,
        )
        // H 17.8899
        horizontalLineTo(x = 17.8899f)
        // C 20.6386 21 22.3761 18.0159 21.0412 15.5877
        curveTo(
          x1 = 20.6386f,
          y1 = 21.0f,
          x2 = 22.3761f,
          y2 = 18.0159f,
          x3 = 21.0412f,
          y3 = 15.5877f,
        )
        // L 15.1513 4.87384z
        lineTo(x = 15.1513f, y = 4.87384f)
        close()
        // M 12 7.75
        moveTo(x = 12.0f, y = 7.75f)
        // C 12.4142 7.75 12.75 8.08579 12.75 8.5
        curveTo(
          x1 = 12.4142f,
          y1 = 7.75f,
          x2 = 12.75f,
          y2 = 8.08579f,
          x3 = 12.75f,
          y3 = 8.5f,
        )
        // V 13.5
        verticalLineTo(y = 13.5f)
        // C 12.75 13.9142 12.4142 14.25 12 14.25
        curveTo(
          x1 = 12.75f,
          y1 = 13.9142f,
          x2 = 12.4142f,
          y2 = 14.25f,
          x3 = 12.0f,
          y3 = 14.25f,
        )
        // C 11.5858 14.25 11.25 13.9142 11.25 13.5
        curveTo(
          x1 = 11.5858f,
          y1 = 14.25f,
          x2 = 11.25f,
          y2 = 13.9142f,
          x3 = 11.25f,
          y3 = 13.5f,
        )
        // V 8.5
        verticalLineTo(y = 8.5f)
        // C 11.25 8.08579 11.5858 7.75 12 7.75z
        curveTo(
          x1 = 11.25f,
          y1 = 8.08579f,
          x2 = 11.5858f,
          y2 = 7.75f,
          x3 = 12.0f,
          y3 = 7.75f,
        )
        close()
        // M 12 17.5
        moveTo(x = 12.0f, y = 17.5f)
        // C 12.5523 17.5 13 17.0523 13 16.5
        curveTo(
          x1 = 12.5523f,
          y1 = 17.5f,
          x2 = 13.0f,
          y2 = 17.0523f,
          x3 = 13.0f,
          y3 = 16.5f,
        )
        // C 13 15.9477 12.5523 15.5 12 15.5
        curveTo(
          x1 = 13.0f,
          y1 = 15.9477f,
          x2 = 12.5523f,
          y2 = 15.5f,
          x3 = 12.0f,
          y3 = 15.5f,
        )
        // C 11.4477 15.5 11 15.9477 11 16.5
        curveTo(
          x1 = 11.4477f,
          y1 = 15.5f,
          x2 = 11.0f,
          y2 = 15.9477f,
          x3 = 11.0f,
          y3 = 16.5f,
        )
        // C 11 17.0523 11.4477 17.5 12 17.5z
        curveTo(
          x1 = 11.0f,
          y1 = 17.0523f,
          x2 = 11.4477f,
          y2 = 17.5f,
          x3 = 12.0f,
          y3 = 17.5f,
        )
        close()
      }
    }.build().also { _warningOutline = it }
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
        imageVector = HedvigIcons.WarningOutline,
        contentDescription = null,
        modifier = Modifier
          .width((24.0).dp)
          .height((24.0).dp),
      )
    }
  }
}

@Suppress("ObjectPropertyName", "ktlint:standard:backing-property-naming")
private var _warningOutline: ImageVector? = null
