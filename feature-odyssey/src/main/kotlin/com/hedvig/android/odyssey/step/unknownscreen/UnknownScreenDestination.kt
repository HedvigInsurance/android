package com.hedvig.android.odyssey.step.unknownscreen

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
import hedvig.resources.R

@Composable
fun UnknownScreenDestination(
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  UnknownScreenScreen(windowSizeClass, openChat, navigateBack)
}

@Composable
private fun UnknownScreenScreen(
  // todo maybe show "Update your app" here instead.
  windowSizeClass: WindowSizeClass,
  openChat: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(20.dp))
    AppStateInformation(
      type = AppStateInformationType.Failure,
      title = stringResource(R.string.something_went_wrong),
      description = stringResource(R.string.home_tab_error_body),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(40.dp))
    Spacer(Modifier.weight(1f))
    LargeOutlinedButton(openChat, sideSpacingModifier) {
      Text(stringResource(R.string.open_chat))
    }
    Spacer(Modifier.height(16.dp))
    LargeContainedButton(navigateBack, sideSpacingModifier) {
      Text(text = stringResource(R.string.general_done_button))
    }
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
      ),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownScreenScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UnknownScreenScreen(WindowSizeClass.calculateForPreview(), {}) {}
    }
  }
}
