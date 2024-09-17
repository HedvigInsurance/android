package com.hedvig.android.feature.help.center.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade.LIGHT
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun <T> HelpCenterSectionWithClickableRows(
  title: String,
  chipContainerColor: HighlightColor,
  items: List<T>,
  itemText: (T) -> String,
  onClickItem: (T) -> Unit,
  modifier: Modifier = Modifier,
  itemSubtitle: ((T) -> String)? = null,
) {
  HelpCenterSection(
    title = title,
    chipContainerColor = chipContainerColor,
    content = {
      Column {
        for (question in items) {
          Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
              .clickable { onClickItem(question) }
              .padding(vertical = 16.dp),
          ) {
            HedvigText(
              text = itemText(question),
              modifier = Modifier
                .fillMaxWidth(),
            )
            if (itemSubtitle != null) {
              HedvigText(
                text = itemSubtitle(question),
                color = HedvigTheme.colorScheme.textSecondary,
                style = HedvigTheme.typography.label,
                modifier = Modifier
                  .fillMaxWidth(),
              )
            }
          }
          HorizontalDivider()
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HelpCenterSectionWithClickableRows(
        title = "Common topics",
        chipContainerColor = HighlightColor.Pink(LIGHT),
        items = listOf("Item 1", "Item 2", "Item 3"),
        itemText = { it },
        onClickItem = {},
      )
    }
  }
}
