package com.hedvig.app.ui.compose.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.designsystem.theme.HedvigTheme

/**
 * A composable which places the [centeredContent] at the center vertically and horizontally also considering the height
 * of [topContent]. It does so by measuring the height of [topContent] and adds at least an equivalent space at the
 * bottom of the layout.
 * Places both [centeredContent] and [topContent] centered horizontally by default without a choice of adjusting it.
 *
 * ```
 * +-------------------------------------+
 * |         +----------------+          |
 * |         |  topContent()  |          |
 * |         +----------------+          |
 * |                                     |
 * |       +--------------------+        |
 * |       |                    |        |
 * |       |  centeredContent() |        |
 * |       |                    |        |
 * |       +--------------------+        |
 * |                                     |
 * |  +-------------------------------+  |
 * |  | at least height of topContent |  |
 * |  +-------------------------------+  |
 * +-------------------------------------+
 * ```
 */
@Composable
fun CenteredContentWithTopBadge(
  modifier: Modifier = Modifier,
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
    modifier = modifier,
  ) { measurables, constraints ->
    val placeableConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val centerPlaceable = measurables.first { it.layoutId == "center" }.measure(placeableConstraints)
    val topPlaceable = measurables.firstOrNull { it.layoutId == "top" }?.measure(placeableConstraints)

    val centerContentHeight = centerPlaceable.measuredHeight
    val topContentHeight = topPlaceable?.measuredHeight ?: 0

    val layoutWidth = constraints.maxWidth
    val heightOfCenterContentPlusTwoTopContents = centerContentHeight + (topContentHeight * 2)
    val layoutHeight = heightOfCenterContentPlusTwoTopContents.coerceIn(
      constraints.minHeight..constraints.maxHeight,
    )

    layout(layoutWidth, layoutHeight) {
      topPlaceable?.place(
        x = (layoutWidth - topPlaceable.width) / 2,
        y = 0,
      )
      centerPlaceable.place(
        x = (layoutWidth - centerPlaceable.width) / 2,
        y = (layoutHeight - centerPlaceable.height) / 2,
      )
    }
  }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CenteredContentWithTopBadgePreview(
  @PreviewParameter(TextAndBadgeProvider::class) textToBadgePair: Pair<String, String?>,
) {
  val (text, badge) = textToBadgePair
  HedvigTheme {
    Card(backgroundColor = MaterialTheme.colors.background) {
      CenteredContentWithTopBadge(
        centeredContent = {
          Text(text = text, modifier = Modifier.padding(16.dp))
        },
        topContent = if (badge == null) {
          null
        } else {
          {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
              Text(text = badge)
            }
          }
        },
      )
    }
  }
}

class TextAndBadgeProvider : CollectionPreviewParameterProvider<Pair<String, String?>>(
  listOf(
    "Text" to "Badge #1",
    "Text".repeat(20) to "Badge #2",
    "Text\n".repeat(5).dropLast(1) to "Badge #3",
    "Badgeless".repeat(10) to null,
    "Badgeless\n".repeat(2).dropLast(1) to null,
  ),
)
