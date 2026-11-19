package com.hedvig.android.feature.importantmessages.ios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isTrue
import com.hedvig.android.feature.importantmessages.GetPublicImportantMessagesUseCase
import com.hedvig.android.feature.importantmessages.PublicImportantMessage
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Link
import com.hedvig.android.feature.importantmessages.PublicImportantMessage.Severity
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesUiState
import com.hedvig.android.feature.importantmessages.PublicImportantMessagesViewModel
import com.hedvig.android.feature.importantmessages.TestApolloServer
import com.hedvig.android.feature.importantmessages.messageJson
import com.hedvig.android.feature.importantmessages.messagesJson
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/**
 * Runs on the test binary's main thread, which drains `Dispatchers.Main` only while a test pumps the main run loop.
 * The display link produces no frame in the test process, so the presenter composes once and never recomposes: what
 * a test can observe is the request it sends, the events its `CollectEvents` consumes, and the state mapping.
 */
class PublicImportantMessagesScreenModelTest {
  private val logs = CapturingLogger()
  private val collections = MainScope()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
    collections.cancel()
  }

  @Test
  fun `the screen state carries each message with its severity and flattened link`() {
    val uiState = PublicImportantMessagesUiState(
      listOf(
        PublicImportantMessage("error-1", "Logins fail", Severity.Error, Link("https://example.test/s", "Status")),
        PublicImportantMessage("warning-1", "Slow claims", Severity.Warning, link = null),
        PublicImportantMessage("info-1", "New app", Severity.Info, link = null),
      ),
    )

    val items = uiState.toScreenState().messages

    assertThat(items.map { listOf(it.id, it.message, it.severity, it.linkUrl, it.linkButtonText) }).containsExactly(
      listOf("error-1", "Logins fail", PublicImportantMessageSeverity.ERROR, "https://example.test/s", "Status"),
      listOf("warning-1", "Slow claims", PublicImportantMessageSeverity.WARNING, null, null),
      listOf("info-1", "New app", PublicImportantMessageSeverity.INFO, null, null),
    )
  }

  /**
   * The ViewModel buffers 20 events. Before a collection the presenter is not running, so the 21st reload overflows.
   * Once collected, the presenter's `CollectEvents` drains the buffer, so 20 more reloads fit again.
   */
  @Test
  fun `reload reaches the presenter`() {
    val server = TestApolloServer { messagesJson(messageJson("info-1")) }
    val model = PublicImportantMessagesScreenModel(factoryOver(server))

    repeat(21) { model.reload() }
    assertThat(droppedEvents()).containsExactly("Reload")
    collections.launch { model.stateUpdates.collect {} }
    assertThat(pumpMainLoopUntil { server.requests == 1 }).isTrue()
    pumpMainLoop(1.seconds)
    repeat(20) { model.reload() }
    pumpMainLoop(1.seconds)

    assertThat(droppedEvents()).containsExactly("Reload")
    model.close()
  }

  @Test
  fun `handle calls the closure of each severity`() {
    val handled = PublicImportantMessageSeverity.entries.map { severity ->
      val calls = mutableListOf<String>()
      severity.handle(onInfo = { calls += "info" }, onWarning = { calls += "warning" }, onError = { calls += "error" })
      calls
    }

    assertThat(handled).containsExactly(listOf("info"), listOf("warning"), listOf("error"))
  }

  /** The names of the events the ViewModel dropped, from the overflow error each drop logs. */
  private fun droppedEvents(): List<String> = logs.entries()
    .filter { (priority, message) ->
      priority == LogPriority.WARN && message.startsWith("PublicImportantMessagesViewModel dropped an event")
    }
    .map { (_, message) -> message.substringAfter("Event buffer overflow on event:").substringBefore(".") }
}

private fun factoryOver(server: TestApolloServer): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    modelClass.cast(PublicImportantMessagesViewModel(GetPublicImportantMessagesUseCase(server.apolloClient())))
}

private fun pumpMainLoop(duration: Duration) {
  pumpMainLoopUntil(duration) { false }
}

/** Drives the main run loop in 10 ms slices until [condition] holds or [timeout] passes. */
private fun pumpMainLoopUntil(timeout: Duration = 10.seconds, condition: () -> Boolean): Boolean {
  val start = TimeSource.Monotonic.markNow()
  while (!condition()) {
    if (start.elapsedNow() > timeout) return false
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return true
}

/** Logs arrive from the main thread and from request threads. */
private class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> {
    lock.lock()
    try {
      return captured.toList()
    } finally {
      lock.unlock()
    }
  }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    lock.lock()
    try {
      captured += priority to text
    } finally {
      lock.unlock()
    }
  }
}
