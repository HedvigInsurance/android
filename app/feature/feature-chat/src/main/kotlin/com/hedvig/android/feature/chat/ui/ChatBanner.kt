package com.hedvig.android.feature.chat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.RichText
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled

@Composable
fun ChatBanner(text: String, modifier: Modifier = Modifier) {
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
    Icon(
      imageVector = Icons.Hedvig.InfoFilled,
      contentDescription = "info",
      modifier = Modifier
        .padding(top = 2.dp)
        .size(16.dp),
      tint = MaterialTheme.colorScheme.infoElement,
    )
    Spacer(Modifier.width(8.dp))
    Column {
      ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onInfoContainer)) {
        RichText {
          Markdown(content = text)
        }
      }
    }
  }
}
