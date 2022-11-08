package com.hedvig.app.feature.payment

import android.content.Context
import com.hedvig.android.market.Market
import com.hedvig.app.feature.adyen.AdyenCurrency
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.hedvig.hanalytics.PaymentType

fun connectPayinIntent(
  context: Context,
  paymentType: PaymentType,
  market: Market,
  isPostSign: Boolean,
) = when (paymentType) {
  PaymentType.ADYEN -> {
    AdyenConnectPayinActivity.newInstance(context, AdyenCurrency.fromMarket(market), isPostSign)
  }
  PaymentType.TRUSTLY -> {
    TrustlyConnectPayinActivity.newInstance(context, isPostSign)
  }
}
