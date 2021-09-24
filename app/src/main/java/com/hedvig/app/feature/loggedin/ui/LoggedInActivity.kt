package com.hedvig.app.feature.loggedin.ui

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.forEach
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.florent37.viewtooltip.ViewTooltip
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.BASE_MARGIN_DOUBLE
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityLoggedInBinding
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.onboarding.MemberIdViewModel
import com.hedvig.app.feature.onboarding.MemberIdViewModelImpl
import com.hedvig.app.feature.ratings.RatingsTracker
import com.hedvig.app.feature.referrals.ui.ReferralsInformationActivity
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModelImpl
import com.hedvig.app.shouldOverrideFeatureFlags
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.getLastOpen
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.setLastOpen
import com.hedvig.app.util.extensions.showReviewDialog
import com.hedvig.app.util.extensions.startClosableChat
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.performOnTapHapticFeedback
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import e
import java.time.LocalDate
import javax.inject.Inject
import javax.money.MonetaryAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoggedInActivity : BaseActivity(R.layout.activity_logged_in) {

    private val claimsViewModel: ClaimsViewModel by viewModels()
    private val whatsNewViewModel: WhatsNewViewModelImpl by viewModels()

    private val memberIdViewModel: MemberIdViewModelImpl by viewModels()
    private val welcomeViewModel: WelcomeViewModel by viewModels()
    private val loggedInViewModel: LoggedInViewModel by viewModels()

    @Inject
    lateinit var ratingsTracker: RatingsTracker

    @Inject
    lateinit var loggedInTracker: LoggedInTracker

    private val binding by viewBinding(ActivityLoggedInBinding::bind)

    private var savedTab: LoggedInTabs? = null
    private var lastSelectedTab: LoggedInTabs? = null

    private lateinit var referralTermsUrl: String
    private lateinit var referralsIncentive: MonetaryAmount

    private val isFromOnboarding: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        savedTab = savedInstanceState?.getSerializable("tab") as? LoggedInTabs

        with(binding) {
            window.compatSetDecorFitsSystemWindows(false)
            toolbar.applyStatusBarInsets()
            tabContent.applyStatusBarInsets()
            bottomNavigation.applyNavigationBarInsets()

            toolbar.background.alpha = 0

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

            loggedInViewModel.shouldOpenReviewDialog
                .flowWithLifecycle(lifecycle)
                .onEach { shouldOpenReviewDialog ->
                    if (shouldOpenReviewDialog) {
                        showReviewWithDelay()
                    }
                }
                .launchIn(lifecycleScope)

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)

            bottomNavigation.itemIconTintList = null
            bottomNavigation.setOnItemSelectedListener { menuItem ->
                val id = LoggedInTabs.fromId(menuItem.itemId)
                if (id == null) {
                    e { "Programmer error: Invalid menu item chosen" }
                    return@setOnItemSelectedListener false
                }

                if (id == lastSelectedTab) {
                    return@setOnItemSelectedListener false
                }
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.tabContent, id.fragment)
                    .commitNowAllowingStateLoss()

                setupToolBar()
                animateGradient(id)
                lastSelectedTab = id
                loggedInViewModel.onTabVisited(id)
                true
            }

            if (isFromOnboarding) {
                fetchAndShowWelcomeDialog(binding)
            }

            bindData()
            setupToolBar()

            if (intent.getBooleanExtra(SHOW_RATING_DIALOG, false)) {
                lifecycleScope.launch {
                    showReviewWithDelay()
                }
            }
        }
    }

    private fun fetchAndShowWelcomeDialog(binding: ActivityLoggedInBinding) {
        welcomeViewModel.fetch()
        welcomeViewModel.data.observe(this@LoggedInActivity) { data ->
            WelcomeDialog.newInstance(
                data.welcome.mapIndexed { index, page ->
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
                }
            )
                .show(supportFragmentManager, WelcomeDialog.TAG)
            binding.loggedInRoot.postDelayed(
                { binding.loggedInRoot.show() },
                resources.getInteger(R.integer.slide_in_animation_duration).toLong()
            )
        }
        intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
    }

    private suspend fun showReviewWithDelay() {
        delay(REVIEW_DIALOG_DELAY_MILLIS)
        showReviewDialog {
            ratingsTracker.rate()
        }
    }

    private fun shouldShowTooltip(lastOpen: Long, currentEpochDay: Long): Boolean {
        val diff = currentEpochDay - lastOpen
        return diff >= 30
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(
            "tab",
            LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)
        )
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        when (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)) {
            LoggedInTabs.HOME,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.PROFILE,
            LoggedInTabs.INSURANCE,
            -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
                menu.getItem(0).actionView.setOnClickListener {
                    onOptionsItemSelected(menu.getItem(0))
                }
            }
            LoggedInTabs.REFERRALS -> {
                menuInflater.inflate(R.menu.referral_more_info_menu, menu)
                menu.getItem(0).actionView.setOnClickListener {
                    onOptionsItemSelected(menu.getItem(0))
                }
            }
            else -> {
                menuInflater.inflate(R.menu.base_tab_menu, menu)
                menu.getItem(0).actionView.setOnClickListener {
                    onOptionsItemSelected(menu.getItem(0))
                }
            }
        }

        val currentEpochDay = LocalDate.now().toEpochDay()
        if (shouldShowTooltip(getLastOpen(), currentEpochDay)) {
            if (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId) == LoggedInTabs.HOME) {
                Handler(mainLooper).postDelayed(
                    {
                        binding.toolbar.performOnTapHapticFeedback()
                        ViewTooltip
                            .on(binding.toolbar.menu.getItem(0).actionView)
                            .autoHide(true, 5000)
                            .clickToHide(true)
                            .corner(BASE_MARGIN_DOUBLE)
                            .arrowTargetMargin(-20)
                            .arrowSourceMargin(-20)
                            .padding(
                                12.dp,
                                12.dp,
                                12.dp,
                                15.dp
                            )
                            .position(ViewTooltip.Position.BOTTOM)
                            .color(compatColor(R.color.colorTooltip))
                            .textColor(colorAttr(R.attr.colorPrimary))
                            .text(R.string.home_tab_chat_hint_text)
                            .withShadow(false)
                            .onDisplay {
                                setLastOpen(currentEpochDay)
                            }
                            .show()
                    },
                    2000
                )
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (LoggedInTabs.fromId(binding.bottomNavigation.selectedItemId)) {
            LoggedInTabs.HOME,
            LoggedInTabs.KEY_GEAR,
            LoggedInTabs.PROFILE,
            LoggedInTabs.INSURANCE,
            -> {
                lifecycleScope.launch {
                    claimsViewModel.triggerFreeTextChat()
                    startClosableChat()
                }
            }
            LoggedInTabs.REFERRALS -> {
                if (::referralTermsUrl.isInitialized && ::referralsIncentive.isInitialized) {
                    startActivity(
                        ReferralsInformationActivity.newInstance(
                            this,
                            referralTermsUrl,
                            referralsIncentive
                        )
                    )
                }
            }
        }
        return true
    }

    private fun bindData() {
        whatsNewViewModel.news.observe(this) { data ->
            if (data.news.isNotEmpty()) {
                WhatsNewDialog.newInstance(
                    data.news.mapIndexed { index, page ->
                        DismissiblePagerModel.TitlePage(
                            ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                            page.title,
                            page.paragraph,
                            getString(
                                if (index == data.news.size - 1) {
                                    R.string.NEWS_DISMISS
                                } else {
                                    R.string.NEWS_PROCEED
                                }
                            )
                        )
                    }
                ).show(supportFragmentManager, WhatsNewDialog.TAG)
            }
        }

        loggedInViewModel.data.observe(this) { data ->
            val keyGearEnabled =
                if (shouldOverrideFeatureFlags()) {
                    true
                } else {
                    data.member.features.contains(Feature.KEYGEAR)
                }
            val referralsEnabled =
                if (shouldOverrideFeatureFlags()) {
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
            val initialTab = savedTab
                ?: intent.extras?.getSerializable(INITIAL_TAB) as? LoggedInTabs
                ?: LoggedInTabs.HOME
            binding.bottomNavigation.selectedItemId = initialTab.id()
            loggedInViewModel
                .tabNotifications
                .flowWithLifecycle(lifecycle)
                .onEach { tabNotifications ->
                    binding.bottomNavigation.menu.forEach { item ->
                        val asTab = LoggedInTabs.fromId(item.itemId) ?: return@forEach
                        if (tabNotifications.contains(asTab)) {
                            val badge = binding.bottomNavigation.getOrCreateBadge(item.itemId)
                            badge.isVisible = true
                            badge.horizontalOffset = 4.dp
                            badge.verticalOffset = 4.dp
                        } else {
                            binding.bottomNavigation.removeBadge(item.itemId)
                        }
                    }
                }
                .launchIn(lifecycleScope)
            setupToolBar()
            binding.loggedInRoot.show()

            referralTermsUrl = data.referralTerms.url
            data
                .referralInformation
                .campaign
                .incentive
                ?.asMonthlyCostDeduction
                ?.amount
                ?.fragments
                ?.monetaryAmountFragment
                ?.toMonetaryAmount()
                ?.let { referralsIncentive = it }
        }

        memberIdViewModel
            .state
            .flowWithLifecycle(lifecycle)
            .onEach { state ->
                when (state) {
                    is MemberIdViewModel.State.Success -> {
                        loggedInTracker.setMemberId(state.id)
                    }
                    else -> {
                    }
                }
            }
            .launchIn(lifecycleScope)
        whatsNewViewModel.fetchNews()
    }

    private fun setupToolBar() {
        invalidateOptionsMenu()
    }

    private fun animateGradient(newTab: LoggedInTabs) = with(binding) {
        if (gradient.drawable == null) {
            gradient.setImageDrawable(
                GradientDrawableWrapper().apply {
                    mutate()
                    colors = newTab.backgroundGradient(resources)
                }
            )
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
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"

        private const val INITIAL_TAB = "INITIAL_TAB"
        private const val SHOW_RATING_DIALOG = "SHOW_RATING_DIALOG"
        private const val REVIEW_DIALOG_DELAY_MILLIS = 2000L
        private val evaluator = ArgbEvaluator()

        fun newInstance(
            context: Context,
            withoutHistory: Boolean = false,
            initialTab: LoggedInTabs = LoggedInTabs.HOME,
            isFromOnboarding: Boolean = false,
            showRatingDialog: Boolean = false
        ) = Intent(context, LoggedInActivity::class.java).apply {
            if (withoutHistory) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            putExtra(INITIAL_TAB, initialTab)
            putExtra(EXTRA_IS_FROM_ONBOARDING, isFromOnboarding)
            putExtra(SHOW_RATING_DIALOG, showRatingDialog)
        }
    }
}
