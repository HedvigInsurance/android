package com.hedvig.android.navigation.common

/**
 * Marker letting the root destination of a top-level tab declare its [TopLevelTab], so the
 * navigation brain can map a destination back to its tab without naming each feature's key (which
 * would force feature-to-feature deps). Only tab roots implement this; other destinations (e.g. the
 * Login root) do not.
 */
interface TopLevelTabRoot {
  val topLevelTab: TopLevelTab
}
