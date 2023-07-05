package com.hedvig.android.feature.odyssey.step.honestypledge

import android.Manifest
import android.os.Build
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold

@Composable
internal fun HonestyPledgeDestination(
  windowSizeClass: WindowSizeClass,
  openNotificationPermissionStep: () -> Unit,
  pledgeAccepted: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  HonestyPledgeScreen(
    windowSizeClass = windowSizeClass,
    openNotificationPermissionStep = openNotificationPermissionStep,
    pledgeAccepted = pledgeAccepted,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun HonestyPledgeScreen(
  windowSizeClass: WindowSizeClass,
  openNotificationPermissionStep: () -> Unit,
  pledgeAccepted: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val hasNotificationPermission = rememberNotificationPermissionStatus()
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.HONESTY_PLEDGE_TITLE),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    Text(
      text = stringResource(hedvig.resources.R.string.HONESTY_PLEDGE_DESCRIPTION),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    HedvigContainedButton(
      onClick = {
        if (hasNotificationPermission) {
          pledgeAccepted()
        } else {
          openNotificationPermissionStep()
        }
      },
      text = stringResource(hedvig.resources.R.string.CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun rememberNotificationPermissionStatus(): Boolean {
  val isPreview = LocalInspectionMode.current
  return if (!isPreview && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).status.isGranted
  } else {
    true
  }
}

@HedvigPreview
@Composable
private fun PreviewHonestyPledgeScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HonestyPledgeScreen(WindowSizeClass.calculateForPreview(), {}, {}, {}, {})
    }
  }
}
