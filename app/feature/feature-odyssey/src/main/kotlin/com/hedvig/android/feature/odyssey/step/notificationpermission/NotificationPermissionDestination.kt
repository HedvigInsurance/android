package com.hedvig.android.feature.odyssey.step.notificationpermission

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
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
import com.google.accompanist.permissions.isGranted
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.notification.permission.NotificationPermissionDialog
import com.hedvig.android.notification.permission.NotificationPermissionState
import com.hedvig.android.notification.permission.rememberNotificationPermissionState
import com.hedvig.android.notification.permission.rememberPreviewNotificationPermissionState
import hedvig.resources.R

@Composable
internal fun SharedTransitionScope.NotificationPermissionDestination(
  animatedContentScope: AnimatedContentScope,
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
    animatedContentScope = animatedContentScope,
    windowSizeClass = windowSizeClass,
    notificationPermissionState = notificationPermissionState,
    onNotificationPermissionDecided = onNotificationPermissionDecided,
    openAppSettings = openAppSettings,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun SharedTransitionScope.NotificationPermissionScreen(
  animatedContentScope: AnimatedContentScope,
  windowSizeClass: WindowSizeClass,
  notificationPermissionState: NotificationPermissionState,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    animatedContentScope = animatedContentScope,
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
      is PermissionStatus.Granted -> {
        stringResource(R.string.general_continue_button) to onNotificationPermissionDecided
      }

      is PermissionStatus.Denied -> {
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
  HedvigPreviewLayout { animatedContentScope ->
    NotificationPermissionScreen(
      animatedContentScope,
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
