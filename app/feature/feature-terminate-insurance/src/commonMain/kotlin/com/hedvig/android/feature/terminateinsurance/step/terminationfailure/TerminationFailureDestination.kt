package com.hedvig.android.feature.terminateinsurance.step.terminationfailure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.Res
import hedvig.resources.TERMINATION_NOT_SUCCESSFUL_TITLE
import hedvig.resources.general_done_button
import hedvig.resources.general_error
import hedvig.resources.open_chat
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TerminationFailureDestination(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  onNavigateToNewConversation: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationFailureScreen(
    windowSizeClass = windowSizeClass,
    errorMessage = errorMessage,
    onNavigateToNewConversation = onNavigateToNewConversation,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@Composable
private fun TerminationFailureScreen(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  onNavigateToNewConversation: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    title = stringResource(Res.string.general_error),
    headerText = stringResource(Res.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
    bodyText = errorMessage.message ?: stringResource(Res.string.something_went_wrong),
    navigateUp = navigateUp,
  ) {
    Column(
      Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HedvigButton(
        modifier = Modifier.fillMaxSize(),
        text = stringResource(Res.string.open_chat),
        enabled = true,
        onClick = onNavigateToNewConversation,
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.general_done_button),
        modifier = Modifier.fillMaxSize(),
        buttonSize = Large,
        onClick = navigateBack,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationFailureScreen() {
  HedvigTheme {
    Surface {
      TerminationFailureScreen(
        WindowSizeClass.calculateForPreview(),
        ErrorMessage(),
        {},
        {},
        {},
      )
    }
  }
}
