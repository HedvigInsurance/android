package com.hedvig.android.feature.payin.account.ui.components

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
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.Surface
import octopus.type.MemberPaymentProvider

@Composable
internal fun PayinMethodHandoverIllustration(provider: MemberPaymentProvider?, modifier: Modifier = Modifier) {
  Column(modifier) {
    Spacer(Modifier.height(48.dp))
    PaymentMethodHandoverIllustration(
      mark = { PayinProviderMark(provider, Modifier.size(PaymentMethodMarkSize)) },
    )
    Spacer(Modifier.height(48.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewPayinMethodHandoverIllustration(
  @PreviewParameter(PayinProviderProvider::class) provider: MemberPaymentProvider?,
) {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      PayinMethodHandoverIllustration(
        provider,
        Modifier.padding(16.dp),
      )
    }
  }
}

private class PayinProviderProvider :
  CollectionPreviewParameterProvider<MemberPaymentProvider?>(
    listOf(
      null,
      MemberPaymentProvider.TRUSTLY,
      MemberPaymentProvider.SWISH,
      MemberPaymentProvider.INVOICE,
    ),
  )
