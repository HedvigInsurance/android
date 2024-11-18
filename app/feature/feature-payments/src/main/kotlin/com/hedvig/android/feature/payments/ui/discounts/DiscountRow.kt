package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.discountsPreviewData
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun DiscountRows(discounts: List<Discount>, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    discounts.forEachIndexed { index, discount ->
      if (index != 0) {
        HorizontalDivider()
      }
      DiscountRow(discount, Modifier.fillMaxWidth())
    }
  }
}

@Composable
private fun DiscountRow(discount: Discount, modifier: Modifier = Modifier) {
  val discountIsExpired = discount.expiredState is Discount.ExpiredState.AlreadyExpired
  Column(modifier = modifier) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HighlightLabel(
          labelText = discount.code,
          color = if (discountIsExpired) HighlightLabelDefaults.HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT)
          else HighlightLabelDefaults.HighlightColor.Blue(HighlightLabelDefaults.HighlightShade.LIGHT),
          size = HighlightLabelDefaults.HighLightSize.Small
        )
      },
      endSlot = {
        discount.amount?.let { discountAmount ->
          HedvigText(
            text = stringResource(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, discount.toString()),
            color = if (discountIsExpired) {
              HedvigTheme.colorScheme.textDisabled
            } else {
              HedvigTheme.colorScheme.textSecondary
            },
            textAlign = TextAlign.End,
            modifier = Modifier.wrapContentSize(Alignment.CenterEnd),
          )
        }
      },
    )
    Spacer(Modifier.height(8.dp))
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Column(
          verticalArrangement = Arrangement.Top,
          horizontalAlignment = Alignment.Start,
        ) {
          discount.displayName?.let {
            HedvigText(
              text = it,
              color = if (discountIsExpired) {
                HedvigTheme.colorScheme.textDisabled
              } else {
                HedvigTheme.colorScheme.textSecondary
              },
              style = HedvigTheme.typography.bodySmall,
            )
          }
          val bottomText = if (discount.isReferral) {
            stringResource(R.string.PAYMENTS_REFERRAL_DISCOUNT)
          } else {
            discount.description
          }
          if (bottomText != null) {
            HedvigText(
              text = bottomText,
              color = if (discountIsExpired) {
                HedvigTheme.colorScheme.textDisabled
              } else {
                HedvigTheme.colorScheme.textSecondary
              },
              style = HedvigTheme.typography.bodySmall,
            )
          }
        }
      },
      endSlot = {
        val dateTimeFormatter = rememberHedvigDateTimeFormatter()
        when (discount.expiredState) {
          is Discount.ExpiredState.AlreadyExpired -> {
            HedvigText(
              text = stringResource(
                id = R.string.PAYMENTS_EXPIRED_DATE,
                dateTimeFormatter.format(discount.expiredState.expirationDate.toJavaLocalDate()),
              ),
              textAlign = TextAlign.End,
              style = HedvigTheme.typography.bodySmall,
              color = HedvigTheme.colorScheme.signalRedText,
              modifier = Modifier.fillMaxWidth(),
            )
          }

          is Discount.ExpiredState.ExpiringInTheFuture -> {
            HedvigText(
              text = stringResource(
                id = R.string.PAYMENTS_VALID_UNTIL,
                dateTimeFormatter.format(discount.expiredState.expirationDate.toJavaLocalDate()),
              ),
              textAlign = TextAlign.End,
              style = HedvigTheme.typography.bodySmall,
              color = HedvigTheme.colorScheme.textSecondary,
              modifier = Modifier.fillMaxWidth(),
            )
          }

          Discount.ExpiredState.NotExpired -> {}
        }
      },
      spaceBetween = 16.dp,
    )
  }
}

@Composable
@HedvigPreview
private fun DiscountRowsPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        DiscountRows(discounts = discountsPreviewData)
      }
    }
  }
}
