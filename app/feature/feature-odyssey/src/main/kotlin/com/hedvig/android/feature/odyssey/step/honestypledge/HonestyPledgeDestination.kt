package com.hedvig.android.feature.odyssey.step.honestypledge

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.notification.permission.rememberNotificationPermissionStatus
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R

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
    FlowHeading(
      stringResource(R.string.HONESTY_PLEDGE_TITLE),
      stringResource(R.string.HONESTY_PLEDGE_DESCRIPTION),
      sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.weight(1f))
    PledgeAcceptingSlider(
      onAccepted = {
        if (hasNotificationPermission) {
          pledgeAccepted()
        } else {
          openNotificationPermissionStep()
        }
      },
      text = stringResource(R.string.CLAIMS_PLEDGE_SLIDE_LABEL),
      modifier = Modifier
        .widthIn(max = 450.dp)
        .fillMaxWidth()
        .systemGestureExclusion()
        .padding(horizontal = 16.dp)
        .align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = closeClaimFlow,
      text = stringResource(R.string.general_cancel_button),
      modifier = sideSpacingModifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewHonestyPledgeScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HonestyPledgeScreen(WindowSizeClass.calculateForPreview(), {}, {}, {}, {})
    }
  }
}
