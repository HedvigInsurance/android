package com.hedvig.android.memberreminders.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.design.system.hedvig.icon.Card
import com.hedvig.android.design.system.hedvig.icon.ChevronRight
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.ID
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import com.hedvig.android.design.system.hedvig.icon.ProfileOutline
import com.hedvig.android.design.system.hedvig.icon.WarningOutline
import com.hedvig.android.memberreminders.MemberReminder
import kotlinx.datetime.LocalDate

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
  modifier: Modifier = Modifier,
) {
  val rows = memberReminders.mapNotNull { reminder ->
    reminder.toToDoRowOrNull(
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToConnectPayout,
      navigateToAddMissingInfo = navigateToAddMissingInfo,
      onNavigateToNewConversation = onNavigateToNewConversation,
      navigateToContactInfo = navigateToContactInfo,
      navigateToChipId = navigateToChipId,
    )
  }
  if (rows.isEmpty()) return
  HedvigCard(
    color = HedvigTheme.colorScheme.fillNegative,
    modifier = modifier
      .fillMaxWidth()
      .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge),
  ) {
    Column(Modifier.fillMaxWidth()) {
      rows.forEachIndexed { index, row ->
        ToDoRow(
          icon = row.icon,
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

private fun MemberReminder.homeToDoOrder(): Int? = when (this) {
  is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> 0
  is MemberReminder.PaymentReminder.ConnectPayment -> 1
  is MemberReminder.PaymentReminder.ConnectPayout -> 2
  is MemberReminder.MissingChipId -> 3
  is MemberReminder.CoInsuredInfo -> 4
  is MemberReminder.ContactInfoUpdateNeeded -> 5
  is MemberReminder.UpcomingRenewal -> null
  is MemberReminder.EnableNotifications -> null
}

private data class ToDoRowData(
  val icon: ImageVector,
  val title: String,
  val onClick: () -> Unit,
)

private fun MemberReminder.toToDoRowOrNull(
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToAddMissingInfo: (String, CoInsuredFlowType) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  navigateToContactInfo: () -> Unit,
  navigateToChipId: () -> Unit,
): ToDoRowData? = when (this) {
  is MemberReminder.PaymentReminder.TerminationDueToMissedPayments -> ToDoRowData(
    icon = HedvigIcons.WarningOutline,
    // TODO: Add "Your payment is overdue" / "Din betalning är försenad" to Lokalise
    title = "Your payment is overdue",
    onClick = onNavigateToNewConversation,
  )

  is MemberReminder.PaymentReminder.ConnectPayment -> ToDoRowData(
    icon = HedvigIcons.Card,
    // TODO: Add "Missing payment method" / "Betalningsmetod saknas" to Lokalise
    title = "Missing payment method",
    onClick = navigateToConnectPayment,
  )

  is MemberReminder.PaymentReminder.ConnectPayout -> ToDoRowData(
    icon = HedvigIcons.Card,
    // TODO: Add "Missing payout method" / "Utbetalningsmetod saknas" to Lokalise
    title = "Missing payout method",
    onClick = navigateToConnectPayout,
  )

  is MemberReminder.MissingChipId -> ToDoRowData(
    icon = HedvigIcons.ID,
    // TODO: Add "Missing pet chip-ID" / "Chip-ID för husdjur saknas" to Lokalise
    title = "Missing pet chip-ID",
    onClick = navigateToChipId,
  )

  is MemberReminder.CoInsuredInfo -> ToDoRowData(
    icon = HedvigIcons.ProfileOutline,
    // TODO: Add "Add co-insured" / "Lägg till medförsäkrad" to Lokalise
    title = "Add co-insured",
    onClick = { navigateToAddMissingInfo(contractId, coInsuredType) },
  )

  is MemberReminder.ContactInfoUpdateNeeded -> ToDoRowData(
    icon = HedvigIcons.InfoOutline,
    // TODO: Add "Update contact details" / "Uppdatera kontaktuppgifter" to Lokalise
    title = "Update contact details",
    onClick = navigateToContactInfo,
  )

  is MemberReminder.UpcomingRenewal -> null

  is MemberReminder.EnableNotifications -> null
}

@Composable
private fun ToDoRow(icon: ImageVector, title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
      tint = HedvigTheme.colorScheme.fillPrimary,
      modifier = Modifier.size(24.dp),
    )
    Column(Modifier.weight(1f)) {
      HedvigText(
        text = title,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textPrimary,
      )
      // TODO: Add "Requires action" / "Kräver åtgärd" to Lokalise
      HedvigText(
        text = "Requires action",
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
          MemberReminder.ContactInfoUpdateNeeded,
        ),
        navigateToConnectPayment = {},
        navigateToConnectPayout = {},
        navigateToAddMissingInfo = { _, _ -> },
        onNavigateToNewConversation = {},
        navigateToContactInfo = {},
        navigateToChipId = {},
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}
