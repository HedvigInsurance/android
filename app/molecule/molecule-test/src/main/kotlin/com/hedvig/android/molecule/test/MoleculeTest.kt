package com.hedvig.android.molecule.test

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.MutableSharedFlow

suspend fun <Event, Model> MoleculePresenter<Event, Model>.test(
  initialState: Model,
  block: suspend MoleculePresenterTestContext<Event, Model>.() -> Unit,
) {
  // Events have a capacity large enough to handle simultaneous UI events, but
  // small enough to surface issues if they get backed up for some reason.
  val events = MutableSharedFlow<Event>(extraBufferCapacity = 20)
  val moleculePresenterScope = MoleculePresenterScope(events)
  moleculeFlow(RecompositionMode.Immediate) {
    moleculePresenterScope.present(initialState)
  }.test {
    MoleculePresenterTestContext(this, PresenterTestingScopeImpl(events)).block()
  }
}

class MoleculePresenterTestContext<Event, Model>(
  turbineTestContext: TurbineTestContext<Model>,
  presenterTestingScope: PresenterTestingScope<Event>,
) : TurbineTestContext<Model> by turbineTestContext,
  PresenterTestingScope<Event> by presenterTestingScope

interface PresenterTestingScope<Event> {
  fun sendEvent(event: Event)
}

private class PresenterTestingScopeImpl<Event>(
  private val eventChannel: MutableSharedFlow<Event>,
) : PresenterTestingScope<Event> {
  override fun sendEvent(event: Event) {
    if (!eventChannel.tryEmit(event)) {
      error("Event buffer overflow on event:$event.")
    }
  }
}
