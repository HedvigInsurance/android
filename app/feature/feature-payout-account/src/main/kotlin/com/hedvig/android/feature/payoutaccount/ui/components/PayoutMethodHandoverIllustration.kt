package com.hedvig.android.feature.payoutaccount.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.Surface
import octopus.type.MemberPaymentProvider

/**
 * @param destinationBadge marks where the setup has got to once it has an outcome to show; absent
 *   while the member is still choosing.
 */
@Composable
internal fun PayoutMethodHandoverIllustration(
  provider: MemberPaymentProvider?,
  modifier: Modifier = Modifier,
  destinationBadge: @Composable (() -> Unit)? = null,
) {
  Column(modifier) {
    Spacer(Modifier.height(48.dp))
    PaymentMethodHandoverIllustration(
      destinationBadge = destinationBadge,
      mark = { PayoutProviderMark(provider, Modifier.size(PaymentMethodMarkSize)) },
    )
    Spacer(Modifier.height(48.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewPayoutMethodHandoverIllustration(
  @PreviewParameter(PayoutPreviewProvider::class) provider: MemberPaymentProvider?,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayoutMethodHandoverIllustration(provider, Modifier.padding(16.dp))
    }
  }
}

private class PayoutPreviewProvider :
  CollectionPreviewParameterProvider<MemberPaymentProvider?>(
    listOf(
      null,
      MemberPaymentProvider.TRUSTLY,
      MemberPaymentProvider.SWISH,
      MemberPaymentProvider.NORDEA,
    ),
  )
