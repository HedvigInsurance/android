package com.hedvig.android.feature.changeaddress.destination

import app.cash.turbine.Turbine
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.data.productvariant.InsurableLimit.InsurableLimitType.BIKE
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.changeaddress.data.Address
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType.GARAGE
import com.hedvig.android.feature.changeaddress.data.HousingType.APARTMENT_OWN
import com.hedvig.android.feature.changeaddress.data.HousingType.VILLA
import com.hedvig.android.feature.changeaddress.data.MoveIntent
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.QuoteInput
import com.hedvig.android.feature.changeaddress.data.SuccessfulMove
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.navigation.NewAddressParameters
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import com.hedvig.android.feature.changeaddress.navigation.VillaOnlyParameters
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

internal val fakeMoveIntent = MoveIntent(
  id = MoveIntentId("moveintentid"),
  currentHomeAddresses = listOf(Address(id = AddressId("address"), postalCode = "12345", street = "Street")),
  movingDateRange = LocalDate(2024, 6, 24)..LocalDate(2024, 9, 30),
  suggestedNumberInsured = 3,
  isApartmentAvailableforStudent = false,
  maxHouseNumberCoInsured = 6,
  maxApartmentSquareMeters = 300,
  maxHouseSquareMeters = 500,
  extraBuildingTypes = listOf(),
  maxApartmentNumberCoInsured = 4,
)

internal val fakeSelectHousingTypeParametersForVilla = SelectHousingTypeParameters(
  extraBuildingTypes = listOf(),
  housingType = VILLA,
  isEligibleForStudent = false,
  maxDate = LocalDate(2024, 9, 30),
  minDate = LocalDate(2024, 6, 24),
  suggestedNumberInsured = "3",
  maxNumberCoInsured = 6,
  maxSquareMeters = 500,
  moveIntentId = "moveintentid",
  moveFromAddressId = AddressId("address"),
)

internal val fakeSelectHousingTypeParametersForApartment = SelectHousingTypeParameters(
  extraBuildingTypes = listOf(),
  housingType = APARTMENT_OWN,
  isEligibleForStudent = false,
  maxDate = LocalDate(2024, 9, 30),
  minDate = LocalDate(2024, 6, 24),
  suggestedNumberInsured = "3",
  maxNumberCoInsured = 4,
  maxSquareMeters = 300,
  moveIntentId = "moveintentid",
  moveFromAddressId = AddressId("address"),
)

private val fakeEnterNewAddressParameters = NewAddressParameters(
  street = "newStreet",
  numberInsured = "1",
  isStudent = false,
  movingDate = LocalDate(2024, 7, 1),
  postalCode = "newPostalCode",
  squareMeters = "93",
)

internal val fakeMovingParametersForVilla = MovingParameters(
  selectHousingTypeParameters = fakeSelectHousingTypeParametersForVilla,
  newAddressParameters = fakeEnterNewAddressParameters,
  villaOnlyParameters = null,
)

internal val fakeVillaOnlyParameters = VillaOnlyParameters(
  isSublet = true,
  ancillaryArea = "15",
  numberOfBathrooms = "2",
  yearOfConstruction = "1999",
  extraBuildings = listOf(
    ExtraBuilding(
      "iddd",
      3,
      type = GARAGE,
      hasWaterConnected = false,
    ),
  ),
)

internal val fakeMoveQuote = MoveQuote(
  id = "fakeId",
  insuranceName = "Insurance Good Home",
  moveIntentId = MoveIntentId(""),
  premium = UiMoney(99.0, CurrencyCode.SEK),
  startDate = LocalDate(2023, 5, 13),
  isExpanded = false,
  productVariant = ProductVariant(
    displayName = "Test",
    contractGroup = ContractGroup.RENTAL,
    contractType = ContractType.SE_APARTMENT_RENT,
    partner = "test",
    perils = listOf(),
    insurableLimits = listOf(
      InsurableLimit(
        label = "test",
        description = "long".repeat(10),
        limit = "long".repeat(10),
        type = BIKE,
      ),
    ),
    documents = listOf(),
  ),
  displayItems = listOf(),
)

internal val fakeMovingParametersForOfferFromVilla = MovingParameters(
  selectHousingTypeParameters = fakeSelectHousingTypeParametersForVilla,
  newAddressParameters = fakeEnterNewAddressParameters,
  villaOnlyParameters = fakeVillaOnlyParameters,
)

internal val fakeMovingParametersForOfferFromApartment = MovingParameters(
  selectHousingTypeParameters = fakeSelectHousingTypeParametersForApartment,
  newAddressParameters = fakeEnterNewAddressParameters,
  villaOnlyParameters = null,
)

internal class FakeChangeAddressRepository : ChangeAddressRepository {
  val createMoveResponseTurbine = Turbine<Either<ErrorMessage, MoveIntent>>()
  val createQuotesResponseTurbine = Turbine<Either<ErrorMessage, List<MoveQuote>>>()
  val commitMoveResponseTurbine = Turbine<Either<ErrorMessage, SuccessfulMove>>()

  override suspend fun createMoveIntent(): Either<ErrorMessage, MoveIntent> {
    return createMoveResponseTurbine.awaitItem()
  }

  override suspend fun createQuotes(input: QuoteInput): Either<ErrorMessage, List<MoveQuote>> {
    return createQuotesResponseTurbine.awaitItem()
  }

  override suspend fun commitMove(id: MoveIntentId): Either<ErrorMessage, SuccessfulMove> {
    return commitMoveResponseTurbine.awaitItem()
  }
}
