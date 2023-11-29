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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.infocard.VectorWarningCard
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
  navigateToContractDetail: (contractId: String) -> Unit,
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
      visible = memberReminders.coInsuredInfo != null,
      enter = cardReminderEnterTransition,
      exit = cardReminderExitTransition,
      label = "coInsured animated visibility",
    ) {
      ReminderCoInsuredInfo(
        navigateToContractDetail = {
          memberReminders.coInsuredInfo
            ?.coInsuredInfoList
            ?.head
            ?.contractId
            ?.let {
              navigateToContractDetail(it)
            }
        },
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

/**
 * Member reminder cards without the notification reminder
 */
@Composable
fun MemberReminderCards(
  memberReminders: ApplicableMemberReminders,
  navigateToConnectPayment: () -> Unit,
  openUrl: (String) -> Unit,
  modifier: Modifier = Modifier,
  navigateToAddMissingInfo: (String) -> Unit,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
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
      visible = memberReminders.coInsuredInfo != null,
      enter = cardReminderEnterTransition,
      exit = cardReminderExitTransition,
      label = "coInsured animated visibility",
    ) {
      ReminderCoInsuredInfo(
        navigateToContractDetail = {
          memberReminders.coInsuredInfo
            ?.coInsuredInfoList
            ?.firstOrNull()
            ?.contractId
            ?.let {
              navigateToAddMissingInfo(it)
            }
        },
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
    upcomingRenewal.draftCertificateUrl?.let {
      InfoCardTextButton(
        onClick = { openUrl(it) },
        text = stringResource(R.string.travel_certificate_download),
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

@Composable
private fun InfoCardTextButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
  HedvigContainedSmallButton(
    text = text,
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.containedButtonContainer,
      contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
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
