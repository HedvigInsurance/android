package com.hedvig.app.util.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.transformLatest
import kotlin.experimental.ExperimentalTypeInference

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
 * }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClaimDetailViewState.Loading)
 * fun retry() {
 *   viewModelScope.launch { retryChannel.retry() }
 * }
 * ```
 *
 */
@OptIn(ExperimentalTypeInference::class, ExperimentalCoroutinesApi::class)
class RetryChannel {

    /**
     * For a retry, we only care for latest values we wouldn't want to keep track of how many retries we need
     * to do, therefore a conflated Channel is what we need. This creates a
     * [kotlinx.coroutines.channels.ConflatedChannel] which also comes with the benefit of [Channel.send] never
     * suspending and [Channel.trySend] always succeeding.
     */
    private val channel: Channel<Unit> = Channel(Channel.CONFLATED)

    /**
     * From the [ConflatedChannel][kotlinx.coroutines.channels.ConflatedChannel] documentation:
     *
     * Sender to this channel never suspends and [Channel.trySend] always succeeds
     */
    fun retry() {
        channel.trySend(Unit)
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
    fun <R> transformLatest(@BuilderInference transform: suspend FlowCollector<R>.(value: Unit) -> Unit): Flow<R> {
        return channel
            .receiveAsFlow()
            .onStart { emit(Unit) }
            .transformLatest(transform)
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
    fun <R> flatMapLatest(@BuilderInference transform: suspend (value: Unit) -> Flow<R>): Flow<R> {
        return channel
            .receiveAsFlow()
            .onStart { emit(Unit) }
            .flatMapLatest(transform)
    }
}
