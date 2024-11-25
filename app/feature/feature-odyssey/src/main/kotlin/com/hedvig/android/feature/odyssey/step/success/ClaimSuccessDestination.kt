package com.hedvig.android.feature.odyssey.step.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.R

@Composable
internal fun ClaimSuccessDestination(closeSuccessScreen: () -> Unit) {
  ClaimSuccessScreen(closeSuccessScreen = closeSuccessScreen)
}

@Composable
private fun ClaimSuccessScreen(closeSuccessScreen: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.height(16.dp))
      EmptyState(
        stringResource(R.string.CLAIMS_SUCCESS_TITLE),
        stringResource(R.string.CLAIMS_SUCCESS_LABEL),
        Modifier
          .fillMaxSize()
          .weight(1f)
          .wrapContentSize(Alignment.Center),
        SUCCESS,
      )
      Spacer(Modifier.height(16.dp))
      HedvigTextButton(
        onClick = closeSuccessScreen,
        text = stringResource(R.string.general_close_button),
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}

@HedvigPreview
@Composable
fun PreviewClaimSuccessScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimSuccessScreen({})
    }
  }
}
