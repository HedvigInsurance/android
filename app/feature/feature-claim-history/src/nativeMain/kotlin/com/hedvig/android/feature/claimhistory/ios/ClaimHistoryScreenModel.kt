@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.claimhistory.ios

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.hedvig.android.feature.claimhistory.ClaimHistory
import com.hedvig.android.feature.claimhistory.ClaimHistoryEvent
import com.hedvig.android.feature.claimhistory.ClaimHistoryUiState
import com.hedvig.android.feature.claimhistory.ClaimHistoryViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.toNSDate

@HiddenFromObjC
fun createClaimHistoryScreenModel(viewModelFactory: ViewModelProvider.Factory): ClaimHistoryScreenModel =
  ClaimHistoryScreenModel(viewModelFactory)

/**
 * One claim history screen. It owns the screen's ViewModel, which runs until [close].
 *
 * Rules for the owner:
 * - Seed the displayed state from [currentState], then call [observe]. Callbacks always arrive on a later turn of the
 *   main queue, never during the [observe] call, and the first one usually repeats [currentState].
 * - The [observe] closure captures its owner weakly (`[weak self]`). The model holds the closure until the
 *   subscription is cancelled, so a strong capture keeps the owner, and with it this model, alive for good.
 * - Call [close] on the main thread when the screen goes away, from the owner's main-actor `deinit`. A model that is
 *   never closed keeps its ViewModel, and the presenter keeps running for 5 seconds after its last subscription ends.
 *
 * After [close], [observe] returns a subscription that never calls back, [reload] and [close] do nothing, and
 * [currentState] keeps returning the last state.
 */
class ClaimHistoryScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) {
  private val viewModelStore = ViewModelStore()
  private val viewModel: ClaimHistoryViewModel =
    ViewModelProvider.create(viewModelStore, viewModelFactory)[ClaimHistoryViewModel::class]
  private val scope = CoroutineScope(
    SupervisorJob() + Dispatchers.Main + CoroutineExceptionHandler { _, throwable ->
      logcat(LogPriority.ERROR, throwable) { "ClaimHistoryScreenModel observer failed" }
    },
  )
  private var closed = false

  init {
    viewModel.addCloseable(AutoCloseable { logcat { "ClaimHistoryViewModel cleared" } })
  }

  /** Readable from any thread. Reading it does not start the presenter. */
  val currentState: ClaimHistoryScreenState
    get() = viewModel.uiState.value.toScreenState()

  /**
   * Calls [onState] on the main thread with the current state and every later one, until the returned subscription
   * is cancelled or the model is closed. Each call adds an independent subscription.
   */
  fun observe(onState: (ClaimHistoryScreenState) -> Unit): ClaimHistorySubscription {
    if (closed) return ClaimHistorySubscription(null)
    val job = scope.launch {
      viewModel.uiState.collect { onState(it.toScreenState()) }
    }
    return ClaimHistorySubscription(job)
  }

  /** Loads the history again. A reload that does not fit in the event buffer is dropped and logged. */
  fun reload() {
    if (closed) return
    try {
      viewModel.emit(ClaimHistoryEvent.Reload)
    } catch (e: IllegalStateException) {
      logcat(LogPriority.WARN, e) { "ClaimHistoryScreenModel dropped a reload" }
    }
  }

  /** Cancels every subscription and clears the ViewModel, which stops the presenter at once. Main thread only. */
  fun close() {
    if (closed) return
    closed = true
    scope.cancel()
    viewModelStore.clear()
  }
}

class ClaimHistorySubscription internal constructor(private val job: Job?) {
  /** Stops the callbacks of this subscription. Calling it again does nothing. */
  fun cancel() {
    job?.cancel()
  }
}

private fun ClaimHistoryUiState.toScreenState(): ClaimHistoryScreenState = when (this) {
  ClaimHistoryUiState.Loading -> ClaimHistoryScreenState.Loading
  ClaimHistoryUiState.NoHistory -> ClaimHistoryScreenState.Empty
  ClaimHistoryUiState.Error -> ClaimHistoryScreenState.Failed
  is ClaimHistoryUiState.Content -> ClaimHistoryScreenState.Content(claims.toList().map { it.toItem() })
}

private fun ClaimHistory.toItem(): ClaimHistoryItem = ClaimHistoryItem(
  id = id,
  claimType = claimType,
  submittedAt = submittedAt?.toNSDate(),
  outcome = when (outcome) {
    ClaimHistory.ClaimOutcome.PAID -> ClaimHistoryOutcome.PAID
    ClaimHistory.ClaimOutcome.NOT_COMPENSATED -> ClaimHistoryOutcome.NOT_COMPENSATED
    ClaimHistory.ClaimOutcome.NOT_COVERED -> ClaimHistoryOutcome.NOT_COVERED
    ClaimHistory.ClaimOutcome.UNRESPONSIVE -> ClaimHistoryOutcome.UNRESPONSIVE
    ClaimHistory.ClaimOutcome.UNKNOWN, null -> ClaimHistoryOutcome.UNKNOWN
  },
  isPartnerClaim = isPartnerClaim,
)
