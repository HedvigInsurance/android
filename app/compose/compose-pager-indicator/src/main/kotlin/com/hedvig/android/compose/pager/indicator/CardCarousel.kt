package com.hedvig.android.compose.pager.indicator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface

/**
 * Shows [items] as one swipeable card per item with a page indicator underneath, or as a single card
 * with no indicator when there is only one item. Renders nothing when [items] is empty.
 *
 * [contentPadding] is the horizontal room around the cards, applied so that the neighbouring cards
 * peek in from the sides while swiping. The [Modifier] handed to [itemContent] makes the card fill
 * its page, so callers pass it straight to their card.
 *
 * @param key identity of an item, letting a card keep its state when items around it are added or
 * removed. Only pass it when the items are guaranteed to be distinct: two items resolving to the
 * same key is an error in [HorizontalPager].
 * @param indicatorColor the color of the active page's dot.
 */
@Composable
fun <T> CardCarousel(
  items: List<T>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  key: ((item: T) -> Any)? = null,
  indicatorColor: Color = LocalContentColor.current,
  itemContent: @Composable (item: T, modifier: Modifier) -> Unit,
) {
  when (items.size) {
    0 -> {}

    1 -> {
      itemContent(items.first(), modifier.padding(contentPadding))
    }

    else -> {
      val pagerState = rememberPagerState(pageCount = { items.size })
      // Settling on a page that no longer exists leaves the pager showing a blank page, which
      // happens whenever the list shrinks while the last one is on screen.
      LaunchedEffect(items.size) {
        if (pagerState.currentPage >= items.size) {
          pagerState.scrollToPage(0)
        }
      }
      Column(modifier) {
        HorizontalPager(
          state = pagerState,
          contentPadding = contentPadding,
          beyondViewportPageCount = 1,
          pageSpacing = 8.dp,
          key = key?.let { keyOfItem ->
            { page -> items.getOrNull(page)?.let(keyOfItem) ?: page }
          },
          modifier = Modifier
            .fillMaxWidth()
            .systemGestureExclusion(),
        ) { page ->
          items.getOrNull(page)?.let { item ->
            itemContent(item, Modifier.fillMaxWidth())
          }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalPagerIndicator(
          pagerState = pagerState,
          pageCount = items.size,
          activeColor = indicatorColor,
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(contentPadding),
        )
      }
    }
  }
}

@Preview
@Composable
private fun PreviewCardCarousel() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CardCarousel(
        items = listOf("First card", "Second card", "Third card"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        key = { it },
      ) { item, modifier ->
        HedvigCard(modifier) {
          HedvigText(text = item, modifier = Modifier.padding(16.dp))
        }
      }
    }
  }
}
