package com.hedvig.android.memberreminders.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigAttentionCard
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.memberreminders.MemberReminder
import hedvig.resources.HOME_TODO_MISSING_PAYMENT_METHOD_TITLE
import hedvig.resources.HOME_TODO_REQUIRES_ACTION_SUBTITLE
import hedvig.resources.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE
import hedvig.resources.Res
import hedvig.resources.info_card_missing_payment_body
import hedvig.resources.info_card_missing_payment_missing_payments_body
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

/**
 * Prompts a member with no payin method connected to set one up. Shown on its own wherever it
 * appears rather than as a row among other reminders, because nothing is charged until it is
 * resolved.
 *
 * @param dueDateToConnect when the member's cover ends if they do not connect one, spelled out in
 *   the body. Absent where the caller cannot know it.
 */
@Composable
fun MissingPayinMethodCard(
  onConnectPaymentClick: () -> Unit,
  modifier: Modifier = Modifier,
  dueDateToConnect: LocalDate? = null,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  HedvigAttentionCard(
    title = stringResource(Res.string.HOME_TODO_MISSING_PAYMENT_METHOD_TITLE),
    subtitle = stringResource(Res.string.HOME_TODO_REQUIRES_ACTION_SUBTITLE),
    body = if (dueDateToConnect != null) {
      stringResource(
        Res.string.info_card_missing_payment_missing_payments_body,
        dateTimeFormatter.format(dueDateToConnect),
      )
    } else {
      stringResource(Res.string.info_card_missing_payment_body)
    },
    buttonText = stringResource(Res.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE),
    onButtonClick = onConnectPaymentClick,
    modifier = modifier,
  )
}

/** The reminder [MissingPayinMethodCard] stands for, so callers can tell whether to render it. */
fun List<MemberReminder>.missingPayinMethodReminder(): MemberReminder.PaymentReminder.ConnectPayment? {
  return filterIsInstance<MemberReminder.PaymentReminder.ConnectPayment>().firstOrNull()
}
