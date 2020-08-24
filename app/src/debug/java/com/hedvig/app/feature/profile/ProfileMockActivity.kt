package com.hedvig.app.feature.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.profileModule
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class ProfileMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    private val binding by viewBinding(ActivityGenericDevelopmentBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(profileModule)
        loadKoinModules(MOCK_MODULE)

        binding.root.adapter = genericDevelopmentAdapter {
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

    companion object {
        private val MOCK_MODULE = module {
            viewModel<ProfileViewModel> { MockProfileViewModel() }
        }
    }
}
