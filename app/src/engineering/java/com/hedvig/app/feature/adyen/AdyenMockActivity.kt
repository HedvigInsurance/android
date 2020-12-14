package com.hedvig.app.feature.adyen

import com.hedvig.app.MockActivity
import com.hedvig.app.adyenModule
import com.hedvig.app.mocks.MockMarketProvider
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketProviderModule
import com.hedvig.app.util.extensions.makeToast
import org.koin.dsl.module

class AdyenMockActivity : MockActivity() {
    private val marketProvider = MockMarketProvider()
    override val original = listOf(adyenModule, marketProviderModule)
    override val mocks = listOf(module {
        single<AdyenViewModel> { MockAdyenViewModel() }
        single<MarketProvider> { marketProvider }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Adyen Connect Payment Screen")
        clickableItem("Not Post-Sign") {
            val currency = runCatching {
                AdyenCurrency.fromMarket(marketProvider.market!!)
            }

            if (currency.isFailure) {
                makeToast("Adyen is not supported in this market")
                return@clickableItem
            }
            startActivity(
                AdyenConnectPayinActivity.newInstance(
                    this@AdyenMockActivity,
                    currency.getOrThrow()
                )
            )
        }
        clickableItem("Post-Sign") {
            val currency = runCatching {
                AdyenCurrency.fromMarket(marketProvider.market!!)
            }

            if (currency.isFailure) {
                makeToast("Adyen is not supported in this market")
                return@clickableItem
            }
            startActivity(
                AdyenConnectPayinActivity.newInstance(
                    this@AdyenMockActivity,
                    currency.getOrThrow(),
                    isPostSign = true
                )
            )
        }
        header("Market")
        marketSpinner { MockMarketProvider.mockedMarket = it }
    }
}
