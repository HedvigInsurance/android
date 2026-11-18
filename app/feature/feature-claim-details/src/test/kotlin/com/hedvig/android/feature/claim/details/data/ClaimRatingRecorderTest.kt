package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CompletableDeferred
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

class ClaimRatingRecorderTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `Rated submits the rating and comment`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record("claim-1", ClaimRatingAnswer.Rated(4, "Great"))
    runCurrent()

    assertThat(events).containsExactly("submit:claim-1:4:Great")
  }

  @Test
  fun `Rated without a comment submits a null comment`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record("claim-1", ClaimRatingAnswer.Rated(2, null))
    runCurrent()

    assertThat(events).containsExactly("submit:claim-1:2:null")
  }

  @Test
  fun `Dismissed dismisses the rating`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events)

    recorder.record("claim-1", ClaimRatingAnswer.Dismissed)
    runCurrent()

    assertThat(events).containsExactly("dismiss:claim-1")
  }

  @Test
  fun `a failed call is logged and does not throw`() = runTest {
    val events = mutableListOf<String>()
    val recorder = recorder(events, fail = true)

    recorder.record("claim-1", ClaimRatingAnswer.Rated(1, null))
    runCurrent()

    assertThat(events).containsExactly("submit:claim-1:1:null")
  }

  @Test
  fun `the call completes after the caller's scope is cancelled`() = runTest {
    val events = mutableListOf<String>()
    val gate = CompletableDeferred<Unit>()
    val recorder = recorder(events, gate = gate)
    val callerScope = CoroutineScope(SupervisorJob())

    callerScope.launch(start = CoroutineStart.UNDISPATCHED) {
      recorder.record("claim-1", ClaimRatingAnswer.Rated(5, null))
    }
    runCurrent()
    callerScope.cancel()
    gate.complete(Unit)
    runCurrent()

    assertThat(events).containsExactly("submit:claim-1:5:null")
  }

  @Test
  fun `nothing is recorded before record is called`() = runTest {
    val events = mutableListOf<String>()
    recorder(events)
    runCurrent()

    assertThat(events).isEmpty()
  }

  private fun TestScope.recorder(
    events: MutableList<String>,
    fail: Boolean = false,
    gate: CompletableDeferred<Unit>? = null,
  ): ClaimRatingRecorder {
    suspend fun result(): Either<ErrorMessage, Unit> {
      gate?.await()
      return if (fail) ErrorMessage().left() else Unit.right()
    }
    return ClaimRatingRecorderImpl(
      applicationScope = ApplicationScope(backgroundScope),
      submitClaimRatingUseCase = object : SubmitClaimRatingUseCase {
        override suspend fun invoke(claimId: String, rating: Int, comment: String?): Either<ErrorMessage, Unit> {
          return result().also { events.add("submit:$claimId:$rating:$comment") }
        }
      },
      dismissClaimRatingUseCase = object : DismissClaimRatingUseCase {
        override suspend fun invoke(claimId: String): Either<ErrorMessage, Unit> {
          return result().also { events.add("dismiss:$claimId") }
        }
      },
    )
  }
}
