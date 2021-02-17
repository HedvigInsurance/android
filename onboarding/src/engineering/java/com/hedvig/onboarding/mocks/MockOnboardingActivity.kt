package com.hedvig.onboarding.mocks

import com.hedvig.app.MockActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.onboarding.chooseplan.ui.ChoosePlanActivity
import com.hedvig.onboarding.embark.ui.MoreOptionsActivity
import org.koin.core.module.Module

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
            startActivity(ChoosePlanActivity.newInstance(this@MockOnboardingActivity))
        }
        header("More Options")
        clickableItem("ID error") {
            MockMoreOptionsViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@MockOnboardingActivity))
        }
    }
}
