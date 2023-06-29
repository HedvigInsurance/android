package com.hedvig.android.feature.odyssey.step.unknownerror

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.information.AppStateInformation
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold

@Composable
internal fun UnknownErrorDestination(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  closeFailureScreenDestination: () -> Unit,
) {
  UnknownErrorScreen(
    windowSizeClass = windowSizeClass,
    openChat = openChat,
    navigateUp = navigateUp,
    closeFailureScreenDestination = closeFailureScreenDestination,
  )
}

@Composable
private fun UnknownErrorScreen(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  closeFailureScreenDestination: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(16.dp))
    Box(
      contentAlignment = Alignment.Center,
      modifier = sideSpacingModifier.weight(1f),
    ) {
      AppStateInformation(
        type = AppStateInformationType.Failure,
        title = stringResource(hedvig.resources.R.string.home_tab_error_title),
        description = stringResource(hedvig.resources.R.string.home_tab_error_body),
        horizontalAlignment = Alignment.CenterHorizontally,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).fillMaxWidth(0.8f),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(stringResource(hedvig.resources.R.string.open_chat), openChat, sideSpacingModifier)
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(
      stringResource(hedvig.resources.R.string.general_close_button),
      closeFailureScreenDestination,
      sideSpacingModifier,
    )
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
