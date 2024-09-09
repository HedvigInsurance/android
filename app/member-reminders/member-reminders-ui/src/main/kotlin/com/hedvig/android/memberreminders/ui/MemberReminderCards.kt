package com.hedvig.android.memberreminders.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.core.common.android.time.daysUntil
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.InfoCardTextButton
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.notification.permission.NotificationPermissionState
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@Composable
fun MemberReminderCardsWithoutNotification(
  memberReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToAddMissingInfo: (String) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  MemberReminderCards(
    memberReminders = memberReminders,
    navigateToConnectPayment = navigateToConnectPayment,
    openUrl = openUrl,
    navigateToAddMissingInfo = navigateToAddMissingInfo,
    onNavigateToNewConversation = onNavigateToNewConversation,
    snoozeNotificationPermissionReminder = {},
    notificationPermissionState = null,
    contentPadding = contentPadding,
    modifier = modifier,
  )
}

@Composable
fun MemberReminderCards(
  memberReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToAddMissingInfo: (String) -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  notificationPermissionState: NotificationPermissionState?,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    if (memberReminders.size == 1) {
      MemberReminderCard(
        memberReminder = memberReminders.first(),
        navigateToAddMissingInfo = navigateToAddMissingInfo,
        navigateToConnectPayment = navigateToConnectPayment,
        openUrl = openUrl,
        onNavigateToNewConversation = onNavigateToNewConversation,
        snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
        notificationPermissionState = notificationPermissionState,
        modifier = modifier.padding(contentPadding),
      )
    } else if (memberReminders.isNotEmpty()) {
      val pagerState = rememberPagerState(pageCount = { memberReminders.size })
      HorizontalPager(
        state = pagerState,
        contentPadding = contentPadding,
        beyondViewportPageCount = 1,
        pageSpacing = 8.dp,
        key = { index -> memberReminders[index].id },
        modifier = Modifier
          .fillMaxWidth()
          .systemGestureExclusion(),
      ) { page ->
        MemberReminderCard(
          memberReminder = memberReminders[page],
          navigateToAddMissingInfo = navigateToAddMissingInfo,
          navigateToConnectPayment = navigateToConnectPayment,
          openUrl = openUrl,
          onNavigateToNewConversation = onNavigateToNewConversation,
          snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
          notificationPermissionState = notificationPermissionState,
          modifier = modifier.fillMaxWidth(),
        )
      }

      Spacer(Modifier.height(16.dp))

      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = memberReminders.size,
        activeColor = LocalContentColor.current,
        modifier = Modifier
          .padding(contentPadding)
          .align(Alignment.CenterHorizontally),
      )
    }
  }
}

@Composable
private fun ColumnScope.MemberReminderCard(
  memberReminder: MemberReminder,
  navigateToAddMissingInfo: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  notificationPermissionState: NotificationPermissionState?,
  modifier: Modifier = Modifier,
) {
  when (memberReminder) {
    is MemberReminder.CoInsuredInfo -> ReminderCoInsuredInfo(
      navigateToContractDetail = {
        navigateToAddMissingInfo(memberReminder.contractId)
      },
      modifier = modifier,
    )

    is MemberReminder.PaymentReminder.ConnectPayment -> ReminderCardConnectPayment(
      navigateToConnectPayment = navigateToConnectPayment,
      modifier = modifier,
    )

    is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> ReminderCardMissingPayment(
      terminationDate = memberReminder.terminationDate,
      onNavigateToNewConversation = onNavigateToNewConversation,
      modifier = modifier,
    )

    is MemberReminder.UpcomingRenewal -> ReminderCardUpcomingRenewals(
      upcomingRenewal = memberReminder,
      openUrl = openUrl,
      modifier = modifier,
    )

    is MemberReminder.EnableNotifications -> {
      if (notificationPermissionState != null) {
        AnimatedVisibility(
          visible = true,
          enter = cardReminderEnterTransition,
          exit = cardReminderExitTransition,
          label = "enableNotifications animated visibility",
          modifier = modifier,
        ) {
          ReminderCardEnableNotifications(
            snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
            requestNotificationPermission = notificationPermissionState::launchPermissionRequest,
          )
        }
      }
    }
  }
}

private val cardReminderEnterTransition = fadeIn() + expandVertically(
  expandFrom = Alignment.Top,
  initialHeight = { (it * 0.9).toInt() },
  clip = false,
)
private val cardReminderExitTransition = fadeOut() + shrinkVertically(
  shrinkTowards = Alignment.CenterVertically,
)

@Composable
fun ReminderCardEnableNotifications(
  snoozeNotificationPermissionReminder: () -> Unit,
  requestNotificationPermission: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigNotificationCard(
    text = stringResource(R.string.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL),
    modifier = modifier,
  ) {
    Row {
      InfoCardTextButton(
        onClick = snoozeNotificationPermissionReminder,
        text = stringResource(R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_NOT_NOW),
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      InfoCardTextButton(
        onClick = requestNotificationPermission,
        text = stringResource(R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_OK),
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ReminderCardConnectPayment(navigateToConnectPayment: () -> Unit, modifier: Modifier = Modifier) {
  VectorWarningCard(
    text = stringResource(R.string.info_card_missing_payment_body),
    modifier = modifier,
  ) {
    InfoCardTextButton(
      onClick = navigateToConnectPayment,
      text = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ReminderCardMissingPayment(
  terminationDate: LocalDate,
  onNavigateToNewConversation: () -> Unit,
  modifier: Modifier = Modifier,
) {
  VectorWarningCard(
    text = stringResource(R.string.info_card_missing_payment_missing_payments_body, terminationDate),
    modifier = modifier,
  ) {
    InfoCardTextButton(
      onClick = onNavigateToNewConversation,
      text = stringResource(R.string.open_chat),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun ReminderCardUpcomingRenewals(
  upcomingRenewal: MemberReminder.UpcomingRenewal,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), upcomingRenewal.renewalDate) {
    daysUntil(upcomingRenewal.renewalDate)
  }
  VectorInfoCard(
    text = stringResource(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
    modifier = modifier,
  ) {
    upcomingRenewal.draftCertificateUrl?.let {
      InfoCardTextButton(
        onClick = { openUrl(it) },
        text = stringResource(R.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun ReminderCoInsuredInfo(navigateToContractDetail: () -> Unit, modifier: Modifier = Modifier) {
  VectorWarningCard(
    text = stringResource(R.string.CONTRACT_COINSURED_MISSING_INFO_TEXT),
    modifier = modifier,
  ) {
    InfoCardTextButton(
      onClick = navigateToContractDetail,
      text = stringResource(R.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewReminderCardEnableNotifications() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ReminderCardEnableNotifications({}, {})
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewReminderCardConnectPayment() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ReminderCardConnectPayment({})
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewReminderCardUpcomingRenewals() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ReminderCardUpcomingRenewals(
        MemberReminder.UpcomingRenewal("contract name", LocalDate.parse("2024-03-05"), ""),
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewReminderCardCoInsuredInfo() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ReminderCoInsuredInfo(
        {},
      )
    }
  }
}
