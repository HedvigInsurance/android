package com.hedvig.app.feature.profile

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.marketManagerModule
import com.hedvig.app.mocks.MockMarketManager
import com.hedvig.app.profileModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_ADYEN_CONNECTED
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_ADYEN_NOT_CONNECTED
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_BANK_ACCOUNT_ACTIVE
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ProfileMockActivity : MockActivity() {
    override val original = listOf(profileModule, marketManagerModule)
    override val mocks = listOf(
        module {
            viewModel<ProfileViewModel> { MockProfileViewModel() }
            single<MarketManager> { MockMarketManager() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Tab")
        clickableItem("Bank account not connected") {
            MockProfileViewModel.profileData = PROFILE_DATA
            startLoggedInActivity()
        }
        clickableItem("Bank account connected") {
            MockProfileViewModel.profileData = PROFILE_DATA_BANK_ACCOUNT_ACTIVE
            startLoggedInActivity()
        }
        clickableItem("Adyen connected") {
            MockProfileViewModel.profileData = PROFILE_DATA_ADYEN_CONNECTED
            startLoggedInActivity()
        }
        clickableItem("Adyen not connected") {
            MockProfileViewModel.profileData = PROFILE_DATA_ADYEN_NOT_CONNECTED
            startLoggedInActivity()
        }

        marketSpinner { MockMarketManager.mockedMarket = it }
    }

    private fun startLoggedInActivity() {
        startActivity(
            LoggedInActivity.newInstance(
                this@ProfileMockActivity,
                initialTab = LoggedInTabs.PROFILE
            )
        )
    }
}
