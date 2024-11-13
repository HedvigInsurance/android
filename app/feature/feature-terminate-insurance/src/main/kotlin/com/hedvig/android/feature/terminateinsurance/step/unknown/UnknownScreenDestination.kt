package com.hedvig.android.feature.terminateinsurance.step.unknown

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
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
    title = stringResource(R.string.general_error),
    headerText = stringResource(R.string.EMBARK_UPDATE_APP_TITLE),
    bodyText = stringResource(R.string.EMBARK_UPDATE_APP_BODY),
    navigateUp = navigateUp,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth(),
    ) {
      HedvigButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.EMBARK_UPDATE_APP_BUTTON),
        enabled = true,
        onClick = openPlayStore,
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(R.string.general_close_button),
        modifier = Modifier.fillMaxWidth(),
        buttonSize = Large,
        onClick = navigateBack,
      )
    }
  }
}

@Composable
@HedvigPreview
fun PreviewSettingsScreen() {
  HedvigTheme {
    Surface {
      UnknownScreenDestination(
        navigateUp = {},
        navigateBack = {},
        windowSizeClass = WindowSizeClass.calculateForPreview(),
        openPlayStore = {},
      )
    }
  }
}
