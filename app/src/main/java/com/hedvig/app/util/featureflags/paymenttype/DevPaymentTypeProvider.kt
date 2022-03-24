package com.hedvig.app.util.featureflags.paymenttype

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.hanalytics.PaymentType

class DevPaymentTypeProvider(
    private val marketManager: MarketManager,
) : PaymentTypeProvider {
    // todo actually check what this needs to be in our dev environment
    override suspend fun getPaymentType(): PaymentType {
        return when (marketManager.market) {
            Market.SE -> PaymentType.ADYEN
            Market.NO -> PaymentType.TRUSTLY
            Market.DK -> PaymentType.TRUSTLY
            Market.FR -> throw IllegalArgumentException()
            null -> PaymentType.TRUSTLY
        }
    }
}
