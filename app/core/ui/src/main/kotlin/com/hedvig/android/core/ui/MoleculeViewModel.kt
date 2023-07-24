package com.hedvig.android.core.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

abstract class MoleculeViewModel<Event, UiState> : ViewModel() {
  private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  // Events have a capacity large enough to handle simultaneous UI events, but
  // small enough to surface issues if they get backed up for some reason.
  private val events = MutableSharedFlow<Event>(extraBufferCapacity = 20)

  fun take(event: Event) {
    if (!events.tryEmit(event)) {
      error("Event buffer overflow on event:$event.")
    }
  }

  @Composable
  protected abstract fun models(events: Flow<Event>): UiState

  /**
   * This value serves as the initial value that the uiState [StateFlow] will emit. Also serves as a way to cache the
   * last emission, so that if the flow goes from being cold (in the backstack) to being hot again, the Presenter won't
   * override the last known value with a Loading state or something similar.
   */
  abstract var seed: UiState

  val uiState: StateFlow<UiState> by lazy(LazyThreadSafetyMode.NONE) {
    moleculeFlow<UiState>(RecompositionMode.ContextClock) {
      models(events)
    }.onEach {
      seed = it
    }.stateIn(
      scope = scope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = seed,
    )
  }
}
