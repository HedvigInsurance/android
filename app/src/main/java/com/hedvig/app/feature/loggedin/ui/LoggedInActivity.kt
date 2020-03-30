package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.BaseActivity
import com.hedvig.app.BuildConfig
import com.hedvig.app.LoggedInTerminatedActivity
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ReferralBottomSheet
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.isDebug
import com.hedvig.app.util.extensions.monthlyCostDeductionIncentive
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.showShareSheet
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.activity_logged_in.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class LoggedInActivity : BaseActivity(R.layout.activity_logged_in) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val tabViewModel: BaseTabViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val welcomeViewModel: WelcomeViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()

    private val profileTracker: ProfileTracker by inject()
    private val loggedInTracker: LoggedInTracker by inject()

    private var lastLoggedInTab = LoggedInTabs.DASHBOARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.updatePadding(end = resources.getDimensionPixelSize(R.dimen.base_margin_double))

        tabContentContainer.adapter = TabPagerAdapter(supportFragmentManager)
        bottomTabs.setOnNavigationItemSelectedListener { menuItem ->
            val id = LoggedInTabs.fromId(menuItem.itemId)
            tabContentContainer.setCurrentItem(id.ordinal, false)
            setupAppBar(id)
            setupFloatingButton(id)
            true
        }

        if (intent.getBooleanExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION, false)) {
            bottomTabs.selectedItemId = R.id.referrals
            intent.removeExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION)
        }

        if (intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)) {
            welcomeViewModel.fetch()
            welcomeViewModel.data.observe(lifecycleOwner = this) { data ->
                if (data != null) {
                    WelcomeDialog.newInstance(data).show(supportFragmentManager, WelcomeDialog.TAG)
                    loggedInRoot.postDelayed({
                        loggedInRoot.show()
                    }, resources.getInteger(R.integer.slide_in_animation_duration).toLong())
                } else {
                    loggedInRoot.show()
                }
            }
            intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
        } else {
            loggedInRoot.show()
        }

        bindData()
        setupAppBar(LoggedInTabs.fromId(bottomTabs.selectedItemId))
    }

    private fun setupFloatingButton(id: LoggedInTabs) = when (id) {
        LoggedInTabs.REFERRALS -> referralButton.show()
        else -> referralButton.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.CLAIMS -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
            }
            LoggedInTabs.PROFILE -> {
                menuInflater.inflate(R.menu.profile_settings_menu, menu)
            }
            LoggedInTabs.REFERRALS -> {
                menuInflater.inflate(R.menu.referral_more_info_menu, menu)
            }
            LoggedInTabs.KEY_GEAR -> {
                // TODO
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.CLAIMS -> {
                claimsViewModel.triggerFreeTextChat {
                    startClosableChat()
                }
            }
            LoggedInTabs.PROFILE -> {
                startActivity(SettingsActivity.newInstance(this))
            }
            LoggedInTabs.REFERRALS -> {
                profileViewModel.data.value?.referralInformation?.campaign?.incentive?.asMonthlyCostDeduction?.amount?.amount?.toBigDecimal()
                    ?.toInt()?.toString()?.let { amount ->
                        ReferralBottomSheet.newInstance(amount)
                            .show(supportFragmentManager, ReferralBottomSheet.TAG)
                    }
            }
            LoggedInTabs.KEY_GEAR -> {
                // TODO
            }
        }
        return true
    }

    private fun bindData() {
        var badge: View? = null

        tabViewModel.tabNotification.observe(lifecycleOwner = this) { tab ->
            if (tab == null) {
                badge?.findViewById<ImageView>(R.id.notificationIcon)?.remove()
            } else {
                when (tab) {
                    TabNotification.REFERRALS -> {
                        val position = when (bottomTabs.menu.size()) {
                            4 -> 3
                            5 -> 4
                            else -> 3
                        }
                        val itemView =
                            (bottomTabs.getChildAt(0) as BottomNavigationMenuView).getChildAt(
                                position
                            ) as BottomNavigationItemView

                        badge = layoutInflater.inflate(
                            R.layout.bottom_navigation_notification,
                            itemView,
                            true
                        )
                    }
                }
            }
        }

        whatsNewViewModel.news.observe(lifecycleOwner = this) { data ->
            data?.let {
                if (data.news.size > 0) {
                    // Yep, this is actually happening
                    GlobalScope.launch(Dispatchers.IO) {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                    }
                    WhatsNewDialog.newInstance(data.news)
                        .show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        profileViewModel.data.observe(lifecycleOwner = this) { data ->
            safeLet(
                data?.referralInformation?.campaign?.monthlyCostDeductionIncentive()?.amount?.amount?.toBigDecimal()?.toDouble(),
                data?.referralInformation?.campaign?.code
            ) { incentive, code -> bindReferralsButton(incentive, code) }

            data?.member?.id?.let { id ->
                loggedInTracker.setMemberId(id)
            }

            val keyGearEnabled = isDebug() || data?.member?.features?.contains(Feature.KEYGEAR) ?: false

            if (keyGearEnabled && bottomTabs.menu.size() != 5) {
                bottomTabs.menu.clear()
                bottomTabs.inflateMenu(R.menu.logged_in_menu_key_gear)
            } else if (!keyGearEnabled && bottomTabs.menu.size() != 4) {
                bottomTabs.menu.clear()
                bottomTabs.inflateMenu(R.menu.logged_in_menu)
            }
        }
        whatsNewViewModel.fetchNews()

        dashboardViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.let { d ->
                if (isTerminated(d.contracts)) {
                    startActivity(LoggedInTerminatedActivity.newInstance(this))
                }
            }
        }
    }

    private fun bindReferralsButton(incentive: Double, code: String) {
        referralButton.setHapticClickListener {
            profileTracker.clickReferral(incentive.toInt())
            showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
                intent.apply {
                    putExtra(
                        Intent.EXTRA_TEXT,
                        interpolateTextKey(
                            resources.getString(R.string.REFERRAL_SMS_MESSAGE),
                            "REFERRAL_VALUE" to incentive.toBigDecimal().toInt().toString(),
                            "REFERRAL_CODE" to code,
                            "REFERRAL_LINK" to BuildConfig.REFERRALS_LANDING_BASE_URL + code
                        )
                    )
                    type = "text/plain"
                }
            }
        }
    }

    private fun setupAppBar(id: LoggedInTabs) {
        invalidateOptionsMenu()
        if (lastLoggedInTab != id) {
            appBarLayout.setExpanded(true, false)
        }
        when (id) {
            LoggedInTabs.DASHBOARD -> {
                setupLargeTitle(R.string.DASHBOARD_SCREEN_TITLE)
            }
            LoggedInTabs.CLAIMS -> {
                setupLargeTitle(R.string.CLAIMS_TITLE)
            }
            LoggedInTabs.KEY_GEAR -> {
                setupLargeTitle(getString(R.string.KEY_GEAR_TAB_TITLE))
            }
            LoggedInTabs.REFERRALS -> {
                setupLargeTitle(R.string.PROFILE_REFERRAL_TITLE)
            }
            LoggedInTabs.PROFILE -> {
                setupLargeTitle(R.string.PROFILE_TITLE)
            }
        }
        lastLoggedInTab = id
    }

    companion object {
        fun newInstance(context: Context, withoutHistory: Boolean = false) = Intent(context, LoggedInActivity::class.java).apply {
            if (withoutHistory) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        }

        fun isTerminated(contracts: List<DashboardQuery.Contract>) = contracts.isNotEmpty() && contracts.all { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }

        const val EXTRA_IS_FROM_REFERRALS_NOTIFICATION = "extra_is_from_referrals_notification"
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"
    }
}
