package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.navigation3.runtime.NavEntryDecorator
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Drop-in replacement for `rememberSaveableStateHolderNavEntryDecorator` that retains the saved
 * state of entries which leave the rendered back stack but remain "live" (parked in another tab).
 *
 * The stock decorator removes an entry's `rememberSaveable`/`SavedStateHandle` state the moment the
 * entry leaves the rendered back stack. We instead consult [retainedContentKeys] (the union of the
 * rendered stack and all parked tab runs) and only remove state for keys that are genuinely gone —
 * popped to nowhere, not merely parked.
 */
@Composable
internal fun rememberRetainedSaveableStateHolderNavEntryDecorator(
  retainedContentKeys: () -> Set<Any>,
  saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): NavEntryDecorator<HedvigNavKey> {
  val latestRetained by rememberUpdatedState(retainedContentKeys)
  return remember(saveableStateHolder) {
    NavEntryDecorator(
      onPop = { contentKey ->
        if (contentKey !in latestRetained()) {
          saveableStateHolder.removeState(contentKey)
        }
      },
      decorate = { entry ->
        saveableStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
      },
    )
  }
}
