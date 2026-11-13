package com.hedvig.android.navigation.compose

/**
 * Which chrome the scene decorator should render for a chrome-bearing scene. Driven by a single
 * runtime probe supplied by `:app`. A lone deep link must never show the rail (it would expose the
 * broken index-0 runs invariant, see spec D7), yet a bare tab root has no Up affordance of its own.
 */
enum class LoneDeepLinkChrome {
  /** Normal: render the rail/bar. */
  ShowSuite,

  /** Lone tab root (no own Up): the decorator supplies a top-app-bar Up. */
  ShowUpBar,

  /** Lone deep "bar-keeper" that already renders its own Up: render nothing extra. */
  ShowNothing,
}
