package com.hedvig.onboarding.mocks

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketManagerModule
import com.hedvig.app.mocks.MockMarketManager
import com.hedvig.onboarding.chooseplan.ChoosePlanModule.choosePlanModule
import com.hedvig.onboarding.chooseplan.ChoosePlanViewModel
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModel
import com.hedvig.onboarding.chooseplan.ui.ChoosePlanActivity
import com.hedvig.onboarding.embark.ui.MoreOptionsActivity
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class MockOnboardingActivity : MockActivity() {

    private val marketManager = MockMarketManager()
    override val original = listOf(choosePlanModule, marketManagerModule)

    override val mocks = listOf(
        module {
            viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
            viewModel<ChoosePlanViewModel> { MockChoosePlanViewModel() }
            single<MarketManager> { marketManager }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Choose Plan")
        clickableItem("Choose plan, Market: Norway") {
            MockMarketManager.mockedMarket = Market.NO
            startActivity(ChoosePlanActivity.newInstance(this@MockOnboardingActivity))
        }
        header("More Options")
        clickableItem("ID error") {
            MockMarketManager.mockedMarket = Market.NO
            MockMoreOptionsViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@MockOnboardingActivity))
        }
    }
}
