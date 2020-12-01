package com.hedvig.app.feature.onboarding

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.onbarding.MoreOptionsViewModel
import com.hedvig.app.feature.onbarding.ui.MoreOptionsActivity
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.onboardingModule
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class OnboardingMockActivity : MockActivity() {
    override val original = listOf(onboardingModule)
    override val mocks = listOf(module {
        viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("More Options")
        clickableItem("ID error") {
            MockMoreOptionsViewModel.shouldLoad = false
            startActivity(MoreOptionsActivity.newInstance(this@OnboardingMockActivity))
        }
    }
}
