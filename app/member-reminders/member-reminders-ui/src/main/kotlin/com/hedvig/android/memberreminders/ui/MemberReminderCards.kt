package com.hedvig.android.memberreminders.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.core.common.daysUntil
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminder.UpcomingRenewal
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
  navigateToContactInfo: () -> Unit,
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
    navigateToContactInfo = navigateToContactInfo,
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
  navigateToContactInfo: () -> Unit,
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
        navigateToContactInfo = navigateToContactInfo,
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
          navigateToContactInfo = navigateToContactInfo,
          modifier = modifier.fillMaxWidth(),
        )
      }

      Spacer(Modifier.height(16.dp))

      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = memberReminders.size,
        activeColor = HedvigTheme.colorScheme.fillPrimary,
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
  navigateToContactInfo: () -> Unit,
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

    is UpcomingRenewal -> ReminderCardUpcomingRenewals(
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

    is MemberReminder.ContactInfoUpdateNeeded -> ReminderCardUpdateContactInfo(
      navigateToContactInfo = navigateToContactInfo,
      modifier = modifier,
    )
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
    message = stringResource(R.string.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL),
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = InfoCardStyle.Buttons(
      leftButtonText = stringResource(R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_NOT_NOW),
      onLeftButtonClick = snoozeNotificationPermissionReminder,
      rightButtonText = stringResource(R.string.PUSH_NOTIFICATIONS_ALERT_ACTION_OK),
      onRightButtonClick = requestNotificationPermission,
    ),
  )
}

@Composable
fun ReminderCardUpdateContactInfo(navigateToContactInfo: () -> Unit, modifier: Modifier = Modifier) {
  HedvigNotificationCard(
    message = stringResource(R.string.MISSING_CONTACT_INFO_CARD_TEXT),
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = InfoCardStyle.Button(
      buttonText = stringResource(R.string.MISSING_CONTACT_INFO_CARD_BUTTON),
      onButtonClick = navigateToContactInfo,
    ),
  )
}

@Composable
private fun ReminderCardConnectPayment(navigateToConnectPayment: () -> Unit, modifier: Modifier = Modifier) {
  HedvigNotificationCard(
    message = stringResource(R.string.info_card_missing_payment_body),
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
      onButtonClick = navigateToConnectPayment,
    ),
  )
}

@Composable
private fun ReminderCardMissingPayment(
  terminationDate: LocalDate,
  onNavigateToNewConversation: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigNotificationCard(
    message = stringResource(R.string.info_card_missing_payment_missing_payments_body, terminationDate),
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(R.string.open_chat),
      onButtonClick = onNavigateToNewConversation,
    ),
  )
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
  val style = upcomingRenewal.draftCertificateUrl?.let {
    InfoCardStyle.Button(
      onButtonClick = { openUrl(it) },
      buttonText = stringResource(R.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
    )
  } ?: InfoCardStyle.Default
  HedvigNotificationCard(
    message = stringResource(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = style,
  )
}

@Composable
private fun ReminderCoInsuredInfo(navigateToContractDetail: () -> Unit, modifier: Modifier = Modifier) {
  HedvigNotificationCard(
    message = stringResource(R.string.CONTRACT_COINSURED_MISSING_INFO_TEXT),
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(R.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
      onButtonClick = navigateToContractDetail,
    ),
  )
}

@Preview
@Composable
private fun PreviewReminderCardEnableNotifications() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardEnableNotifications({}, {})
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardConnectPayment() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardConnectPayment({})
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardUpcomingRenewals() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardUpcomingRenewals(
        UpcomingRenewal("contract name", LocalDate.parse("2024-03-05"), ""),
        {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardCoInsuredInfo() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCoInsuredInfo(
        {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardUpdateContactInfo() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardUpdateContactInfo(
        {},
      )
    }
  }
}
