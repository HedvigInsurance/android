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
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val CartAdded: ImageVector
    get() {
        val current = _cartAdded
        if (current != null) return current

        return ImageVector.Builder(
            name = "com.hedvig.android.design.system.hedvig.HedvigTheme.CartAdded",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f,
        ).apply {
            group {
                // M24 12 c0 -8.6 -3.77 -12 -12.01 -12 S0 3.26 0 12 s3.74 12 11.99 12 S24 20.6 24 12
                path(
                    fill = SolidColor(Color(0xFF121212)),
                ) {
                    // M 24 12
                    moveTo(x = 24.0f, y = 12.0f)
                    // c 0 -8.6 -3.77 -12 -12.01 -12
                    curveToRelative(
                        dx1 = 0.0f,
                        dy1 = -8.6f,
                        dx2 = -3.77f,
                        dy2 = -12.0f,
                        dx3 = -12.01f,
                        dy3 = -12.0f,
                    )
                    // S 0 3.26 0 12
                    reflectiveCurveTo(
                        x1 = 0.0f,
                        y1 = 3.26f,
                        x2 = 0.0f,
                        y2 = 12.0f,
                    )
                    // s 3.74 12 11.99 12
                    reflectiveCurveToRelative(
                        dx1 = 3.74f,
                        dy1 = 12.0f,
                        dx2 = 11.99f,
                        dy2 = 12.0f,
                    )
                    // S 24 20.6 24 12
                    reflectiveCurveTo(
                        x1 = 24.0f,
                        y1 = 20.6f,
                        x2 = 24.0f,
                        y2 = 12.0f,
                    )
                }
                // M12.1 17 V8.5 l-2.46 2.23 V9.29 l2.47 -2.09 h1.28 V17z
                path(
                    fill = SolidColor(Color(0xFFFAFAFA)),
                ) {
                    // M 12.1 17
                    moveTo(x = 12.1f, y = 17.0f)
                    // V 8.5
                    verticalLineTo(y = 8.5f)
                    // l -2.46 2.23
                    lineToRelative(dx = -2.46f, dy = 2.23f)
                    // V 9.29
                    verticalLineTo(y = 9.29f)
                    // l 2.47 -2.09
                    lineToRelative(dx = 2.47f, dy = -2.09f)
                    // h 1.28
                    horizontalLineToRelative(dx = 1.28f)
                    // V 17z
                    verticalLineTo(y = 17.0f)
                    close()
                }
            }
        }.build().also { _cartAdded = it }
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
                imageVector = CartAdded,
                contentDescription = null,
                modifier = Modifier
                    .width((24.0).dp)
                    .height((24.0).dp),
            )
        }
    }
}

@Suppress("ObjectPropertyName")
private var _cartAdded: ImageVector? = null
