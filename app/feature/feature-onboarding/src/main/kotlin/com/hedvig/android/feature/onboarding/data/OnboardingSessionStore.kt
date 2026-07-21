package com.hedvig.android.feature.onboarding.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the eagerly fetched onboarding data plus the computed step path for the lifetime of one
 * Activity. Keys stay tiny (see OnboardingStepKey); after process death this cache is empty and
 * the visible step re-fetches through [getOrFetchSession].
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingSessionStore(
  private val onboardingRepository: OnboardingRepository,
) {
  private val mutex = Mutex()
  private var cachedSession: OnboardingSession? = null

  val currentSession: OnboardingSession?
    get() = cachedSession

  suspend fun getOrFetchSession(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    cachedSession?.let { return@withLock it.right() }
    onboardingRepository.getOnboardingData().map { data ->
      OnboardingSession(data = data, path = buildOnboardingPath(data)).also { cachedSession = it }
    }
  }

  /**
   * Refetches the data (used after returning from an external flow like edit co-insured or
   * Trustly) but keeps the original path: the progress bar must not reshuffle mid-flow.
   */
  suspend fun refreshData(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    val existing = cachedSession
      ?: return@withLock ErrorMessage("No onboarding session to refresh").left()
    onboardingRepository.getOnboardingData().map { data ->
      existing.copy(data = data).also { cachedSession = it }
    }
  }
}

internal data class OnboardingSession(
  val data: OnboardingData,
  val path: List<OnboardingStepId>,
)
