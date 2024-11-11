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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.shared.foreverui.ui.data.Referral
import com.hedvig.android.shared.foreverui.ui.data.ReferralState
import com.hedvig.android.shared.foreverui.ui.data.ReferralState.ACTIVE
import com.hedvig.android.shared.foreverui.ui.data.ReferralState.IN_PROGRESS
import com.hedvig.android.shared.foreverui.ui.data.ReferralState.TERMINATED
import com.hedvig.android.shared.foreverui.ui.data.ReferralState.UNKNOWN
import hedvig.resources.R

@Composable
internal fun ReferralList(
  referrals: List<Referral>,
  grossPriceAmount: UiMoney?,
  currentNetAmount: UiMoney?,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigText(
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
      HedvigText(stringResource(id = R.string.FOREVER_TAB_TOTAL_DISCOUNT_LABEL))
      Row {
        HedvigText(
          text = grossPriceAmount?.toString() ?: "-",
          style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
          color = HedvigTheme.colorScheme.textSecondary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        HedvigText(currentNetAmount?.toString() ?: "-")
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
      HedvigText(referral.name ?: "-")
    }
    when (referral.state) {
      ACTIVE -> {
        HedvigText(referral.discount?.toString()?.let { "-$it" } ?: "-")
      }

      IN_PROGRESS -> {
        HedvigText(
          text = stringResource(id = R.string.REFERRAL_PENDING_STATUS_LABEL),
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }

      TERMINATED -> {
        HedvigText(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }

      UNKNOWN -> {
        HedvigText(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
  HorizontalDivider()
}

@Composable
private fun ReferralState.toColor(): Color = when (this) {
  ACTIVE -> HedvigTheme.colorScheme.signalGreenElement
  IN_PROGRESS -> HedvigTheme.colorScheme.signalAmberElement
  TERMINATED -> HedvigTheme.colorScheme.signalRedElement
  ReferralState.UNKNOWN -> HedvigTheme.colorScheme.signalRedElement
}

@Composable
@HedvigPreview
private fun PreviewReferralList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReferralList(
        grossPriceAmount = UiMoney(138.0, SEK),
        currentNetAmount = UiMoney(118.0, SEK),
        referrals = listOf(
          Referral(
            name = "Ermir",
            state = ACTIVE,
            discount = UiMoney(10.0, SEK),
          ),
          Referral(
            name = "Genc",
            state = IN_PROGRESS,
            discount = null,
          ),
          Referral(
            name = "Ermir",
            state = TERMINATED,
            discount = UiMoney(10.0, SEK),
          ),
        ),
      )
    }
  }
}
