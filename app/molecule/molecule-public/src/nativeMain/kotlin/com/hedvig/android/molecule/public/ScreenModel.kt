@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package com.hedvig.android.molecule.public

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.rickclephas.kmp.nativecoroutines.NativeFlow
import com.rickclephas.kmp.nativecoroutines.asNativeFlow
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * One screen shown by Swift. It owns the screen's [MoleculeViewModel], which runs until [close], and publishes the
 * ViewModel's state mapped to [State], the type Swift renders.
 *
 * A screen subclasses it with a concrete class whose constructor is internal, creates its ViewModel once through
 * [viewModel], and turns each Swift call into an event with [send].
 *
 * Rules for the Swift owner:
 * - Seed the displayed state from [currentState], then collect [states]. The first item always arrives on a later turn
 *   of the main queue, never during the call that starts the collection, and it usually repeats [currentState].
 * - Hold the collection's task, and capture the owner weakly inside it, so the collection does not keep the owner
 *   alive.
 * - When the screen goes away, cancel the collection, then call [close] on the main thread, from the owner's
 *   main-actor `deinit`. A model that is never closed keeps its ViewModel. Its presenter runs while any collection
 *   is active, and stops 5 seconds after the last one is cancelled.
 *
 * [states] delivers every item on the main thread. [close] runs on the main thread. [currentState] and the
 * subclass's event functions can be called from any thread.
 *
 * After [close], a new collection of [states] ends at once without an item, events are dropped, [close] does nothing,
 * and [currentState] keeps returning the last state.
 */
@ObjCName(name = "HedvigSharedScreenModel", swiftName = "ScreenModel", exact = true)
abstract class ScreenModel<State : Any> protected constructor() {
  private val viewModelStore = ViewModelStore()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var source: Source<*, State>? = null
  private var closed = false

  /** Readable from any thread. Reading it does not start the presenter. */
  val currentState: State
    get() = checkNotNull(source).current()

  /**
   * The current state and every later one, delivered on the main thread. Each collection is independent, ends when it
   * is cancelled or when [close] is called, and keeps the presenter running while it lasts.
   */
  val states: NativeFlow<State>
    get() = stateUpdates.asNativeFlow(scope)

  /** The Kotlin side of [states], for tests. It is not ended by [close]. */
  @HiddenFromObjC
  val stateUpdates: Flow<State>
    get() = checkNotNull(source).flow()

  /** Ends every collection of [states] and clears the ViewModel, which stops the presenter at once. Main thread only. */
  fun close() {
    if (closed) return
    closed = true
    scope.cancel()
    viewModelStore.clear()
  }

  /** Creates the screen's ViewModel. Call it once, while the subclass is constructed. */
  @HiddenFromObjC
  protected fun <Event, UiState, VM : MoleculeViewModel<Event, UiState>> viewModel(
    viewModelFactory: ViewModelProvider.Factory,
    viewModelClass: KClass<VM>,
    toState: (UiState) -> State,
  ): VM {
    check(source == null) { "${this::class.simpleName} already holds a ViewModel" }
    val viewModel = ViewModelProvider.create(viewModelStore, viewModelFactory)[viewModelClass]
    viewModel.addCloseable(AutoCloseable { logcat { "${viewModelClass.simpleName} cleared" } })
    source = Source(viewModel.uiState, toState)
    return viewModel
  }

  /**
   * Offers [event] to the ViewModel without waiting. Does nothing after [close]. An event that does not fit in the
   * ViewModel's buffer is dropped and logged. A call that races [close] on another thread can reach a ViewModel that
   * no longer consumes events.
   */
  @HiddenFromObjC
  protected fun <Event> MoleculeViewModel<Event, *>.send(event: Event) {
    if (closed) return
    try {
      emit(event)
    } catch (e: IllegalStateException) {
      logcat(LogPriority.WARN, e) { "${this::class.simpleName} dropped an event" }
    }
  }

  private class Source<UiState, State>(
    private val uiState: StateFlow<UiState>,
    private val toState: (UiState) -> State,
  ) {
    fun current(): State = toState(uiState.value)

    fun flow(): Flow<State> = uiState.map(toState)
  }
}
