package com.hedvig.android.feature.importantmessages

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Severity
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

  private val responses = Channel<String?>(Channel.UNLIMITED)
  private val server = TestApolloServer { responses.receive() }
  private val presenter = PublicImportantMessagesPresenter(GetPublicImportantMessagesUseCase(server.apolloClient()))

  @Test
  fun `starts empty and shows the messages after the first load`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(emptyList()))

      responses.send(messagesJson(messageJson("info-1", "Hello")))

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
      responses.send(messagesJson(messageJson("info-1", "Hello")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("info-1", "Hello"))))
      assertThat(server.requests).isEqualTo(2)
    }
  }

  @Test
  fun `Reload fetches again and replaces the list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(messagesJson(messageJson("old-1", "Old")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("old-1", "Old"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(messagesJson(messageJson("new-1", "New")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("new-1", "New"))))
      assertThat(server.requests).isEqualTo(2)
    }
  }

  @Test
  fun `a failed Reload keeps the previously loaded list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(messagesJson(messageJson("kept-1", "Kept")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("kept-1", "Kept"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(null)
      awaitFailureLogs(count = 1)
      // The next state after the failure is the one a later successful load produces.
      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(messagesJson(messageJson("new-1", "New")))

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("new-1", "New"))))
      assertThat(server.requests).isEqualTo(3)
    }
  }

  @Test
  fun `a successful Reload returning nothing clears the list`() = runTest {
    presenter.test(PublicImportantMessagesUiState(emptyList())) {
      skipItems(1)
      responses.send(messagesJson(messageJson("gone-1", "Gone")))
      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(listOf(info("gone-1", "Gone"))))

      sendEvent(PublicImportantMessagesEvent.Reload)
      responses.send(messagesJson())

      assertThat(awaitItem()).isEqualTo(PublicImportantMessagesUiState(emptyList()))
    }
  }

  private fun info(id: String, message: String) = PublicImportantMessage(id, message, Severity.Info, link = null)

  /** The request runs on the mock engine's threads, so this waits in real time for the presenter to log it. */
  private suspend fun awaitFailureLogs(count: Int) = withContext(Dispatchers.Default) {
    withTimeout(5.seconds) {
      while (logs.messages().count { it.startsWith("Failed to load public important messages") } < count) {
        delay(10)
      }
    }
  }
}

private class CapturingLogger : LogcatLogger {
  private val captured = CopyOnWriteArrayList<String>()

  fun messages(): List<String> = captured.toList()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    captured += message()
  }
}
