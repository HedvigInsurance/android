package com.hedvig.app.feature.onboarding

import com.hedvig.app.MockActivity
import com.hedvig.app.choosePlanModule
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.feature.onboarding.ui.MoreOptionsActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketProviderModule
import com.hedvig.app.mocks.MockMarketProvider
import com.hedvig.app.onboardingModule
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class OnboardingMockActivity : MockActivity() {
    private val marketProvider = MockMarketProvider()
    override val original = listOf(onboardingModule, choosePlanModule, marketProviderModule)
    override val mocks = listOf(module {
        viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
        viewModel<ChoosePlanViewModel> { MockChoosePlanViewModel() }
        single<MarketProvider> { marketProvider }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Choose Plan")
        clickableItem("Choose plan, Market: Norway") {
            MockMarketProvider.mockedMarket = Market.NO
            startActivity(ChoosePlanActivity.newInstance(this@OnboardingMockActivity))
        }
        header("More Options")
        clickableItem("ID error") {
            MockMarketProvider.mockedMarket = Market.NO
            MockMoreOptionsViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@OnboardingMockActivity))
        }
    }
}
