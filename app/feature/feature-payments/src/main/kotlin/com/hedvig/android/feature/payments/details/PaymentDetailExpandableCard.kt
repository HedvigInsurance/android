package com.hedvig.android.feature.payments.details

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronDown
import com.hedvig.android.core.ui.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import hedvig.resources.R
import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode

@Composable
internal fun PaymentDetailExpandableCard(
  displayName: String,
  subtitle: String,
  totalAmount: String,
  periods: List<MemberCharge.ChargeBreakdown.Period>,
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
          indication = rememberRipple(
            bounded = false,
            // This fixes the problem of the ripple not properly resizing as the card expands
            radius = 1000.dp,
          ),
          onClick = onClick,
        ),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(displayName) },
        spaceBetween = 8.dp,
        endSlot = {
          Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = totalAmount,
              textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Hedvig.ChevronDown,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
          }
        },
      )
      Text(
        text = subtitle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
              startSlot = {
                Text(
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
                  Text(
                    text = it.amount.toString(),
                    textAlign = TextAlign.End,
                    color = it.toSubtitleColor(),
                  )
                }
              },
            )
            if (it.isPreviouslyFailedCharge) {
              Text(
                text = stringResource(id = R.string.PAYMENTS_OUTSTANDING_PAYMENT),
                style = MaterialTheme.typography.labelMedium,
                color = it.toSubtitleColor(),
              )
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
          }

          Spacer(Modifier.height(16.dp))
          Row {
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                Text(stringResource(id = R.string.PAYMENTS_SUBTOTAL))
              },
              endSlot = {
                Row(
                  Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
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
    MaterialTheme.colorScheme.error
  } else {
    LocalContentColor.current
  }
}

@Composable
private fun MemberCharge.ChargeBreakdown.Period.toSubtitleColor(): Color {
  return if (isPreviouslyFailedCharge) {
    MaterialTheme.colorScheme.error
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailExpandableCardPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentDetailExpandableCard(
        displayName = "Bilförsäkring",
        subtitle = "ABH 234",
        totalAmount = "978 kr",
        periods = listOf(
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(400.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = true,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(150.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
        ),
        isExpanded = false,
        onClick = {},
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PaymentDetailExpandableCardExpandedPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentDetailExpandableCard(
        displayName = "Bilförsäkring",
        subtitle = "ABH 234",
        totalAmount = "978 kr",
        periods = listOf(
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(200.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(400.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = true,
          ),
          MemberCharge.ChargeBreakdown.Period(
            amount = UiMoney(150.0, CurrencyCode.SEK),
            fromDate = LocalDate.fromEpochDays(200),
            toDate = LocalDate.fromEpochDays(300),
            isPreviouslyFailedCharge = false,
          ),
        ),
        isExpanded = true,
        onClick = {},
      )
    }
  }
}
