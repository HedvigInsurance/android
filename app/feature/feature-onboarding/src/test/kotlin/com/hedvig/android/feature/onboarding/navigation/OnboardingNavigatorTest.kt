package com.hedvig.android.feature.onboarding.navigation

import androidx.compose.runtime.mutableStateListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.data.CompleteOnboardingUseCase
import com.hedvig.android.feature.onboarding.data.OnboardingContract
import com.hedvig.android.feature.onboarding.data.OnboardingData
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.feature.onboarding.testSessionStore
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

internal class OnboardingNavigatorTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private class TestBackstack : Backstack {
    override val entries: MutableList<HedvigNavKey> = mutableStateListOf()
  }

  private class FakeCompleteOnboardingUseCase : CompleteOnboardingUseCase {
    var invoked: Boolean = false

    override suspend fun invoke() {
      invoked = true
    }
  }

  private suspend fun sessionStoreWithData(
    repository: FakeOnboardingRepository,
    analyticsDisabled: Boolean = false,
    data: OnboardingData = testOnboardingData(),
  ): OnboardingSessionStore {
    val store = testSessionStore(repository, FakeOnboardingMemberIdProvider(), analyticsDisabled)
    repository.onboardingDataResponses.add(data.right())
    store.getOrFetchSession()
    return store
  }

  @Test
  fun `continue from welcome pushes the first step of the path`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val repository = FakeOnboardingRepository()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), FakeCompleteOnboardingUseCase())

    navigator.continueFrom(null)

    assertThat(backstack.entries).containsExactly(
      OnboardingKey,
      OnboardingStepKey(OnboardingStepId.AnalyticsConsent),
    )
  }

  @Test
  fun `continue from welcome skips straight past consent when analytics is disabled`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val repository = FakeOnboardingRepository()
    val navigator = OnboardingNavigator(
      backstack,
      sessionStoreWithData(repository, analyticsDisabled = true),
      FakeCompleteOnboardingUseCase(),
    )

    navigator.continueFrom(null)

    assertThat(backstack.entries).containsExactly(
      OnboardingKey,
      OnboardingStepKey(OnboardingStepId.PhoneNumber),
    )
  }

  @Test
  fun `continue from a mid step pushes the next step`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
    val repository = FakeOnboardingRepository()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), FakeCompleteOnboardingUseCase())

    navigator.continueFrom(OnboardingStepId.AnalyticsConsent)

    assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.PhoneNumber))
  }

  @Test
  fun `continue from a step the rebuilt path lacks pushes the next step that is still in it`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.CoInsured))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    // No contract needs co-insured info, so CoInsured is absent from the path the member stands on.
    val navigator = OnboardingNavigator(
      backstack,
      sessionStoreWithData(repository, data = testOnboardingData(contracts = listOf(contractMissingNothing))),
      completeOnboarding,
    )

    navigator.continueFrom(OnboardingStepId.CoInsured)

    assertThat(backstack.entries.last()).isEqualTo(OnboardingStepKey(OnboardingStepId.InviteFriend))
    assertThat(completeOnboarding.invoked).isFalse()
  }

  @Test
  fun `continue from a missing step with nothing after it in the path exits the flow`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    // No cross-sells, so BundleDiscount is absent and it is the last step in declaration order.
    val navigator = OnboardingNavigator(
      backstack,
      sessionStoreWithData(repository, data = testOnboardingData(crossSells = emptyList())),
      completeOnboarding,
    )

    navigator.continueFrom(OnboardingStepId.BundleDiscount)

    assertThat(completeOnboarding.invoked).isTrue()
    assertThat(backstack.entries).isEmpty()
  }

  @Test
  fun `continue from the last step marks seen and removes all onboarding keys`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.BundleDiscount))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), completeOnboarding)

    navigator.continueFrom(OnboardingStepId.BundleDiscount)

    assertThat(completeOnboarding.invoked).isTrue()
    assertThat(backstack.entries).isEmpty()
  }

  @Test
  fun `exit marks seen and removes all onboarding keys, leaving surrounding entries alone`() = runTest {
    val backstack = TestBackstack().apply {
      entries.add(NonOnboardingKey)
      entries.add(OnboardingKey)
      entries.add(OnboardingStepKey(OnboardingStepId.AnalyticsConsent))
    }
    val repository = FakeOnboardingRepository()
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(backstack, sessionStoreWithData(repository), completeOnboarding)

    navigator.exitOnboarding()

    assertThat(completeOnboarding.invoked).isTrue()
    assertThat(backstack.entries).containsExactly(NonOnboardingKey)
  }

  @Test
  fun `continue without a session exits the flow without marking crash-level state`() = runTest {
    val backstack = TestBackstack().apply { entries.add(OnboardingKey) }
    val completeOnboarding = FakeCompleteOnboardingUseCase()
    val navigator = OnboardingNavigator(
      backstack,
      testSessionStore(FakeOnboardingRepository(), FakeOnboardingMemberIdProvider()),
      completeOnboarding,
    )

    navigator.continueFrom(null)

    assertThat(backstack.entries).isEmpty()
    assertThat(completeOnboarding.invoked).isTrue()
  }
}

private val contractMissingNothing = OnboardingContract(
  id = "contract-1",
  displayName = "Home Insurance",
  exposureName = "Bellmansgatan 19A",
  typeOfContract = "SE_APARTMENT_RENT",
  missingCoInsuredCount = 0,
  missingCoOwnersCount = 0,
  isMissingPetId = false,
)

private data object NonOnboardingKey : HedvigNavKey
