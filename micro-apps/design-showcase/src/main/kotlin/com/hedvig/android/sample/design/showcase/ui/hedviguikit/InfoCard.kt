package com.hedvig.android.sample.design.showcase.ui.hedviguikit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard

@Composable
internal fun InfoCard() {
  HedvigInfoCard(contentPadding = PaddingValues(24.dp)) {
    Text("I am an info card")
  }
}
