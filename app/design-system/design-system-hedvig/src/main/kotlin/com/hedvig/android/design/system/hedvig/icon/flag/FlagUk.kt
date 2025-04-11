package com.hedvig.android.design.system.hedvig.icon.flag

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

@Suppress("UnusedReceiverParameter")
public val HedvigIcons.FlagUk: ImageVector
  get() {
    if (_flagUk != null) {
      return _flagUk!!
    }
    _flagUk = ImageVector.Builder(
      name = "Flag of the UK",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      group(
        clipPathData = PathData {
          moveTo(2f, 4f)
          lineTo(22f, 4f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 6f)
          lineTo(24f, 18f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 20f)
          lineTo(2f, 20f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 18f)
          lineTo(0f, 6f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 4f)
          close()
        },
      ) {
        path(
          fill = SolidColor(Color(0xFF30577E)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(2f, 4f)
          lineTo(22f, 4f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 24f, 6f)
          lineTo(24f, 18f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 20f)
          lineTo(2f, 20f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 18f)
          lineTo(0f, 6f)
          arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 4f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(-0.485f, 4.277f)
          lineTo(-0.624f, 4.485f)
          lineTo(-0.416f, 4.624f)
          lineTo(11.584f, 12.624f)
          lineTo(11.792f, 12.763f)
          lineTo(11.931f, 12.555f)
          lineTo(12.485f, 11.723f)
          lineTo(12.624f, 11.515f)
          lineTo(12.416f, 11.376f)
          lineTo(0.416f, 3.376f)
          lineTo(0.208f, 3.237f)
          lineTo(0.069f, 3.445f)
          lineTo(-0.485f, 4.277f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(23.931f, 3.445f)
          lineTo(23.792f, 3.237f)
          lineTo(23.584f, 3.376f)
          lineTo(11.584f, 11.376f)
          lineTo(11.376f, 11.515f)
          lineTo(11.515f, 11.723f)
          lineTo(12.069f, 12.555f)
          lineTo(12.208f, 12.763f)
          lineTo(12.416f, 12.624f)
          lineTo(24.416f, 4.624f)
          lineTo(24.624f, 4.485f)
          lineTo(24.485f, 4.277f)
          lineTo(23.931f, 3.445f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11.931f, 11.445f)
          lineTo(11.792f, 11.237f)
          lineTo(11.584f, 11.376f)
          lineTo(-0.416f, 19.376f)
          lineTo(-0.624f, 19.515f)
          lineTo(-0.485f, 19.723f)
          lineTo(0.069f, 20.555f)
          lineTo(0.208f, 20.763f)
          lineTo(0.416f, 20.624f)
          lineTo(12.416f, 12.624f)
          lineTo(12.624f, 12.485f)
          lineTo(12.485f, 12.277f)
          lineTo(11.931f, 11.445f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = SolidColor(Color(0xFFFAFAFA)),
          strokeAlpha = 1.0f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11.515f, 12.277f)
          lineTo(11.376f, 12.485f)
          lineTo(11.584f, 12.624f)
          lineTo(23.584f, 20.624f)
          lineTo(23.792f, 20.763f)
          lineTo(23.931f, 20.555f)
          lineTo(24.485f, 19.723f)
          lineTo(24.624f, 19.515f)
          lineTo(24.416f, 19.376f)
          lineTo(12.416f, 11.376f)
          lineTo(12.208f, 11.237f)
          lineTo(12.069f, 11.445f)
          lineTo(11.515f, 12.277f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(11.723f, 12.416f)
          lineTo(-0.277f, 4.416f)
          lineTo(0.277f, 3.584f)
          lineTo(12.277f, 11.584f)
          lineTo(11.723f, 12.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(-0.277f, 4.416f)
          lineTo(0f, 2.197f)
          lineTo(13.664f, 11.307f)
          lineTo(11.723f, 12.416f)
          lineTo(-0.277f, 4.416f)
          close()
          moveTo(12.277f, 11.584f)
          lineTo(11.723f, 12.416f)
          lineTo(-0.277f, 4.416f)
          lineTo(0.277f, 3.584f)
          lineTo(12.277f, 11.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(11.723f, 11.584f)
          lineTo(23.723f, 3.584f)
          lineTo(24.277f, 4.416f)
          lineTo(12.277f, 12.416f)
          lineTo(11.723f, 11.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(23.723f, 3.584f)
          lineTo(25.664f, 4.693f)
          lineTo(12f, 13.803f)
          lineTo(11.723f, 11.584f)
          lineTo(23.723f, 3.584f)
          close()
          moveTo(12.277f, 12.416f)
          lineTo(11.723f, 11.584f)
          lineTo(23.723f, 3.584f)
          lineTo(24.277f, 4.416f)
          lineTo(12.277f, 12.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(-0.277f, 19.584f)
          lineTo(11.723f, 11.584f)
          lineTo(12.277f, 12.416f)
          lineTo(0.277f, 20.416f)
          lineTo(-0.277f, 19.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(12f, 10.197f)
          lineTo(12.277f, 12.416f)
          lineTo(0.277f, 20.416f)
          lineTo(-1.664f, 19.307f)
          lineTo(12f, 10.197f)
          close()
          moveTo(0.277f, 20.416f)
          lineTo(-0.277f, 19.584f)
          lineTo(11.723f, 11.584f)
          lineTo(12.277f, 12.416f)
          lineTo(0.277f, 20.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(23.723f, 20.416f)
          lineTo(11.723f, 12.416f)
          lineTo(12.277f, 11.584f)
          lineTo(24.277f, 19.584f)
          lineTo(23.723f, 20.416f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.EvenOdd,
        ) {
          moveTo(10.336f, 12.693f)
          lineTo(12.277f, 11.584f)
          lineTo(24.277f, 19.584f)
          lineTo(24f, 21.803f)
          lineTo(10.336f, 12.693f)
          close()
          moveTo(24.277f, 19.584f)
          lineTo(23.723f, 20.416f)
          lineTo(11.723f, 12.416f)
          lineTo(12.277f, 11.584f)
          lineTo(24.277f, 19.584f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(0f, 10f)
          horizontalLineTo(24f)
          verticalLineTo(14f)
          horizontalLineTo(0f)
          verticalLineTo(10f)
          close()
        }
        group {
          path(
            fill = SolidColor(Color(0xFF121212)),
            fillAlpha = 0.07f,
            stroke = null,
            strokeAlpha = 1.0f,
            strokeLineWidth = 1.0f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Miter,
            strokeLineMiter = 1.0f,
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(23.5f, 10f)
            verticalLineTo(14f)
            horizontalLineTo(24.5f)
            verticalLineTo(10f)
            horizontalLineTo(23.5f)
            close()
            moveTo(0.5f, 14f)
            verticalLineTo(10f)
            horizontalLineTo(-0.5f)
            verticalLineTo(14f)
            horizontalLineTo(0.5f)
            close()
          }
        }
        path(
          fill = SolidColor(Color(0xFFFAFAFA)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(10f, 4f)
          horizontalLineTo(14f)
          verticalLineTo(20f)
          horizontalLineTo(10f)
          verticalLineTo(4f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF121212)),
          fillAlpha = 0.07f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(10f, 4.5f)
          horizontalLineTo(14f)
          verticalLineTo(3.5f)
          horizontalLineTo(10f)
          verticalLineTo(4.5f)
          close()
          moveTo(14f, 19.5f)
          horizontalLineTo(10f)
          verticalLineTo(20.5f)
          horizontalLineTo(14f)
          verticalLineTo(19.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11f, 4f)
          horizontalLineTo(13f)
          verticalLineTo(20f)
          horizontalLineTo(11f)
          verticalLineTo(4f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF121212)),
          fillAlpha = 0.07f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(11f, 4.5f)
          horizontalLineTo(13f)
          verticalLineTo(3.5f)
          horizontalLineTo(11f)
          verticalLineTo(4.5f)
          close()
          moveTo(13f, 19.5f)
          horizontalLineTo(11f)
          verticalLineTo(20.5f)
          horizontalLineTo(13f)
          verticalLineTo(19.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFFFF513A)),
          fillAlpha = 1.0f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(0f, 11f)
          horizontalLineTo(24f)
          verticalLineTo(13f)
          horizontalLineTo(0f)
          verticalLineTo(11f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF121212)),
          fillAlpha = 0.07f,
          stroke = null,
          strokeAlpha = 1.0f,
          strokeLineWidth = 1.0f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(23.5f, 11f)
          verticalLineTo(13f)
          horizontalLineTo(24.5f)
          verticalLineTo(11f)
          horizontalLineTo(23.5f)
          close()
          moveTo(0.5f, 13f)
          verticalLineTo(11f)
          horizontalLineTo(-0.5f)
          verticalLineTo(13f)
          horizontalLineTo(0.5f)
          close()
        }
        path(
          fill = SolidColor(Color(0xFF000000)),
          fillAlpha = 0.0f,
          stroke = SolidColor(Color(0xFF121212)),
          strokeAlpha = 0.068f,
          strokeLineWidth = 0.5f,
          strokeLineCap = StrokeCap.Butt,
          strokeLineJoin = StrokeJoin.Miter,
          strokeLineMiter = 1.0f,
          pathFillType = PathFillType.NonZero,
        ) {
          moveTo(2f, 4.25f)
          lineTo(22f, 4.25f)
          arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.75f, 6f)
          lineTo(23.75f, 18f)
          arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 22f, 19.75f)
          lineTo(2f, 19.75f)
          arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0.25f, 18f)
          lineTo(0.25f, 6f)
          arcTo(1.75f, 1.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2f, 4.25f)
          close()
        }
      }
    }.build()
    return _flagUk!!
  }

@Suppress("ktlint:standard:backing-property-naming")
private var _flagUk: ImageVector? = null

@Preview
@Composable
private fun IconPreview() {
  HedvigTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Image(
        imageVector = HedvigIcons.FlagUk,
        contentDescription = com.hedvig.android.compose.ui.EmptyContentDescription,
        modifier = Modifier
          .width((40.0).dp)
          .height((40.0).dp),
      )
    }
  }
}
