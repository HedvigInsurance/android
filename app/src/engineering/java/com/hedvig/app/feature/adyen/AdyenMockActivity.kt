package com.hedvig.app.feature.adyen

import com.hedvig.app.MockActivity
import com.hedvig.app.adyenModule
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutActivity
import com.hedvig.app.feature.adyen.payout.AdyenConnectPayoutViewModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketManagerModule
import com.hedvig.app.mocks.MockMarketManager
import com.hedvig.app.util.extensions.makeToast
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AdyenMockActivity : MockActivity() {
    private val marketManager = MockMarketManager()
    override val original = listOf(adyenModule, marketManagerModule)
    override val mocks = listOf(
        module {
            viewModel<AdyenConnectPayinViewModel> { MockAdyenConnectPayinViewModel() }
            viewModel<AdyenConnectPayoutViewModel> { MockAdyenConnectPayoutViewModel() }
            single<MarketManager> { marketManager }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Adyen Connect Payment Screen")
        clickableItem("Not Post-Sign") {
            val currency = runCatching {
                AdyenCurrency.fromMarket(marketManager.market!!)
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
                AdyenCurrency.fromMarket(marketManager.market!!)
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
        header("Adyen Connect Payout Screen")
        clickableItem("Open") {
            val currency = runCatching {
                AdyenCurrency.fromMarket(marketManager.market!!)
            }

            if (currency.isFailure) {
                makeToast("Adyen is not supported in this market")
                return@clickableItem
            }
            startActivity(
                AdyenConnectPayoutActivity.newInstance(
                    context,
                    currency.getOrThrow()
                )
            )
        }
        header("Market")
        marketSpinner { MockMarketManager.mockedMarket = it }
    }
}
