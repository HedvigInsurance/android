package com.hedvig.android.feature.forever.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hedvig.android.apollo.format
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.forever.ForeverUiState
import hedvig.resources.R
import org.javamoney.moneta.Money
import javax.money.CurrencyContext
import javax.money.CurrencyUnit

@Composable
internal fun ReferralList(uiState: ForeverUiState) {
  Column(modifier = Modifier.padding(horizontal = 16.dp)) {
    Text(
      text = stringResource(id = R.string.FOREVER_REFERRAL_LIST_LABEL),
      modifier = Modifier.padding(vertical = 16.dp),
    )
    Divider()
    uiState.referrals.forEach { referral ->
      ReferralRow(referral)
    }
    Row(
      modifier = Modifier
        .padding(vertical = 16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(text = stringResource(id = R.string.FOREVER_TAB_TOTAL_DISCOUNT_LABEL))
      Row {
        Text(
          text = uiState.grossPriceAmount?.format(getLocale()) ?: "-",
          style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(uiState.currentNetAmount?.format(getLocale()) ?: "-")
      }
    }
  }
}

@Composable
private fun ReferralRow(referral: ForeverUiState.Referral) {
  Row(
    modifier = Modifier
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
      Text(text = referral.name ?: "-")
    }
    when (referral.state) {
      ForeverUiState.ReferralState.ACTIVE -> {
        Text(text = referral.discount?.format(getLocale()) ?: "-")
      }

      ForeverUiState.ReferralState.IN_PROGRESS -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_PENDING_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ForeverUiState.ReferralState.TERMINATED -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ForeverUiState.ReferralState.UNKNOWN -> {
        Text(
          text = stringResource(id = R.string.REFERRAL_TERMINATED_STATUS_LABEL),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
  Divider()
}

@Composable
private fun ForeverUiState.ReferralState.toColor(): Color = when (this) {
  ForeverUiState.ReferralState.ACTIVE -> MaterialTheme.colorScheme.typeElement
  ForeverUiState.ReferralState.IN_PROGRESS -> MaterialTheme.colorScheme.warningElement
  ForeverUiState.ReferralState.TERMINATED -> MaterialTheme.colorScheme.error
  ForeverUiState.ReferralState.UNKNOWN -> MaterialTheme.colorScheme.error
}

@Composable
@HedvigPreview
private fun PreviewReferralList() {
  HedvigTheme {
    Surface {
      ReferralList(
        uiState = ForeverUiState(
          referrals = listOf(
            ForeverUiState.Referral(
              name = "Ermir",
              state = ForeverUiState.ReferralState.ACTIVE,
              discount = Money.of(10, fakeSekCurrency),
            ),
            ForeverUiState.Referral(
              name = "Genc",
              state = ForeverUiState.ReferralState.IN_PROGRESS,
              discount = null,
            ),
            ForeverUiState.Referral(
              name = "Ermir",
              state = ForeverUiState.ReferralState.TERMINATED,
              discount = Money.of(10, fakeSekCurrency),
            ),
          ),
          currentDiscountAmount = Money.of(40, fakeSekCurrency),
          currentNetAmount = Money.of(118, fakeSekCurrency),
          grossPriceAmount = Money.of(138, fakeSekCurrency),
        ),
      )
    }
  }
}

internal val fakeSekCurrency = object : CurrencyUnit {
  override fun compareTo(other: CurrencyUnit?): Int = 0
  override fun getCurrencyCode(): String = "SEK"
  override fun getNumericCode(): Int = 0
  override fun getDefaultFractionDigits(): Int = 0
  override fun getContext(): CurrencyContext? = null
}
