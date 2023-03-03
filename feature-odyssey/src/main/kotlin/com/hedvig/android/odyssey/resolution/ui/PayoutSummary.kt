package com.hedvig.android.odyssey.resolution.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
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
        text = stringResource(R.string.claims_payout_summary_title),
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

      Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.claims_payout_total))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(text = resolution.payoutAmount.amount ?: "")
        }
      }

      Spacer(modifier = Modifier.padding(top = 48.dp))

      Text(stringResource(R.string.claims_payout_summary_method), style = MaterialTheme.typography.h5)

      Text(stringResource(R.string.claims_payout_method_autogiro))
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
