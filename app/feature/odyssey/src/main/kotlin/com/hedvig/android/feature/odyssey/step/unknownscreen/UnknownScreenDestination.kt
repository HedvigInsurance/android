package com.hedvig.android.feature.odyssey.step.unknownscreen

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
import hedvig.resources.R

@Composable
internal fun UnknownScreenDestination(
  windowSizeClass: WindowSizeClass,
  openPlayStore: () -> Unit,
  navigateUp: () -> Unit,
  closeUnknownScreenDestination: () -> Unit,
) {
  UnknownScreenScreen(windowSizeClass, openPlayStore, navigateUp, closeUnknownScreenDestination)
}

@Composable
private fun UnknownScreenScreen(
  windowSizeClass: WindowSizeClass,
  openPlayStore: () -> Unit,
  navigateUp: () -> Unit,
  closeUnknownScreenDestination: () -> Unit,
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
        title = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
        description = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
        horizontalAlignment = Alignment.CenterHorizontally,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).fillMaxWidth(0.8f),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(stringResource(R.string.EMBARK_UPDATE_APP_BUTTON), openPlayStore, sideSpacingModifier)
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(stringResource(R.string.general_close_button), closeUnknownScreenDestination, sideSpacingModifier)
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@HedvigPreview
@Composable
private fun PreviewUnknownScreenScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UnknownScreenScreen(WindowSizeClass.calculateForPreview(), {}, {}, {})
    }
  }
}
