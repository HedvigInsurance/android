package com.hedvig.app.feature.profile

import com.hedvig.app.MockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.profileModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ProfileMockActivity : MockActivity() {
    override val original = listOf(profileModule)
    override val mocks = listOf(module {
        viewModel<ProfileViewModel> { MockProfileViewModel() }
    })

    override fun adapter() = genericDevelopmentAdapter {
        header("Tab")
        clickableItem("Success") {
            MockProfileViewModel.profileData = PROFILE_DATA
            startActivity(
                LoggedInActivity.newInstance(
                    this@ProfileMockActivity,
                    initialTab = LoggedInTabs.PROFILE
                )
            )
        }
    }
}
