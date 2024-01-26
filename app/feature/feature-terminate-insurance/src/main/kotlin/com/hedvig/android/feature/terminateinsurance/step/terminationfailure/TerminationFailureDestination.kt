package com.hedvig.android.feature.terminateinsurance.step.terminationfailure

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigOutlinedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R

@Composable
internal fun TerminationFailureDestination(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationFailureScreen(
    windowSizeClass = windowSizeClass,
    errorMessage = errorMessage,
    openChat = openChat,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@Composable
private fun TerminationFailureScreen(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    title = stringResource(id = R.string.general_error),
    headerText = stringResource(R.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
    bodyText = errorMessage.message ?: stringResource(R.string.something_went_wrong),
    navigateUp = navigateUp,
  ) {
    Column {
      HedvigOutlinedTextButton(
        text = stringResource(id = R.string.open_chat),
        onClick = openChat,
      )
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(R.string.general_done_button),
        onClick = navigateBack,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationFailureScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationFailureScreen(WindowSizeClass.calculateForPreview(), ErrorMessage(), {}, {}, {})
    }
  }
}
