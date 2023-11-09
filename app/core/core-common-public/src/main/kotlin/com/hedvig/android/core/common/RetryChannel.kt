package com.hedvig.android.core.common

import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest

/**
 * A class to use in the following use-case:
 *
 * There is an operation which is mapped to a StateFlow using the `stateIn` operator and needs a retry functionality.
 *
 * RetryChannel acts as a way to cancel the previous ongoing work due to built-in cancellation mechanisms of coroutines
 * with the operations on flows with the "Latest" suffix. This retry trigger is done by calling [retry].
 *
 * Full usage example
 * ```
 * val retryChannel = RetryChannel()
 * val viewState: StateFlow<ViewState> = retryChannel.transformLatest {
 *   val result = someUseCase.invoke(claimId).fold(
 *     ifFailure = { emit(ClaimDetailViewState.Error) },
 *     ifSuccess = { emit(ClaimDetailViewState.Content(it) },
 *   )
 * }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5.seconds), ClaimDetailViewState.Loading)
 * fun retry() {
 *   viewModelScope.launch { retryChannel.retry() }
 * }
 * ```
 *
 */
@OptIn(ExperimentalTypeInference::class)
class RetryChannel {
  /**
   * For a retry, we only care for latest values we wouldn't want to keep track of how many retries we need
   * to do, therefore a conflated Channel is what we need.
   * This creates a [kotlinx.coroutines.flow.MutableSharedFlow] with a replay of 1 so that all new observers start
   * doing work immediatelly. Dropping the last emission on buffer overflow means that all calls on
   * [MutableSharedFlow.emit] never suspend and [Channel.trySend] always succeed.
   */
  private val channel: MutableSharedFlow<Unit> = MutableSharedFlow<Unit>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  ).apply {
    tryEmit(Unit)
  }

  /**
   * From the [ConflatedChannel][kotlinx.coroutines.channels.ConflatedChannel] documentation:
   *
   * Sender to this channel never suspends and [Channel.trySend] always succeeds
   */
  fun retry() {
    channel.tryEmit(Unit)
  }

  /**
   * Converts the channel into a flow with an initial value in it to trigger the first operation automatically.
   * [transformLatest documentation provided here][Flow.transformLatest]
   *
   * Example usage
   * ```
   * retryChannel.transformLatest {
   *   suspendingUseCase.invoke().fold(
   *     ifFailure = { emit(ViewState.Error) },
   *     ifSuccess = { emit(ViewState.Content(it)) },
   *   )
   * }
   * ```
   */
  fun <R> transformLatest(
    @BuilderInference transform: suspend FlowCollector<R>.(value: Unit) -> Unit,
  ): Flow<R> {
    return channel.transformLatest(transform)
  }

  /**
   * Converts the channel into a flow with an initial value in it to trigger the first operation automatically.
   * [flatMapLatest documentation provided here][Flow.flatMapLatest]
   *
   * Example usage 1:
   * ```
   * retryChannel.flatMapLatest {
   *   useCaseReturningFlow.invoke()
   * }.map { it: UseCaseFlowItem -> ... }
   * ```
   *
   * Example usage 2:
   * ```
   * retryChannel.flatMapLatest {
   *   flow {
   *     emit(ViewState.Loading)
   *     suspendingUseCase.invoke().fold(
   *       ifFailure = { emit(ViewState.Error) },
   *       ifSuccess = { emit(ViewState.Content(it)) },
   *     )
   *   }
   * }
   * ```
   */
  fun <R> flatMapLatest(
    @BuilderInference transform: suspend (value: Unit) -> Flow<R>,
  ): Flow<R> {
    return channel.flatMapLatest(transform)
  }

  fun <R> mapLatest(
    @BuilderInference transform: suspend (value: Unit) -> R,
  ): Flow<R> {
    return channel.mapLatest(transform)
  }
}
