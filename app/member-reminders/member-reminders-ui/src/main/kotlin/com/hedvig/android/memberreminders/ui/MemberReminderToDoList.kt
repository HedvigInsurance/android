package com.hedvig.android.memberreminders.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.EQ
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.ID
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.icon.ProfileOutline
import com.hedvig.android.design.system.hedvig.icon.WarningOutline
import com.hedvig.android.memberreminders.MemberReminder
import hedvig.resources.HOME_TODO_ADD_COINSURED_TITLE
import hedvig.resources.HOME_TODO_ADD_COOWNER_TITLE
import hedvig.resources.HOME_TODO_MISSING_CHIP_ID_TITLE
import hedvig.resources.HOME_TODO_MISSING_PAYMENT_METHOD_TITLE
import hedvig.resources.HOME_TODO_MISSING_PAYOUT_METHOD_TITLE
import hedvig.resources.HOME_TODO_PAYMENT_OVERDUE_TITLE
import hedvig.resources.HOME_TODO_REQUIRES_ACTION_SUBTITLE
import hedvig.resources.HOME_TODO_SELECT_USAGE_DATA_TITLE
import hedvig.resources.HOME_TODO_UPDATE_CONTACT_DETAILS_TITLE
import hedvig.resources.Res
import hedvig.resources.open_chat
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Renders the "To do" list of action-required member reminders as a grouped card of tappable rows,
 * used on the home screen. Purely informational reminders ([MemberReminder.UpcomingRenewal],
 * [MemberReminder.EnableNotifications]) are not shown here.
 *
 * Pass the list through [homeActionRequiredReminders] before calling this so the caller can decide
 * whether to render the surrounding "To do" section at all.
 */
@Composable
fun MemberReminderToDoList(
  memberReminders: List<MemberReminder>,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
  navigateToUsageData: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showMissedPaymentsDialog by rememberSaveable { mutableStateOf(false) }
  val missedPaymentsReminder = memberReminders
    .filterIsInstance<MemberReminder.PaymentReminder.TerminationDueToMissedPayments>()
    .firstOrNull()
  if (showMissedPaymentsDialog && missedPaymentsReminder != null) {
    MissedPaymentsDialog(
      reminder = missedPaymentsReminder,
      onNavigateToNewConversation = onNavigateToNewConversation,
      onDismissRequest = { showMissedPaymentsDialog = false },
    )
  }
  val rows = memberReminders.mapNotNull { reminder ->
    reminder.toToDoRowOrNull(
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToConnectPayout,
      navigateToAddMissingInfo = navigateToAddMissingInfo,
      onShowMissedPaymentsDialog = { showMissedPaymentsDialog = true },
      navigateToContactInfo = navigateToContactInfo,
      navigateToChipId = navigateToChipId,
      navigateToUsageData = navigateToUsageData,
    )
  }
  if (rows.isEmpty()) return
  HedvigCard(
    color = HedvigTheme.colorScheme.fillNegative,
    // fillNegative matches the surface this sits on in both themes, and the drop shadow that would
    // otherwise separate them is light-only, so the outline is what gives the card an edge in dark mode.
    borderColor = HedvigTheme.colorScheme.borderPrimary,
    modifier = modifier
      .fillMaxWidth()
      .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
    Column(Modifier.fillMaxWidth()) {
      rows.forEachIndexed { index, row ->
        ToDoRow(
          icon = row.icon,
          iconTint = row.iconTint,
          title = row.title,
          onClick = row.onClick,
          modifier = Modifier.horizontalDivider(DividerPosition.Top, show = index != 0),
        )
      }
    }
  }
}

/**
 * Filters to the action-required reminders shown in the home "To do" list and orders them to match
 * the design: payment overdue, then payin, then payout, then the remaining action items, with the
 * reminders not depicted in the design appended last.
 */
fun List<MemberReminder>.homeActionRequiredReminders(): List<MemberReminder> {
  return filter { it.homeToDoOrder() != null }.sortedBy { it.homeToDoOrder() }
}

/**
 * Reminders shown as cards in the home important-messages carousel rather than the To do list, i.e.
 * the informational reminders that don't demand a direct action.
 */
fun List<MemberReminder>.homeInformationalReminders(): List<MemberReminder.UpcomingRenewal> {
  return filterIsInstance<MemberReminder.UpcomingRenewal>()
}

private fun MemberReminder.homeToDoOrder(): Int? = when (this) {
  is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> 0
  is MemberReminder.PaymentReminder.ConnectPayment -> 1
  is MemberReminder.PaymentReminder.ConnectPayout -> 2
  is MemberReminder.MissingChipId -> 3
  is MemberReminder.CoInsuredInfo -> 4
  is MemberReminder.ContactInfoUpdateNeeded -> 5
  is MemberReminder.DecideAnalyticsConsent -> 6
  is MemberReminder.UpcomingRenewal -> null
  is MemberReminder.EnableNotifications -> null
}

private data class ToDoRowData(
  val icon: ImageVector,
  val iconTint: ToDoRowIconTint,
  val title: StringResource,
  val onClick: () -> Unit,
)

/**
 * The tint of a To do row's leading icon, resolved against the theme in [ToDoRow]. [Attention] marks the rows whose
 * action is time critical, so the icon carries the same urgency as the row's red subtitle.
 */
private enum class ToDoRowIconTint {
  Primary,
  Attention,
  ;

  val color: Color
    @Composable
    get() = when (this) {
      Primary -> HedvigTheme.colorScheme.fillPrimary
      Attention -> HedvigTheme.colorScheme.signalRedElement
    }
}

private fun MemberReminder.toToDoRowOrNull(
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onShowMissedPaymentsDialog: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
  navigateToUsageData: () -> Unit,
): ToDoRowData? = when (this) {
  is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> ToDoRowData(
    icon = HedvigIcons.WarningOutline,
    iconTint = ToDoRowIconTint.Attention,
    title = Res.string.HOME_TODO_PAYMENT_OVERDUE_TITLE,
    onClick = onShowMissedPaymentsDialog,
  )

  is MemberReminder.PaymentReminder.ConnectPayment -> ToDoRowData(
    icon = HedvigIcons.Card,
    iconTint = ToDoRowIconTint.Primary,
    title = Res.string.HOME_TODO_MISSING_PAYMENT_METHOD_TITLE,
    onClick = navigateToConnectPayment,
  )

  is MemberReminder.PaymentReminder.ConnectPayout -> ToDoRowData(
    icon = HedvigIcons.Card,
    iconTint = ToDoRowIconTint.Primary,
    title = Res.string.HOME_TODO_MISSING_PAYOUT_METHOD_TITLE,
    onClick = navigateToConnectPayout,
  )

  is MemberReminder.MissingChipId -> ToDoRowData(
    icon = HedvigIcons.ID,
    iconTint = ToDoRowIconTint.Primary,
    title = Res.string.HOME_TODO_MISSING_CHIP_ID_TITLE,
    onClick = navigateToChipId,
  )

  is MemberReminder.CoInsuredInfo -> ToDoRowData(
    icon = HedvigIcons.ProfileOutline,
    iconTint = ToDoRowIconTint.Primary,
    title = when (coInsuredType) {
      CoInsuredFlowType.CoInsured -> Res.string.HOME_TODO_ADD_COINSURED_TITLE
      CoInsuredFlowType.CoOwners -> Res.string.HOME_TODO_ADD_COOWNER_TITLE
    },
    onClick = { navigateToAddMissingInfo(contractId, coInsuredType) },
  )

  is MemberReminder.ContactInfoUpdateNeeded -> ToDoRowData(
    icon = HedvigIcons.InfoOutline,
    iconTint = ToDoRowIconTint.Primary,
    title = Res.string.HOME_TODO_UPDATE_CONTACT_DETAILS_TITLE,
    onClick = navigateToContactInfo,
  )

  is MemberReminder.DecideAnalyticsConsent -> ToDoRowData(
    icon = HedvigIcons.EQ,
    iconTint = ToDoRowIconTint.Primary,
    title = Res.string.HOME_TODO_SELECT_USAGE_DATA_TITLE,
    onClick = navigateToUsageData,
  )

  is MemberReminder.UpcomingRenewal -> null

  is MemberReminder.EnableNotifications -> null
}

/**
 * Explains the pending termination and offers chat as the way out, since the member can't resolve missed payments in
 * the app themselves.
 */
@Composable
private fun MissedPaymentsDialog(
  reminder: MemberReminder.PaymentReminder.TerminationDueToMissedPayments,
  onNavigateToNewConversation: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  HedvigDialog(
    onDismissRequest = onDismissRequest,
    style = NoButtons,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      HedvigText(
        text = getMemberReminderMessage(reminder),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 20.dp),
      )
      Spacer(Modifier.height(24.dp))
      HedvigButton(
        text = stringResource(Res.string.open_chat),
        onClick = {
          onDismissRequest()
          onNavigateToNewConversation()
        },
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Primary,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun ToDoRow(
  icon: ImageVector,
  iconTint: ToDoRowIconTint,
  title: StringResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 16.dp),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint.color,
      modifier = Modifier.size(24.dp),
    )
    Column(Modifier.weight(1f)) {
      HedvigText(
        text = stringResource(title),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textPrimary,
      )
      HedvigText(
        text = stringResource(Res.string.HOME_TODO_REQUIRES_ACTION_SUBTITLE),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.signalRedText,
      )
    }
    Icon(
      imageVector = HedvigIcons.ChevronRight,
      contentDescription = null,
      tint = HedvigTheme.colorScheme.fillPrimary,
      modifier = Modifier.size(24.dp),
    )
  }
}

@Preview
@Composable
private fun PreviewMemberReminderToDoList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MemberReminderToDoList(
        memberReminders = listOf(
          MemberReminder.PaymentReminder.TerminationDueToMissedPayments(terminationDate = LocalDate(2029, 1, 1)),
          MemberReminder.PaymentReminder.ConnectPayment(),
          MemberReminder.PaymentReminder.ConnectPayout(),
          MemberReminder.MissingChipId(),
          MemberReminder.CoInsuredInfo("contractId", CoInsuredFlowType.CoInsured),
          MemberReminder.CoInsuredInfo("contractId", CoInsuredFlowType.CoOwners),
          MemberReminder.ContactInfoUpdateNeeded,
          MemberReminder.DecideAnalyticsConsent(),
        ),
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToAddMissingInfo = { _, _ -> },
        onNavigateToNewConversation = {},
        navigateToContactInfo = {},
        navigateToChipId = {},
        navigateToUsageData = {},
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
