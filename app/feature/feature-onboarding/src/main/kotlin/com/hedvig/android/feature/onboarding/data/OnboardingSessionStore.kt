package com.hedvig.android.feature.onboarding.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Resolves the current member id so a cached [OnboardingSession] can be scoped to the member it was
 * fetched for. Exists as a seam so tests can fake member identity without an auth backend.
 */
internal fun interface OnboardingMemberIdProvider {
  fun memberId(): Flow<String?>
}

@ContributesBinding(AppScope::class)
@Inject
internal class OnboardingMemberIdProviderImpl(
  private val memberIdService: MemberIdService,
) : OnboardingMemberIdProvider {
  override fun memberId(): Flow<String?> = memberIdService.getMemberId()
}

/**
 * Holds the eagerly fetched onboarding data plus the computed step path for the lifetime of one
 * Activity. Keys stay tiny (see OnboardingStepKey); after process death this cache is empty and
 * the visible step re-fetches through [getOrFetchSession].
 *
 * The cache is scoped to the member it was fetched for: this store is
 * `@SingleIn(ActivityRetainedScope::class)` and survives a logout/login within one Activity, so a
 * different member reaching [getOrFetchSession] must not receive the previous member's session.
 */
@SingleIn(ActivityRetainedScope::class)
@Inject
internal class OnboardingSessionStore(
  private val onboardingRepository: OnboardingRepository,
  private val memberIdProvider: OnboardingMemberIdProvider,
) {
  private val mutex = Mutex()
  private var cachedSession: OnboardingSession? = null

  val currentSession: OnboardingSession?
    get() = cachedSession

  suspend fun getOrFetchSession(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    val currentMemberId = memberIdProvider.memberId().first()
      ?: return@withLock ErrorMessage("No member id for onboarding session").left()
    cachedSession?.let { cached ->
      if (cached.memberId == currentMemberId) return@withLock cached.right()
    }
    cachedSession = null
    onboardingRepository.getOnboardingData().map { data ->
      OnboardingSession(memberId = currentMemberId, data = data, path = buildOnboardingPath(data))
        .also { cachedSession = it }
    }
  }

  /**
   * Refetches the data (used after returning from an external flow like edit co-insured or
   * Trustly) but keeps the original path: the progress bar must not reshuffle mid-flow.
   */
  suspend fun refreshData(): Either<ErrorMessage, OnboardingSession> = mutex.withLock {
    val currentMemberId = memberIdProvider.memberId().first()
      ?: return@withLock ErrorMessage("No member id for onboarding session").left()
    val existing = cachedSession?.takeIf { it.memberId == currentMemberId }
    if (existing == null) {
      cachedSession = null
      return@withLock ErrorMessage("No onboarding session to refresh").left()
    }
    onboardingRepository.getOnboardingData().map { data ->
      existing.copy(data = data).also { cachedSession = it }
    }
  }
}

internal data class OnboardingSession(
  val memberId: String,
  val data: OnboardingData,
  val path: List<OnboardingStepId>,
)
