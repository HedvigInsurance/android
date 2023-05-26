package com.hedvig.app.feature.loggedin.ui

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.forEach
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.serializableExtra
import com.hedvig.app.R
import com.hedvig.app.databinding.ActivityLoggedInBinding
import com.hedvig.app.feature.claims.ui.ClaimsViewModel
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.welcome.WelcomeDialog
import com.hedvig.app.feature.welcome.WelcomeViewModel
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.showReviewDialog
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.extensions.viewDps
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import slimber.log.e

class LoggedInActivity : AppCompatActivity(R.layout.activity_logged_in) {
  private val claimsViewModel: ClaimsViewModel by viewModel()

  private val welcomeViewModel: WelcomeViewModel by viewModel()
  private val loggedInViewModel: LoggedInViewModel by viewModel()

  private val binding by viewBinding(ActivityLoggedInBinding::bind)

  private var lastMenuIdInflated: Int? = null
  private var lastSelectedTab: LoggedInTabs? = null

  private val isFromOnboarding: Boolean by lazy {
    intent.getBooleanExtra(EXTRA_IS_FROM_ONBOARDING, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    with(binding) {
      bottomNavigation.applyNavigationBarInsets()

      loggedInViewModel.shouldOpenReviewDialog
        .flowWithLifecycle(lifecycle)
        .onEach { shouldOpenReviewDialog ->
          if (shouldOpenReviewDialog) {
            showReviewWithDelay()
          }
        }
        .launchIn(lifecycleScope)

      claimsViewModel.events
        .flowWithLifecycle(lifecycle)
        .onEach { event ->
          when (event) {
            ClaimsViewModel.Event.Error -> {
              showErrorDialog(getString(com.adyen.checkout.dropin.R.string.component_error)) {}
            }
            ClaimsViewModel.Event.StartChat -> startChat()
          }
        }
        .launchIn(lifecycleScope)

      supportActionBar?.setDisplayShowTitleEnabled(false)

      bottomNavigation.itemIconTintList = null
      bottomNavigation.setOnItemSelectedListener { menuItem ->
        val selectedTab = LoggedInTabs.fromId(menuItem.itemId)
        if (selectedTab == null) {
          e { "Programmer error: Invalid menu item chosen" }
          return@setOnItemSelectedListener false
        }

        if (selectedTab == lastSelectedTab) {
          return@setOnItemSelectedListener false
        }
        supportFragmentManager
          .beginTransaction()
          .replace(R.id.tabContent, selectedTab.fragment)
          .commitNowAllowingStateLoss()

        animateGradient(selectedTab)
        lastSelectedTab = selectedTab
        loggedInViewModel.onTabVisited(selectedTab)
        true
      }

      if (isFromOnboarding) {
        fetchAndShowWelcomeDialog(binding)
      }

      bindData()

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
                hedvig.resources.R.string.NEWS_DISMISS
              } else {
                hedvig.resources.R.string.NEWS_PROCEED
              },
            ),
          )
        },
      )
        .show(supportFragmentManager, WelcomeDialog.TAG)
      binding.loggedInRoot.postDelayed(
        { binding.loggedInRoot.show() },
        resources.getInteger(R.integer.slide_in_animation_duration).toLong(),
      )
    }
    intent.removeExtra(EXTRA_IS_FROM_ONBOARDING)
  }

  private suspend fun showReviewWithDelay() {
    delay(REVIEW_DIALOG_DELAY_MILLIS)
    showReviewDialog()
  }

  private fun bindData() {
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        loggedInViewModel
          .viewState
          .filterNotNull() // Emulate LiveData behavior of doing nothing until we get valid data
          .collectLatest { viewState: LoggedInViewState ->
            setupBottomNav(
              isReferralsEnabled = viewState.isReferralsEnabled,
              unseenTabNotifications = viewState.unseenTabNotifications,
            )
            binding.loggedInRoot.show()
          }
      }
    }
  }

  private fun setupBottomNav(
    isReferralsEnabled: Boolean,
    unseenTabNotifications: Set<LoggedInTabs>,
  ) {
    val menuId = if (isReferralsEnabled) {
      R.menu.logged_in_menu_referrals
    } else {
      R.menu.logged_in_menu_no_referrals
    }
    val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
    if (lastMenuIdInflated == null || lastMenuIdInflated != menuId) {
      bottomNavigationView.menu.clear()
      bottomNavigationView.inflateMenu(menuId)

      val initialTab: LoggedInTabs = intent.extras?.serializableExtra(INITIAL_TAB) ?: LoggedInTabs.HOME
      bottomNavigationView.selectedItemId = initialTab.id()
    }
    bottomNavigationView.menu.forEach { item ->
      val bottomNavTab = LoggedInTabs.fromId(item.itemId) ?: return@forEach
      if (unseenTabNotifications.contains(bottomNavTab)) {
        val badge = bottomNavigationView.getOrCreateBadge(item.itemId)
        badge.isVisible = true
        badge.horizontalOffset = 4.viewDps
        badge.verticalOffset = 4.viewDps
      } else {
        bottomNavigationView.removeBadge(item.itemId)
      }
    }
    lastMenuIdInflated = menuId
  }

  private fun animateGradient(newTab: LoggedInTabs) = with(binding) {
    if (gradient.drawable == null) {
      gradient.setImageDrawable(
        GradientDrawableWrapper().apply {
          mutate()
          colors = newTab.backgroundGradient(resources)
        },
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
              newGradientComponents[0],
            ) as Int
            val second = evaluator.evaluate(
              progress,
              initialGradientComponents[1],
              newGradientComponents[1],
            ) as Int
            val third = evaluator.evaluate(
              progress,
              initialGradientComponents[2],
              newGradientComponents[2],
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
      showRatingDialog: Boolean = false,
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
