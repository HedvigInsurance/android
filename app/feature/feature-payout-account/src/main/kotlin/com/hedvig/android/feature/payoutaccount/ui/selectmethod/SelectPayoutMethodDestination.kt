package com.hedvig.android.feature.payoutaccount.ui.selectmethod

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_DESCRIPTION
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYOUT_METHOD_INVOICE_DESCRIPTION
import hedvig.resources.PAYOUT_METHOD_SWISH_DESCRIPTION
import hedvig.resources.PAYOUT_METHOD_TRUSTLY_DESCRIPTION
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.Res
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPayoutMethodDestination(
  availableProviders: List<MemberPaymentProvider>,
  onTrustlySelected: () -> Unit,
  onNordeaSelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PAYOUT_SELECT_PAYOUT_METHOD),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (provider in availableProviders) {
        when (provider) {
          MemberPaymentProvider.TRUSTLY -> {
            PayoutMethodRow(
              title = stringResource(Res.string.trustly),
              subtitle = stringResource(Res.string.PAYOUT_METHOD_TRUSTLY_DESCRIPTION),
              onClick = onTrustlySelected,
            )
          }

          MemberPaymentProvider.NORDEA -> {
            PayoutMethodRow(
              title = stringResource(Res.string.BANK_PAYOUT_METHOD_CARD_TITLE),
              subtitle = stringResource(Res.string.BANK_PAYOUT_METHOD_CARD_DESCRIPTION),
              onClick = onNordeaSelected,
            )
          }

          MemberPaymentProvider.SWISH -> {
            PayoutMethodRow(
              title = stringResource(Res.string.swish),
              subtitle = stringResource(Res.string.PAYOUT_METHOD_SWISH_DESCRIPTION),
              onClick = onSwishSelected,
            )
          }

          MemberPaymentProvider.INVOICE -> {
            PayoutMethodRow(
              title = stringResource(Res.string.PAYMENTS_INVOICE),
              subtitle = stringResource(Res.string.PAYOUT_METHOD_INVOICE_DESCRIPTION),
              onClick = onInvoiceSelected,
            )
          }

          else -> {}
        }
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun PayoutMethodRow(title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      HedvigText(text = title)
      HedvigText(
        text = subtitle,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}
