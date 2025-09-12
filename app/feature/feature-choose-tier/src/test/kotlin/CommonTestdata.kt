import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.changetier.data.TotalCost
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant

internal class FakeChangeTierRepository() : ChangeTierRepository {
  val changeTierIntentTurbine = Turbine<Either<ErrorMessage, ChangeTierDeductibleIntent>>()
  val quoteTurbine = Turbine<Either<ErrorMessage, TierDeductibleQuote>>()
  val quoteListTurbine = Turbine<List<TierDeductibleQuote>>()

  override suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    return changeTierIntentTurbine.awaitItem()
  }

  override suspend fun getQuoteById(id: String): Either<ErrorMessage, TierDeductibleQuote> {
    return quoteTurbine.awaitItem()
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    return quoteListTurbine.awaitItem()
  }

  override suspend fun addQuotesToStorage(quotes: List<TierDeductibleQuote>) {
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    return either {}
  }

  override suspend fun getCurrentQuoteId(): String {
    return CURRENT_ID
  }
}

internal const val CURRENT_ID = "current"
internal val basTier = Tier(
  "BAS",
  tierLevel = 0,
  tierDescription = "Vårt paket med grundläggande villkor.",
  tierDisplayName = "Bas",
)

internal val standardTier = Tier(
  "STANDARD",
  tierLevel = 1,
  tierDescription = "Vårt standard paket.",
  tierDisplayName = "Standard",
)

internal val testQuote = TierDeductibleQuote(
  id = "id0",
  deductible = Deductible(
    UiMoney(1000.0, SEK),
    deductiblePercentage = 0,
    description = "description",
  ),
  displayItems = listOf(
    ChangeTierDeductibleDisplayItem(
      displayValue = "hhh",
      displaySubtitle = "mmm",
      displayTitle = "ioi",
    ),
  ),
  premium = UiMoney(299.0, SEK),
  tier = standardTier,
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
  addons = emptyList(),
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

internal val testQuote2 = TierDeductibleQuote(
  id = "id1",
  deductible = Deductible(
    UiMoney(3000.0, SEK),
    deductiblePercentage = 0,
    description = "description",
  ),
  displayItems = listOf(
    ChangeTierDeductibleDisplayItem(
      displayValue = "hhh",
      displaySubtitle = "mmm",
      displayTitle = "ioi",
    ),
  ),
  premium = UiMoney(259.0, SEK),
  tier = standardTier,
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
  addons = emptyList(),
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

internal val testQuote3 = TierDeductibleQuote(
  id = "id3",
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
  premium = UiMoney(205.0, SEK),
  tier = standardTier,
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
  addons = emptyList(),
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

internal val currentQuote = TierDeductibleQuote(
  id = CURRENT_ID,
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
  tier = basTier,
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
  addons = emptyList(),
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
