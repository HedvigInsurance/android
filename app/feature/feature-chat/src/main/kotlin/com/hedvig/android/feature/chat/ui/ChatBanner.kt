package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled

@Composable
internal fun ChatBanner(text: String, modifier: Modifier = Modifier) {
  HedvigInfoCard(
    modifier = modifier,
    contentPadding = PaddingValues(
      start = 12.dp,
      top = 12.dp,
      end = 16.dp,
      bottom = 12.dp,
    ),
    shape = RectangleShape,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.infoContainer,
      contentColor = MaterialTheme.colorScheme.onInfoContainer,
    ),
  ) {
    ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onInfoContainer)) {
      // Lays out the info icon in a way that is reliably center aligned with the text next to it
      LayoutWithoutPlacement(
        sizeAdjustingContent = { Text("H") },
      ) {
        Icon(
          imageVector = Icons.Hedvig.InfoFilled,
          contentDescription = "info",
          tint = MaterialTheme.colorScheme.infoElement,
          modifier = Modifier.size(16.dp),
        )
      }
      Spacer(Modifier.width(8.dp))
      RichText {
        Markdown(
          content = text,
        )
      }
    }
  }
}

@HedvigPreview
@PreviewFontScale
@Composable
private fun PreviewChatBanner() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChatBanner("HHHHHH".repeat(15))
    }
  }
}
