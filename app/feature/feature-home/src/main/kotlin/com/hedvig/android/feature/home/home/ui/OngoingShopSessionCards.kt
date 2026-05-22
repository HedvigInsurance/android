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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.feature.home.home.data.HomeData.OngoingShopSession
import hedvig.resources.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_TOTAL
import hedvig.resources.ongoing_shop_session_action_button
import hedvig.resources.ongoing_shop_session_offer_expires_in_days
import hedvig.resources.ongoing_shop_session_offer_expires_in_hours
import hedvig.resources.ongoing_shop_session_offer_expires_in_minutes
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import org.jetbrains.compose.resources.pluralStringResource
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
    Column(modifier = Modifier.padding(16.dp)) {
      Header(session = session, imageLoader = imageLoader)
      if (session.monthlyNet != null) {
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))
        TotalPriceRow(monthlyNet = session.monthlyNet, monthlyGross = session.monthlyGross)
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = stringResource(Res.string.ongoing_shop_session_action_button),
        onClick = onClick,
        buttonSize = ButtonSize.Medium,
        buttonStyle = ButtonStyle.Secondary,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
      )
      val expiresInText = formatExpiresIn(session.validTo)
      if (expiresInText != null) {
        Spacer(Modifier.height(8.dp))
        HedvigText(
          text = expiresInText,
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun Header(session: OngoingShopSession, imageLoader: ImageLoader) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    if (session.pillowImage != null) {
      val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerXXLarge)
      Box(modifier = Modifier.size(40.dp)) {
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
      Spacer(Modifier.size(12.dp))
    }
    Column(modifier = Modifier.weight(1f)) {
      HedvigText(text = session.title, style = HedvigTheme.typography.headlineSmall)
      if (session.subtitle != null) {
        HedvigText(
          text = session.subtitle,
          style = HedvigTheme.typography.bodySmall,
          color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        )
      }
    }
  }
}

@Composable
private fun TotalPriceRow(monthlyNet: UiMoney, monthlyGross: UiMoney?) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    HedvigText(text = stringResource(Res.string.TIER_FLOW_TOTAL))
    Spacer(Modifier.weight(1f))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End)) {
      if (monthlyGross != null && monthlyGross != monthlyNet) {
        HedvigText(
          text = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, monthlyGross),
          style = LocalTextStyle.current.copy(
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            textDecoration = TextDecoration.LineThrough,
          ),
        )
      }
      HedvigText(text = stringResource(Res.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, monthlyNet))
    }
  }
}

/**
 * Returns null when the session isn't close enough to expiry to be worth surfacing (or has already expired).
 */
@Composable
private fun formatExpiresIn(validTo: kotlin.time.Instant): String? {
  val remaining = validTo - Clock.System.now()
  if (remaining <= Duration.ZERO) return null
  return when {
    remaining < 1.hours -> pluralStringResource(
      Res.plurals.ongoing_shop_session_offer_expires_in_minutes,
      remaining.inWholeMinutes.toInt(),
      remaining.inWholeMinutes,
    )

    remaining < 1.days -> pluralStringResource(
      Res.plurals.ongoing_shop_session_offer_expires_in_hours,
      remaining.inWholeHours.toInt(),
      remaining.inWholeHours,
    )

    remaining < 7.days -> pluralStringResource(
      Res.plurals.ongoing_shop_session_offer_expires_in_days,
      remaining.inWholeDays.toInt(),
      remaining.inWholeDays,
    )

    else -> null
  }
}
