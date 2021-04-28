package com.hedvig.app.feature.referrals

import android.content.Intent
import android.net.Uri
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.hedvig.app.MockActivity
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedActivity
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedViewModel
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.loggedInModule
import com.hedvig.app.referralsModule
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
import com.hedvig.app.testdata.feature.referrals.builders.EditCodeDataBuilder
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class ReferralsMockActivity : MockActivity() {
    override val original = listOf(loggedInModule, referralsModule)
    override val mocks = listOf(
        module {
            viewModel<ReferralsViewModel> { MockReferralsViewModel() }
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<ReferralsActivatedViewModel> { MockReferralsActivatedViewModel() }
            viewModel<ReferralsEditCodeViewModel> { MockReferralsEditCodeViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Referrals Tab")
        clickableItem("Loading") {
            MockReferralsViewModel.loadInitially = false
            startReferralsTab()
        }
        clickableItem("Error") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = false
            }
            startReferralsTab()
        }
        clickableItem("Empty") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = true
                hasLoadedOnce = false
                afterRefreshData = REFERRALS_DATA_WITH_NO_DISCOUNTS
            }
            startReferralsTab()
        }
        clickableItem("One Referee") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = true
                hasLoadedOnce = false
                referralsData = REFERRALS_DATA_WITH_ONE_REFEREE
                afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE
            }
            startReferralsTab()
        }
        clickableItem("Multiple Referrals") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = true
                hasLoadedOnce = false
                referralsData = REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
                afterRefreshData =
                    REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
            }
            startReferralsTab()
        }
        clickableItem("One Referee + Another Discount") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = true
                hasLoadedOnce = false
                referralsData = REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
                afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
            }
            startReferralsTab()
        }
        clickableItem("Pull to Refresh, Empty then One Referee") {
            MockReferralsViewModel.apply {
                loadInitially = true
                shouldSucceed = true
                referralsData = REFERRALS_DATA_WITH_NO_DISCOUNTS
                hasLoadedOnce = false
                afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE
            }
            startReferralsTab()
        }
        header("Referrals Activated Screen")
        clickableItem("Load quickly") {
            MockReferralsActivatedViewModel.loadDelay = 1000
            startActivity(ReferralsActivatedActivity.newInstance(this@ReferralsMockActivity))
        }
        clickableItem("Load slowly") {
            MockReferralsActivatedViewModel.loadDelay = 5000
            startActivity(ReferralsActivatedActivity.newInstance(this@ReferralsMockActivity))
        }
        header("Edit Code Screen")
        clickableItem("Stay in Submit") {
            MockReferralsEditCodeViewModel.shouldLoad = false
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Success") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.SUCCESS
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Error") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = false
                variant = EditCodeDataBuilder.ResultVariant.SUCCESS
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Code already taken") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.ALREADY_TAKEN
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Code too short") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.TOO_SHORT
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Code too long") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.TOO_LONG
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Too many code changes") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.EXCEEDED_MAX_UPDATES
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        clickableItem("Unknown result type") {
            MockReferralsEditCodeViewModel.apply {
                shouldLoad = true
                shouldSucceed = true
                variant = EditCodeDataBuilder.ResultVariant.UNKNOWN
            }
            startActivity(
                ReferralsEditCodeActivity.newInstance(
                    this@ReferralsMockActivity,
                    "TEST123"
                )
            )
        }
        header("Notifications")
        clickableItem("Referrals Enabled") {
            ReferralsNotificationManager.sendReferralsEnabledNotification(
                this@ReferralsMockActivity
            )
        }
        clickableItem("Referrals Success") {
            ReferralsNotificationManager.sendReferralNotification(
                this@ReferralsMockActivity,
                RemoteMessage(
                    bundleOf(
                        ReferralsNotificationManager.DATA_MESSAGE_REFERRED_SUCCESS_NAME to "William"
                    )
                )
            )
        }
        header("Deep Links")
        clickableItem("`/forever`-Deep Link") {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://${getString(R.string.FIREBASE_LINK_DOMAIN)}/forever")
                }
            )
        }
    }

    private fun startReferralsTab() = startActivity(
        LoggedInActivity.newInstance(
            this,
            initialTab = LoggedInTabs.REFERRALS
        )
    )
}
