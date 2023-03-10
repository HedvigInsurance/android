package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
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
    buttonText = stringResource(R.string.TERMINATION_OPEN_SURVEY_LABEL),
    onPrimaryButton = onPrimaryButtonClick,
    icon = Icons.Outlined.CheckCircle,
  )
}
