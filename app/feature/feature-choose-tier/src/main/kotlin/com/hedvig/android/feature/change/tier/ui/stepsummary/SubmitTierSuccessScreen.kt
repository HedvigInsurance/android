package com.hedvig.android.feature.change.tier.ui.stepsummary

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun SubmitTierSuccessScreen(activationDateEpochDays: Int, navigateUp: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(
        WindowInsets.safeDrawing.only(
          WindowInsetsSides.Horizontal +
            WindowInsetsSides.Bottom,
        ),
      ),
  ) {
    Spacer(Modifier.weight(1f))
    EmptyState(
      text = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_TITLE),
      description = stringResource(
        R.string.TIER_FLOW_COMMIT_PROCESSING_DESCRIPTION,
        LocalDate.fromEpochDays(activationDateEpochDays),
      ),
      iconStyle = SUCCESS,
      buttonStyle = NoButton,
    )
    Spacer(Modifier.weight(1f))
    HedvigTextButton(
      stringResource(R.string.general_close_button),
      onClick = dropUnlessResumed { navigateUp() },
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
  }
}

@HedvigPreview
@Composable
private fun SubmitTierSuccessScreenPreview() {
  SubmitTierSuccessScreen(LocalDate(2024, 9, 23).toEpochDays(), {})
}
