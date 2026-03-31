package com.hedvig.android.feature.purchase.apartment.ui.sign

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress

@Composable
internal fun SigningDestination(
  viewModel: SigningViewModel,
  navigateToSuccess: (startDate: String?) -> Unit,
  navigateToFailure: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  LaunchedEffect(Unit) {
    val token = (uiState as? SigningUiState.Polling)?.autoStartToken ?: return@LaunchedEffect
    val bankIdUri = Uri.parse("https://app.bankid.com/?autostarttoken=$token&redirect=null")
    context.startActivity(Intent(Intent.ACTION_VIEW, bankIdUri))
    viewModel.emit(SigningEvent.BankIdOpened)
  }

  when (val state = uiState) {
    is SigningUiState.Polling -> HedvigFullScreenCenterAlignedProgress()
    is SigningUiState.Success -> {
      LaunchedEffect(state) {
        navigateToSuccess(state.startDate)
      }
      HedvigFullScreenCenterAlignedProgress()
    }
    is SigningUiState.Failed -> {
      LaunchedEffect(state) {
        navigateToFailure()
      }
      HedvigFullScreenCenterAlignedProgress()
    }
  }
}
