package com.hedvig.app.feature.referrals

import android.content.Intent
import android.net.Uri
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
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeActivity
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.loggedInModule
import com.hedvig.app.referralsModule
import com.hedvig.app.service.push.managers.ReferralsNotificationManager
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
import com.hedvig.app.testdata.feature.referrals.builders.EditCodeDataBuilder
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
                        hasLoadedOnce = false
                        afterRefreshData = REFERRALS_DATA_WITH_NO_DISCOUNTS
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("One Referee") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        hasLoadedOnce = false
                        referralsData = REFERRALS_DATA_WITH_ONE_REFEREE
                        afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Multiple Referrals") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        hasLoadedOnce = false
                        referralsData = REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
                        afterRefreshData =
                            REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("One Referee + Another Discount") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        hasLoadedOnce = false
                        referralsData = REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
                        afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
                    }
                    startReferralsTab()
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Pull to Refresh, Empty then One Referee") {
                    MockReferralsViewModel.apply {
                        loadInitially = true
                        shouldSucceed = true
                        referralsData = REFERRALS_DATA_WITH_NO_DISCOUNTS
                        hasLoadedOnce = false
                        afterRefreshData = REFERRALS_DATA_WITH_ONE_REFEREE
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
                GenericDevelopmentAdapter.Item.Header("Edit Code Screen"),
                GenericDevelopmentAdapter.Item.ClickableItem("Success") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.SUCCESS
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Error") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.SUCCESS
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Code already taken") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.ALREADY_TAKEN
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Code too short") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.TOO_SHORT
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Code too long") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.TOO_LONG
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Too many code changes") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.EXCEEDED_MAX_UPDATES
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
                },
                GenericDevelopmentAdapter.Item.ClickableItem("Unknown result type") {
                    MockReferralsEditCodeViewModel.apply {
                        shouldSucceed = true
                        variant = EditCodeDataBuilder.ResultVariant.UNKNOWN
                    }
                    startActivity(ReferralsEditCodeActivity.newInstance(this, "TEST123"))
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
                },
                GenericDevelopmentAdapter.Item.Header("Deep Links"),
                GenericDevelopmentAdapter.Item.ClickableItem("`/forever`-Deep Link") {
                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data =
                            Uri.parse("https://${getString(R.string.FIREBASE_LINK_DOMAIN)}/forever")
                    })
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
            viewModel<ReferralsEditCodeViewModel> { MockReferralsEditCodeViewModel() }
        }
    }
}
