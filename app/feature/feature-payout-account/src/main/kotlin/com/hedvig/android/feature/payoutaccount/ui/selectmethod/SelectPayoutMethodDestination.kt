package com.hedvig.android.feature.payoutaccount.ui.selectmethod

import androidx.compose.foundation.clickable
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
import octopus.type.MemberPaymentProvider

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
    topAppBarText = "Connect payout account",
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    Column(Modifier.padding(horizontal = 16.dp)) {
      for (provider in availableProviders) {
        when (provider) {
          MemberPaymentProvider.TRUSTLY -> {
            PayoutMethodRow(
              title = "Trustly",
              subtitle = "Connect via Trustly",
              onClick = onTrustlySelected,
            )
            Spacer(Modifier.height(8.dp))
          }

          MemberPaymentProvider.NORDEA -> {
            PayoutMethodRow(
              title = "Bank account",
              subtitle = "Enter clearing and account number",
              onClick = onNordeaSelected,
            )
            Spacer(Modifier.height(8.dp))
          }

          MemberPaymentProvider.SWISH -> {
            PayoutMethodRow(
              title = "Swish",
              subtitle = "Connect via Swish",
              onClick = onSwishSelected,
            )
            Spacer(Modifier.height(8.dp))
          }

          MemberPaymentProvider.INVOICE -> {
            PayoutMethodRow(
              title = "Invoice",
              subtitle = "Connect via Kivra",
              onClick = onInvoiceSelected,
            )
            Spacer(Modifier.height(8.dp))
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
        color = com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme.textSecondary,
      )
    }
  }
}
