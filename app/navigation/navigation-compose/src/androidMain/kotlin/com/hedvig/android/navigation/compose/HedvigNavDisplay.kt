package com.hedvig.android.navigation.compose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Nav3 replacement for the Nav2 `NavHost`. Renders [backstack] (a list of [HedvigNavKey] keys) and
 * resolves each key to a [androidx.navigation3.runtime.NavEntry] via the [builder] DSL
 * (`entry`).
 *
 * Decorator order matters — outermost first:
 * 1. retained saveable-state holder (per-entry `rememberSaveable` state is kept alive while the key
 *    is in [retainedContentKeys], i.e. in the rendered stack or parked in another tab run),
 * 2. retained per-entry ViewModelStore (same union-key check; ViewModels survive tab switches).
 *
 * The transitions are supplied by the caller as scene-pair classifiers (the design system owns the
 * actual motion specs and the `:app` module owns the entry→tab mapping; this module stays free of
 * both dependencies). Each lambda runs in [NavDisplay]'s [AnimatedContentTransitionScope] over the
 * outgoing/incoming [Scene]s, so the caller can pick a transition from the (from, to) pair rather
 * than from a single destination's metadata.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HedvigNavDisplay(
  backstack: Backstack,
  onBack: () -> Unit,
  retainedContentKeys: () -> Set<Any>,
  transitionSpec: AnimatedContentTransitionScope<Scene<HedvigNavKey>>.() -> ContentTransform,
  popTransitionSpec: AnimatedContentTransitionScope<Scene<HedvigNavKey>>.() -> ContentTransform,
  modifier: Modifier = Modifier,
  predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<HedvigNavKey>>.(Int) -> ContentTransform = {
    popTransitionSpec()
  },
  sharedTransitionScope: SharedTransitionScope? = null,
  sceneDecoratorStrategies: List<SceneDecoratorStrategy<HedvigNavKey>> = emptyList(),
  builder: EntryProviderScope<HedvigNavKey>.() -> Unit,
) {
  NavDisplay(
    backStack = backstack.entries,
    modifier = modifier,
    onBack = { onBack() },
    entryDecorators = listOf(
      rememberRetainedSaveableStateHolderNavEntryDecorator(retainedContentKeys),
      rememberRetainedViewModelStoreNavEntryDecorator(retainedContentKeys),
    ),
    sharedTransitionScope = sharedTransitionScope,
    sceneDecoratorStrategies = sceneDecoratorStrategies,
    transitionSpec = transitionSpec,
    popTransitionSpec = popTransitionSpec,
    predictivePopTransitionSpec = predictivePopTransitionSpec,
    entryProvider = entryProvider(builder = builder),
  )
}
