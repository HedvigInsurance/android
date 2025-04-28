package com.hedvig.android.feature.payments.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.ripple
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.discountsPreviewData
import com.hedvig.android.feature.payments.ui.discounts.DiscountRow
import hedvig.resources.R
import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun PaymentDetailExpandableCard(
  displayName: String,
  subtitle: String,
  totalAmount: String,
  periods: List<MemberCharge.ChargeBreakdown.Period>,
  discounts: List<Discount>,
  isExpanded: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  HedvigCard(
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .fillMaxWidth()
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(
            bounded = false,
            // This fixes the problem of the ripple not properly resizing as the card expands
            radius = 1000.dp,
          ),
          onClick = onClick,
        ),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { HedvigText(displayName) },
        spaceBetween = 8.dp,
        endSlot = {
          Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            HedvigText(
              text = totalAmount,
              textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
              imageVector = HedvigIcons.ChevronDown,
              contentDescription = null,
              tint = HedvigTheme.colorScheme.fillSecondary,
              modifier = Modifier.size(16.dp),
            )
          }
        },
      )
      HedvigText(
        text = subtitle,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
      ) {
        Column {
          periods.forEach {
            Spacer(Modifier.height(16.dp))

            HorizontalItemsWithMaximumSpaceTaken(
              spaceBetween = 8.dp,
              startSlot = {
                HedvigText(
                  text = it.toString(dateTimeFormatter),
                  color = it.toColor(),
                )
              },
              endSlot = {
                Row(
                  Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  HedvigText(
                    text = it.amount.toString(),
                    textAlign = TextAlign.End,
                    color = it.toSubtitleColor(),
                  )
                }
              },
            )
            if (it.isPreviouslyFailedCharge) {
              HedvigText(
                text = stringResource(id = R.string.PAYMENTS_OUTSTANDING_PAYMENT),
                style = HedvigTheme.typography.label,
                color = it.toSubtitleColor(),
              )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
          }
          discounts.forEach { discount ->
            Spacer(Modifier.height(16.dp))
            DiscountRow(discount, labelColor = HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.MEDIUM))
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
          }
          Spacer(Modifier.height(16.dp))
          Row {
            HorizontalItemsWithMaximumSpaceTaken(
              spaceBetween = 8.dp,
              startSlot = {
                HedvigText(stringResource(id = R.string.PAYMENTS_SUBTOTAL))
              },
              endSlot = {
                Row(
                  Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  HedvigText(
                    text = totalAmount,
                    textAlign = TextAlign.End,
                  )
                }
              },
            )
          }
          Spacer(Modifier.height(8.dp))
        }
      }
    }
  }
}

private fun MemberCharge.ChargeBreakdown.Period.toString(dateTimeFormatter: DateTimeFormatter): String {
  return "${dateTimeFormatter.format(
    fromDate.toJavaLocalDate(),
  )} - ${dateTimeFormatter.format(toDate.toJavaLocalDate())}"
}

@Composable
private fun MemberCharge.ChargeBreakdown.Period.toColor(): Color {
  return if (isPreviouslyFailedCharge) {
    HedvigTheme.colorScheme.signalRedElement
  } else {
    LocalContentColor.current
  }
}

@Composable
private fun MemberCharge.ChargeBreakdown.Period.toSubtitleColor(): Color {
  return if (isPreviouslyFailedCharge) {
    HedvigTheme.colorScheme.signalRedElement
  } else {
    HedvigTheme.colorScheme.textSecondary
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailExpandableCardPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PaymentDetailExpandableCard(
        displayName = "Bilförsäkring",
        subtitle = "ABH 234",
        totalAmount = "978 kr",
        periods = listOf(
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(400.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = true,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(150.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
        ),
        isExpanded = false,
        onClick = {},
        discounts = discountsPreviewData,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailExpandableCardExpandedPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PaymentDetailExpandableCard(
        displayName = "Bilförsäkring",
        subtitle = "ABH 234",
        totalAmount = "978 kr",
        periods = listOf(
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(400.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = true,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(150.0, UiCurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
        ),
        isExpanded = true,
        onClick = {},
        discounts = discountsPreviewData,
      )
    }
  }
}
