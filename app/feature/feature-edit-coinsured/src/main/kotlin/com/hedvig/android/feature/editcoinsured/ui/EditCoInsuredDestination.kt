package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun EditCoInsuredDestination(
  contractId: String,
  navigateUp: () -> Unit,
) {
  val viewModel: EditCoInsuredViewModel = koinViewModel { parametersOf(contractId) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp,
    uiState,
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  uiState: EditCoInsuredState,
) {
  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = "Insured people", // TODO
      onClick = navigateUp,
    )
  }
}
