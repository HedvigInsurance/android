package com.hedvig.app.feature.loggedin

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.testdata.feature.loggedin.NO_WHATS_NEW
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW
import com.hedvig.app.whatsNewModule
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class LoggedInMockActivity : MockActivity() {
    override val original = listOf(whatsNewModule)
    override val mocks = listOf(
        module {
            viewModel<WhatsNewViewModel> { MockWhatsNewViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Logged in Activity")
        clickableItem("Whats New") {
            MockWhatsNewViewModel.whatsNewData = WHATS_NEW

            startActivity(LoggedInActivity.newInstance(this@LoggedInMockActivity))
        }
        clickableItem("welcome-screen") {
            MockWhatsNewViewModel.whatsNewData = NO_WHATS_NEW
            startActivity(
                LoggedInActivity.newInstance(this@LoggedInMockActivity)
                    .putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
            )
        }
    }
}
