package com.hedvig.app.feature.onboarding

import com.hedvig.app.MockActivity
import com.hedvig.app.choosePlanModule
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.feature.onboarding.ui.ChoosePlanActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketManagerModule
import com.hedvig.app.mocks.MockMarketManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class OnboardingMockActivity : MockActivity() {
    private val marketManager = MockMarketManager()
    override val original = listOf(choosePlanModule, marketManagerModule)
    override val mocks = listOf(
        module {
            viewModel<MemberIdViewModel> { MockMemberIdViewModel() }
            viewModel<ChoosePlanViewModel> { MockChoosePlanViewModel() }
            single<MarketManager> { marketManager }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Choose Plan")
        clickableItem("Choose plan, Market: Norway") {
            MockMarketManager.mockedMarket = Market.NO
            startActivity(ChoosePlanActivity.newInstance(this@OnboardingMockActivity))
        }
        header("More Options")
        clickableItem("ID error") {
            MockMarketManager.mockedMarket = Market.NO
            MockMemberIdViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@OnboardingMockActivity))
        }
    }
}
