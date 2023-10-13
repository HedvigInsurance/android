package com.hedvig.app.feature.payment

import android.content.Context
import com.hedvig.android.market.Market
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity

fun connectPayinIntent(
  context: Context,
  market: Market,
  isPostSign: Boolean,
) = when (market) {
  Market.SE -> {
    TrustlyConnectPayinActivity.newInstance(context, isPostSign)
  }
  Market.NO,
  Market.DK,
  -> {
    AdyenConnectPayinActivity.newInstance(context, AdyenCurrency.fromMarket(market), isPostSign)
  }
}
