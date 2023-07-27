package com.hedvig.android.molecule.public

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

fun interface MoleculePresenter<Event, State> {
  @Composable
  fun MoleculePresenterScope<Event>.present(lastState: State): State
}

class MoleculePresenterScope<Event>(
  private val events: Flow<Event>,
) {
  @Composable
  fun CollectEvents(block: suspend (Event) -> Unit) {
    LaunchedEffect(Unit) {
      events.collect(block)
    }
  }
}
