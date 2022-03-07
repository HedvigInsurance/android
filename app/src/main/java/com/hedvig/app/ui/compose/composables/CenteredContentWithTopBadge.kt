package com.hedvig.app.ui.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

/**
 * A composable which places the [centeredContent] at the center vertically and horizontally also considering the height
 * of [topContent]. It does so by measuring the height of [topContent] and adds an equivalent space at the bottom of
 * the layout.
 * Places both [centeredContent] and [topContent] centered horizontally by default without a choice of adjusting that.
 *
 * ```
 * +----------------------------------+
 * |        +----------------+        |
 * |        |  topContent()  |        |
 * |        +----------------+        |
 * |      +--------------------+      |
 * |      |                    |      |
 * |      |  centeredContent() |      |
 * |      |                    |      |
 * |      +--------------------+      |
 * |     +----------------------+     |
 * |     | height of topContent |     |
 * |     +----------------------+     |
 * +----------------------------------+
 * ```
 */
@Composable
fun CenteredContentWithTopBadge(
    centeredContent: @Composable () -> Unit,
    topContent: (@Composable () -> Unit)? = {},
) {
    Layout(
        content = {
            Box(Modifier.layoutId("center")) {
                centeredContent()
            }
            if (topContent != null) {
                Box(Modifier.layoutId("top")) {
                    topContent()
                }
            }
        },
    ) { measurables, constraints ->
        val centerPlaceable = measurables.first { it.layoutId == "center" }.measure(constraints)
        val topPlaceable = measurables.firstOrNull { it.layoutId == "top" }?.measure(constraints)

        val centerContentHeight = centerPlaceable.height
        val topContentHeight = topPlaceable?.height ?: 0

        val maxWidth = constraints.maxWidth
        val layoutHeight = centerContentHeight + (topContentHeight * 2)

        layout(maxWidth, layoutHeight) {
            topPlaceable?.place(
                x = (maxWidth - topPlaceable.width) / 2,
                y = 0,
            )
            centerPlaceable.place(
                x = (maxWidth - centerPlaceable.width) / 2,
                y = topContentHeight,
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CenteredContentWithTopBadgePreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            CenteredContentWithTopBadge(
                centeredContent = {
                    Text(text = "Text".repeat(8), modifier = Modifier.padding(16.dp))
                },
                topContent = {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = "Badge")
                    }
                }
            )
        }
    }
}
