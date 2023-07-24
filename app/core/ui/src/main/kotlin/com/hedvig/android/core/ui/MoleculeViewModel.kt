package com.hedvig.android.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

  /**
   * This value serves as the initial value that the uiState [StateFlow] will emit. Also serves as a way to cache the
   * last emission, so that if the flow goes from being cold (in the backstack) to being hot again, the Presenter won't
   * override the last known value with a Loading state or something similar.
   */
  abstract var seed: UiState

  /**
   * This is the function that returns the Presenter to be used to produce the [UiState]. This will be remembered in the
   * context of the moleculeFlow, so that it stays alive for as long as the uiState [StateFlow] stays warm for.
   */
  protected abstract fun presenterFactory(): MoleculePresenter<Event, UiState>

  val uiState: StateFlow<UiState> by lazy(LazyThreadSafetyMode.NONE) {
    moleculeFlow<UiState>(RecompositionMode.ContextClock) {
      val presenter = remember { presenterFactory() }
      presenter.present(events)
    }.onEach {
      seed = it
    }.stateIn(
      scope = scope,
      started = SharingStarted.WhileSubscribed(0.1.seconds),
      initialValue = seed,
    )
  }
}

interface MoleculePresenter<Event, UiState> {
  val seed: UiState

  @Composable
  fun present(events: Flow<Event>): UiState
}
