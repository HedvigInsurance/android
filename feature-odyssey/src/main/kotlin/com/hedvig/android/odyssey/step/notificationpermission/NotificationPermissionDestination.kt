package com.hedvig.android.odyssey.step.notificationpermission

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.permission.PermissionDialog
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun NotificationPermissionDestination(
  viewModel: NotificationPermissionViewModel,
  windowSizeClass: WindowSizeClass,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startClaimFlow: () -> Unit,
  openAppSettings: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  NotificationPermissionScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    startClaimFlow = startClaimFlow,
    openAppSettings = openAppSettings,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    navigateUp = navigateUp,
  )
}

@Composable
private fun NotificationPermissionScreen(
  uiState: NotificationPermissionUiState,
  windowSizeClass: WindowSizeClass,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startClaimFlow: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    isLoading = uiState.isLoading,
  ) { sideSpacingModifier ->
    var showDialog by remember { mutableStateOf(false) }
    val notificationPermissionState: PermissionState = rememberNotificationPermissionState { isGranted ->
      if (isGranted) {
        startClaimFlow()
      } else {
        showDialog = true
      }
    }
    if (showDialog) {
      PermissionDialog(
        permissionDescription = stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY),
        isPermanentlyDeclined = !shouldShowNotificationPermissionRationale(shouldShowRequestPermissionRationale),
        onDismiss = { showDialog = false },
        okClick = notificationPermissionState::launchPermissionRequest,
        openAppSettings = openAppSettings,
      )
    }

    val bottomButton: Pair<String, () -> Unit> = when {
      uiState.hasError -> stringResource(R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION) to startClaimFlow
      else -> when (notificationPermissionState.status) {
        is PermissionStatus.Granted -> {
          stringResource(R.string.general_continue_button) to startClaimFlow
        }
        is PermissionStatus.Denied -> {
          Pair(
            stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA),
            notificationPermissionState::launchPermissionRequest,
          )
        }
      }
    }

    Spacer(Modifier.height(40.dp))
    Crossfade(
      targetState = uiState.hasError,
      label = "Notification or error crossfade",
      modifier = sideSpacingModifier.weight(1f),
    ) { hasError ->
      Column {
        if (hasError) {
          Icon(
            painter = painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
            contentDescription = null,
            Modifier.size(24.dp),
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.something_went_wrong),
            style = MaterialTheme.typography.headlineSmall,
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.NETWORK_ERROR_ALERT_MESSAGE),
            style = androidx.compose.material.MaterialTheme.typography.body1,
          )
        } else {
          Text(
            text = stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA),
            style = MaterialTheme.typography.headlineSmall,
          )
          Spacer(Modifier.height(16.dp))
          Text(
            text = if (notificationPermissionState.status.isGranted) {
              stringResource(R.string.CLAIMS_NOTIFICATIONS_ACTIVATED)
            } else {
              stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY)
            },
          )
        }
      }
    }
    if (notificationPermissionState.status.isGranted.not()) {
      Spacer(Modifier.height(16.dp))
      LargeOutlinedTextButton(
        onClick = startClaimFlow,
        enabled = !uiState.isLoading,
        text = stringResource(R.string.ONBOARDING_ACTIVATE_NOTIFICATIONS_DISMISS),
        modifier = sideSpacingModifier,
      )
    }
    Spacer(Modifier.height(16.dp))
    LargeContainedTextButton(
      onClick = bottomButton.second,
      enabled = !uiState.isLoading,
      text = bottomButton.first,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberNotificationPermissionState(
  onPermissionResult: (Boolean) -> Unit,
): PermissionState {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS, onPermissionResult)
  } else {
    object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Granted

      override fun launchPermissionRequest() {}
    }
  }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU, lambda = 0)
@Composable
private fun shouldShowNotificationPermissionRationale(
  shouldShowRequestPermissionRationale: (String) -> Boolean,
): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
  } else {
    false
  }
}

@HedvigPreview
@Composable
private fun PreviewNotificationPermissionScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      NotificationPermissionScreen(
        NotificationPermissionUiState(),
        WindowSizeClass.calculateForPreview(),
        { true },
        {},
        {},
        {},
      )
    }
  }
}
