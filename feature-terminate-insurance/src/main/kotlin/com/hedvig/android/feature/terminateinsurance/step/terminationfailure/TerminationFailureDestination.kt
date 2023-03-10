package com.hedvig.android.feature.terminateinsurance.step.terminationfailure

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R

@Composable
internal fun TerminationFailureDestination(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  navigateBack: () -> Unit,
) {
  TerminationFailureScreen(
    windowSizeClass = windowSizeClass,
    errorMessage = errorMessage,
    navigateBack = navigateBack,
  )
}

@Composable
internal fun TerminationFailureScreen(
  windowSizeClass: WindowSizeClass,
  errorMessage: ErrorMessage,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
    title = "",
    headerText = stringResource(R.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
    bodyText = errorMessage.message ?: stringResource(R.string.general_unknown_error),
    onPrimaryButton = navigateBack,
    icon = ImageVector.vectorResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
  )
}
