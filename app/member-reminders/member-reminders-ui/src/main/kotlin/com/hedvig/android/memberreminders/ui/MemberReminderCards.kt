package com.hedvig.android.memberreminders.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.core.common.daysUntil
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminder.UpcomingRenewal
import com.hedvig.android.notification.permission.NotificationPermissionState
import hedvig.resources.CHIP_ID_MISSING_BUTTON
import hedvig.resources.CHIP_ID_MISSING_MESSAGE
import hedvig.resources.CONTRACT_COINSURED_MISSING_ADD_INFO
import hedvig.resources.CONTRACT_COINSURED_MISSING_INFO_TEXT
import hedvig.resources.CONTRACT_COOWNERS_MISSING_INFO_TEXT
import hedvig.resources.CONTRACT_VIEW_CERTIFICATE_BUTTON
import hedvig.resources.DASHBOARD_RENEWAL_PROMPTER_BODY
import hedvig.resources.MISSING_CONTACT_INFO_CARD_BUTTON
import hedvig.resources.MISSING_CONTACT_INFO_CARD_TEXT
import hedvig.resources.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL
import hedvig.resources.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON
import hedvig.resources.PUSH_NOTIFICATIONS_ALERT_ACTION_NOT_NOW
import hedvig.resources.PUSH_NOTIFICATIONS_ALERT_ACTION_OK
import hedvig.resources.Res
import hedvig.resources.info_card_missing_payment_body
import hedvig.resources.info_card_missing_payment_missing_payments_body
import hedvig.resources.open_chat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Composable
fun getMemberReminderMessage(reminder: MemberReminder): String {
  return when (reminder) {
    is MemberReminder.CoInsuredInfo -> stringResource(
      when (reminder.coInsuredType) {
        CoInsuredFlowType.CoInsured -> Res.string.CONTRACT_COINSURED_MISSING_INFO_TEXT
        CoInsuredFlowType.CoOwners -> Res.string.CONTRACT_COOWNERS_MISSING_INFO_TEXT
      }
    )

    is MemberReminder.PaymentReminder.ConnectPayment ->
      stringResource(Res.string.info_card_missing_payment_body)

    is MemberReminder.PaymentReminder.TerminationDueToMissedPayments ->
      stringResource(Res.string.info_card_missing_payment_missing_payments_body, reminder.terminationDate)

    is UpcomingRenewal ->
    {
      val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), reminder.renewalDate) {
        daysUntil(reminder.renewalDate)
      }
      stringResource(Res.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal)
    }


    is MemberReminder.EnableNotifications ->
      stringResource(Res.string.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL)

    is MemberReminder.ContactInfoUpdateNeeded ->
      stringResource(Res.string.MISSING_CONTACT_INFO_CARD_TEXT)

    is MemberReminder.MissingChipId ->
      stringResource(Res.string.CHIP_ID_MISSING_MESSAGE)
  }
}

@Composable
fun rememberMaxLineCountForReminders(
  memberReminders: List<MemberReminder>,
  maxWidthPx: Int,
): Int {
  val textMeasurer = rememberTextMeasurer()
  val density = LocalDensity.current
  val fontFamilyResolver = LocalFontFamilyResolver.current

  val messages = memberReminders.map { reminder -> getMemberReminderMessage(reminder) }
  val textStyle = LocalTextStyle.current

  return remember(messages, textMeasurer, maxWidthPx, textStyle, density, fontFamilyResolver) {
    messages.maxOfOrNull { message ->
      val textLayout = textMeasurer.measure(
        text = AnnotatedString(message),
        style = textStyle,
        constraints = Constraints(maxWidth = maxWidthPx),
        density = density,
        fontFamilyResolver = fontFamilyResolver,
      )
      textLayout.lineCount
    } ?: 1
  }
}

@Composable
fun MemberReminderCardsWithoutNotification(
  memberReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  contentPadding: PaddingValues,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
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
    navigateToChipId = navigateToChipId,
    modifier = modifier,
  )
}

@Composable
fun MemberReminderCards(
  memberReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
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
        navigateToContactInfo = navigateToContactInfo,
        navigateToChipId = navigateToChipId,
        modifier = modifier.padding(contentPadding),
        minLines = 1
      )
    } else if (memberReminders.isNotEmpty()) {
      val stableReminderIds = remember(memberReminders.map { it.id }) {
        memberReminders.map { it.id }
      }

      val pagerState = rememberPagerState(pageCount = { memberReminders.size })

      LaunchedEffect(memberReminders.size) {
        if (pagerState.currentPage >= memberReminders.size && memberReminders.isNotEmpty()) {
          pagerState.scrollToPage(0)
        }
      }

      BoxWithConstraints(Modifier.fillMaxWidth()) {
        val minLineCount = rememberMaxLineCountForReminders(
          memberReminders = memberReminders,
          maxWidthPx = constraints.maxWidth
        )
        Column {
          HorizontalPager(
            state = pagerState,
            contentPadding = contentPadding,
            beyondViewportPageCount = 1,
            pageSpacing = 8.dp,
            key = { index -> stableReminderIds.getOrNull(index) ?: index },
            modifier = Modifier
              .fillMaxWidth()
              .systemGestureExclusion(),
          ) { page ->
            memberReminders.getOrNull(page)?.let { reminder ->
              MemberReminderCard(
                memberReminder = reminder,
                navigateToAddMissingInfo = navigateToAddMissingInfo,
                navigateToConnectPayment = navigateToConnectPayment,
                openUrl = openUrl,
                onNavigateToNewConversation = onNavigateToNewConversation,
                snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
                notificationPermissionState = notificationPermissionState,
                navigateToContactInfo = navigateToContactInfo,
                navigateToChipId = navigateToChipId,
                modifier = modifier.fillMaxWidth(),
                minLines = minLineCount
              )
            }
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = pagerState.pageCount,
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
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
  openUrl: (String) -> Unit,
  snoozeNotificationPermissionReminder: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  notificationPermissionState: NotificationPermissionState?,
  minLines: Int,
  modifier: Modifier = Modifier,
) {
  when (memberReminder) {
    is MemberReminder.CoInsuredInfo -> {
      ReminderCoInsuredInfo(
        memberReminder = memberReminder,
        navigateToAddMissingInfo = {
          navigateToAddMissingInfo(memberReminder.contractId, memberReminder.coInsuredType)
        },
        modifier = modifier,
        minLines = minLines,
      )
    }

    is MemberReminder.PaymentReminder.ConnectPayment -> {
      ReminderCardConnectPayment(
        navigateToConnectPayment = navigateToConnectPayment,
        modifier = modifier,
        minLines = minLines,
        memberReminder = memberReminder,
      )
    }

    is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> {
      ReminderCardMissingPayment(
        memberReminder = memberReminder,
        onNavigateToNewConversation = onNavigateToNewConversation,
        modifier = modifier,
        minLines = minLines,
      )
    }

    is UpcomingRenewal -> {
      ReminderCardUpcomingRenewals(
        openUrl = openUrl,
        memberReminder = memberReminder,
        modifier = modifier,
        minLines = minLines,
      )
    }

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
            minLines = minLines,
          )
        }
      }
    }

    is MemberReminder.ContactInfoUpdateNeeded -> {
      ReminderCardUpdateContactInfo(
        navigateToContactInfo = navigateToContactInfo,
        modifier = modifier,
        minLines = minLines,
      )
    }

    is MemberReminder.MissingChipId -> {
      ReminderMissingChipId(
        navigateToChipId = navigateToChipId,
        minLines = minLines,
        modifier = modifier,
      )
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
  minLines: Int = 1,
  modifier: Modifier = Modifier,
) {
  val message = getMemberReminderMessage(MemberReminder.EnableNotifications())
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = InfoCardStyle.Buttons(
      leftButtonText = stringResource(Res.string.PUSH_NOTIFICATIONS_ALERT_ACTION_NOT_NOW),
      onLeftButtonClick = snoozeNotificationPermissionReminder,
      rightButtonText = stringResource(Res.string.PUSH_NOTIFICATIONS_ALERT_ACTION_OK),
      onRightButtonClick = requestNotificationPermission,
    ),
    minLines = minLines,
  )
}

@Composable
fun ReminderCardUpdateContactInfo(
  navigateToContactInfo: () -> Unit,
  modifier: Modifier = Modifier,
  minLines: Int = 1,
) {
  val message = getMemberReminderMessage(MemberReminder.ContactInfoUpdateNeeded)
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = InfoCardStyle.Button(
      buttonText = stringResource(Res.string.MISSING_CONTACT_INFO_CARD_BUTTON),
      onButtonClick = navigateToContactInfo,
    ),
    minLines = minLines,
  )
}

@Composable
internal fun ReminderMissingChipId(
  navigateToChipId: () -> Unit,
  minLines: Int,
  modifier: Modifier = Modifier,
) {
  val message = getMemberReminderMessage(MemberReminder.MissingChipId())
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(Res.string.CHIP_ID_MISSING_BUTTON),
      onButtonClick = navigateToChipId,
    ),
    minLines = minLines
  )
}

@Composable
private fun ReminderCardConnectPayment(
  memberReminder: MemberReminder,
  navigateToConnectPayment: () -> Unit,
  modifier: Modifier = Modifier,
  minLines: Int = 1,
) {
  val message = getMemberReminderMessage(memberReminder)
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(Res.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
      onButtonClick = navigateToConnectPayment,
    ),
    minLines = minLines,
  )
}

@Composable
private fun ReminderCardMissingPayment(
  memberReminder: MemberReminder,
  onNavigateToNewConversation: () -> Unit,
  modifier: Modifier = Modifier,
  minLines: Int = 1,
) {
  val message = getMemberReminderMessage(memberReminder)
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(Res.string.open_chat),
      onButtonClick = onNavigateToNewConversation,
    ),
    minLines = minLines,
  )
}

@Composable
private fun ReminderCardUpcomingRenewals(
  memberReminder: MemberReminder.UpcomingRenewal,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
  minLines: Int = 1,
) {
  val message = getMemberReminderMessage(memberReminder)
  val style = memberReminder.draftCertificateUrl?.let {
    InfoCardStyle.Button(
      onButtonClick = { openUrl(it) },
      buttonText = stringResource(Res.string.CONTRACT_VIEW_CERTIFICATE_BUTTON),
    )
  } ?: InfoCardStyle.Default
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Info,
    style = style,
    minLines = minLines,
  )
}

@Composable
private fun ReminderCoInsuredInfo(
  memberReminder: MemberReminder,
  navigateToAddMissingInfo: () -> Unit,
  modifier: Modifier = Modifier,
  minLines: Int = 1,
) {
  val message = getMemberReminderMessage(memberReminder)
  HedvigNotificationCard(
    message = message,
    modifier = modifier,
    priority = NotificationPriority.Attention,
    style = InfoCardStyle.Button(
      buttonText = stringResource(Res.string.CONTRACT_COINSURED_MISSING_ADD_INFO),
      onButtonClick = navigateToAddMissingInfo,
    ),
    minLines = minLines,
  )
}

@Preview
@Composable
private fun PreviewReminderCardEnableNotifications() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardEnableNotifications(
        snoozeNotificationPermissionReminder = {},
        requestNotificationPermission = {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardConnectPayment() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardConnectPayment(
        navigateToConnectPayment = {},
        memberReminder = MemberReminder.PaymentReminder.ConnectPayment()
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardMissingPayment() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardConnectPayment(
        navigateToConnectPayment = {},
        memberReminder = MemberReminder.PaymentReminder.TerminationDueToMissedPayments(
          terminationDate = LocalDate(2029,1,1))
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderCardUpcomingRenewals() {
  val upcomingRenewal = UpcomingRenewal("contract name", LocalDate.parse("2024-03-05"), "")
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderCardUpcomingRenewals(
        openUrl = {},
        memberReminder = upcomingRenewal
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
        memberReminder = MemberReminder.CoInsuredInfo("", CoInsuredFlowType.CoInsured),
        navigateToAddMissingInfo = {},
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
        navigateToContactInfo = {},
      )
    }
  }
}

@Preview
@Composable
private fun PreviewReminderMissingChipId() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ReminderMissingChipId(
        navigateToChipId = {},
        minLines = 1
      )
    }
  }
}
