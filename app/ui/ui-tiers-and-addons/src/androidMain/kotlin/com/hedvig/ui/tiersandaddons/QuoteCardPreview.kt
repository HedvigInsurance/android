package com.hedvig.android.tiersandaddons

import androidx.compose.ui.tooling.preview.PreviewParameter
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup.DOG
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@HedvigPreview
@androidx.compose.runtime.Composable
private fun PreviewQuoteCard(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) samePreviousPremium: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val premium = UiMoney(281.0, UiCurrencyCode.SEK)
      val higherPremium = UiMoney(381.0, UiCurrencyCode.SEK)
      QuoteCard(
        displayName = "displayName",
        contractGroup = DOG,
        insurableLimits = List(3) {
          InsurableLimit(
            label = "label#$it",
            limit = "limit#$it",
            description = "description#$it",
          )
        },
        documents = List(3) {
          DisplayDocument(
            displayName = "displayName#$it",
            url = "url#$it",
          )
        },
        subtitle = "subtitle",
        premium = premium,
        previousPremium = if (samePreviousPremium) {
          premium
        } else {
          higherPremium
        },
        costBreakdown = List(3) {
          CostBreakdownEntry(
            "#$it",
            "discount#$it",
            false,
          )
        },
        displayItems = List(5) {
          QuoteDisplayItem(
            title = "title$it",
            value = "value$it",
            subtitle = "subtitle$it",
          )
        },
      )
    }
  }
}