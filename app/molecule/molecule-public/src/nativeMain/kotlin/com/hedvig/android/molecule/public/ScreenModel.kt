@file:OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)

package com.hedvig.android.molecule.public

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.rickclephas.kmp.nativecoroutines.NativeFlow
import com.rickclephas.kmp.nativecoroutines.asNativeFlow
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * One screen shown by Swift. It owns the screen's [MoleculeViewModel], which runs until [close], and publishes the
 * ViewModel's state mapped to [State], the type Swift renders.
 *
 * A screen subclasses it with a concrete class whose constructor is internal, creates its ViewModel once through
 * [viewModel], and turns each Swift call into an event with [send]. A ViewModel without runtime arguments comes from
 * the graph's `ViewModelProvider.Factory`. One with arguments, such as a navigation argument, comes from the overload
 * taking `create`, which calls the ViewModel's assisted factory with them. Each model keeps its ViewModel in its own
 * store, so no two models share one.
 *
 * Rules for the Swift owner:
 * - Seed the displayed state from [currentState], then collect [states]. The first item always arrives on a later turn
 *   of the main queue, never during the call that starts the collection, and it usually repeats [currentState].
 * - Hold the collection's task, and capture the owner weakly inside it, so the collection does not keep the owner
 *   alive.
 * - Keep consuming items until the collection ends. A collection holding an item it has not asked past is ended by
 *   neither its cancellation nor [close], and stays suspended for the life of the process.
 * - When the screen goes away, cancel the collection, then call [close] on the main thread, from the owner's
 *   main-actor `deinit`. A model that is never closed keeps its ViewModel. Its presenter runs while any collection
 *   is active, and stops 5 seconds after the last one is cancelled.
 *
 * [states] delivers every item on the main thread. [close] runs on the main thread. [currentState] and the
 * subclass's event functions can be called from any thread.
 *
 * After [close], a new collection of [states] ends at once without an item, events are dropped, [close] does nothing,
 * and [currentState] keeps returning the last state.
 *
 * An exception while producing a state, such as a throwing mapping, is logged as an error and ends that collection
 * with an error; the screen keeps showing its last state.
 *
 * Swift must neither create nor subclass this class. Such an instance holds no ViewModel, so reading [currentState]
 * or [states] throws, and a Kotlin exception that reaches Swift terminates the app.
 */
@ObjCName(name = "HedvigSharedScreenModel", swiftName = "ScreenModel", exact = true)
abstract class ScreenModel<State : Any> protected constructor() {
  private val viewModelStore = ViewModelStore()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var source: Source<*, State>? = null
  private var closed = false

  /** Readable from any thread. Reading it does not start the presenter. */
  val currentState: State
    get() = requireSource().current()

  /**
   * The current state and every later one, delivered on the main thread. Each collection is independent, ends when it
   * is cancelled or when [close] is called, and keeps the presenter running while it lasts.
   */
  val states: NativeFlow<State>
    get() = stateUpdates
      .catch { throwable ->
        logcat(LogPriority.ERROR, throwable) { "${this@ScreenModel::class.simpleName} state collection failed" }
        throw throwable
      }
      .asNativeFlow(scope)

  /** The Kotlin side of [states], for tests. It is not ended by [close]. */
  @HiddenFromObjC
  val stateUpdates: Flow<State>
    get() = requireSource().flow()

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
    check(!closed) { "${this::class.simpleName} is closed, so it cannot create a ViewModel" }
    check(source == null) { "${this::class.simpleName} already holds a ViewModel" }
    val viewModel = ViewModelProvider.create(viewModelStore, viewModelFactory)[viewModelClass]
    viewModel.addCloseable(AutoCloseable { logcat { "${viewModelClass.simpleName} cleared" } })
    source = Source(viewModel.uiState, toState)
    return viewModel
  }

  /**
   * Creates the screen's ViewModel through [create], for a ViewModel built from runtime arguments, such as by its
   * assisted factory. Call it once, while the subclass is constructed.
   */
  @HiddenFromObjC
  protected fun <Event, UiState, VM : MoleculeViewModel<Event, UiState>> viewModel(
    viewModelClass: KClass<VM>,
    create: () -> VM,
    toState: (UiState) -> State,
  ): VM {
    val viewModelFactory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
        modelClass.cast(create.invoke())
    }
    return viewModel(viewModelFactory, viewModelClass, toState)
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

  private fun requireSource(): Source<*, State> = checkNotNull(source) {
    "${this::class.simpleName} has no ViewModel: call viewModel() while the subclass is constructed"
  }

  private class Source<UiState, State>(
    private val uiState: StateFlow<UiState>,
    private val toState: (UiState) -> State,
  ) {
    fun current(): State = toState(uiState.value)

    fun flow(): Flow<State> = uiState.map(toState)
  }
}
