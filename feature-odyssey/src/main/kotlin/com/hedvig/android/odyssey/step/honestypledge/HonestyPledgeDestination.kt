package com.hedvig.android.odyssey.step.honestypledge

import android.Manifest
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.R
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold

@Composable
internal fun HonestyPledgeDestination(
  viewModel: HonestyPledgeViewModel,
  windowSizeClass: WindowSizeClass,
  openNotificationPermissionStep: () -> Unit,
  startClaimFlow: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState: HonestyPledgeUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val nextStep = uiState.nextStep
  LaunchedEffect(nextStep) {
    if (nextStep != null) {
      navigateToNextStep(nextStep)
    }
  }
  HonestyPledgeScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    openNotificationPermissionStep = openNotificationPermissionStep,
    startClaimFlow = startClaimFlow,
    navigateUp = navigateUp,
  )
}

@Composable
private fun HonestyPledgeScreen(
  uiState: HonestyPledgeUiState,
  windowSizeClass: WindowSizeClass,
  openNotificationPermissionStep: () -> Unit,
  startClaimFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    isLoading = uiState.isLoading,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(40.dp))
    Crossfade(
      targetState = uiState.hasError,
      label = "Pledge or error crossfade",
      modifier = sideSpacingModifier.weight(1f),
    ) { hasError ->
      Column {
        if (hasError) {
          Icon(
            painter = painterResource(R.drawable.ic_warning_triangle),
            contentDescription = null,
            Modifier.size(24.dp),
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.something_went_wrong),
            style = MaterialTheme.typography.headlineSmall,
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE),
            style = androidx.compose.material.MaterialTheme.typography.body1,
          )
        } else {
          Text(
            text = stringResource(hedvig.resources.R.string.HONESTY_PLEDGE_TITLE),
            style = MaterialTheme.typography.headlineSmall,
          )
          Spacer(Modifier.height(16.dp))
          Text(stringResource(hedvig.resources.R.string.HONESTY_PLEDGE_DESCRIPTION))
        }
      }
    }
    Spacer(Modifier.height(16.dp))
    val hasNotificationPermission = rememberNotificationPermissionStatus()
    LargeContainedTextButton(
      onClick = {
        if (hasNotificationPermission) {
          startClaimFlow()
        } else {
          openNotificationPermissionStep()
        }
      },
      enabled = !uiState.isLoading,
      text = if (uiState.hasError) {
        stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION)
      } else {
        stringResource(hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL)
      },
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberNotificationPermissionStatus(): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).status.isGranted
  } else {
    true
  }
}

@HedvigPreview
@Composable
private fun PreviewHonestyPledgeScreen(
  @PreviewParameter(UiStateProvider::class) uiState: HonestyPledgeUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HonestyPledgeScreen(uiState, WindowSizeClass.calculateForPreview(), {}, {}, {})
    }
  }
}

private class UiStateProvider : CollectionPreviewParameterProvider<HonestyPledgeUiState>(
  listOf(
    HonestyPledgeUiState(),
    HonestyPledgeUiState(isLoading = true),
    HonestyPledgeUiState(hasError = true),
  ),
)
