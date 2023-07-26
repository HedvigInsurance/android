package com.hedvig.android.molecule.public

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

fun interface MoleculePresenter<Event, Model> {
  @Composable
  fun MoleculePresenterScope<Event>.present(seed: Model): Model
}

class MoleculePresenterScope<Event>(
  val events: Flow<Event>,
) {
  @Composable
  fun CollectEvents(block: suspend (Event) -> Unit) {
    LaunchedEffect(Unit) {
      events.collect(block)
    }
  }
}
