package com.hedvig.android.app.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.common.navigation.SummaryParameters
import com.hedvig.android.feature.purchase.common.navigation.TierOfferData
import com.hedvig.android.feature.purchase.common.ui.summary.PurchaseSummaryScreen

class MockPurchaseSummaryActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      var groupIndex by remember { mutableStateOf(0) }
      val group = ContractGroup.entries[groupIndex % ContractGroup.entries.size]
      HedvigTheme {
        Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
          PurchaseSummaryScreen(
            params = SummaryParameters(
              shopSessionId = "mock-session",
              selectedOffer = TierOfferData(
                offerId = "mock-1",
                tierDisplayName = "Hem Standard",
                tierDescription = "Vår mest populära försäkring",
                grossAmount = 139.0,
                grossCurrencyCode = "SEK",
                netAmount = 118.0,
                netCurrencyCode = "SEK",
                usps = emptyList(),
                exposureDisplayName = "Storgatan 1",
                deductibleDisplayName = "1 500 kr",
                hasDiscount = true,
              ),
              productDisplayName = mockProductName(group),
              contractGroup = group,
            ),
            isSubmitting = false,
            navigateUp = { finish() },
            onConfirm = { groupIndex++ },
          )
        }
      }
    }
  }
}

private fun mockProductName(group: ContractGroup): String = when (group) {
  ContractGroup.HOMEOWNER -> "Hemförsäkring Bostadsrätt"
  ContractGroup.RENTAL -> "Hemförsäkring Hyresrätt"
  ContractGroup.HOUSE -> "Villaförsäkring"
  ContractGroup.STUDENT -> "Studentförsäkring"
  ContractGroup.ACCIDENT -> "Olycksfallsförsäkring"
  ContractGroup.CAR -> "Bilförsäkring"
  ContractGroup.CAT -> "Kattförsäkring"
  ContractGroup.DOG -> "Hundförsäkring"
  ContractGroup.TRAVEL -> "Reseförsäkring"
  ContractGroup.COUNTRY_HOME -> "Fritidshusförsäkring"
  ContractGroup.UNKNOWN -> "Försäkring"
}
