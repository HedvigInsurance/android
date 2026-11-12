package com.hedvig.android.feature.connect.payment.trustly.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.NotAvailableOnDesktop

@Composable
internal actual fun TrustlyCheckoutWebView(
  url: String,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  connectingCardCancelled: () -> Unit,
  modifier: Modifier,
) {
  NotAvailableOnDesktop(modifier)
}
