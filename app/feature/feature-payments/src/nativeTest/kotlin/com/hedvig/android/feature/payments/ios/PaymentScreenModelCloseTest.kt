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
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModelFactory
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.molecule.public.ScreenModel
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.concurrent.AtomicInt
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred

/**
 * Collects each screen model's states through the native flow, as Swift does, while its use case is suspended in a
 * load, then closes the model and lets the load finish. The display link of the test process produces no frames, so
 * the proof that nothing can follow close is that the load is cancelled: its result never reaches the presenter.
 */
class PaymentScreenModelCloseTest {
  private val logs = CapturingLogger()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `a history load finishing after close is cancelled and not delivered`() {
    val load = SuspendedLoad()
    val model = PaymentHistoryScreenModel(
      factory { PaymentHistoryViewModel(GetPaymentsHistoryUseCase { load.await(Either.Right(emptyList())) }) },
    )

    assertNothingFollowsClose(model, load, PaymentHistoryScreenState.Loading)
  }

  @Test
  fun `a details load finishing after close is cancelled and not delivered`() {
    val load = SuspendedLoad()
    val useCase = GetChargeDetailsUseCase { load.await(Either.Left(ErrorMessage())) }
    val model = createPaymentDetailsScreenModel(detailsFactory(useCase), "charge-1")

    assertNothingFollowsClose(model, load, PaymentDetailsScreenState.Loading)
  }

  @Test
  fun `an overview load finishing after close is cancelled and not delivered`() {
    val load = SuspendedLoad()
    val useCase = object : GetUpcomingPaymentUseCase {
      override suspend fun invoke(): Either<ErrorMessage, PaymentOverview> = load.await(Either.Left(ErrorMessage()))
    }
    val model = createPaymentOverviewScreenModel(factory { PaymentsViewModel(useCase) })

    assertNothingFollowsClose(model, load, PaymentOverviewScreenState.Loading)
  }

  @Test
  fun `a manual charge load finishing after close is cancelled and not delivered`() {
    val load = SuspendedLoad()
    val charges = AtomicInt(0)
    val info = object : GetManualChargeInfoUseCase {
      override suspend fun invoke(): Either<ErrorMessage, ManualChargeInfoResult> =
        load.await(Either.Right(ManualChargeInfoResult.NoLongerChargeable))
    }
    val trigger = object : TriggerManualChargeUseCase {
      override suspend fun invoke(): Either<ErrorMessage, Unit> {
        charges.incrementAndGet()
        return Either.Right(Unit)
      }
    }
    val model = createPaymentManualChargeScreenModel(factory { ManualChargeViewModel(info, trigger) })

    assertNothingFollowsClose(model, load, PaymentManualChargeScreenState.Loading)
    assertThat(charges.value).isEqualTo(0)
  }

  @Test
  fun `history reload reaches the ViewModel as Reload`() {
    val model = PaymentHistoryScreenModel(
      factory { PaymentHistoryViewModel(GetPaymentsHistoryUseCase { Either.Right(emptyList<PaymentHistoryItem>()) }) },
    )

    repeat(21) { model.reload() }
    model.close()

    assertThat(droppedEvents("PaymentHistoryViewModel")).containsExactly("Reload")
  }

  @Test
  fun `details reload reaches the ViewModel as Reload`() {
    val useCase = GetChargeDetailsUseCase { Either.Left(ErrorMessage()) }
    val model = createPaymentDetailsScreenModel(detailsFactory(useCase), "charge-1")

    repeat(21) { model.reload() }
    model.close()

    assertThat(droppedEvents("PaymentDetailsViewModel")).containsExactly("Reload")
  }

  private fun <S : Any> assertNothingFollowsClose(model: ScreenModel<S>, load: SuspendedLoad, loading: S) {
    val collection = NativeCollection(model)
    assertThat(pumpMainLoopUntil { load.started.value == 1 }).isTrue()

    model.close()
    load.finish()
    pumpMainLoop(1.seconds)

    assertThat(collection.items).containsExactly(loading)
    assertThat(collection.cancellations).isEqualTo(1)
    assertThat(load.cancelled.value).isEqualTo(1)
    assertThat(model.currentState).isEqualTo(loading)
  }

  /** The names of the events [viewModelName] dropped, from the overflow error each drop logs. */
  private fun droppedEvents(viewModelName: String): List<String> = logs.entries()
    .filter { (priority, message) -> priority == LogPriority.WARN && message.startsWith("$viewModelName dropped") }
    .map { (_, message) -> message.substringAfter("Event buffer overflow on event:").substringBefore(".") }
}

/** A load that stays suspended until [finish], recording whether it started and whether it was cancelled. */
private class SuspendedLoad {
  private val gate = CompletableDeferred<Unit>()
  val started = AtomicInt(0)
  val cancelled = AtomicInt(0)

  suspend fun <T> await(result: T): T {
    started.incrementAndGet()
    try {
      gate.await()
    } catch (e: CancellationException) {
      cancelled.incrementAndGet()
      throw e
    }
    return result
  }

  fun finish() {
    gate.complete(Unit)
  }
}

/** One collection of [ScreenModel.states] through the native flow's callbacks, as Swift collects it. */
private class NativeCollection<S : Any>(model: ScreenModel<S>) {
  val items = mutableListOf<S>()
  var cancellations = 0

  init {
    model.states(
      { item, next, _ ->
        items += item
        next()
      },
      { _, _ -> },
      { _, _ -> cancellations++ },
    )
  }
}

private fun factory(create: () -> ViewModel): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST")
    return create() as T
  }
}

private fun detailsFactory(useCase: GetChargeDetailsUseCase): MetroViewModelFactory = object : MetroViewModelFactory() {
  override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> = mapOf(
      PaymentDetailsViewModelFactory::class to {
        PaymentDetailsViewModelFactory { chargeId -> PaymentDetailsViewModel(chargeId, useCase) }
      },
    )
}

private fun GetPaymentsHistoryUseCase(
  body: suspend () -> Either<ErrorMessage, List<PaymentHistoryItem>>,
): GetPaymentsHistoryUseCase = object : GetPaymentsHistoryUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<PaymentHistoryItem>> = body()
}

private fun GetChargeDetailsUseCase(
  body: suspend () -> Either<ErrorMessage, PaymentDetails>,
): GetChargeDetailsUseCase = object : GetChargeDetailsUseCase {
  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> = body()
}
