package com.hedvig.android.feature.onboarding.data

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.onboarding.FakeOnboardingMemberIdProvider
import com.hedvig.android.feature.onboarding.FakeOnboardingRepository
import com.hedvig.android.feature.onboarding.testOnboardingData
import com.hedvig.android.feature.onboarding.testSessionStore
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class OnboardingSessionStoreTest {
  @Test
  fun `same member reuses the cached session without refetching`() = runTest {
    val repository = FakeOnboardingRepository()
    val store = testSessionStore(repository, FakeOnboardingMemberIdProvider("member-a"))

    repository.onboardingDataResponses.add(testOnboardingData(phoneNumber = "111").right())
    val first = store.getOrFetchSession()
    // No second response is fed; a refetch here would suspend forever on the Turbine.
    val second = store.getOrFetchSession()

    assertThat(first).isEqualTo(second)
    assertThat(second.getOrNull()?.data?.phoneNumber).isEqualTo("111")
    repository.onboardingDataResponses.expectNoEvents()
  }

  @Test
  fun `a member change discards the cache and fetches fresh`() = runTest {
    val repository = FakeOnboardingRepository()
    val memberIdProvider = FakeOnboardingMemberIdProvider("member-a")
    val store = testSessionStore(repository, memberIdProvider)

    repository.onboardingDataResponses.add(testOnboardingData(phoneNumber = "111").right())
    val memberASession = store.getOrFetchSession()
    assertThat(memberASession.getOrNull()?.memberId).isEqualTo("member-a")
    assertThat(memberASession.getOrNull()?.data?.phoneNumber).isEqualTo("111")

    memberIdProvider.memberId = "member-b"
    repository.onboardingDataResponses.add(testOnboardingData(phoneNumber = "222").right())
    val memberBSession = store.getOrFetchSession()

    assertThat(memberBSession.getOrNull()?.memberId).isEqualTo("member-b")
    assertThat(memberBSession.getOrNull()?.data?.phoneNumber).isEqualTo("222")
  }
}
