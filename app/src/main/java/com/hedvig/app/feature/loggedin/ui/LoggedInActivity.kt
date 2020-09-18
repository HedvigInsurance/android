package com.hedvig.app.feature.loggedin.ui

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isEmpty
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.BaseActivity
import com.hedvig.app.HedvigApplication
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityLoggedInBinding
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.referrals.ui.ReferralsInformationActivity
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.shouldOverrideFeatureFlags
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import e
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import javax.money.MonetaryAmount

class LoggedInActivity : BaseActivity(R.layout.activity_logged_in) {
    private val claimsViewModel: ClaimsViewModel by viewModel()
    private val whatsNewViewModel: WhatsNewViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val welcomeViewModel: WelcomeViewModel by viewModel()
    private val insuranceViewModel: InsuranceViewModel by viewModel()
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

            toolbar.background.alpha = 0
            toolbar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
                loggedInViewModel.updateToolbarInset(view.measuredHeight)
            }

            bottomNavigation.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom)
                loggedInViewModel.updateBottomTabInset(view.measuredHeight)
            }

            val isDarkTheme = isDarkThemeActive

            loggedInViewModel.scroll.observe(this@LoggedInActivity) { scroll ->
                val positionInSpan = scroll.toFloat() / toolbar.measuredHeight
                toolbar.background.alpha = boundedLerp(0, 242, positionInSpan / 2)
                if (isDarkTheme) {
                    gradient.drawable?.alpha = boundedLerp(127, 255, positionInSpan / 5)
                } else {
                    gradient.drawable?.alpha = boundedLerp(255, 0, positionInSpan / 5)
                }
            }

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)

            bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                val id = LoggedInTabs.fromId(menuItem.itemId)
                if (id == null) {
                    e { "Programmer error: Invalid menu item chosen" }
                    return@setOnNavigationItemSelectedListener false
                }
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.tabContent, id.fragment)
                    .commitAllowingStateLoss()

                setupToolBar(id)
                animateGradient(id)
                true
            }

            if (intent.getBooleanExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION, false)) {
                bottomNavigation.selectedItemId = R.id.referrals
                intent.removeExtra(EXTRA_IS_FROM_REFERRALS_NOTIFICATION)
            }

            if (intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)) {
                welcomeViewModel.fetch()
                welcomeViewModel.data.observe(this@LoggedInActivity) { data ->
                    WelcomeDialog.newInstance(data.welcome.mapIndexed { index, page ->
                        DismissiblePagerModel.TitlePage(
                            ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                            page.title,
                            page.paragraph,
                            getString(
                                if (index == data.welcome.size - 1) {
                                    R.string.NEWS_DISMISS
                                } else {
                                    R.string.NEWS_PROCEED
                                }
                            )
                        )
                    })
                        .show(supportFragmentManager, WelcomeDialog.TAG)
                    loggedInRoot.postDelayed({
                        loggedInRoot.show()
                    }, resources.getInteger(R.integer.slide_in_animation_duration).toLong())
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
            LoggedInTabs.PROFILE,
            LoggedInTabs.INSURANCE -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
                menu.getItem(0).actionView.setOnClickListener {
                    onOptionsItemSelected(menu.getItem(0))
                }
            }
            LoggedInTabs.REFERRALS -> {
                menuInflater.inflate(R.menu.referral_more_info_menu, menu)
            }
            else -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
                menu.getItem(0).actionView.setOnClickListener {
                    onOptionsItemSelected(menu.getItem(0))
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)) {
            LoggedInTabs.HOME,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.PROFILE,
            LoggedInTabs.INSURANCE -> {
                lifecycleScope.launch {
                    claimsViewModel.triggerFreeTextChat()
                    startClosableChat()
                }
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
        whatsNewViewModel.news.observe(this) { data ->
            if (data.news.isNotEmpty()) {
                WhatsNewDialog.newInstance(data.news)
                    .show(supportFragmentManager, WhatsNewDialog.TAG)
            }
        }

        loggedInViewModel.data.observe(this) { data ->
            if (binding.bottomNavigation.menu.isEmpty()) {
                val keyGearEnabled =
                    if (shouldOverrideFeatureFlags(application as HedvigApplication)) {
                        true
                    } else {
                        data.member.features.contains(Feature.KEYGEAR)
                    }
                val referralsEnabled =
                    if (shouldOverrideFeatureFlags(application as HedvigApplication)) {
                        true
                    } else {
                        data.member.features.contains(Feature.REFERRALS)
                    }

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

            referralTermsUrl = data.referralTerms.url
            data.referralInformation.campaign.incentive?.asMonthlyCostDeduction?.amount?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                ?.let { referralsIncentive = it }

        }

        profileViewModel.data.observe(this) { data ->
            data.member.id?.let { id ->
                loggedInTracker.setMemberId(id)
            }

        }
        whatsNewViewModel.fetchNews()
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

    private fun animateGradient(newTab: LoggedInTabs) = with(binding) {
        if (gradient.drawable == null) {
            gradient.setImageDrawable(GradientDrawableWrapper().apply {
                mutate()
                colors = newTab.backgroundGradient(resources)
            })
            if (gradient.context.isDarkThemeActive) {
                gradient.drawable.alpha = 127
            }
        } else {
            val initialGradientComponents =
                (gradient.drawable as? GradientDrawableWrapper)?.getColorsLowerApi() ?: return@with
            val newGradientComponents = newTab.backgroundGradient(resources)
            (gradient.getTag(R.id.gradient_animation) as? SpringAnimation)?.cancel()

            val animation = SpringAnimation(FloatValueHolder())
                .apply {
                    spring = SpringForce().apply {
                        stiffness = SpringForce.STIFFNESS_LOW
                        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
                    }
                    addUpdateListener { _, value, _ ->
                        val progress = value / 100
                        val first = evaluator.evaluate(
                            progress,
                            initialGradientComponents[0],
                            newGradientComponents[0]
                        ) as Int
                        val second = evaluator.evaluate(
                            progress,
                            initialGradientComponents[1],
                            newGradientComponents[1]
                        ) as Int
                        val third = evaluator.evaluate(
                            progress,
                            initialGradientComponents[2],
                            newGradientComponents[2]
                        ) as Int

                        (gradient.drawable.mutate() as? GradientDrawableWrapper)
                            ?.colors = intArrayOf(first, second, third)
                    }
                    animateToFinalPosition(100f)
                }

            gradient.setTag(R.id.gradient_animation, animation)
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

        fun isTerminated(contracts: List<InsuranceQuery.Contract>) =
            contracts.isNotEmpty() && contracts.all { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }

        const val EXTRA_IS_FROM_REFERRALS_NOTIFICATION = "extra_is_from_referrals_notification"
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"

        private val evaluator = ArgbEvaluator()
    }
}
