package com.hedvig.android.feature.odyssey.step.notificationpermission

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.PermissionStatus.Denied
import com.google.accompanist.permissions.PermissionStatus.Granted
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.notification.permission.rememberPreviewNotificationPermissionState
import hedvig.resources.R

@Composable
internal fun NotificationPermissionDestination(
  windowSizeClass: WindowSizeClass,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val notificationPermissionState = rememberNotificationPermissionState(
    onNotificationGranted = onNotificationPermissionDecided,
  )
  NotificationPermissionScreen(
    windowSizeClass = windowSizeClass,
    notificationPermissionState = notificationPermissionState,
    onNotificationPermissionDecided = onNotificationPermissionDecided,
    openAppSettings = openAppSettings,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun NotificationPermissionScreen(
  windowSizeClass: WindowSizeClass,
  notificationPermissionState: NotificationPermissionState,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) { sideSpacingModifier ->
    NotificationPermissionDialog(notificationPermissionState, openAppSettings)

    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA),
      style = MaterialTheme.typography.headlineMedium,
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Text(
      text = if (notificationPermissionState.status.isGranted) {
        stringResource(R.string.CLAIMS_NOTIFICATIONS_ACTIVATED)
      } else {
        stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY)
      },
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    if (notificationPermissionState.status.isGranted.not()) {
      HedvigTextButton(
        onClick = onNotificationPermissionDecided,
        text = stringResource(R.string.ONBOARDING_ACTIVATE_NOTIFICATIONS_DISMISS),
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(16.dp))
    }
    val bottomButton: Pair<String, () -> Unit> = when (notificationPermissionState.status) {
      is Granted -> {
        stringResource(R.string.general_continue_button) to onNotificationPermissionDecided
      }
      is Denied -> {
        Pair(
          stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_CTA),
          notificationPermissionState::launchPermissionRequest,
        )
      }
    }
    HedvigContainedButton(
      onClick = bottomButton.second,
      text = bottomButton.first,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewNotificationPermissionScreen(
  @PreviewParameter(PermissionStatusCollectionPreviewParameterProvider::class)
  previewPermissionState: PreviewPermissionState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      NotificationPermissionScreen(
        WindowSizeClass.calculateForPreview(),
        rememberPreviewNotificationPermissionState(
          previewPermissionState.permissionStatus,
          previewPermissionState.isDialogShowing,
        ),
        {},
        {},
        {},
        {},
      )
    }
  }
}

private data class PreviewPermissionState(
  val permissionStatus: PermissionStatus,
  val isDialogShowing: Boolean,
)

@OptIn(ExperimentalPermissionsApi::class)
private class PermissionStatusCollectionPreviewParameterProvider :
  CollectionPreviewParameterProvider<PreviewPermissionState>(
    listOf(
      PreviewPermissionState(PermissionStatus.Granted, false),
      PreviewPermissionState(PermissionStatus.Denied(false), false),
      PreviewPermissionState(PermissionStatus.Denied(true), true),
      PreviewPermissionState(PermissionStatus.Denied(false), true),
    ),
  )
