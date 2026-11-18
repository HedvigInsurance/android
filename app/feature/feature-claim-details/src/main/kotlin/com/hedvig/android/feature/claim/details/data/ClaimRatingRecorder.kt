package com.hedvig.android.feature.claim.details.data

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

internal sealed interface ClaimRatingAnswer {
  data class Rated(val rating: Int, val comment: String?) : ClaimRatingAnswer

  data object Dismissed : ClaimRatingAnswer
}

internal interface ClaimRatingRecorder {
  /** Fire-and-forget: the backend call runs on the application scope so it survives the screen being popped. */
  fun record(claimId: String, answer: ClaimRatingAnswer)
}

@ContributesBinding(AppScope::class)
@Inject
internal class ClaimRatingRecorderImpl(
  private val applicationScope: ApplicationScope,
  private val submitClaimRatingUseCase: SubmitClaimRatingUseCase,
  private val dismissClaimRatingUseCase: DismissClaimRatingUseCase,
) : ClaimRatingRecorder {
  override fun record(claimId: String, answer: ClaimRatingAnswer) {
    applicationScope.launch {
      val result = when (answer) {
        is ClaimRatingAnswer.Rated -> submitClaimRatingUseCase.invoke(claimId, answer.rating, answer.comment)
        ClaimRatingAnswer.Dismissed -> dismissClaimRatingUseCase.invoke(claimId)
      }
      result.onLeft { logcat(LogPriority.WARN) { "Failed to record rating answer for claim $claimId: $it" } }
    }
  }
}
