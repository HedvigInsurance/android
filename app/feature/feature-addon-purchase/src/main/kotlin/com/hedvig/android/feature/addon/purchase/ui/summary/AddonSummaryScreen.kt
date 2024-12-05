package com.hedvig.android.feature.addon.purchase.ui.summary

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

@Composable
internal fun AddonSummaryScreen(
  viewModel: AddonSummaryViewModel,
  navigateUp: () -> Unit,
  popBackStack: () -> Unit,
  onFailure: () -> Unit,
  onSuccess: (activationDate: LocalDate) -> Unit,
) {
  TODO()
}
