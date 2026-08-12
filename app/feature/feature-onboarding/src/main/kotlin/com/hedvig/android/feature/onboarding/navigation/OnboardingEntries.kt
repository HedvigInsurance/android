package com.hedvig.android.feature.onboarding.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.feature.onboarding.ui.bundle.OnboardingBundleDestination
import com.hedvig.android.feature.onboarding.ui.bundle.OnboardingBundleViewModel
import com.hedvig.android.feature.onboarding.ui.coinsured.OnboardingCoInsuredDestination
import com.hedvig.android.feature.onboarding.ui.coinsured.OnboardingCoInsuredViewModel
import com.hedvig.android.feature.onboarding.ui.consent.OnboardingConsentDestination
import com.hedvig.android.feature.onboarding.ui.consent.OnboardingConsentViewModel
import com.hedvig.android.feature.onboarding.ui.invite.OnboardingInviteDestination
import com.hedvig.android.feature.onboarding.ui.invite.OnboardingInviteViewModel
import com.hedvig.android.feature.onboarding.ui.payment.OnboardingPaymentDestination
import com.hedvig.android.feature.onboarding.ui.payment.OnboardingPaymentViewModel
import com.hedvig.android.feature.onboarding.ui.petid.OnboardingPetIdDestination
import com.hedvig.android.feature.onboarding.ui.petid.OnboardingPetIdViewModel
import com.hedvig.android.feature.onboarding.ui.phone.OnboardingPhoneDestination
import com.hedvig.android.feature.onboarding.ui.phone.OnboardingPhoneViewModel
import com.hedvig.android.feature.onboarding.ui.theme.OnboardingThemeDestination
import com.hedvig.android.feature.onboarding.ui.theme.OnboardingThemeViewModel
import com.hedvig.android.feature.onboarding.ui.welcome.OnboardingWelcomeDestination
import com.hedvig.android.feature.onboarding.ui.welcome.OnboardingWelcomeViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.onboardingEntries(
  backstack: Backstack,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openPrivacyPolicy: () -> Unit,
  navigateToChipId: (contractId: String) -> Unit,
) {
  entry<OnboardingKey> {
    val viewModel: OnboardingWelcomeViewModel = metroViewModel()
    OnboardingWelcomeDestination(viewModel)
  }

  entry<OnboardingForeverKey> {
    val viewModel: ForeverViewModel = metroViewModel()
    ForeverDestination(viewModel)
  }

  entry<OnboardingStepKey> { key ->
    when (key.stepId) {
      OnboardingStepId.AnalyticsConsent -> {
        val viewModel: OnboardingConsentViewModel = metroViewModel()
        OnboardingConsentDestination(viewModel, backstack::navigateUp, openPrivacyPolicy)
      }

      OnboardingStepId.PhoneNumber -> {
        val viewModel: OnboardingPhoneViewModel = metroViewModel()
        OnboardingPhoneDestination(viewModel, backstack::navigateUp)
      }

      OnboardingStepId.Theme -> {
        val viewModel: OnboardingThemeViewModel = metroViewModel()
        OnboardingThemeDestination(viewModel, backstack::navigateUp)
      }

      OnboardingStepId.CoInsured -> {
        val viewModel: OnboardingCoInsuredViewModel = metroViewModel()
        OnboardingCoInsuredDestination(viewModel, backstack::navigateUp)
      }

      OnboardingStepId.PetIds -> {
        val viewModel: OnboardingPetIdViewModel = metroViewModel()
        OnboardingPetIdDestination(viewModel, backstack::navigateUp, navigateToChipId)
      }

      OnboardingStepId.InviteFriend -> {
        val viewModel: OnboardingInviteViewModel = metroViewModel()
        OnboardingInviteDestination(viewModel, backstack::navigateUp)
      }

      OnboardingStepId.ConnectPayment -> {
        val viewModel: OnboardingPaymentViewModel = metroViewModel()
        OnboardingPaymentDestination(viewModel, backstack::navigateUp)
      }

      OnboardingStepId.BundleDiscount -> {
        val viewModel: OnboardingBundleViewModel = metroViewModel()
        OnboardingBundleDestination(viewModel, imageLoader, backstack::navigateUp, openUrl)
      }
    }
  }
}
