package com.hedvig.android.memberreminders.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigAttentionCard
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_AMOUNT_DUE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_BODY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_BUTTON
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_TITLE
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

/** Prompts a member whose latest charge failed to pay the overdue [amountDue] themselves. */
@Composable
fun FailedPaymentCard(amountDue: String, onReviewPaymentClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigAttentionCard(
    title = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_TITLE),
    subtitle = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_AMOUNT_DUE, amountDue),
    body = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_BODY),
    buttonText = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_BUTTON),
    onButtonClick = onReviewPaymentClick,
    modifier = modifier,
  )
}
