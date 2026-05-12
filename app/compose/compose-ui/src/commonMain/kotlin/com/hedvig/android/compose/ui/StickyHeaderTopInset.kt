package com.hedvig.android.compose.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * Computes the top padding to apply to a `stickyHeader`'s inner content so that, when stuck,
 * its visible content lands [topContentPadding] below the LazyColumn's outer top edge instead
 * of *at* it.
 *
 * Compose pins a stuck header at offset = `-beforeContentPadding`
 * (LazyLayoutStickyItems.kt:98 in compose-foundation), which puts its visible top edge at the
 * LazyColumn's outer top edge regardless of `contentPadding.top`. When the LazyColumn has a
 * top contentPadding so content can scroll behind a translucent system bar, this means the
 * stuck header lands behind that bar. Apply the returned value as `Modifier.padding(top = ...)`
 * on the sticky header's inner content (inside any background-painting `Surface`) to push it
 * back down to the post-contentPadding line; the transition through natural→stuck is smooth.
 *
 * When [topContentPadding] is 0 the returned value is always 0, so it's safe to leave wired
 * up on platforms (like Android) where the top app bar already consumed the inset.
 *
 * @param listState The LazyColumn's state.
 * @param stickyHeaderKey The same key passed to `stickyHeader(key = ...)`, needed to find the
 *   header's measured offset in `listState.layoutInfo.visibleItemsInfo`.
 * @param topContentPadding The top value of the LazyColumn's `contentPadding`.
 */
@Composable
fun rememberStickyHeaderTopInset(
  listState: LazyListState,
  stickyHeaderKey: Any,
  topContentPadding: Dp,
): Dp {
  val density = LocalDensity.current
  val topInsetPx = with(density) { topContentPadding.roundToPx() }
  val offsetPx by remember(listState, stickyHeaderKey, topInsetPx) {
    derivedStateOf {
      val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == stickyHeaderKey }
      if (info == null) 0 else (-info.offset).coerceIn(0, topInsetPx)
    }
  }
  return with(density) { offsetPx.toDp() }
}
