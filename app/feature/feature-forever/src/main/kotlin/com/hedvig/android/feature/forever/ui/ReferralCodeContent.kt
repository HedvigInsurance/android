package com.hedvig.android.feature.forever.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigBigCard
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Copy
import com.hedvig.android.feature.forever.ForeverUiState
import com.hedvig.android.feature.forever.copyToClipboard
import hedvig.resources.R
import java.util.*
import javax.money.MonetaryAmount

@Suppress("UnusedReceiverParameter")
@Composable
internal fun ColumnScope.ReferralCodeContent(
  uiState: ForeverUiState,
  onChangeCodeClicked: () -> Unit,
  onShareCodeClick: (code: String, incentive: MonetaryAmount) -> Unit,
) {
  val context = LocalContext.current

  HedvigBigCard(
    onClick = {
      uiState.campaignCode?.let {
        context.copyToClipboard(uiState.campaignCode)
      }
    },
    enabled = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    Row(
      modifier = Modifier
        .heightIn(min = 72.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Column {
        Text(
          text = stringResource(id = R.string.referrals_empty_code_headline),
          style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
        )
        Text(
          text = uiState.campaignCode ?: "",
          style = MaterialTheme.typography.headlineSmall,
        )
      }
      Spacer(modifier = Modifier.weight(1f))
      Icon(
        imageVector = Icons.Hedvig.Copy,
        contentDescription = "Copy",
        modifier = Modifier
          .align(Alignment.Bottom)
          .padding(bottom = 8.dp),
      )
    }
  }

  if (uiState.incentive != null && uiState.campaignCode != null) {
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.referrals_empty_share_code_button),
      onClick = { onShareCodeClick(uiState.campaignCode, uiState.incentive) },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.referrals_change_change_code),
      onClick = { onChangeCodeClicked() },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
  Spacer(Modifier.height(16.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}
