package com.hedvig.android.memberreminders.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.memberreminders.ApplicableMemberReminders
import com.hedvig.android.memberreminders.UpcomingRenewal
import com.hedvig.android.notification.permission.NotificationPermissionState
import hedvig.resources.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

@Composable
fun MemberReminderCards(
  memberReminders: ApplicableMemberReminders,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  notificationPermissionState: NotificationPermissionState,
  snoozeNotificationPermissionReminder: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    AnimatedVisibility(
      visible = memberReminders.enableNotifications != null,
      enter = cardReminderEnterTransition,
      exit = cardReminderExitTransition,
      label = "enableNotifications animated visibility",
    ) {
      ReminderCardEnableNotifications(
        snoozeNotificationPermissionReminder = snoozeNotificationPermissionReminder,
        requestNotificationPermission = notificationPermissionState::launchPermissionRequest,
      )
    }
    AnimatedVisibility(
      visible = memberReminders.connectPayment != null,
      enter = cardReminderEnterTransition,
      exit = cardReminderExitTransition,
      label = "connectPayment animated visibility",
    ) {
      ReminderCardConnectPayment(
        navigateToConnectPayment = navigateToConnectPayment,
      )
    }
    AnimatedVisibility(
      visible = memberReminders.upcomingRenewals != null,
      enter = cardReminderEnterTransition,
      exit = cardReminderExitTransition,
      label = "upcomingRenewals animated visibility",
    ) {
      memberReminders.upcomingRenewals?.upcomingRenewals?.withIndex()?.forEach { (renewalsIndex, upcomingRenewal) ->
        ReminderCardUpcomingRenewals(
          upcomingRenewal = upcomingRenewal,
          openUrl = openUrl,
        )
        if (renewalsIndex != memberReminders.upcomingRenewals?.upcomingRenewals?.lastIndex) {
          Spacer(Modifier.height(8.dp))
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
  VectorInfoCard(
    text = stringResource(R.string.PROFILE_ALLOW_NOTIFICATIONS_INFO_LABEL),
    modifier = modifier,
  ) {
    Row(Modifier.padding(vertical = 4.dp)) {
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
private fun ReminderCardConnectPayment(
  navigateToConnectPayment: () -> Unit,
  modifier: Modifier = Modifier,
) {
  VectorInfoCard(
    text = stringResource(R.string.info_card_missing_payment_body),
    icon = Icons.Hedvig.WarningFilled,
    iconColor = MaterialTheme.colorScheme.warningElement,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.warningContainer,
      contentColor = MaterialTheme.colorScheme.onWarningContainer,
    ),
    modifier = modifier,
  ) {
    InfoCardTextButton(
      onClick = navigateToConnectPayment,
      text = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    )
  }
}

@Composable
private fun ReminderCardUpcomingRenewals(
  upcomingRenewal: UpcomingRenewal,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val daysUntilRenewal = remember(TimeZone.currentSystemDefault(), upcomingRenewal.renewalDate) {
    val timeZone = TimeZone.currentSystemDefault()
    val startOfToday = Clock.System.now().toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)
    val startOfDayOfRenewal = upcomingRenewal.renewalDate.atStartOfDayIn(timeZone)
    startOfToday.daysUntil(startOfDayOfRenewal, timeZone)
  }
  VectorInfoCard(
    text = stringResource(R.string.DASHBOARD_RENEWAL_PROMPTER_BODY, daysUntilRenewal),
    modifier = modifier,
  ) {
    InfoCardTextButton(
      onClick = { openUrl(upcomingRenewal.draftCertificateUrl) },
      text = stringResource(R.string.travel_certificate_download),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    )
  }
}

@Composable
private fun InfoCardTextButton(
  onClick: () -> Unit,
  text: String,
  modifier: Modifier = Modifier,
) {
  HedvigContainedSmallButton(
    text = text,
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.background,
      contentColor = MaterialTheme.colorScheme.onBackground,
    ),
    textStyle = MaterialTheme.typography.bodyMedium,
    modifier = modifier,
  )
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
      ReminderCardUpcomingRenewals(UpcomingRenewal("contract name", LocalDate.parse("2024-03-05"), ""), {})
    }
  }
}
