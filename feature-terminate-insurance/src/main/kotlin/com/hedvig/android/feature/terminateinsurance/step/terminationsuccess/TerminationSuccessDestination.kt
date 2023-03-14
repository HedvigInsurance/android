package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminationSuccessDestination(
  terminationDate: LocalDate,
  surveyUrl: String,
  windowSizeClass: WindowSizeClass,
  navigateBack: () -> Unit,
) {
  val uriHandler = LocalUriHandler.current
  TerminationSuccessScreen(
    terminationDate = terminationDate,
    windowSizeClass = windowSizeClass,
    onPrimaryButtonClick = { uriHandler.openUri(surveyUrl) },
    navigateBack = navigateBack,
  )
}

@Composable
private fun TerminationSuccessScreen(
  terminationDate: LocalDate,
  windowSizeClass: WindowSizeClass,
  onPrimaryButtonClick: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
    title = "",
    headerText = stringResource(R.string.TERMINATION_SUCCESSFUL_TITLE),
    bodyText = stringResource(
      R.string.TERMINATION_SUCCESSFUL_TEXT,
      terminationDate,
      "Hedvig",
    ),
    bottomContent = {
      LargeContainedTextButton(
        text = stringResource(R.string.TERMINATION_OPEN_SURVEY_LABEL),
        onClick = onPrimaryButtonClick,
      )
    },
    icon = Icons.Outlined.CheckCircle,
  )
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminationSuccessScreen(
        LocalDate(2021, 12, 21),
        WindowSizeClass.calculateForPreview(),
        {},
        {},
      )
    }
  }
}
