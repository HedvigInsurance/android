package com.hedvig.app.feature.payment

import android.content.Context
import com.hedvig.android.market.Market

@Deprecated(
  "Replace with navigating to AppDestination.ConnectPaymentTrustly|AppDestination.ConnectPaymentAdyen",
  level = DeprecationLevel.ERROR,
)
fun connectPayinIntent(context: Context, market: Market, isPostSign: Boolean) = when (market) {
  Market.SE -> {}
  Market.NO -> {}
  Market.DK -> {}
}
