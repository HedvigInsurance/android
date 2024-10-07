package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.hedvig.android.design.system.hedvig.HedvigText

@Composable
internal fun ChangeTierSummaryDestination(
  viewModel: SummaryViewModel,
  navigateUp: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
) {
  Box(contentAlignment = Alignment.Center) {
    HedvigText("Summary")
  }
}
