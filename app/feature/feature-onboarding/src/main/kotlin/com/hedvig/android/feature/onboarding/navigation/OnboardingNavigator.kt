package com.hedvig.android.feature.onboarding.navigation

import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.removeAllOf
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Owns forward/exit movement through the onboarding flow. Steps are pushed as real back stack
 * entries so predictive back and the up arrow behave natively; exiting removes every onboarding
 * key, landing on whatever was underneath (Home).
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingNavigator(
  private val backstack: Backstack,
  private val sessionStore: OnboardingSessionStore,
  private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) {
  /** [current] is null when continuing from the welcome screen (OnboardingKey). */
  suspend fun continueFrom(current: OnboardingStepId?) {
    val session = sessionStore.currentSession
    if (session == null) {
      logcat { "Onboarding continue without a session, exiting flow" }
      exitOnboarding()
      return
    }
    val next = if (current == null) {
      session.path.firstOrNull()
    } else {
      val currentIndex = session.path.indexOf(current)
      if (currentIndex == -1) null else session.path.getOrNull(currentIndex + 1)
    }
    if (next == null) {
      exitOnboarding()
    } else {
      backstack.add(OnboardingStepKey(next))
    }
  }

  suspend fun exitOnboarding() {
    completeOnboardingUseCase.invoke()
    backstack.removeAllOf<OnboardingStepKey>()
    backstack.removeAllOf<OnboardingKey>()
  }

  /** Pushes the existing edit-co-insured flow; it pops itself back here when done. */
  fun openAddCoInsured(contractId: String) {
    backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
  }

  /** Pushes the Trustly connect-payment flow; it pops itself back here when done. */
  fun openConnectPayment() {
    backstack.add(TrustlyKey)
  }

  /** Pushes the shared Forever screen; the member leaves it with system back. */
  fun openForeverScreen() {
    backstack.add(OnboardingForeverKey)
  }
}
