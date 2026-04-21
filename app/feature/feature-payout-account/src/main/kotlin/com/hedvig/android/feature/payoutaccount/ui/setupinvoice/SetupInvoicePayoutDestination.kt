package com.hedvig.android.feature.payoutaccount.ui.setupinvoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority

@Composable
internal fun SetupInvoicePayoutDestination(
  viewModel: SetupInvoicePayoutViewModel,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupInvoicePayoutScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    onConnect = { viewModel.emit(SetupInvoicePayoutEvent.Connect) },
    showedSnackBar = {
      viewModel.emit(SetupInvoicePayoutEvent.ShowedSnackBar)
      navigateUp()
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SetupInvoicePayoutScreen(
  uiState: SetupInvoicePayoutUiState,
  globalSnackBarState: GlobalSnackBarState,
  onConnect: () -> Unit,
  showedSnackBar: () -> Unit,
  navigateUp: () -> Unit,
) {
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show("Changes saved", NotificationPriority.Campaign)
    showedSnackBar()
  }

  HedvigScaffold(
    topAppBarText = "Invoice",
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.weight(1f))
    AnimatedVisibility(
      visible = uiState.errorMessage != null,
      enter = expandVertically(),
      exit = shrinkVertically(),
    ) {
      HedvigNotificationCard(
        message = uiState.errorMessage ?: "",
        priority = NotificationPriority.Attention,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(bottom = 4.dp)
          .fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Connect",
      onClick = onConnect,
      enabled = !uiState.isLoading,
      isLoading = uiState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}
