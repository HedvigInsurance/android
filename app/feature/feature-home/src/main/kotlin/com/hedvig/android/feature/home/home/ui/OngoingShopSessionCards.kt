package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.feature.home.home.data.HomeData.OngoingShopSession
import hedvig.resources.Res
import hedvig.resources.resume_shopping
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OngoingShopSessionCards(
  sessions: List<OngoingShopSession>,
  onSessionClick: (OngoingShopSession) -> Unit,
  imageLoader: ImageLoader,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  if (sessions.isEmpty()) return
  if (sessions.size == 1) {
    OngoingShopSessionCard(
      session = sessions.single(),
      onClick = { onSessionClick(sessions.single()) },
      imageLoader = imageLoader,
      modifier = modifier.padding(contentPadding),
    )
  } else {
    val pagerState = rememberPagerState(pageCount = { sessions.size })
    Column(modifier) {
      HorizontalPager(
        state = pagerState,
        contentPadding = contentPadding,
        beyondViewportPageCount = 1,
        pageSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
      ) { page ->
        val session = sessions[page]
        OngoingShopSessionCard(
          session = session,
          onClick = { onSessionClick(session) },
          imageLoader = imageLoader,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))
      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = sessions.size,
        activeColor = LocalContentColor.current,
        modifier = Modifier.padding(contentPadding).align(Alignment.CenterHorizontally),
      )
    }
  }
}

@Composable
private fun OngoingShopSessionCard(
  session: OngoingShopSession,
  onClick: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        HedvigText(text = session.title, style = HedvigTheme.typography.headlineSmall)
        if (session.subtitle != null) {
          HedvigText(text = session.subtitle, style = HedvigTheme.typography.bodySmall)
        }
        HedvigText(
          text = stringResource(Res.string.resume_shopping),
          style = HedvigTheme.typography.label,
        )
      }
      if (session.pillowImage != null) {
        val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerXXLarge)
        Box(modifier = Modifier.size(72.dp)) {
          AsyncImage(
            model = session.pillowImage.src,
            contentDescription = session.pillowImage.description ?: EmptyContentDescription,
            placeholder = placeholder,
            error = placeholder,
            fallback = placeholder,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
