package com.hedvig.app.feature.loggedin.ui

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.BuildConfig
import com.hedvig.app.LoggedInActivity
import com.hedvig.app.R
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ReferralBottomSheet
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
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
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.logged_in_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class LoggedInFragment : androidx.fragment.app.Fragment() {

    private val claimsViewModel: ClaimsViewModel by sharedViewModel()
    private val tabViewModel: BaseTabViewModel by sharedViewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()
    private val welcomeViewModel: WelcomeViewModel by viewModel()

    private val profileTracker: ProfileTracker by inject()

    private var lastLoggedInTab = LoggedInTabs.DASHBOARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.logged_in_screen, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.updatePadding(end = resources.getDimensionPixelSize(R.dimen.base_margin_double))

        tabContentContainer.adapter = TabPagerAdapter(childFragmentManager)
        bottomTabs.setOnNavigationItemSelectedListener { menuItem ->
            val id = LoggedInTabs.fromId(menuItem.itemId)
            tabContentContainer.setCurrentItem(id.ordinal, false)
            setupAppBar(id)
            setupFloatingButton(id)
            true
        }

        if (requireActivity().intent.getBooleanExtra(LoggedInActivity.EXTRA_IS_FROM_REFERRALS_NOTIFICATION, false)) {
            bottomTabs.selectedItemId = R.id.referrals
            requireActivity().intent.removeExtra(LoggedInActivity.EXTRA_IS_FROM_REFERRALS_NOTIFICATION)
        }

        if (requireActivity().intent.getBooleanExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, false)) {
            welcomeViewModel.fetch()
            welcomeViewModel.data.observe(this) { data ->
                if (data != null) {
                    WelcomeDialog.newInstance(data).show(requireFragmentManager(), WelcomeDialog.TAG)
                    view.postDelayed({
                        view.show()
                    }, resources.getInteger(R.integer.slide_in_animation_duration).toLong())
                } else {
                    view.show()
                }
            }
            requireActivity().intent.removeExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING)
        } else {
            view.show()
        }

        bindData()
        setupAppBar(LoggedInTabs.fromId(bottomTabs.selectedItemId))
    }

    private fun setupFloatingButton(id: LoggedInTabs) = when (id) {
        LoggedInTabs.DASHBOARD, LoggedInTabs.CLAIMS, LoggedInTabs.PROFILE -> referralButton.remove()
        LoggedInTabs.REFERRALS -> referralButton.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.CLAIMS,
            LoggedInTabs.PROFILE -> {
                inflater.inflate(R.menu.base_tab_menu, menu)
            }
            LoggedInTabs.REFERRALS -> {
                inflater.inflate(R.menu.referral_more_info_menu, menu)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (LoggedInTabs.fromId(bottomTabs.selectedItemId)) {
            LoggedInTabs.DASHBOARD,
            LoggedInTabs.CLAIMS,
            LoggedInTabs.PROFILE -> {
                claimsViewModel.triggerFreeTextChat {
                    requireActivity().startClosableChat()
                }
            }
            LoggedInTabs.REFERRALS -> {
                (profileViewModel.data.value?.referralInformation?.campaign?.incentive as? ProfileQuery.AsMonthlyCostDeduction)?.amount?.amount?.toBigDecimal()
                    ?.toInt()?.toString()?.let { amount ->
                        ReferralBottomSheet.newInstance(amount).show(childFragmentManager, ReferralBottomSheet.TAG)
                    }
            }
        }
        return true
    }

    private fun bindData() {
        var badge: View? = null

        tabViewModel.tabNotification.observe(this) { tab ->
            if (tab == null) {
                badge?.findViewById<ImageView>(R.id.notificationIcon)?.remove()
            } else {
                when (tab) {
                    TabNotification.REFERRALS -> {
                        val itemView =
                            (bottomTabs.getChildAt(0) as BottomNavigationMenuView).getChildAt(
                                LoggedInTabs.REFERRALS.ordinal
                            ) as BottomNavigationItemView

                        badge = LayoutInflater
                            .from(requireContext())
                            .inflate(R.layout.bottom_navigation_notification, itemView, true)
                    }
                }
            }
        }

        whatsNewViewModel.news.observe(this) { data ->
            data?.let {
                if (data.news.size > 0) {
                    // Yep, this is actually happening
                    GlobalScope.launch(Dispatchers.IO) {
                        FirebaseInstanceId.getInstance().deleteInstanceId()
                    }
                    WhatsNewDialog.newInstance(data.news).show(childFragmentManager, WhatsNewDialog.TAG)
                }
            }
        }

        profileViewModel.data.observe(this) { data ->
            safeLet(
                data?.referralInformation?.campaign?.monthlyCostDeductionIncentive()?.amount?.amount?.toBigDecimal()?.toDouble(),
                data?.referralInformation?.campaign?.code
            ) { incentive, code -> bindReferralsButton(incentive, code) }
        }
        whatsNewViewModel.fetchNews()
    }

    private fun bindReferralsButton(incentive: Double, code: String) {
        referralButton.setHapticClickListener {
            profileTracker.clickReferral(incentive.toInt())
            requireContext().showShareSheet(R.string.REFERRALS_SHARE_SHEET_TITLE) { intent ->
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
        activity?.invalidateOptionsMenu()
        if (lastLoggedInTab != id) {
            appBarLayout.setExpanded(true, false)
        }
        when (id) {
            LoggedInTabs.DASHBOARD -> {
                setupLargeTitle(R.string.DASHBOARD_SCREEN_TITLE, R.font.circular_bold)
            }
            LoggedInTabs.CLAIMS -> {
                setupLargeTitle(R.string.CLAIMS_TITLE, R.font.circular_bold)
            }
            LoggedInTabs.REFERRALS -> {
                setupLargeTitle(R.string.PROFILE_REFERRAL_TITLE, R.font.circular_bold)
            }
            LoggedInTabs.PROFILE -> {
                setupLargeTitle(R.string.PROFILE_TITLE, R.font.circular_bold)
            }
        }
        lastLoggedInTab = id
    }
}

