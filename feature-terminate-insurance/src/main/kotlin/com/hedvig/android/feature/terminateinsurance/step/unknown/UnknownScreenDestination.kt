package com.hedvig.android.feature.terminateinsurance.step.unknown

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R

@Composable
internal fun UnknownScreenDestination(
  windowSizeClass: WindowSizeClass,
  navigateBack: () -> Unit,
) {
  UnknownScreenScreen(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
  )
}

@Composable
private fun UnknownScreenScreen(
  windowSizeClass: WindowSizeClass,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    navigateBack = navigateBack,
    title = "",
    headerText = stringResource(R.string.TERMINATION_NOT_SUCCESSFUL_TITLE),
    bodyText = "Could not find next step in flow. Please try again.",
    onPrimaryButton = navigateBack,
    icon = ImageVector.vectorResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
  )
}
