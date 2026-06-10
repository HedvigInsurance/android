package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Drop-in replacement for `rememberViewModelStoreNavEntryDecorator` that retains the ViewModelStore
 * of entries which leave the rendered back stack but remain "live" (parked in another tab).
 *
 * The stock decorator clears an entry's ViewModelStore when it is popped from the rendered stack.
 * We instead consult [retainedContentKeys] (the union of the rendered stack and all parked tab runs)
 * and clear only the stores of keys that are genuinely gone. A key that merely moved into a parked
 * run keeps its ViewModels (and their SavedStateHandles) alive for when the run is restored.
 *
 * Disposal runs two ways: onPop clears a store promptly when its key leaves the rendered stack, and
 * a [snapshotFlow]-driven reconcile pass ([EntryViewModel.retainOnly]) clears any store whose key is
 * no longer in [retainedContentKeys] — covering parked/stashed keys that never fire onPop.
 */
@Composable
internal fun rememberRetainedViewModelStoreNavEntryDecorator(
  retainedContentKeys: () -> Set<Any>,
  viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
  },
): NavEntryDecorator<HedvigNavKey> {
  val latestRetained by rememberUpdatedState(retainedContentKeys)
  LaunchedEffect(viewModelStoreOwner) {
    snapshotFlow { latestRetained() }.collect { retained ->
      viewModelStoreOwner.viewModelStore.getEntryViewModel().retainOnly(retained)
    }
  }
  return remember(viewModelStoreOwner) {
    RetainedViewModelStoreNavEntryDecorator(viewModelStoreOwner.viewModelStore) { contentKey ->
      contentKey !in latestRetained()
    }
  }
}

private class RetainedViewModelStoreNavEntryDecorator(
  rootViewModelStore: ViewModelStore,
  shouldRemoveStoreForKey: (Any) -> Boolean,
) : NavEntryDecorator<HedvigNavKey>(
    onPop = { contentKey ->
      if (shouldRemoveStoreForKey(contentKey)) {
        rootViewModelStore.getEntryViewModel().clearViewModelStoreOwnerForKey(contentKey)
      }
    },
    decorate = { entry ->
      val entryViewModelStore = rootViewModelStore.getEntryViewModel().viewModelStoreForKey(entry.contentKey)
      val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
      val childViewModelStoreOwner = remember {
        object :
          ViewModelStoreOwner,
          SavedStateRegistryOwner by savedStateRegistryOwner,
          HasDefaultViewModelProviderFactory {
          override val viewModelStore: ViewModelStore
            get() = entryViewModelStore

          override val defaultViewModelProviderFactory: ViewModelProvider.Factory
            get() = SavedStateViewModelFactory()

          override val defaultViewModelCreationExtras: CreationExtras
            get() = MutableCreationExtras().also {
              it[SAVED_STATE_REGISTRY_OWNER_KEY] = this
              it[VIEW_MODEL_STORE_OWNER_KEY] = this
            }

          init {
            require(this.lifecycle.currentState == Lifecycle.State.INITIALIZED) {
              "The Lifecycle state is already beyond INITIALIZED. The " +
                "RetainedViewModelStoreNavEntryDecorator requires adding the " +
                "saveable-state decorator to ensure support for SavedStateHandles."
            }
            enableSavedStateHandles()
          }
        }
      }
      CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
        entry.Content()
      }
    },
  )

private class EntryViewModel : ViewModel() {
  private val owners = mutableMapOf<Any, ViewModelStore>()

  fun viewModelStoreForKey(key: Any): ViewModelStore = owners.getOrPut(key) { ViewModelStore() }

  fun clearViewModelStoreOwnerForKey(key: Any) {
    owners.remove(key)?.clear()
  }

  fun retainOnly(keys: Set<Any>) {
    val gone = owners.keys.filter { it !in keys }
    gone.forEach { owners.remove(it)?.clear() }
  }

  override fun onCleared() {
    owners.forEach { (_, store) -> store.clear() }
  }
}

private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
  val provider = ViewModelProvider.create(
    store = this,
    factory = viewModelFactory { initializer { EntryViewModel() } },
  )
  return provider[EntryViewModel::class]
}
