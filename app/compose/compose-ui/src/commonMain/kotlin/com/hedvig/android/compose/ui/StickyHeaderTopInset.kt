package com.hedvig.android.compose.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * For a LazyColumn whose content scrolls behind a translucent system bar (so its
 * `contentPadding.top` is the bar's inset), a `stickyHeader` will pin against the screen
 * edge and end up behind the bar. Apply the returned Dp as `Modifier.padding(top = ...)` on
 * the sticky's inner content to keep it visible just below the bar instead.
 *
 * ```
 * stickyHeader(key = HeaderKey) {
 *   val top = rememberStickyHeaderTopInset(listState, HeaderKey, contentPadding.calculateTopPadding())
 *   Surface(modifier = Modifier.fillMaxWidth()) {
 *     Column(modifier = Modifier.padding(top = top)) { /* header content */ }
 *   }
 * }
 * ```
 *
 * Returns `0.dp` when [topContentPadding] is `0.dp`, so it's a no-op on screens where the
 * top bar is drawn by Compose (Android) and only does work on screens where the list scrolls
 * behind the top bar.
 */
@Composable
fun rememberStickyHeaderTopInset(listState: LazyListState, stickyHeaderKey: Any, topContentPadding: Dp): Dp {
  val density = LocalDensity.current
  val topContentPaddingPx = with(density) { topContentPadding.roundToPx() }
  val offsetPx by remember(listState, stickyHeaderKey, topContentPaddingPx) {
    derivedStateOf {
      val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == stickyHeaderKey }
      if (info == null) 0 else (-info.offset).coerceIn(0, topContentPaddingPx)
    }
  }
  return with(density) { offsetPx.toDp() }
}
