package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isEmpty
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.BaseActivity
import com.hedvig.app.LoggedInTerminatedActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityLoggedInBinding
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ui.ReferralsInformationActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.isDebug
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import javax.money.MonetaryAmount

class LoggedInActivity : BaseActivity(R.layout.activity_logged_in) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val welcomeViewModel: WelcomeViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val loggedInViewModel: LoggedInViewModel by viewModel()

    private val loggedInTracker: LoggedInTracker by inject()

    private val binding by viewBinding(ActivityLoggedInBinding::bind)

    private var lastLoggedInTab = LoggedInTabs.HOME

    private lateinit var referralTermsUrl: String
    private lateinit var referralsIncentive: MonetaryAmount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            loggedInRoot.setEdgeToEdgeSystemUiFlags(true)
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
            }

            bottomNavigation.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
                loggedInViewModel.updateBottomTabInset(view.measuredHeight)
            }

            loggedInViewModel.scroll.observe(this@LoggedInActivity) { elevation ->
                elevation?.let { toolbar.elevation = it }
            }

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)

            tabContent.adapter = TabPagerAdapter(supportFragmentManager)
            bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                val id = LoggedInTabs.fromId(menuItem.itemId)
                if (id == null) {
                    e { "Programmer error: Invalid menu item chosen" }
                    return@setOnNavigationItemSelectedListener false
                }
                tabContent.setCurrentItem(id.ordinal, false)
                setupToolBar(id)
                true
            }

            if (intent.getBooleanExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION, false)) {
                bottomNavigation.selectedItemId = R.id.referrals
                intent.removeExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION)
            }

            if (intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)) {
                welcomeViewModel.fetch()
                welcomeViewModel.data.observe(lifecycleOwner = this@LoggedInActivity) { data ->
                    if (data != null) {
                        WelcomeDialog.newInstance(data)
                            .show(supportFragmentManager, WelcomeDialog.TAG)
                        loggedInRoot.postDelayed({
                            loggedInRoot.show()
                        }, resources.getInteger(R.integer.slide_in_animation_duration).toLong())
                    }
                }
                intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
            }

            bottomNavigation.itemIconTintList = null

            bindData()
            setupToolBar(LoggedInTabs.fromId(bottomNavigation.selectedItemId))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)) {
            LoggedInTabs.HOME,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.INSURANCE -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
            }
            LoggedInTabs.PROFILE -> {
                menuInflater.inflate(R.menu.profile_settings_menu, menu)
            }
            LoggedInTabs.REFERRALS -> {
                menuInflater.inflate(R.menu.referral_more_info_menu, menu)
            }
            else -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)) {
            LoggedInTabs.HOME,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.INSURANCE -> {
                claimsViewModel.triggerFreeTextChat {
                    startClosableChat()
                }
            }
            LoggedInTabs.PROFILE -> {
                startActivity(SettingsActivity.newInstance(this))
            }
            LoggedInTabs.REFERRALS -> {
                startActivity(
                    ReferralsInformationActivity.newInstance(
                        this,
                        referralTermsUrl,
                        referralsIncentive
                    )
                )
            }
        }
        return true
    }

    private fun bindData() {
        whatsNewViewModel.news.observe(lifecycleOwner = this) { data ->
            data?.let {
                if (data.news.isNotEmpty()) {
                    WhatsNewDialog.newInstance(data.news)
                        .show(supportFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        loggedInViewModel.data.observe(this) { data ->
            data?.let { d ->
                if (binding.bottomNavigation.menu.isEmpty()) {
                    val keyGearEnabled = isDebug() || d.member.features.contains(Feature.KEYGEAR)
                    val referralsEnabled =
                        isDebug() || d.member.features.contains(Feature.REFERRALS)

                    val menuId = when {
                        keyGearEnabled && referralsEnabled -> R.menu.logged_in_menu_key_gear
                        referralsEnabled -> R.menu.logged_in_menu
                        !keyGearEnabled && !referralsEnabled -> R.menu.logged_in_menu_no_referrals
                        else -> R.menu.logged_in_menu
                    }
                    binding.bottomNavigation.inflateMenu(menuId)
                    val initialTab = intent.extras?.getSerializable(INITIAL_TAB) as? LoggedInTabs
                        ?: LoggedInTabs.HOME
                    binding.bottomNavigation.selectedItemId = initialTab.id()
                    setupToolBar(LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId))
                    binding.loggedInRoot.show()
                }

                referralTermsUrl = d.referralTerms.url
                d.referralInformation.campaign.incentive?.asMonthlyCostDeduction?.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                    ?.let { referralsIncentive = it }
            }
        }

        profileViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.member?.id?.let { id ->
                loggedInTracker.setMemberId(id)
            }

        }
        whatsNewViewModel.fetchNews()

        dashboardViewModel.data.observe(lifecycleOwner = this) { data ->
            data?.first?.let { d ->
                if (isTerminated(d.contracts)) {
                    startActivity(LoggedInTerminatedActivity.newInstance(this))
                }
            }
        }
    }

    private fun setupToolBar(id: LoggedInTabs?) {
        if (lastLoggedInTab != id) {
            binding.bottomNavigation.elevation = 0f
            invalidateOptionsMenu()
        }
        if (id != null) {
            lastLoggedInTab = id
        }
    }

    companion object {
        private const val INITIAL_TAB = "INITIAL_TAB"
        fun newInstance(
            context: Context,
            withoutHistory: Boolean = false,
            initialTab: LoggedInTabs = LoggedInTabs.HOME
        ) =
            Intent(context, LoggedInActivity::class.java).apply {
                if (withoutHistory) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                putExtra(INITIAL_TAB, initialTab)
            }

        fun isTerminated(contracts: List<DashboardQuery.Contract>) =
            contracts.isNotEmpty() && contracts.all { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }

        const val EXTRA_IS_FROM_REFERRALS_NOTIFICATION = "extra_is_from_referrals_notification"
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"
    }
}
