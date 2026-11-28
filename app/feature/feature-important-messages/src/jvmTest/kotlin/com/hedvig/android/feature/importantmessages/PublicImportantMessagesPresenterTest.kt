package com.hedvig.android.feature.importantmessages

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.importantmessages.GetPublicImportantMessagesUseCase
import com.hedvig.android.data.importantmessages.PublicImportantMessage
import com.hedvig.android.data.importantmessages.PublicImportantMessage.Severity
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test

class PublicImportantMessagesPresenterTest {
  private val logs = CapturingLogger()

  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule(testLogcatLogger = logs)

  private val responses = Channel<List<PublicImportantMessage>?>(Channel.UNLIMITED)
  private val useCase = FakeGetPublicImportantMessagesUseCase { responses.receive() }
  private val presenter = PublicImportantMessagesPresenter(useCase)

  @Test
  fun `starts empty and shows the messages after the first load`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(emptyList()))

      responses.send(listOf(info("info-1", "Hello")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("info-1", "Hello"))))
    }
  }

  @Test
  fun `a failed first load stays empty`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(emptyList()))

      responses.send(null)
      awaitFailureLogs(count = 1)
      // The next state after the failure is the one a later successful load produces.
      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(listOf(info("info-1", "Hello")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("info-1", "Hello"))))
      assertThat(useCase.requests).isEqualTo(2)
    }
  }

  @Test
  fun `Reload fetches again and replaces the list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(listOf(info("old-1", "Old")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("old-1", "Old"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(listOf(info("new-1", "New")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("new-1", "New"))))
      assertThat(useCase.requests).isEqualTo(2)
    }
  }

  @Test
  fun `a failed Reload keeps the previously loaded list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(listOf(info("kept-1", "Kept")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("kept-1", "Kept"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(null)
      awaitFailureLogs(count = 1)
      // The next state after the failure is the one a later successful load produces.
      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(listOf(info("new-1", "New")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("new-1", "New"))))
      assertThat(useCase.requests).isEqualTo(3)
    }
  }

  @Test
  fun `a successful Reload returning nothing clears the list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(listOf(info("gone-1", "Gone")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("gone-1", "Gone"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(emptyList())

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(emptyList()))
    }
  }

  private fun info(id: String, message: String) = PublicImportantMessage(id, message, Severity.Info, link = null)

  /** Waits in real time for the presenter to log the failure, which no state change shows. */
  private suspend fun awaitFailureLogs(count: Int) = withContext(Dispatchers.Default) {
    withTimeout(5.seconds) {
      while (logs.messages().count { it.startsWith("Failed to load public important messages") } < count) {
        delay(10)
      }
    }
  }
}

/** Answers each load with the list [nextResponse] returns, or fails it when that is null. */
private class FakeGetPublicImportantMessagesUseCase(
  private val nextResponse: suspend () -> List<PublicImportantMessage>?,
) : GetPublicImportantMessagesUseCase {
  var requests = 0
    private set

  override suspend fun invoke(): Either<ErrorMessage, List<PublicImportantMessage>> {
    requests++
    return nextResponse()?.right() ?: ErrorMessage("failed").left()
  }
}

private class CapturingLogger : LogcatLogger {
  private val captured = CopyOnWriteArrayList<String>()

  fun messages(): List<String> = captured.toList()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    captured += message()
  }
}
