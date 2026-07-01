package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.snapshotFlow
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
 *
 * Disposal runs two ways: [NavEntryDecorator.onPop] disposes promptly when a key leaves the rendered
 * stack, and a [snapshotFlow]-driven reconcile pass disposes any tracked key that is no longer in
 * [retainedContentKeys] — covering keys that never render (parked runs, or a session stashed on
 * logout) and so never fire onPop.
 */
@Composable
internal fun rememberRetainedSaveableStateHolderNavEntryDecorator(
  retainedContentKeys: () -> Set<Any>,
  saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): NavEntryDecorator<HedvigNavKey> {
  val latestRetained by rememberUpdatedState(retainedContentKeys)
  // Keys we have provided a SaveableStateProvider slot for. SaveableStateHolder can't enumerate its
  // own keys, so we mirror them here to drive the reconcile pass below. Snapshot-aware so reads and
  // writes from composition (decorate/onPop) and the reconcile coroutine stay consistent.
  val decoratedKeys = remember { mutableStateListOf<Any>() }

  // Reconcile pass: whenever the live-key set shrinks (a key was popped, parked-then-dropped, or its
  // whole session was stashed on logout), dispose the saved state of every tracked key that is no
  // longer live. This catches keys that never fire onPop because they never rendered.
  LaunchedEffect(saveableStateHolder) {
    snapshotFlow { latestRetained() }.collect { retained ->
      val gone = decoratedKeys.filter { it !in retained }
      gone.forEach { key ->
        saveableStateHolder.removeState(key)
        decoratedKeys.remove(key)
      }
    }
  }

  return remember(saveableStateHolder) {
    NavEntryDecorator(
      onPop = { contentKey ->
        if (contentKey !in latestRetained()) {
          saveableStateHolder.removeState(contentKey)
          decoratedKeys.remove(contentKey)
        }
      },
      // NavDisplay must never hand two live entries the same contentKey here, or SaveableStateProvider
      // throws "Key <X> was used multiple times" (b/516312097). Our custom Scenes therefore compare by
      // contentKey, not NavEntry identity (see BottomSheetScene and NavSuiteScene/NavUpBarScene), and
      // AuthTokenServiceImpl avoids the transient logged-in start-scene flap that stressed this path.
      decorate = { entry ->
        if (entry.contentKey !in decoratedKeys) {
          decoratedKeys.add(entry.contentKey)
        }
        saveableStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
      },
    )
  }
}
