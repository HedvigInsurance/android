package com.hedvig.android.feature.payin.account.ui.selectmethod

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
import com.hedvig.android.design.system.hedvig.icon.Autogiro
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYOUT_METHOD_INVOICE_DESCRIPTION
import hedvig.resources.Res
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPayinMethodDestination(
  availableProviders: List<MemberPaymentProvider>,
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = "Add or change billing method", // todo
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (provider in availableProviders) {
        PayinMethodRow(
          provider = provider,
          onTrustlySelected = onTrustlySelected,
          onSwishSelected = onSwishSelected,
          onInvoiceSelected = onInvoiceSelected,
        )
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun PayinMethodRow(
  provider: MemberPaymentProvider,
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = {
      when (provider) {
        MemberPaymentProvider.TRUSTLY -> {
          onTrustlySelected()
        }

        MemberPaymentProvider.SWISH -> {
          onSwishSelected()
        }

        MemberPaymentProvider.INVOICE -> {
          onInvoiceSelected()
        }

        else -> {}
    }
    },
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      when (provider) {
        MemberPaymentProvider.TRUSTLY -> Icon(
          HedvigIcons.Autogiro,
          null, //todo
          modifier = Modifier.size(32.dp),
        )

        MemberPaymentProvider.SWISH -> Image(
          HedvigIcons.Swish,
          null,  //todo
          modifier = Modifier.size(32.dp),
        )

        MemberPaymentProvider.INVOICE -> Image(
          HedvigIcons.Kivra,
          null,  //todo
          modifier = Modifier.size(32.dp),
        )

        else -> {}
      }

      Spacer(Modifier.width(16.dp))
      Column(Modifier.padding(vertical = 12.dp)) {
        HedvigText(
          text = when (provider) {
            MemberPaymentProvider.TRUSTLY -> "Autogiro" // todo
            MemberPaymentProvider.SWISH -> stringResource(Res.string.swish)
            MemberPaymentProvider.INVOICE -> stringResource(Res.string.PAYMENTS_INVOICE)
            else -> ""
          },
        )
        HedvigText(
          text = when (provider) {
            MemberPaymentProvider.TRUSTLY -> "Connect your bank via Trustly" // todo
            MemberPaymentProvider.SWISH -> "Monthly auto-payments via Swish" // todo
            MemberPaymentProvider.INVOICE -> stringResource(Res.string.PAYOUT_METHOD_INVOICE_DESCRIPTION)
            else -> ""
          },
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }


    }
  }
}

@Composable
@HedvigPreview
private fun PreviewSelectPayinMethodScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectPayinMethodDestination(
        availableProviders = listOf(
          MemberPaymentProvider.SWISH,
          MemberPaymentProvider.INVOICE,
          MemberPaymentProvider.TRUSTLY,
        ),
        onTrustlySelected = {},
        onSwishSelected = {},
        onInvoiceSelected = {},
        navigateUp = {},
      )
    }
  }
}
