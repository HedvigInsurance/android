package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.cross.sell.after.claim.closed.ClosedClaimInfo
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

internal sealed interface ClaimClosedAnswer {
  data class Rated(val rating: Int, val comment: String?) : ClaimClosedAnswer

  data object Dismissed : ClaimClosedAnswer

  /** The member was never asked to rate, so the closed status is only acknowledged. */
  data object NotAsked : ClaimClosedAnswer
}

internal interface ClaimClosedExitRecorder {
  /** Fire-and-forget: the backend call runs on the application scope so it survives the screen being popped. */
  fun record(claim: ClosedClaimInfo, answer: ClaimClosedAnswer)
}

@ContributesBinding(AppScope::class)
@Inject
internal class ClaimClosedExitRecorderImpl(
  private val applicationScope: ApplicationScope,
  private val submitClaimRatingUseCase: SubmitClaimRatingUseCase,
  private val dismissClaimRatingUseCase: DismissClaimRatingUseCase,
  private val crossSellAfterClaimClosedRepository: CrossSellAfterClaimClosedRepository,
) : ClaimClosedExitRecorder {
  override fun record(claim: ClosedClaimInfo, answer: ClaimClosedAnswer) {
    crossSellAfterClaimClosedRepository.triggerCrossSell(claim)
    applicationScope.launch {
      val result: Either<ErrorMessage, Unit> = when (answer) {
        is ClaimClosedAnswer.Rated -> submitClaimRatingUseCase.invoke(claim.id, answer.rating, answer.comment)
        ClaimClosedAnswer.Dismissed -> dismissClaimRatingUseCase.invoke(claim.id)
        ClaimClosedAnswer.NotAsked -> crossSellAfterClaimClosedRepository.acknowledgeClaimClosedStatus(claim.id)
      }
      result.onLeft { logcat(LogPriority.WARN) { "Failed to record exit of closed claim ${claim.id}: $it" } }
    }
  }
}
