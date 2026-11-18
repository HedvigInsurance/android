package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.cross.sell.after.claim.closed.ClosedClaimInfo
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ClaimClosedExitRecorderTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val claim = ClosedClaimInfo("claim-1", "CLOSED", "THEFT", "SE_APARTMENT_RENT", "contract-1")

  @Test
  fun `Rated submits the rating and comment and triggers the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record(claim, ClaimClosedAnswer.Rated(4, "Great"))
    runCurrent()

    assertThat(events).containsExactly("crossSell:claim-1", "submit:claim-1:4:Great")
  }

  @Test
  fun `Dismissed dismisses and triggers the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record(claim, ClaimClosedAnswer.Dismissed)
    runCurrent()

    assertThat(events).containsExactly("crossSell:claim-1", "dismiss:claim-1")
  }

  @Test
  fun `NotAsked acknowledges and triggers the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record(claim, ClaimClosedAnswer.NotAsked)
    runCurrent()

    assertThat(events).containsExactly("crossSell:claim-1", "acknowledge:claim-1")
  }

  @Test
  fun `a failed call still triggers the cross-sell`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events, fail = true)

    recorder.record(claim, ClaimClosedAnswer.Rated(1, null))
    runCurrent()

    assertThat(events).containsExactly("crossSell:claim-1", "submit:claim-1:1:null")
  }

  @Test
  fun `the call completes after the caller's scope is cancelled`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)
    val callerScope = CoroutineScope(SupervisorJob())

    callerScope.launch(start = CoroutineStart.UNDISPATCHED) {
      recorder.record(claim, ClaimClosedAnswer.Rated(5, null))
    }
    callerScope.cancel()
    runCurrent()

    assertThat(events).containsExactly("crossSell:claim-1", "submit:claim-1:5:null")
  }

  @Test
  fun `nothing is recorded before record is called`() = runTest {
    val events = mutableListOf<String>()
    recorder(events)
    runCurrent()

    assertThat(events).isEmpty()
  }

  private fun TestScope.recorder(events: MutableList<String>, fail: Boolean = false): ClaimClosedExitRecorder {
    fun result(): Either<ErrorMessage, Unit> = if (fail) ErrorMessage().left() else Unit.right()
    return ClaimClosedExitRecorderImpl(
      applicationScope = ApplicationScope(backgroundScope),
      submitClaimRatingUseCase = object : SubmitClaimRatingUseCase {
        override suspend fun invoke(claimId: String, rating: Int, comment: String?): Either<ErrorMessage, Unit> {
          events.add("submit:$claimId:$rating:$comment")
          return result()
        }
      },
      dismissClaimRatingUseCase = object : DismissClaimRatingUseCase {
        override suspend fun invoke(claimId: String): Either<ErrorMessage, Unit> {
          events.add("dismiss:$claimId")
          return result()
        }
      },
      crossSellAfterClaimClosedRepository = object : CrossSellAfterClaimClosedRepository {
        override suspend fun acknowledgeClaimClosedStatus(claimId: String): Either<ErrorMessage, Unit> {
          events.add("acknowledge:$claimId")
          return result()
        }

        override fun triggerCrossSell(claim: ClosedClaimInfo) {
          events.add("crossSell:${claim.id}")
        }
      },
    )
  }
}
