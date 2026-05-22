package com.hedvig.android.feature.payoutaccount.ui.selectmethod

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.BankAccount
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Link
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_DESCRIPTION
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
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
              provider = provider,
            )
          }

          MemberPaymentProvider.NORDEA -> {
            PayoutMethodRow(
              title = stringResource(Res.string.BANK_PAYOUT_METHOD_CARD_TITLE),
              subtitle = "Payout to a bank account",  //todo: removed BANK_PAYOUT_METHOD_CARD_DESCRIPTION for demo
              onClick = onNordeaSelected,
              provider = provider,
            )
          }

          MemberPaymentProvider.SWISH -> {
            PayoutMethodRow(
              title = stringResource(Res.string.swish),
              subtitle = stringResource(Res.string.PAYOUT_METHOD_SWISH_DESCRIPTION),
              onClick = onSwishSelected,
              provider = provider,
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
private fun PayoutMethodRow(
  provider: MemberPaymentProvider,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      when (provider) {
        MemberPaymentProvider.TRUSTLY -> Icon(
          HedvigIcons.Link,
          null, //todo
          modifier = Modifier.size(32.dp),
        )

        MemberPaymentProvider.SWISH -> Image(
          HedvigIcons.Swish,
          null,  //todo
          modifier = Modifier.size(32.dp),
        )

        MemberPaymentProvider.NORDEA -> Icon(
          HedvigIcons.Card,
          null, //todo
          modifier = Modifier.size(32.dp),
        )

        else -> {}
      }

      Spacer(Modifier.width(16.dp))
      Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        HedvigText(text = title)
        HedvigText(
          text = subtitle,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewSelectPayoutMethodScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectPayoutMethodDestination(
        availableProviders = listOf(
          MemberPaymentProvider.SWISH,
          MemberPaymentProvider.TRUSTLY,
          MemberPaymentProvider.NORDEA,
        ),
        onTrustlySelected = {},
        onNordeaSelected = {},
        onSwishSelected = {},
        navigateUp = {},
      )
    }
  }
}
