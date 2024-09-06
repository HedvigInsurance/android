package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.onPinkContainer
import com.hedvig.android.core.designsystem.material3.pinkContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
internal fun <T> HelpCenterSectionWithClickableRows(
  title: String,
  chipContainerColor: Color,
  contentColor: Color,
  items: List<T>,
  itemText: (T) -> String,
  onClickItem: (T) -> Unit,
  modifier: Modifier = Modifier,
  itemSubtitle: ((T) -> String)? = null,
) {
  HelpCenterSection(
    title = title,
    chipContainerColor = chipContainerColor,
    contentColor = contentColor,
    content = {
      Column {
        for (question in items) {
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onClickItem(question) }
              .padding(vertical = 16.dp, horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          ) {
            Text(
              text = itemText(question),
              modifier = Modifier
                .fillMaxWidth(),
            )
            if (itemSubtitle != null) {
              Text(
                text = itemSubtitle(question),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                  .fillMaxWidth(),
              )
            }
          }

          HorizontalDivider(
            Modifier
              .padding(horizontal = 16.dp)
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          )
        }
      }
    },
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewHelpCenterSectionWithClickableRows() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HelpCenterSectionWithClickableRows(
        title = "Common topics",
        chipContainerColor = MaterialTheme.colorScheme.pinkContainer,
        contentColor = MaterialTheme.colorScheme.onPinkContainer,
        items = listOf("Item 1", "Item 2", "Item 3"),
        itemText = { it },
        onClickItem = {},
      )
    }
  }
}
