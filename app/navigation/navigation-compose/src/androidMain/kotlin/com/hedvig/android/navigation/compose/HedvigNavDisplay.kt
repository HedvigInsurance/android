package com.hedvig.android.navigation.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import com.hedvig.android.navigation.common.HedvigNavKey

/**
 * Nav3 replacement for the Nav2 `NavHost`. Renders [backstack] (a list of [HedvigNavKey] keys) and
 * resolves each key to a [androidx.navigation3.runtime.NavEntry] via the [builder] DSL
 * (`entry`).
 *
 * Decorator order matters — outermost first:
 * 1. saveable-state holder (per-entry `rememberSaveable` survives pop/re-push),
 * 2. per-entry ViewModelStore (each key owns its store; pop clears it).
 *
 * The four transitions are supplied by the caller (the design system owns the actual motion specs;
 * this module stays free of a design-system dependency). They are combined into the
 * forward/pop/predictive-pop [androidx.compose.animation.ContentTransform]s that [NavDisplay] needs.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HedvigNavDisplay(
  backstack: Backstack,
  onBack: () -> Unit,
  enterTransition: EnterTransition,
  exitTransition: ExitTransition,
  popEnterTransition: EnterTransition,
  popExitTransition: ExitTransition,
  modifier: Modifier = Modifier,
  sharedTransitionScope: SharedTransitionScope? = null,
  sceneDecoratorStrategies: List<SceneDecoratorStrategy<HedvigNavKey>> = emptyList(),
  builder: EntryProviderScope<HedvigNavKey>.() -> Unit,
) {
  NavDisplay(
    backStack = backstack.entries,
    modifier = modifier,
    onBack = { onBack() },
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    sharedTransitionScope = sharedTransitionScope,
    sceneDecoratorStrategies = sceneDecoratorStrategies,
    transitionSpec = { enterTransition togetherWith exitTransition },
    popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
    predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
    entryProvider = entryProvider(builder = builder),
  )
}
