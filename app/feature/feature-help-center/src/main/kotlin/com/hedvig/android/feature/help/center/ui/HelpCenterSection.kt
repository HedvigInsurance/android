package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.onPinkContainer
import com.hedvig.android.core.designsystem.material3.onPurpleContainer
import com.hedvig.android.core.designsystem.material3.onYellowContainer
import com.hedvig.android.core.designsystem.material3.pinkContainer
import com.hedvig.android.core.designsystem.material3.purpleContainer
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.yellowContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun HelpCenterSection(
  title: String,
  chipContainerColor: Color,
  contentColor: Color,
  content: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val pillShape = MaterialTheme.shapes.squircleExtraSmall
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    HedvigCard(
      colors = CardDefaults.outlinedCardColors(
        containerColor = chipContainerColor,
        contentColor = contentColor,
      ),
      shape = pillShape,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .drawWithCache {
          val stroke = Stroke(Dp.Hairline.toPx())
          val outline = pillShape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
          onDrawWithContent {
            drawContent()
            drawOutline(outline, outlineColor, style = stroke)
          }
        },
    ) {
      Text(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        text = title,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    content()
  }
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterSection(
  @PreviewParameter(ColorIndexParameterProvider::class) colorIndex: Int,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterSection(
        title = "Common topics",
        chipContainerColor = when (colorIndex) {
          0 -> MaterialTheme.colorScheme.yellowContainer
          1 -> MaterialTheme.colorScheme.purpleContainer
          2 -> MaterialTheme.colorScheme.pinkContainer
          else -> Color.Red
        },
        contentColor = when (colorIndex) {
          0 -> MaterialTheme.colorScheme.onYellowContainer
          1 -> MaterialTheme.colorScheme.onPurpleContainer
          2 -> MaterialTheme.colorScheme.onPinkContainer
          else -> Color.Red
        },
        content = {
          Text("Content")
        },
      )
    }
  }
}

private class ColorIndexParameterProvider : CollectionPreviewParameterProvider<Int>(listOf(0, 1, 2))
