package com.hedvig.android.feature.payments.ios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCase
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModelFactory
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.concurrent.AtomicInt
import kotlin.reflect.KClass
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
 * The display link produces no frame in the test process, so a presenter composes once: it calls its use case and
 * delivers only Loading. The mapping of every other state is covered by the screen state tests.
 */
class PaymentScreenModelsTest {
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
  fun `the history model starts as Loading without loading`() {
    val useCase = FakeHistoryUseCase()
    val model = PaymentHistoryScreenModel(historyFactory(useCase))

    val seeded = model.currentState
    pumpMainLoop(1.seconds)

    assertThat(seeded).isEqualTo(PaymentHistoryScreenState.Loading)
    assertThat(useCase.calls.value).isEqualTo(0)
    model.close()
  }

  @Test
  fun `a collection of the history model loads once and delivers Loading`() {
    val useCase = FakeHistoryUseCase()
    val model = PaymentHistoryScreenModel(historyFactory(useCase))
    val states = mutableListOf<PaymentHistoryScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { useCase.calls.value == 1 }).isTrue()
    pumpMainLoop(1.seconds)

    assertThat(states).containsExactly(PaymentHistoryScreenState.Loading)
    model.close()
  }

  @Test
  fun `history reloads reach the ViewModel's event buffer`() {
    val model = PaymentHistoryScreenModel(historyFactory(FakeHistoryUseCase()))

    repeat(25) { model.reload() }

    assertThat(droppedEvents("PaymentHistoryViewModel")).isEqualTo(5)
    model.close()
  }

  @Test
  fun `closing the history model clears its ViewModel once and drops later reloads`() {
    val useCase = FakeHistoryUseCase()
    val model = PaymentHistoryScreenModel(historyFactory(useCase))

    model.close()
    model.close()
    repeat(25) { model.reload() }
    collections.launch { model.stateUpdates.collect {} }
    pumpMainLoop(1.seconds)

    assertThat(logs.messages().count { it == "PaymentHistoryViewModel cleared" }).isEqualTo(1)
    assertThat(droppedEvents("PaymentHistoryViewModel")).isEqualTo(0)
    assertThat(useCase.calls.value).isEqualTo(0)
  }

  @Test
  fun `the details model passes its charge id to the use case`() {
    assertThat(chargeIdReachingUseCase("charge-1")).isEqualTo("charge-1")
  }

  @Test
  fun `the details model passes a null charge id to the use case`() {
    assertThat(chargeIdReachingUseCase(null)).isEqualTo(null)
  }

  @Test
  fun `the details model passes an empty charge id to the use case as null`() {
    assertThat(chargeIdReachingUseCase("")).isEqualTo(null)
  }

  @Test
  fun `the details model starts as Loading without loading`() {
    val useCase = FakeDetailsUseCase()
    val model = createPaymentDetailsScreenModel(detailsFactory(useCase), "charge-1")

    val seeded = model.currentState
    pumpMainLoop(1.seconds)

    assertThat(seeded).isEqualTo(PaymentDetailsScreenState.Loading)
    assertThat(useCase.chargeIds()).containsExactly()
    model.close()
  }

  @Test
  fun `details reloads reach the ViewModel's event buffer`() {
    val model = createPaymentDetailsScreenModel(detailsFactory(FakeDetailsUseCase()), "charge-1")

    repeat(25) { model.reload() }

    assertThat(droppedEvents("PaymentDetailsViewModel")).isEqualTo(5)
    model.close()
  }

  @Test
  fun `closing the details model clears its ViewModel once`() {
    val useCase = FakeDetailsUseCase()
    val model = createPaymentDetailsScreenModel(detailsFactory(useCase), "charge-1")
    collections.launch { model.stateUpdates.collect {} }
    assertThat(pumpMainLoopUntil { useCase.chargeIds().size == 1 }).isTrue()

    model.close()
    model.close()
    model.reload()
    pumpMainLoop(1.seconds)

    assertThat(logs.messages().count { it == "PaymentDetailsViewModel cleared" }).isEqualTo(1)
    assertThat(useCase.chargeIds()).containsExactly("charge-1")
  }

  private fun chargeIdReachingUseCase(chargeId: String?): String? {
    val useCase = FakeDetailsUseCase()
    val model = createPaymentDetailsScreenModel(detailsFactory(useCase), chargeId)
    val states = mutableListOf<PaymentDetailsScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { useCase.chargeIds().size == 1 }).isTrue()
    pumpMainLoop(1.seconds)
    model.close()

    assertThat(states).containsExactly(PaymentDetailsScreenState.Loading)
    return useCase.chargeIds().single()
  }

  private fun droppedEvents(viewModelName: String): Int = logs.entries().count { (priority, message) ->
    priority == LogPriority.WARN && message.startsWith("$viewModelName dropped an event")
  }
}

private fun historyFactory(useCase: GetPaymentsHistoryUseCase): ViewModelProvider.Factory =
  object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
      @Suppress("UNCHECKED_CAST")
      return PaymentHistoryViewModel(useCase) as T
    }
  }

/** Holds only the details ViewModel's assisted factory, as the graph's factory does among its others. */
private fun detailsFactory(useCase: GetChargeDetailsUseCase): MetroViewModelFactory = object : MetroViewModelFactory() {
  override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> = mapOf(
      PaymentDetailsViewModelFactory::class to {
        PaymentDetailsViewModelFactory { chargeId -> PaymentDetailsViewModel(chargeId, useCase) }
      },
    )
}

private class FakeHistoryUseCase : GetPaymentsHistoryUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, List<PaymentHistoryItem>> {
    calls.incrementAndGet()
    return Either.Right(emptyList())
  }
}

private class FakeDetailsUseCase : GetChargeDetailsUseCase {
  private val lock = NSLock()
  private val received = mutableListOf<String?>()

  fun chargeIds(): List<String?> = locked { received.toList() }

  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> {
    locked { received += id }
    return Either.Left(ErrorMessage())
  }

  private fun <T> locked(block: () -> T): T {
    lock.lock()
    try {
      return block()
    } finally {
      lock.unlock()
    }
  }
}

internal fun pumpMainLoop(duration: Duration) {
  pumpMainLoopUntil(duration) { false }
}

/** Drives the main run loop in 10 ms slices until [condition] holds or [timeout] passes. */
internal fun pumpMainLoopUntil(timeout: Duration = 10.seconds, condition: () -> Boolean): Boolean {
  val start = TimeSource.Monotonic.markNow()
  while (!condition()) {
    if (start.elapsedNow() > timeout) return false
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return true
}

/** Logs arrive from the main thread and from other threads. */
internal class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> = locked { captured.toList() }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message() + (throwable?.let { "\n" + it.stackTraceToString() } ?: "")
    locked { captured += priority to text }
  }

  private fun <T> locked(block: () -> T): T {
    lock.lock()
    try {
      return block()
    } finally {
      lock.unlock()
    }
  }
}
