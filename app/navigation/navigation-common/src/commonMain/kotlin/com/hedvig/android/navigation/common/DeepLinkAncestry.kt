package com.hedvig.android.navigation.common

/**
 * Opt-in marker letting a deep-linkable key declare how to rebuild a synthetic back stack to its
 * "task" when it is entered alone (e.g. via a notification while logged out).
 *
 * Keys that do not implement this fall back to the pure tab-rooted default `[HomeKey, tabRoot, key]`.
 */
interface DeepLinkAncestry {
  /** The tab this key's task lives under. The synthetic stack starts `[HomeKey, owningTab.root, …]`. */
  val owningTab: TopLevelTab

  /**
   * Ordered intermediate ancestors, ROOT → NEAR. Same-module invariant: a key may only reference
   * keys from its own feature module here (a flow's ancestors are always its own screens). The jump
   * up to a tab is expressed abstractly via [owningTab], so no feature ever names another feature's
   * key — keeping "features can't depend on features" intact.
   */
  val syntheticParents: List<HedvigNavKey>
}
