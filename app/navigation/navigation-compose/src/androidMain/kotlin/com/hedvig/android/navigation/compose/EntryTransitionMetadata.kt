package com.hedvig.android.navigation.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

/**
 * Builds the [NavEntry][androidx.navigation3.runtime.NavEntry] metadata that overrides the
 * [HedvigNavDisplay] default transitions for a single destination. `NavDisplay` reads these keys off
 * the target entry and falls back to its own defaults for any entry that doesn't provide them, so
 * only the few destinations that need a bespoke transition (e.g. the top-level tab roots that
 * fade through instead of sliding) have to opt in.
 *
 * Lives in androidMain because `NavDisplay`/`Scene` are android-only; the design system owns the
 * actual [EnterTransition]/[ExitTransition] specs and passes them in, keeping this module free of a
 * design-system dependency.
 */
fun entryTransitionMetadata(
  enter: EnterTransition,
  exit: ExitTransition,
  popEnter: EnterTransition = enter,
  popExit: ExitTransition = exit,
): Map<String, Any> = buildMap {
  putAll(NavDisplay.transitionSpec { enter togetherWith exit })
  putAll(NavDisplay.popTransitionSpec { popEnter togetherWith popExit })
}
