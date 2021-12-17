package com.hedvig.app.feature.adyen.payin

import android.app.Activity
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.hedvig.app.R
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.getLocale
import com.hedvig.app.isDebug

fun Activity.startAdyenPayment(market: Market?, paymentMethods: PaymentMethodsApiResponse) {
    val cardConfig = CardConfiguration.Builder(this, getString(R.string.ADYEN_CLIENT_KEY))
        .setShowStorePaymentField(false)
        .setEnvironment(getEnvironment())
        .build()

    val googlePayConfig =
        GooglePayConfiguration.Builder(this, getString(R.string.ADYEN_CLIENT_KEY))
            .setEnvironment(getEnvironment())
            .setGooglePayEnvironment(
                if (isDebug()) {
                    AdyenConnectPayinActivity.GOOGLE_WALLET_ENVIRONMENT_TEST
                } else {
                    AdyenConnectPayinActivity.GOOGLE_WALLET_ENVIRONMENT_PRODUCTION
                }
            )
            .build()

    val dropInConfiguration = DropInConfiguration
        .Builder(
            this,
            AdyenPayinDropInService::class.java,
            getString(R.string.ADYEN_CLIENT_KEY)
        )
        .addCardConfiguration(cardConfig)
        .addGooglePayConfiguration(googlePayConfig)
        .setShopperLocale(getLocale(this, market))
        .setEnvironment(getEnvironment())
        .build()

    DropIn.startPayment(this, paymentMethods, dropInConfiguration)
    // trackingFacade.track("connect_payment_visible")
}

private fun getEnvironment() = if (isDebug()) {
    Environment.TEST
} else {
    Environment.EUROPE
}
