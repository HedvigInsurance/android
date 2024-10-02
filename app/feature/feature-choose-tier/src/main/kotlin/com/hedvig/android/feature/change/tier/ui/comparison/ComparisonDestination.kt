package com.hedvig.android.feature.change.tier.ui.comparison

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.hedvig.android.design.system.hedvig.HedvigText

@Composable
internal fun ComparisonDestination(viewModel: ComparisonViewModel, navigateUp: () -> Unit) {
  Box(contentAlignment = Alignment.Center) {
    HedvigText("Comparison")
  }
}
