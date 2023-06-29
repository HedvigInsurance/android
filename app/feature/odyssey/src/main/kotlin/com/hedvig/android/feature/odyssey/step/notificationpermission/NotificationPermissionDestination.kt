package com.hedvig.android.feature.odyssey.step.notificationpermission

import android.Manifest
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.permission.PermissionDialog
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R

@Composable
internal fun NotificationPermissionDestination(
  windowSizeClass: WindowSizeClass,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  NotificationPermissionScreen(
    windowSizeClass = windowSizeClass,
    onNotificationPermissionDecided = onNotificationPermissionDecided,
    openAppSettings = openAppSettings,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    navigateUp = navigateUp,
  )
}

@Composable
private fun NotificationPermissionScreen(
  windowSizeClass: WindowSizeClass,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }
  val notificationPermissionState: PermissionState = rememberNotificationPermissionState { isGranted ->
    if (isGranted) {
      onNotificationPermissionDecided()
    } else {
      showDialog = true
    }
  }
  NotificationPermissionScreen(
    windowSizeClass = windowSizeClass,
    notificationPermissionState = notificationPermissionState,
    showDialog = showDialog,
    hideDialog = { showDialog = false },
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    onNotificationPermissionDecided = onNotificationPermissionDecided,
    openAppSettings = openAppSettings,
    navigateUp = navigateUp,
  )
}

@Composable
private fun NotificationPermissionScreen(
  windowSizeClass: WindowSizeClass,
  notificationPermissionState: PermissionState,
  showDialog: Boolean,
  hideDialog: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  onNotificationPermissionDecided: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
  ) { sideSpacingModifier ->
    if (showDialog) {
      PermissionDialog(
        permissionDescription = stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY),
        isPermanentlyDeclined = !shouldShowNotificationPermissionRationale(shouldShowRequestPermissionRationale),
        onDismiss = { hideDialog() },
        okClick = notificationPermissionState::launchPermissionRequest,
        openAppSettings = openAppSettings,
      )
    }

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
private fun PreviewNotificationPermissionScreen(
  @PreviewParameter(PermissionStateParameterProvider::class) previewStatus: Pair<Boolean, PermissionState>,
) {
  val showDialog = previewStatus.first
  val permissionState = previewStatus.second
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      NotificationPermissionScreen(
        WindowSizeClass.calculateForPreview(),
        permissionState,
        showDialog,
        {},
        { true },
        {},
        {},
        {},
      )
    }
  }
}

private class PermissionStateParameterProvider : CollectionPreviewParameterProvider<Pair<Boolean, PermissionState>>(
  listOf(
    false to object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Granted
      override fun launchPermissionRequest() {}
    },
    false to object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Denied(false)
      override fun launchPermissionRequest() {}
    },
    true to object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Denied(false)
      override fun launchPermissionRequest() {}
    },
  ),
)
