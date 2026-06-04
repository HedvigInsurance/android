package com.hedvig.android.navigation.core

/**
 * Marker letting the root destination of a top-level tab declare its [TopLevelGraph], so the
 * navigation brain can map a destination back to its tab without naming each feature's key (which
 * would force feature-to-feature deps). Only tab roots implement this; other destinations (e.g. the
 * Login root) do not.
 */
interface TopLevelGraphRoot {
  val topLevelGraph: TopLevelGraph
}
