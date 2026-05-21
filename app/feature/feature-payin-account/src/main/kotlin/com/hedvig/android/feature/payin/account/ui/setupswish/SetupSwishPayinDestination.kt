package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.simulateHotReload
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.CONTACT_INFO_CHANGES_SAVED
import hedvig.resources.ODYSSEY_PHONE_NUMBER_LABEL
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION
import hedvig.resources.general_save_button
import hedvig.resources.something_went_wrong
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetupSwishPayinDestination(
  viewModel: SetupSwishPayinViewModel,
  globalSnackBarState: GlobalSnackBarState,
  onSuccessfullyConnected: () -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupSwishPayoutScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    onSave = { viewModel.emit(SetupSwishPayoutEvent.Save) },
    showedSnackBar = {
      viewModel.emit(SetupSwishPayoutEvent.ShowedSnackBar)
      onSuccessfullyConnected()
    },
    navigateUp = navigateUp,
    openUrl = openUrl
  )
}

//todo fetch payment methods continuously to see if it already not in pending state

@Composable
private fun SetupSwishPayoutScreen(
  uiState: SetupSwishPayoutUiState,
  globalSnackBarState: GlobalSnackBarState,
  onSave: () -> Unit,
  showedSnackBar: () -> Unit,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val changesSaved = stringResource(Res.string.CONTACT_INFO_CHANGES_SAVED)
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show(changesSaved, NotificationPriority.Campaign)
    showedSnackBar()
  }

  HedvigScaffold(
    topAppBarText = stringResource(Res.string.swish),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.weight(1f))
    if (uiState.error != null) {
      EmptyState(
        text = stringResource(Res.string.something_went_wrong),
        description = uiState.error.message,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        iconStyle = EmptyStateDefaults.EmptyStateIconStyle.ERROR,
      )
    }
    if (uiState.successUrl!=null) {
      Column(
        modifier= Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        EmptyState(
          text = "The process started",
          description = "Please confirm in Swish app",
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.SUCCESS_WITH_WARNING,
        )
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          "Open Swish",
          enabled = true,
          onClick = {
            openUrl(uiState.successUrl)
          }
        )
      }

    }
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(horizontal = 16.dp)) {
      HedvigTextField(
        state = uiState.phoneNumberState,
        labelText = stringResource(Res.string.ODYSSEY_PHONE_NUMBER_LABEL),
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Phone,
        ),
        modifier = Modifier.fillMaxWidth(),
      )
    }

    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_save_button),
      onClick = onSave,
      enabled = !uiState.isLoading &&
        uiState.phoneNumberState.text.length >= 10 &&
        uiState.successUrl==null,
      isLoading = uiState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewSetupSwishPayinScreen(
  @PreviewParameter(SetupSwishPayinUiStateProvider::class) uiState: SetupSwishPayoutUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SetupSwishPayoutScreen(
        uiState = uiState,
        globalSnackBarState = GlobalSnackBarState(),
        onSave = {},
        showedSnackBar = {},
        navigateUp = {},
        {}
      )
    }
  }
}


private class SetupSwishPayinUiStateProvider : CollectionPreviewParameterProvider<SetupSwishPayoutUiState>(
  listOf(
    SetupSwishPayoutUiState(
      phoneNumberState = TextFieldState("287334432273"),
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumberState = TextFieldState(),
      isLoading = false,
      error = ErrorMessage(),
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumberState = TextFieldState("837286428"),
      isLoading = true,
      error = null,
      showSuccessSnackBar = false,
    ),
    SetupSwishPayoutUiState(
      phoneNumberState = TextFieldState("83728644428"),
      isLoading = false,
      error = null,
      showSuccessSnackBar = false,
      successUrl = "hwdjhew"
    )
  ),
)
