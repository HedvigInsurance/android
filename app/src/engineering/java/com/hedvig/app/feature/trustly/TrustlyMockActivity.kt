package com.hedvig.app.feature.trustly

import com.hedvig.app.MockActivity
import com.hedvig.app.mocks.MockMarketProvider
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketProviderModule
import org.koin.dsl.module

class TrustlyMockActivity : MockActivity() {
    private val marketProvider = MockMarketProvider()
    override val original = listOf(marketProviderModule)
    override val mocks = listOf(module { single<MarketProvider> { marketProvider } })

    init {
        MockMarketProvider.mockedMarket = Market.SE
    }

    override fun adapter() = genericDevelopmentAdapter {
        header("Trustly Connect Payment Screen")
        clickableItem("Not Post-Sign") {
            startActivity(TrustlyConnectPayinActivity.newInstance(this@TrustlyMockActivity))
        }
        clickableItem("Post-Sign") {
            startActivity(TrustlyConnectPayinActivity.newInstance(this@TrustlyMockActivity, true))
        }
    }
}
