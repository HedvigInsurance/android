package ui

import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.InsuranceVariantDocument.InsuranceDocumentType.CERTIFICATE
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.ProductVariantPeril
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.AddressInfo
import com.hedvig.android.feature.movingflow.ui.summary.SummaryInfo
import kotlinx.datetime.LocalDate

internal val fakeProductVariant = ProductVariant(
  displayName = "Variant",
  contractGroup = ContractGroup.RENTAL,
  contractType = ContractType.SE_APARTMENT_RENT,
  partner = null,
  perils = listOf(
    ProductVariantPeril(
      "id",
      "peril title",
      "peril description",
      emptyList(),
      emptyList(),
      null,
    ),
  ),
  insurableLimits = listOf(
    InsurableLimit(
      label = "insurable limit label",
      limit = "insurable limit limit",
      description = "insurable limit description",
    ),
  ),
  documents = listOf(
    InsuranceVariantDocument(
      displayName = "displayName",
      url = "url",
      type = CERTIFICATE,
    ),
  ),
  displayTierName = "tierDescription",
  tierDescription = "displayNameTier",
  termsVersion = "termsVersion",
)
internal val fakeAddonVariant = AddonVariant(
  termsVersion = "terrrms",
  displayName = "Addon 1",
  product = "product",
  documents = listOf(
    InsuranceVariantDocument(
      displayName = "displayName",
      url = "url",
      type = CERTIFICATE,
    ),
  ),
  perils = listOf(),
  insurableLimits = listOf(),
)
internal val fakeStartDate = LocalDate.parse("2025-01-01")

internal val fakeHomeQuoteWithAddon = MoveHomeQuote(
  id = homeQuoteIdFake,
  premium = UiMoney(99.0, SEK),
  startDate = LocalDate(2025, 1, 1),
  displayItems = listOf(
    DisplayItem(
      title = "display title",
      subtitle = "display subtitle",
      value = "display value",
    ),
  ),
  exposureName = "exposureName",
  productVariant = fakeProductVariant,
  tierName = "tierName",
  tierLevel = 1,
  tierDescription = "tierDescription",
  deductible = Deductible(UiMoney(1500.0, SEK), null, "displayText"),
  defaultChoice = false,
  relatedAddonQuotes = List(1) {
    AddonQuote(
      premium = UiMoney(129.0, SEK),
      startDate = fakeStartDate,
      displayItems = listOf(
        DisplayItem(
          title = "display title",
          subtitle = "display subtitle",
          value = "display value",
        ),
      ),
      exposureName = "exposureName",
      addonVariant = fakeAddonVariant,
    )
  },
)

internal val fakeHomeQuoteNoAddon = MoveHomeQuote(
  id = homeQuoteIdFake,
  premium = UiMoney(99.0, SEK),
  startDate = LocalDate(2025, 1, 1),
  displayItems = listOf(
    DisplayItem(
      title = "display title",
      subtitle = "display subtitle",
      value = "display value",
    ),
  ),
  exposureName = "exposureName",
  productVariant = fakeProductVariant,
  tierName = "tierName",
  tierLevel = 1,
  tierDescription = "tierDescription",
  deductible = Deductible(UiMoney(1500.0, SEK), null, "displayText"),
  defaultChoice = false,
  relatedAddonQuotes = emptyList(),
)

internal val fakeMta1 = MoveMtaQuote(
  premium = UiMoney(49.0, SEK),
  exposureName = "exposureName",
  productVariant = fakeProductVariant,
  startDate = fakeStartDate,
  displayItems = emptyList(),
  relatedAddonQuotes = emptyList(),
)

internal val fakeMta2 = MoveMtaQuote(
  premium = UiMoney(59.0, SEK),
  exposureName = "exposureName",
  productVariant = fakeProductVariant,
  startDate = fakeStartDate,
  displayItems = emptyList(),
  relatedAddonQuotes = emptyList(),
)

internal val fakeMta2WithAddon = MoveMtaQuote(
  premium = UiMoney(23.0, SEK),
  exposureName = "exposureName",
  productVariant = fakeProductVariant,
  startDate = fakeStartDate,
  displayItems = emptyList(),
  relatedAddonQuotes = listOf(
    AddonQuote(
      premium = UiMoney(30.0, SEK),
      startDate = fakeStartDate,
      displayItems = listOf(
        DisplayItem(
          title = "display title",
          subtitle = "display subtitle",
          value = "display value",
        ),
      ),
      exposureName = "exposureName",
      addonVariant = fakeAddonVariant,
    ),
  ),
)

internal val fakeSummaryInfoNoAddons = SummaryInfo(
  moveHomeQuote = fakeHomeQuoteNoAddon,
  moveMtaQuotes = listOf(
    fakeMta1,
    fakeMta2,
  ),
)

internal val fakeSummaryInfoWithTwoAddons = SummaryInfo(
  moveHomeQuote = fakeHomeQuoteWithAddon,
  moveMtaQuotes = listOf(
    fakeMta1,
    fakeMta2WithAddon,
  ),
)

internal val fakeSummaryInfoWithOnlyHomeQuote = SummaryInfo(
  moveHomeQuote = fakeHomeQuoteNoAddon,
  moveMtaQuotes = emptyList(),
)

internal const val moveIntentIdFake = "moveIntentId"
internal const val homeQuoteIdFake = "homeQuoteId"

internal val fakePropertyState = MovingFlowState.PropertyState.ApartmentState(
  numberCoInsuredState = MovingFlowState.NumberCoInsuredState(allowedNumberCoInsuredRange = 0..3, 1),
  squareMetersState = MovingFlowState.SquareMetersState(10..100, 69),
  apartmentType = MovingFlowState.PropertyState.ApartmentState.ApartmentType.BRF,
  isAvailableForStudentState = MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState.NotAvailable,
)

internal val fakeMovingStateNoAddons = MovingFlowState(
  id = moveIntentIdFake,
  moveFromAddressId = "id",
  housingType = HousingType.ApartmentOwn,
  addressInfo = AddressInfo("street", "18888"),
  movingDateState = MovingFlowState.MovingDateState(
    selectedMovingDate = null,
    allowedMovingDateRange = LocalDate(2025, 1, 1)..LocalDate(2025, 3, 1),
  ),
  propertyState = fakePropertyState,
  movingFlowQuotes = MovingFlowQuotes(
    homeQuotes = listOf(fakeHomeQuoteNoAddon),
    mtaQuotes = listOf(
      fakeMta1,
      fakeMta2,
    ),
  ),
  lastSelectedHomeQuoteId = null,
  oldAddressCoverageDurationDays = 30,
)

internal val fakeMovingStateWithOnlyHomeQuote = MovingFlowState(
  id = moveIntentIdFake,
  moveFromAddressId = "id",
  housingType = HousingType.ApartmentOwn,
  addressInfo = AddressInfo("street", "18888"),
  movingDateState = MovingFlowState.MovingDateState(
    selectedMovingDate = null,
    allowedMovingDateRange = LocalDate(2025, 1, 1)..LocalDate(2025, 3, 1),
  ),
  propertyState = fakePropertyState,
  movingFlowQuotes = MovingFlowQuotes(
    homeQuotes = listOf(fakeHomeQuoteNoAddon),
    mtaQuotes = emptyList(),
  ),
  lastSelectedHomeQuoteId = null,
  oldAddressCoverageDurationDays = 30,
)

internal val fakeMovingStateWithTwoAddons = MovingFlowState(
  id = moveIntentIdFake,
  moveFromAddressId = "id",
  housingType = HousingType.ApartmentOwn,
  addressInfo = AddressInfo("street", "18888"),
  movingDateState = MovingFlowState.MovingDateState(
    selectedMovingDate = null,
    allowedMovingDateRange = LocalDate(2025, 1, 1)..LocalDate(2025, 3, 1),
  ),
  propertyState = fakePropertyState,
  movingFlowQuotes = MovingFlowQuotes(
    homeQuotes = listOf(fakeHomeQuoteWithAddon),
    mtaQuotes = listOf(
      fakeMta1,
      fakeMta2WithAddon,
    ),
  ),
  lastSelectedHomeQuoteId = null,
  oldAddressCoverageDurationDays = 30,
)
