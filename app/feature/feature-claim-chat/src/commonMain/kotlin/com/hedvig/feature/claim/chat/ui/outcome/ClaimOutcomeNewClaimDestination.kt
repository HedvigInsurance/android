package com.hedvig.feature.claim.chat.ui.outcome

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
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import hedvig.resources.CLAIMS_SUCCESS_LABEL
import hedvig.resources.CLAIMS_SUCCESS_TITLE
import hedvig.resources.Res
import hedvig.resources.general_done_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ClaimOutcomeNewClaimDestination(closeSuccessScreen: () -> Unit) {
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
      Spacer(Modifier.weight(1f))
      EmptyState(
        stringResource(Res.string.CLAIMS_SUCCESS_TITLE),
        stringResource(Res.string.CLAIMS_SUCCESS_LABEL),
        Modifier
          .fillMaxSize()
          .wrapContentSize(Alignment.Center),
        SUCCESS,
      )
      Spacer(Modifier.weight(1f))
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        onClick = closeSuccessScreen,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        text = stringResource(Res.string.general_done_button),
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
private fun PreviewClaimOutcomeNewClaimDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimOutcomeNewClaimDestination(
        {},
      )
    }
  }
}
