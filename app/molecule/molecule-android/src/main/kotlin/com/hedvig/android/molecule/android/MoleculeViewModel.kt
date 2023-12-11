package com.hedvig.android.molecule.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

abstract class MoleculeViewModel<Event, State>(
  initialState: State,
  presenter: MoleculePresenter<Event, State>,
  sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
) : ViewModel() {
  private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  /**
   * Events have a capacity large enough to handle simultaneous UI events, but small enough to surface issues if they
   * get backed up for some reason.
   * The channel will be re-used across instances where the SharedFlow is turned cold again after there were no
   * collectors according to [sharingStarted]. The new Presenter launched by the new StateFlow instance will then be
   * responsible of consuming the unconsumed channel [Event]s.
   */
  private val events: Channel<Event> = Channel<Event>(
    capacity = 20,
    onBufferOverflow = BufferOverflow.SUSPEND,
  )
  private val eventsFlow: Flow<Event> = events.receiveAsFlow()

  private var lastState: State = initialState

  fun emit(event: Event) {
    if (events.trySend(event).isFailure) {
      error("Event buffer overflow on event:$event.")
    }
  }

  val uiState: StateFlow<State> = moleculeFlow<State>(RecompositionMode.ContextClock) {
    with(presenter) {
      MoleculePresenterScope<Event>(eventsFlow).present(lastState)
    }
  }.onEach { newState: State ->
    lastState = newState
  }.stateIn(
    scope = scope,
    started = sharingStarted,
    initialValue = lastState,
  )
}
