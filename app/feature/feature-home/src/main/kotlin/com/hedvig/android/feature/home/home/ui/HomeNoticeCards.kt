package com.hedvig.android.feature.home.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.feature.home.home.data.HomeData.VeryImportantMessage
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.ui.getMemberReminderMessage
import hedvig.resources.CONTRACT_VIEW_CERTIFICATE_BUTTON
import hedvig.resources.Res
import hedvig.resources.important_message_hide
import hedvig.resources.important_message_read_more
import org.jetbrains.compose.resources.stringResource

/**
 * A card shown in the home notice carousel at the top of the content: the backend's very important
 * messages and the informational member reminders (currently upcoming renewals) that don't belong
 * in the To do list.
 */
internal sealed interface HomeNoticeCard {
  data class Important(val message: VeryImportantMessage) : HomeNoticeCard

  data class Renewal(val reminder: MemberReminder.UpcomingRenewal) : HomeNoticeCard
}

@Composable
internal fun HomeNoticeCarousel(
  cards: List<HomeNoticeCard>,
  openUrl: (String) -> Unit,
  hideImportantMessage: (id: String) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    targetState = cards,
    modifier = modifier,
  ) { animatedList ->
    if (animatedList.size == 1) {
      HomeNoticeCardItem(
        card = animatedList.first(),
        openUrl = openUrl,
        hideImportantMessage = hideImportantMessage,
        modifier = Modifier.padding(contentPadding),
      )
    } else {
      val pagerState = rememberPagerState(pageCount = { animatedList.size })
      Column {
        HorizontalPager(
          state = pagerState,
          contentPadding = contentPadding,
          beyondViewportPageCount = 1,
          pageSpacing = 8.dp,
          modifier = Modifier
            .fillMaxWidth()
            .systemGestureExclusion(),
        ) { page ->
          HomeNoticeCardItem(
            card = animatedList[page],
            openUrl = openUrl,
            hideImportantMessage = hideImportantMessage,
          )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalPagerIndicator(
          pagerState = pagerState,
          pageCount = animatedList.size,
          activeColor = LocalContentColor.current,
          modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(contentPadding),
        )
      }
    }
  }
}

@Composable
private fun HomeNoticeCardItem(
  card: HomeNoticeCard,
  openUrl: (String) -> Unit,
  hideImportantMessage: (id: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (card) {
    is HomeNoticeCard.Important -> VeryImportantMessageCard(
      openUrl = openUrl,
      hideImportantMessage = hideImportantMessage,
      veryImportantMessage = card.message,
      modifier = modifier,
    )

    is HomeNoticeCard.Renewal -> UpcomingRenewalCard(
      reminder = card.reminder,
      openUrl = openUrl,
      modifier = modifier,
    )
  }
}

@Composable
private fun VeryImportantMessageCard(
  openUrl: (String) -> Unit,
  hideImportantMessage: (id: String) -> Unit,
  veryImportantMessage: VeryImportantMessage,
  modifier: Modifier = Modifier,
) {
  key(veryImportantMessage.id) {
    HedvigNotificationCard(
      message = veryImportantMessage.message,
      priority = NotificationPriority.Attention,
      modifier = modifier.fillMaxSize(),
      withIcon = false,
      style = if (veryImportantMessage.linkInfo != null) {
        NotificationDefaults.InfoCardStyle.Buttons(
          leftButtonText = stringResource(Res.string.important_message_hide),
          rightButtonText = veryImportantMessage.linkInfo.buttonText
            ?: stringResource(Res.string.important_message_read_more),
          onLeftButtonClick = { hideImportantMessage(veryImportantMessage.id) },
          onRightButtonClick = { openUrl(veryImportantMessage.linkInfo.link) },
        )
      } else {
        NotificationDefaults.InfoCardStyle.Button(
          buttonText = stringResource(Res.string.important_message_hide),
          onButtonClick = { hideImportantMessage(veryImportantMessage.id) },
        )
      },
    )
  }
}

@Composable
private fun UpcomingRenewalCard(
  reminder: MemberReminder.UpcomingRenewal,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  key(reminder.id) {
    val certificateUrl = reminder.draftCertificateUrl
    HedvigNotificationCard(
      message = getMemberReminderMessage(reminder),
      priority = NotificationPriority.Info,
      modifier = modifier.fillMaxSize(),
      withIcon = false,
      style = if (certificateUrl != null) {
        NotificationDefaults.InfoCardStyle.Button(
          buttonText = stringResource(Res.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
          onButtonClick = { openUrl(certificateUrl) },
        )
      } else {
        NotificationDefaults.InfoCardStyle.Default
      },
    )
  }
}
