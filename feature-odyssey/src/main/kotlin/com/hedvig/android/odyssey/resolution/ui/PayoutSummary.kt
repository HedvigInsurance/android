package com.hedvig.android.odyssey.resolution.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.odyssey.remote.money.MonetaryAmount
import hedvig.resources.R

@Composable
fun PayoutSummary(
  resolution: Resolution.SingleItemPayout,
  onPayout: (MonetaryAmount) -> Unit,
) {
  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      Spacer(modifier = Modifier.padding(top = 28.dp))

      Text(
        text = stringResource(R.string.claims_payout_summary_subtitle),
        style = MaterialTheme.typography.h5,
      )

      Spacer(modifier = Modifier.padding(top = 33.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.claims_payout_purchase_price))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = resolution.purchasePrice.amount ?: "")
        }
      }

      Divider(modifier = Modifier.padding(vertical = 16.dp))

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.claims_payout_age_deductable))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = "-${resolution.deductible.amount ?: ""}")
        }
      }

      Divider(modifier = Modifier.padding(vertical = 16.dp))

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(text = stringResource(R.string.claims_payout_total))
        Text(text = resolution.payoutAmount.amount ?: "", style = MaterialTheme.typography.h6)
      }

      Spacer(modifier = Modifier.padding(top = 48.dp))

      Text(stringResource(R.string.claims_payout_summary_method), style = MaterialTheme.typography.h5)

      Spacer(modifier = Modifier.padding(top = 4.dp))

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colors.surface,
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
      ) {
        Text(stringResource(R.string.claims_payout_method_autogiro), modifier = Modifier.padding(16.dp))
      }
    }

    LargeContainedTextButton(
      onClick = {
        onPayout(resolution.payoutAmount)
      },
      text = stringResource(R.string.claims_payout_button_label, resolution.payoutAmount.amount ?: "-"),
      modifier = Modifier.align(Alignment.BottomCenter),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewPayoutSummary() {
  HedvigTheme {
    Surface {
      PayoutSummary(
        resolution = Resolution.SingleItemPayout(
          purchasePrice = MonetaryAmount("1000", "SEK"),
          depreciation = MonetaryAmount("1000", "SEK"),
          deductible = MonetaryAmount("1000", "SEK"),
          payoutAmount = MonetaryAmount("1000", "SEK"),
        ),
        onPayout = {},
      )
    }
  }
}
