package com.hedvig.android.odyssey.step.unknownerror

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.information.AppStateInformation
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold

@Composable
fun UnknownErrorDestination(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
  navigateBack: () -> Unit,
) {
  UnknownErrorScreen(
    windowSizeClass = windowSizeClass,
    openChat = openChat,
    finishClaimFlow = finishClaimFlow,
    navigateBack = navigateBack,
  )
}

@Composable
private fun UnknownErrorScreen(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
  ) { sideSpacingModifier: Modifier ->
    Spacer(Modifier.height(60.dp))
    AppStateInformation(
      type = AppStateInformationType.Failure,
      title = stringResource(hedvig.resources.R.string.home_tab_error_title),
      description = stringResource(hedvig.resources.R.string.home_tab_error_body),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(40.dp))
    Spacer(Modifier.weight(1f))
    LargeOutlinedButton(openChat, sideSpacingModifier) {
      Text(stringResource(hedvig.resources.R.string.open_chat))
    }
    Spacer(Modifier.height(16.dp))
    LargeContainedButton(finishClaimFlow, sideSpacingModifier) {
      Text(stringResource(hedvig.resources.R.string.general_close_button))
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownErrorScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UnknownErrorScreen(WindowSizeClass.calculateForPreview(), {}, {}, {})
    }
  }
}
