package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer

@Composable
internal fun WarningCard() {
  HedvigWarningCard(contentPadding = PaddingValues(24.dp)) {
    Text("I am a warning card")
  }
}

// todo maybe move this into the design system if we will ever use such a card
@Composable
private fun HedvigWarningCard(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  content: @Composable RowScope.() -> Unit,
) {
  HedvigCard(
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.warningContainer,
      contentColor = MaterialTheme.colorScheme.onWarningContainer,
    ),
    modifier = modifier,
  ) {
    Row(modifier = Modifier.padding(contentPadding)) {
      content()
    }
  }
}
