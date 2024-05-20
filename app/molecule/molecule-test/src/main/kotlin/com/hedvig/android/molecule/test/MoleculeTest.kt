package com.hedvig.android.molecule.test

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.MutableSharedFlow

suspend fun <Event, State> MoleculePresenter<Event, State>.test(
  initialState: State,
  block: suspend MoleculePresenterTestContext<Event, State>.() -> Unit,
) {
  // Events have a capacity large enough to handle simultaneous UI events, but
  // small enough to surface issues if they get backed up for some reason.
  val events = MutableSharedFlow<Event>(extraBufferCapacity = 20)
  val moleculePresenterScope = MoleculePresenterScope(events)
  moleculeFlow(RecompositionMode.Immediate) {
    moleculePresenterScope.present(initialState)
  }.test(name = "molecule presenter turbine") {
    MoleculePresenterTestContextImpl(events, this).block()
  }
}

interface MoleculePresenterTestContext<Event, State> : TurbineTestContext<State> {
  fun sendEvent(event: Event)

  /**
   *  Awaits the next item, and checks that it's the exact same [State] as it was in the previous emission
   */
  suspend fun awaitUnchanged()
}

private class MoleculePresenterTestContextImpl<Event, State>(
  private val eventChannel: MutableSharedFlow<Event>,
  private val turbineTestContext: TurbineTestContext<State>,
) : MoleculePresenterTestContext<Event, State>,
  TurbineTestContext<State> by turbineTestContext {
  override fun sendEvent(event: Event) {
    if (!eventChannel.tryEmit(event)) {
      error("Event buffer overflow on event:$event.")
    }
  }

  private var lastModel: State? = null

  override suspend fun awaitItem(): State {
    while (true) {
      val nextModel = turbineTestContext.awaitItem()
      if (nextModel != lastModel) {
        lastModel = nextModel
        return nextModel
      }
    }
  }

  override suspend fun skipItems(count: Int) {
    repeat(count) { awaitItem() }
  }

  override suspend fun awaitUnchanged() {
    val nextModel = turbineTestContext.awaitItem()
    check(nextModel == lastModel) {
      "Expected recomposition to re-emit $lastModel, but received $nextModel"
    }
  }
}
