package com.hedvig.android.feature.payments.discounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.HedvigPill
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.onSecondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.secondaryContainedButtonContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
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
        HedvigPill(
          text = discount.code,
          color = MaterialTheme.colorScheme.secondaryContainedButtonContainer,
          contentColor = if (discountIsExpired) {
            MaterialTheme.colorScheme.onSurface.copy(DisabledAlpha)
          } else {
            MaterialTheme.colorScheme.onSecondaryContainedButtonContainer
          },
          modifier = Modifier.wrapContentSize(Alignment.CenterStart),
        )
      },
      endSlot = {
        discount.amount?.let { discount ->
          Text(
            text = stringResource(R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION, discount.toString()),
            color = if (discountIsExpired) {
              MaterialTheme.colorScheme.onSurface.copy(DisabledAlpha)
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
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
            Text(
              text = it,
              color = if (discountIsExpired) {
                MaterialTheme.colorScheme.onSurface.copy(DisabledAlpha)
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          val bottomText = if (discount.isReferral) {
            stringResource(R.string.PAYMENTS_REFERRAL_DISCOUNT)
          } else {
            discount.description
          }
          if (bottomText != null) {
            Text(
              text = bottomText,
              color = if (discountIsExpired) {
                MaterialTheme.colorScheme.onSurface.copy(DisabledAlpha)
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      },
      endSlot = {
        val dateTimeFormatter = rememberHedvigDateTimeFormatter()
        when (discount.expiredState) {
          is Discount.ExpiredState.AlreadyExpired -> {
            Text(
              text = stringResource(
                id = R.string.PAYMENTS_EXPIRED_DATE,
                dateTimeFormatter.format(discount.expiredState.expirationDate.toJavaLocalDate()),
              ),
              textAlign = TextAlign.End,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.fillMaxWidth(),
            )
          }

          is Discount.ExpiredState.ExpiringInTheFuture -> {
            Text(
              text = stringResource(
                id = R.string.PAYMENTS_VALID_UNTIL,
                dateTimeFormatter.format(discount.expiredState.expirationDate.toJavaLocalDate()),
              ),
              textAlign = TextAlign.End,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Surface(color = MaterialTheme.colorScheme.background) {
      Column {
        DiscountRows(discounts = discountsPreviewData)
      }
    }
  }
}
