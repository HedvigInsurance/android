package com.hedvig.android.shared.foreverui.ui.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.shared.foreverui.ui.data.Referral
import com.hedvig.android.shared.foreverui.ui.data.ReferralState
import hedvig.resources.R

@Composable
internal fun ReferralList(
  referrals: List<Referral>,
  grossPriceAmount: UiMoney?,
  currentNetAmount: UiMoney?,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = stringResource(id = R.string.FOREVER_REFERRAL_LIST_LABEL),
      modifier = Modifier.padding(vertical = 16.dp),
    )
    HorizontalDivider()
    referrals.forEach { referral ->
      ReferralRow(referral)
    }
    Row(
      modifier = Modifier
        .padding(vertical = 16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(stringResource(id = R.string.FOREVER_TAB_TOTAL_DISCOUNT_LABEL))
      Row {
        Text(
          text = grossPriceAmount?.toString() ?: "-",
          style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(currentNetAmount?.toString() ?: "-")
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.ReferralRow(referral: Referral, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .padding(vertical = 16.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Spacer(
        Modifier
          .size(16.dp)
          .wrapContentSize(Alignment.Center)
          .size(20.dp)
          .background(referral.state.toColor(), CircleShape),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(referral.name ?: "-")
    }
    when (referral.state) {
      ReferralState.ACTIVE -> {
        Text(referral.discount?.toString()?.let { "-$it" } ?: "-")
      }

      ReferralState.IN_PROGRESS -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_PENDING_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ReferralState.TERMINATED -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ReferralState.UNKNOWN -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
  HorizontalDivider()
}

@Composable
private fun ReferralState.toColor(): Color = when (this) {
  ReferralState.ACTIVE -> MaterialTheme.colorScheme.typeElement
  ReferralState.IN_PROGRESS -> MaterialTheme.colorScheme.warningElement
  ReferralState.TERMINATED -> MaterialTheme.colorScheme.error
  ReferralState.UNKNOWN -> MaterialTheme.colorScheme.error
}

@Composable
@HedvigPreview
private fun PreviewReferralList() {
  HedvigTheme {
    Surface {
      ReferralList(
        grossPriceAmount = UiMoney(138.0, UiCurrencyCode.SEK),
        currentNetAmount = UiMoney(118.0, UiCurrencyCode.SEK),
        referrals = listOf(
          Referral(
            name = "Ermir",
            state = ReferralState.ACTIVE,
            discount = UiMoney(10.0, UiCurrencyCode.SEK),
          ),
          Referral(
            name = "Genc",
            state = ReferralState.IN_PROGRESS,
            discount = null,
          ),
          Referral(
            name = "Ermir",
            state = ReferralState.TERMINATED,
            discount = UiMoney(10.0, UiCurrencyCode.SEK),
          ),
        ),
      )
    }
  }
}
