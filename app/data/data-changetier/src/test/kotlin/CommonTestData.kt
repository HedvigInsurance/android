import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.changetier.data.TotalCost
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant

internal val oldTestQuote = TierDeductibleQuote(
  id = "oldQuote",
  deductible = Deductible(
    UiMoney(0.0, SEK),
    deductiblePercentage = 25,
    description = "Endast en rörlig del om 25% av skadekostnaden.",
  ),
  displayItems = listOf(
    ChangeTierDeductibleDisplayItem(
      displayValue = "hhh",
      displaySubtitle = "mmm",
      displayTitle = "ioi",
    ),
  ),
  premium = UiMoney(199.0, SEK),
  tier = Tier(
    "BAS",
    tierLevel = 0,
    tierDescription = "Vårt paket med grundläggande villkor.",
    tierDisplayName = "Bas",
  ),
  addons = emptyList(),
  productVariant = ProductVariant(
    displayName = "Test",
    contractGroup = ContractGroup.RENTAL,
    contractType = ContractType.SE_APARTMENT_RENT,
    partner = "test",
    perils = listOf(),
    insurableLimits = listOf(),
    documents = listOf(),
    displayTierName = "Bas",
    tierDescription = "Our most basic coverage",
    termsVersion = "termsVersion",
  ),
  currentTotalCost = TotalCost(
    monthlyGross = UiMoney(250.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(200.0, UiCurrencyCode.SEK),
  ),
  newTotalCost = TotalCost(
    monthlyGross = UiMoney(380.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(304.0, UiCurrencyCode.SEK),
  ),
  costBreakdown = listOf(
    "Home Insurance Max" to "300 kr/mo",
    "Bundle discount 20%" to "76 kr/mo",
  ),
)

internal val testQuote = TierDeductibleQuote(
  id = "id0",
  deductible = Deductible(
    UiMoney(0.0, SEK),
    deductiblePercentage = 25,
    description = "Endast en rörlig del om 25% av skadekostnaden.",
  ),
  displayItems = listOf(
    ChangeTierDeductibleDisplayItem(
      displayValue = "hhh",
      displaySubtitle = "mmm",
      displayTitle = "ioi",
    ),
  ),
  premium = UiMoney(199.0, SEK),
  tier = Tier(
    "BAS",
    tierLevel = 0,
    tierDescription = "Vårt paket med grundläggande villkor.",
    tierDisplayName = "Bas",
  ),
  addons = emptyList(),
  productVariant = ProductVariant(
    displayName = "Test",
    contractGroup = ContractGroup.RENTAL,
    contractType = ContractType.SE_APARTMENT_RENT,
    partner = "test",
    perils = listOf(),
    insurableLimits = listOf(),
    documents = listOf(),
    displayTierName = "Bas",
    tierDescription = "Our most basic coverage",
    termsVersion = "termsVersion",
  ),
  currentTotalCost = TotalCost(
    monthlyGross = UiMoney(250.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(200.0, UiCurrencyCode.SEK),
  ),
  newTotalCost = TotalCost(
    monthlyGross = UiMoney(380.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(304.0, UiCurrencyCode.SEK),
  ),
  costBreakdown = listOf(
    "Home Insurance Max" to "300 kr/mo",
    "Bundle discount 20%" to "76 kr/mo",
  ),
)
