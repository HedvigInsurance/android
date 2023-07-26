package com.hedvig.android.molecule.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

abstract class MoleculeViewModel<Event, Model>(
  initialState: Model,
  presenter: MoleculePresenter<Event, Model>,
  val sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5.seconds),
) : ViewModel() {
  private val scope = CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

  // Events have a capacity large enough to handle simultaneous UI events, but
  // small enough to surface issues if they get backed up for some reason.
  private val events = MutableSharedFlow<Event>(extraBufferCapacity = 20)

  private var seed: Model = initialState

  fun take(event: Event) {
    if (!events.tryEmit(event)) {
      error("Event buffer overflow on event:$event.")
    }
  }

  val models: StateFlow<Model> by lazy(LazyThreadSafetyMode.NONE) {
    val moleculePresenterScope = MoleculePresenterScope(events)
    moleculeFlow<Model>(RecompositionMode.ContextClock) {
      with(presenter) {
        moleculePresenterScope.present(seed)
      }
    }.onEach { model: Model ->
      seed = model
    }.stateIn(
      scope = scope,
      started = sharingStarted,
      initialValue = seed,
    )
  }
}
