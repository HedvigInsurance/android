package com.hedvig.app.feature.referrals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.GenericDevelopmentAdapter
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedActivity
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedViewModel
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.loggedInModule
import com.hedvig.app.referralsModule
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
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
                GenericDevelopmentAdapter.Item.Header("Referrals Tab"),
                GenericDevelopmentAdapter.Item.ClickableItem("Loading") {
                    MockReferralsViewModel.loadInitially = false
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Error") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = false
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Empty") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("One Referee") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        referralsData = REFERRALS_DATA_WITH_ONE_REFEREE
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Multiple Referrals") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        referralsData = REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("One Referee + Another Discount") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        referralsData = REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.Header("Referrals Activated Screen"),
                GenericDevelopmentAdapter.Item.ClickableItem("Load quickly") {
                    MockReferralsActivatedViewModel.loadDelay = 1000
                    startActivity(ReferralsActivatedActivity.newInstance(this))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Load slowly") {
                    MockReferralsActivatedViewModel.loadDelay = 5000
                    startActivity(ReferralsActivatedActivity.newInstance(this))
                },
                GenericDevelopmentAdapter.Item.Header("Notifications"),
                GenericDevelopmentAdapter.Item.ClickableItem(
                    "Referrals Enabled"
                ) { ReferralsNotificationManager.sendReferralsEnabledNotification(this) },
                GenericDevelopmentAdapter.Item.ClickableItem(
                    "Referrals Success"
                ) {
                    ReferralsNotificationManager.sendReferralNotification(
                        this, RemoteMessage(
                            bundleOf(
                                ReferralsNotificationManager.DATA_MESSAGE_REFERRED_SUCCESS_NAME to "William"
                            )
                        )
                    )
                }
            )
        )
    }

    private fun startReferralsTab() = startActivity(
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
            viewModel<ReferralsActivatedViewModel> { MockReferralsActivatedViewModel() }
        }
    }
}
