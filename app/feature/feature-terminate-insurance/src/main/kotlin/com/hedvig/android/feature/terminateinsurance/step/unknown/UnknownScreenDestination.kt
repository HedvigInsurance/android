package com.hedvig.android.feature.terminateinsurance.step.unknown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.feature.terminateinsurance.ui.TerminationInfoScreen
import hedvig.resources.R

@Composable
internal fun UnknownScreenDestination(
  windowSizeClass: WindowSizeClass,
  openPlayStore: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  UnknownScreenScreen(
    windowSizeClass = windowSizeClass,
    openPlayStore = openPlayStore,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@Composable
private fun UnknownScreenScreen(
  windowSizeClass: WindowSizeClass,
  openPlayStore: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  TerminationInfoScreen(
    windowSizeClass = windowSizeClass,
    title = "",
    headerText = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
    bodyText = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
    icon = ImageVector.vectorResource(com.hedvig.android.core.design.system.R.drawable.ic_warning_triangle),
    navigateUp = navigateUp,
  ) {
    Column {
      LargeOutlinedTextButton(
        text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
        onClick = openPlayStore,
      )
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(R.string.general_close_button),
        onClick = navigateBack,
      )
    }
  }
}
