package com.hedvig.app.feature.loggedin

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.genericDevelopmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.koin.core.module.Module

@AndroidEntryPoint
class LoggedInMockActivity : MockActivity() {
    override val original = listOf<Module>()
    override val mocks = listOf<Module>()

    @Inject
    lateinit var mockRepository: WhatsNewRepository

    override fun adapter() = genericDevelopmentAdapter {
        header("Logged in Activity")
        clickableItem("Whats New") {
            // MockWhatsNewViewModel.whatsNewData = WHATS_NEW
            startActivity(LoggedInActivity.newInstance(this@LoggedInMockActivity))
        }
        clickableItem("welcome-screen") {
            // MockWhatsNewViewModel.whatsNewData = NO_WHATS_NEW
            startActivity(
                LoggedInActivity.newInstance(this@LoggedInMockActivity)
                    .putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
            )
        }
    }
}
