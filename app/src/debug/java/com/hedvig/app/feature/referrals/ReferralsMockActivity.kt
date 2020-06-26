package com.hedvig.app.feature.referrals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.loggedInModule
import com.hedvig.app.referralsModule
import kotlinx.android.synthetic.debug.activity_generic_development.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class ReferralsMockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(listOf(loggedInModule, referralsModule))
        loadKoinModules(MOCK_MODULE)

        root.adapter = GenericDevelopmentAdapter(
            listOf(
                GenericDevelopmentAdapter.Item(
                    "Loading"
                ) {
                    MockReferralsViewModel.loadInitially = false
                    startScreen()
                },
                GenericDevelopmentAdapter.Item("Error") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = false
                    }
                    startScreen()
                },
                GenericDevelopmentAdapter.Item("Empty") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                    }
                    startScreen()
                }
            )
        )
    }

    private fun startScreen() = startActivity(
        LoggedInActivity.newInstance(
            this,
            initialTab = LoggedInTabs.REFERRALS
        )
    )

    override fun finish() {
        unloadKoinModules(MOCK_MODULE)
        loadKoinModules(listOf(loggedInModule, referralsModule))
        super.finish()
    }

    companion object {
        private val MOCK_MODULE = module {
            viewModel<ReferralsViewModel> { MockReferralsViewModel() }
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
        }
    }
}
