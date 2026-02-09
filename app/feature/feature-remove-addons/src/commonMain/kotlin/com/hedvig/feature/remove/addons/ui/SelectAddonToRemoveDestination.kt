package com.hedvig.feature.remove.addons.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SelectAddonToRemoveDestination(

  navigateUp: () -> Unit,
  navigateToSummary: (contractId: String,
                      addonIds: List<String>,
                      activationDate: LocalDate,
                      notificationMessage: String?) -> Unit,
) {
  val viewModel: SelectAddonToRemoveViewModel = koinViewModel()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}


