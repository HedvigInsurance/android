package com.hedvig.android.feature.change.tier.ui.stepsummary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import hedvig.resources.R


@Composable
internal fun SubmitTierSuccessScreen(
  navigateUp: () -> Unit,
) {
  Column(
      modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(
              WindowInsets.safeDrawing.only(
                  WindowInsetsSides.Horizontal +
                          WindowInsetsSides.Bottom,
              ),
          ),
  ) {
    EmptyState(
        modifier = Modifier.weight(1f),
        text = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_TITLE),
        description = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_DESCRIPTION),
        iconStyle = SUCCESS,
        buttonStyle = NoButton,
    )
    HedvigTextButton(stringResource(R.string.general_close_button), onClick = navigateUp, buttonSize = Large)
    Spacer(Modifier.height(32.dp))
  }
}
