package com.hedvig.android.feature.payments.ui.discounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.discountsPreviewData
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun DiscountRows(
  discounts: List<Discount>,
  showDisplayName: Boolean,
  modifier: Modifier = Modifier,
  labelColor: HighlightColor = HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    discounts.forEachIndexed { index, discount ->
      if (index != 0) {
        HorizontalDivider()
      }
      DiscountRow(
        discount,
        modifier = Modifier.fillMaxWidth(),
        labelColor = labelColor,
        showDisplayName = showDisplayName,
      )
    }
  }
}

@Composable
internal fun DiscountRow(
  discount: Discount,
  showDisplayName: Boolean, // we do not need it for payment details, but do need it for discounts
  modifier: Modifier = Modifier,
  labelColor: HighlightColor = HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
) {
  val discountIsExpired = discount.expiredState is Discount.ExpiredState.AlreadyExpired
  Column(modifier = modifier) {
    HorizontalItemsWithMaximumSpaceTaken(
      spaceBetween = 8.dp,
      startSlot = {
        Column {
          HighlightLabel(
            labelText = discount.code,
            modifier = Modifier
              .wrapContentWidth(),
            color = labelColor,
            size = HighlightLabelDefaults.HighLightSize.Small,
          )
          Spacer(Modifier.height(4.dp))
          Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
          ) {
            val descriptionText = if (discount.isReferral) {
              stringResource(R.string.PAYMENTS_REFERRAL_DISCOUNT)
            } else {
              discount.description
            }
            if (descriptionText != null) {
              HedvigText(
                text = descriptionText,
                color = if (discountIsExpired) {
                  HedvigTheme.colorScheme.textDisabled
                } else {
                  HedvigTheme.colorScheme.textSecondaryTranslucent
                },
                style = HedvigTheme.typography.label,
              )
            }
            discount.displayName?.let {
              if (showDisplayName) {
                HedvigText(
                  text = it,
                  color = if (discountIsExpired) {
                    HedvigTheme.colorScheme.textDisabled
                  } else {
                    HedvigTheme.colorScheme.textSecondaryTranslucent
                  },
                  style = HedvigTheme.typography.label,
                )
              }
            }
          }
        }
      },
      endSlot = {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
          ) {
            discount.amount?.let { discountAmount ->
              HedvigText(
                text = discountAmount.toString(),
                color = if (discountIsExpired) {
                  HedvigTheme.colorScheme.textDisabled
                } else {
                  HedvigTheme.colorScheme.textSecondaryTranslucent
                },
                textAlign = TextAlign.End,
                fontSize = HedvigTheme.typography.bodySmall.fontSize,
                modifier = Modifier.wrapContentSize(Alignment.CenterEnd),
              )
            }
          }
          Spacer(Modifier.height(4.dp))
          val dateTimeFormatter = rememberHedvigDateTimeFormatter()
          when (discount.expiredState) {
            is Discount.ExpiredState.AlreadyExpired -> {
              HedvigText(
                text = stringResource(
                  id = R.string.PAYMENTS_EXPIRED_DATE,
                  dateTimeFormatter.format(discount.expiredState.expirationDate.toJavaLocalDate()),
                ),
                textAlign = TextAlign.End,
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.signalRedElement,
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
                style = HedvigTheme.typography.label,
                color = HedvigTheme.colorScheme.textSecondaryTranslucent,
                modifier = Modifier.fillMaxWidth(),
              )
            }

            Discount.ExpiredState.NotExpired -> {}
          }
        }
      },
    )

    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
      },
      endSlot = {
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
        DiscountRows(
          discounts = discountsPreviewData,
          showDisplayName = false,
        )
      }
    }
  }
}
