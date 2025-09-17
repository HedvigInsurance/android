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
import com.hedvig.android.feature.payments.data.DiscountedContract
import com.hedvig.android.feature.payments.data.DiscountsDetails
import com.hedvig.android.feature.payments.discountsPreviewData
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun DiscountRows(
  affectedContracts: List<DiscountedContract>,
  modifier: Modifier = Modifier,
  labelColor: HighlightColor = HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
) {
  Column(
    modifier = modifier,
  ) {
    affectedContracts.forEach { contract ->
      val relatedDiscounts = contract.discountsDetails.appliedDiscounts
      Spacer(modifier = Modifier.height(16.dp))
      HedvigText(contract.contractDisplayName)
      Spacer(modifier = Modifier.height(8.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(16.dp))
      relatedDiscounts.forEachIndexed { index, discount ->
        if (index != 0) {
          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))
        }
        DiscountRow(
          discount,
          modifier = Modifier.fillMaxWidth(),
          labelColor = labelColor,
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
internal fun DiscountRow(
  discount: Discount,
  modifier: Modifier = Modifier,
  labelColor: HighlightColor = HighlightColor.Grey(HighlightLabelDefaults.HighlightShade.LIGHT),
) {
  val discountIsExpired = discount.status == Discount.DiscountStatus.EXPIRED
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
                text = stringResource(
                  R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                  "-$discountAmount",
                ),
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
          if (discount.statusDescription != null) {
            HedvigText(
              text = discount.statusDescription,
              textAlign = TextAlign.End,
              style = HedvigTheme.typography.label,
              color = when (discount.status) {
                Discount.DiscountStatus.EXPIRED -> HedvigTheme.colorScheme.signalRedElement
                else -> HedvigTheme.colorScheme.textSecondaryTranslucent
              },
              modifier = Modifier.fillMaxWidth(),
            )
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
          affectedContracts = mockDiscountedContracts,
        )
      }
    }
  }
}

internal val mockDiscountedContracts = listOf(
  DiscountedContract(
    discountsDetails =
      DiscountsDetails(null, discountsPreviewData),
    contractId = "id1",
    contractDisplayName = "House Standard ∙ Villagatan 25",
  ),
  DiscountedContract(
    discountsDetails =
      DiscountsDetails(
        "Your bundle discount will activate when you have two active insurances.",
        discountsPreviewData,
      ),
    contractId = "id1",
    contractDisplayName = "Dog Premium ∙ Fido",
  ),
  DiscountedContract(
    discountsDetails = DiscountsDetails(
      null,
      listOf(
        Discount(
          code = "LOOP",
          description = "Desc",
          status = Discount.DiscountStatus.PENDING,
          amount = null,
          isReferral = false,
          statusDescription = "Pending",
        ),
      ),
    ),
    contractId = "id1",
    contractDisplayName = "Some pending contract ∙ Daboodee",
  ),
)
