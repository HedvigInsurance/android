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
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.concurrent.AtomicInt
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Runs on the test binary's main thread, which drains `Dispatchers.Main` only while a test pumps the main run loop.
 * A presenter composes once, so it calls its use case and delivers only Loading; the other states are covered by the
 * screen state tests. An event is shown to reach the ViewModel as a given presenter event by overflowing the
 * ViewModel's buffer of 20 with it: the dropped event is logged by name, and no presenter consumes it.
 */
class PaymentOverviewAndManualChargeScreenModelsTest {
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
  fun `the overview model starts as Loading without loading`() {
    val useCase = FakeUpcomingPaymentUseCase()
    val model = createPaymentOverviewScreenModel(overviewFactory(useCase))

    val seeded = model.currentState
    pumpMainLoop(1.seconds)

    assertThat(seeded).isEqualTo(PaymentOverviewScreenState.Loading)
    assertThat(useCase.calls.value).isEqualTo(0)
    model.close()
  }

  @Test
  fun `a collection of the overview model loads once and delivers Loading`() {
    val useCase = FakeUpcomingPaymentUseCase()
    val model = createPaymentOverviewScreenModel(overviewFactory(useCase))
    val states = mutableListOf<PaymentOverviewScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { useCase.calls.value == 1 }).isTrue()
    pumpMainLoop(1.seconds)

    assertThat(states).containsExactly(PaymentOverviewScreenState.Loading)
    model.close()
  }

  @Test
  fun `overview retry reaches the ViewModel as Retry`() {
    val model = createPaymentOverviewScreenModel(overviewFactory(FakeUpcomingPaymentUseCase()))

    repeat(21) { model.retry() }

    assertThat(droppedEvents("PaymentsViewModel")).containsExactly("Retry")
    model.close()
  }

  @Test
  fun `closing the overview model clears its ViewModel once and drops later retries`() {
    val useCase = FakeUpcomingPaymentUseCase()
    val model = createPaymentOverviewScreenModel(overviewFactory(useCase))

    model.close()
    model.close()
    repeat(25) { model.retry() }
    collections.launch { model.stateUpdates.collect {} }
    pumpMainLoop(1.seconds)

    assertThat(logs.messages().count { it == "PaymentsViewModel cleared" }).isEqualTo(1)
    assertThat(droppedEvents("PaymentsViewModel")).containsExactly()
    assertThat(useCase.calls.value).isEqualTo(0)
  }

  @Test
  fun `the manual charge model starts as Loading without loading or charging`() {
    val info = FakeManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val model = createPaymentManualChargeScreenModel(manualChargeFactory(info, trigger))

    val seeded = model.currentState
    pumpMainLoop(1.seconds)

    assertThat(seeded).isEqualTo(PaymentManualChargeScreenState.Loading)
    assertThat(info.calls.value).isEqualTo(0)
    assertThat(trigger.calls.value).isEqualTo(0)
    model.close()
  }

  @Test
  fun `a collection of the manual charge model loads once and delivers Loading without charging`() {
    val info = FakeManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val model = createPaymentManualChargeScreenModel(manualChargeFactory(info, trigger))
    val states = mutableListOf<PaymentManualChargeScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { info.calls.value == 1 }).isTrue()
    pumpMainLoop(1.seconds)
    model.close()
    pumpMainLoop(1.seconds)

    assertThat(states).containsExactly(PaymentManualChargeScreenState.Loading)
    assertThat(trigger.calls.value).isEqualTo(0)
  }

  @Test
  fun `manual charge retry reaches the ViewModel as Retry`() {
    assertThat(droppedManualChargeEvent { it.retry() }).isEqualTo("Retry")
  }

  @Test
  fun `manual charge refresh reaches the ViewModel as Refresh`() {
    assertThat(droppedManualChargeEvent { it.refresh() }).isEqualTo("Refresh")
  }

  @Test
  fun `manual charge triggerCharge reaches the ViewModel as TriggerCharge`() {
    assertThat(droppedManualChargeEvent { it.triggerCharge() }).isEqualTo("TriggerCharge")
  }

  @Test
  fun `closing the manual charge model clears its ViewModel once and drops later events`() {
    val info = FakeManualChargeInfoUseCase()
    val trigger = FakeTriggerManualChargeUseCase()
    val model = createPaymentManualChargeScreenModel(manualChargeFactory(info, trigger))

    model.close()
    model.close()
    repeat(25) {
      model.retry()
      model.refresh()
      model.triggerCharge()
    }
    collections.launch { model.stateUpdates.collect {} }
    pumpMainLoop(1.seconds)

    assertThat(logs.messages().count { it == "ManualChargeViewModel cleared" }).isEqualTo(1)
    assertThat(droppedEvents("ManualChargeViewModel")).containsExactly()
    assertThat(info.calls.value).isEqualTo(0)
    assertThat(trigger.calls.value).isEqualTo(0)
  }

  /** Fills the buffer with 20 calls of [send], then makes one more, which is dropped; returns its event's name. */
  private fun droppedManualChargeEvent(send: (PaymentManualChargeScreenModel) -> Unit): String {
    val trigger = FakeTriggerManualChargeUseCase()
    val model = createPaymentManualChargeScreenModel(manualChargeFactory(FakeManualChargeInfoUseCase(), trigger))

    repeat(21) { send(model) }
    model.close()

    assertThat(trigger.calls.value).isEqualTo(0)
    return droppedEvents("ManualChargeViewModel").single()
  }

  /** The names of the events [viewModelName] dropped, from the overflow error each drop logs. */
  private fun droppedEvents(viewModelName: String): List<String> = logs.entries()
    .filter { (priority, message) ->
      priority == LogPriority.WARN &&
        message.startsWith("$viewModelName dropped an event")
    }
    .map { (_, message) -> message.substringAfter("Event buffer overflow on event:").substringBefore(".") }
}

private fun overviewFactory(useCase: GetUpcomingPaymentUseCase): ViewModelProvider.Factory =
  object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
      @Suppress("UNCHECKED_CAST")
      return PaymentsViewModel(useCase) as T
    }
  }

private fun manualChargeFactory(
  info: GetManualChargeInfoUseCase,
  trigger: TriggerManualChargeUseCase,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST")
    return ManualChargeViewModel(info, trigger) as T
  }
}

private class FakeUpcomingPaymentUseCase : GetUpcomingPaymentUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> {
    calls.incrementAndGet()
    return Either.Left(ErrorMessage())
  }
}

private class FakeManualChargeInfoUseCase : GetManualChargeInfoUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult> {
    calls.incrementAndGet()
    return Either.Right(ManualChargeInfoResult.NoLongerChargeable)
  }
}

private class FakeTriggerManualChargeUseCase : TriggerManualChargeUseCase {
  val calls = AtomicInt(0)

  override suspend fun invoke(): Either<ErrorMessage, Unit> {
    calls.incrementAndGet()
    return Either.Right(Unit)
  }
}
