package com.hedvig.app.feature.profile

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.profileModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA_BANK_ACCOUNT_ACTIVE
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ProfileMockActivity : MockActivity() {
    override val original = listOf(profileModule)
    override val mocks = listOf(module {
        viewModel<ProfileViewModel> { MockProfileViewModel() }
    })

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
