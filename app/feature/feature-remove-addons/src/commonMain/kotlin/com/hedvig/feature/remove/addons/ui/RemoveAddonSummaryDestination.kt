package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RemoveAddonSummaryDestination(
  navigateToSuccess: (activationDate: LocalDate) -> Unit
) {
  val viewModel: RemoveAddonSummaryViewModel = koinViewModel() //TODO
  //TODO
}
