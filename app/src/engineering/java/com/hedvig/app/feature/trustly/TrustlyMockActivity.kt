package com.hedvig.app.feature.trustly

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketManagerModule
import com.hedvig.app.mocks.MockMarketManager
import org.koin.dsl.module

class TrustlyMockActivity : MockActivity() {
    private val marketManager = MockMarketManager()
    override val original = listOf(marketManagerModule)
    override val mocks = listOf(module { single<MarketManager> { marketManager } })

    init {
        MockMarketManager.mockedMarket = Market.SE
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
