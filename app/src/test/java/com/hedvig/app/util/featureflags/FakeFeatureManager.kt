package com.hedvig.app.util.featureflags

import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.featureflags.flags.DevFeatureFlagProvider
import com.hedvig.app.util.featureflags.flags.FeatureFlagProvider
import com.hedvig.app.util.featureflags.loginmethod.DevLoginMethodProvider
import com.hedvig.app.util.featureflags.loginmethod.LoginMethodProvider
import com.hedvig.app.util.featureflags.paymenttype.DevPaymentTypeProvider
import com.hedvig.app.util.featureflags.paymenttype.PaymentTypeProvider

class FakeFeatureManager(
    private val featureFlagProvider: FeatureFlagProvider,
    private val loginMethodProvider: LoginMethodProvider,
    private val paymentTypeProvider: PaymentTypeProvider,
) : FeatureManager,
    FeatureFlagProvider by featureFlagProvider,
    LoginMethodProvider by loginMethodProvider,
    PaymentTypeProvider by paymentTypeProvider {
    companion object {
        /**
         * With the features not being only booleans no more, we need another solution for tests than
         * [com.hedvig.app.util.FeatureFlagRule]
         * Not sure if this is the correct API for this but I had to add something to get it working, and we can
         * explore what makes more sense going forward.
         */
        operator fun invoke(
            market: Market,
        ): FeatureManager {
            val marketManager = object : MarketManager {
                override val enabledMarkets: List<Market> = listOf(Market.SE, Market.NO, Market.DK)
                override var market: Market? = market
                override var hasSelectedMarket: Boolean = true
            }
            return FakeFeatureManager(
                DevFeatureFlagProvider(marketManager),
                DevLoginMethodProvider(marketManager),
                DevPaymentTypeProvider(marketManager),
            )
        }
    }
}
