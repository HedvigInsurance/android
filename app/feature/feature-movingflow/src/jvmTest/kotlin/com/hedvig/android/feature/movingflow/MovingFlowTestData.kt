package com.hedvig.android.feature.movingflow

import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.fromFragments
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import octopus.builder.Data
import octopus.builder.MoveAddonQuoteBuilder
import octopus.builder.MoveAddressBuilder
import octopus.builder.MoveHomeQuoteBuilder
import octopus.builder.MoveMtaQuoteBuilder
import octopus.builder.buildDeductible
import octopus.builder.buildItemCost
import octopus.builder.buildItemDiscount
import octopus.builder.buildMoney
import octopus.builder.buildMoveAddonQuote
import octopus.builder.buildMoveAddress
import octopus.builder.buildMoveExtraBuildingTypeV2
import octopus.builder.buildMoveHomeQuote
import octopus.builder.buildMoveIntent
import octopus.builder.buildMoveIntentMutationOutput
import octopus.builder.buildMoveMtaQuote
import octopus.builder.buildMoveQuoteDisplayItem
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment
import octopus.type.CurrencyCode

internal class TestBackstack(
  override val entries: MutableList<HedvigNavKey> = mutableListOf(),
) : Backstack

/**
 * The test DataStore writes its file off the test scheduler, so waiting for a write to land needs real time.
 */
internal suspend fun awaitCondition(condition: suspend () -> Boolean) {
  withContext(Dispatchers.Default) {
    withTimeout(5.seconds) {
      while (!condition()) {
        delay(10.milliseconds)
      }
    }
  }
}

internal const val TEST_MOVE_INTENT_ID = "moveIntentId"
internal val testMinMovingDate = LocalDate(2026, 1, 1)
internal val testMaxMovingDate = LocalDate(2026, 3, 1)

internal fun testMoveAddress(
  id: String = "addressId",
  suggestedNumberCoInsured: Int = 1,
): MoveAddressBuilder.() -> Unit = {
  this.id = id
  displayTitle = "Street $id"
  displaySubtitle = "Subtitle $id"
  this.suggestedNumberCoInsured = suggestedNumberCoInsured
  minMovingDate = testMinMovingDate
  maxMovingDate = testMaxMovingDate
}

internal fun moveIntentCreateData(
  addresses: List<MoveAddressBuilder.() -> Unit> = listOf(testMoveAddress()),
  maxHouseNumberCoInsured: Int? = 5,
  maxHouseSquareMeters: Int? = 300,
  maxApartmentNumberCoInsured: Int? = 4,
  maxApartmentSquareMeters: Int? = 200,
  isApartmentAvailableForStudent: Boolean? = true,
): MoveIntentV2CreateMutation.Data = MoveIntentV2CreateMutation.Data(OctopusFakeResolver) {
  moveIntentCreate = buildMoveIntentMutationOutput {
    moveIntent = buildMoveIntent {
      id = TEST_MOVE_INTENT_ID
      this.maxHouseNumberCoInsured = maxHouseNumberCoInsured
      this.maxHouseSquareMeters = maxHouseSquareMeters
      this.maxApartmentNumberCoInsured = maxApartmentNumberCoInsured
      this.maxApartmentSquareMeters = maxApartmentSquareMeters
      isApartmentAvailableforStudent = isApartmentAvailableForStudent
      extraBuildingTypesV2 = listOf(
        buildMoveExtraBuildingTypeV2 {
          type = "GARAGE"
          displayName = "Garage"
        },
      )
      currentHomeAddresses = addresses.map { buildMoveAddress(it) }
    }
    userError = null
  }
}

internal fun testMoveIntentFragment(
  addresses: List<MoveAddressBuilder.() -> Unit> = listOf(testMoveAddress()),
  maxHouseNumberCoInsured: Int? = 5,
  maxHouseSquareMeters: Int? = 300,
  maxApartmentNumberCoInsured: Int? = 4,
  maxApartmentSquareMeters: Int? = 200,
  isApartmentAvailableForStudent: Boolean? = true,
): MoveIntentFragment = moveIntentCreateData(
  addresses = addresses,
  maxHouseNumberCoInsured = maxHouseNumberCoInsured,
  maxHouseSquareMeters = maxHouseSquareMeters,
  maxApartmentNumberCoInsured = maxApartmentNumberCoInsured,
  maxApartmentSquareMeters = maxApartmentSquareMeters,
  isApartmentAvailableForStudent = isApartmentAvailableForStudent,
).moveIntentCreate.moveIntent!!

internal fun testMovingFlowState(
  moveIntentFragment: MoveIntentFragment = testMoveIntentFragment(),
  movingSource: MovingSource = MovingSource.INSURANCE,
): MovingFlowState = MovingFlowState.fromFragments(
  moveIntentFragment = moveIntentFragment,
  moveIntentQuotesFragment = null,
  moveFromAddressId = "addressId",
  movingSource = movingSource,
)

/**
 * A state as it is after the user has picked [housingType] and filled in the new address.
 */
internal fun testMovingFlowStateWithPropertyInput(
  housingType: HousingType,
  movingSource: MovingSource = MovingSource.INSURANCE,
): MovingFlowState {
  val initial = testMovingFlowState(movingSource = movingSource)
  val propertyState = when (val propertyState = initial.mapOfPropertyStates.getValue(housingType)) {
    is MovingFlowState.PropertyState.HouseState -> propertyState.copy(
      squareMetersState = propertyState.squareMetersState.copy(selectedSquareMeters = 120),
    )

    is MovingFlowState.PropertyState.ApartmentState -> propertyState.copy(
      squareMetersState = propertyState.squareMetersState.copy(selectedSquareMeters = 60),
    )
  }
  return initial.copy(
    housingType = housingType,
    propertyState = propertyState,
    addressInfo = MovingFlowState.AddressInfo(street = "New street 1", postalCode = "12345"),
    movingDateState = initial.movingDateState.copy(selectedMovingDate = LocalDate(2026, 2, 1)),
  )
}

internal fun testMoveHomeQuote(
  id: String,
  amount: Double = 200.0,
  defaultChoice: Boolean = false,
  tierLevel: Int = 1,
  addons: List<MoveAddonQuoteBuilder.() -> Unit> = emptyList(),
): MoveHomeQuoteBuilder.() -> Unit = {
  this.id = id
  premium = buildMoney {
    this.amount = amount
    currencyCode = CurrencyCode.SEK
  }
  totalCost = buildItemCost {
    monthlyNet = buildMoney {
      this.amount = amount - 20
      currencyCode = CurrencyCode.SEK
    }
    monthlyGross = buildMoney {
      this.amount = amount
      currencyCode = CurrencyCode.SEK
    }
    discounts = listOf(
      buildItemDiscount {
        displayName = "Bundle discount"
        displayValue = "-20 kr"
      },
    )
  }
  startDate = LocalDate(2026, 2, 1)
  this.defaultChoice = defaultChoice
  tierName = "Tier $id"
  this.tierLevel = tierLevel
  deductible = buildDeductible {
    this.amount = buildMoney {
      this.amount = 1500.0
      currencyCode = CurrencyCode.SEK
    }
    percentage = 0
    displayText = "1500 kr"
  }
  displayItems = listOf(
    buildMoveQuoteDisplayItem {
      displayTitle = "Address"
      displaySubtitle = null
      displayValue = "New street 1"
    },
  )
  exposureName = "New street 1"
  this.addons = addons.map { buildMoveAddonQuote(it) }
}

internal fun testMoveAddonQuote(addonId: String, amount: Double = 49.0): MoveAddonQuoteBuilder.() -> Unit = {
  this.addonId = addonId
  displayName = "Addon $addonId"
  coverageDisplayName = "45 days"
  displayItems = emptyList()
  premium = buildMoney {
    this.amount = amount
    currencyCode = CurrencyCode.SEK
  }
  startDate = LocalDate(2026, 2, 1)
}

internal fun testMoveMtaQuote(exposureName: String = "Car ABC123"): MoveMtaQuoteBuilder.() -> Unit = {
  premium = buildMoney {
    amount = 99.0
    currencyCode = CurrencyCode.SEK
  }
  totalCost = buildItemCost {
    monthlyNet = buildMoney {
      amount = 99.0
      currencyCode = CurrencyCode.SEK
    }
    monthlyGross = buildMoney {
      amount = 99.0
      currencyCode = CurrencyCode.SEK
    }
    discounts = emptyList()
  }
  startDate = LocalDate(2026, 2, 1)
  displayItems = emptyList()
  this.exposureName = exposureName
  addons = emptyList()
}

internal fun moveIntentRequestData(
  homeQuotes: List<MoveHomeQuoteBuilder.() -> Unit>,
  mtaQuotes: List<MoveMtaQuoteBuilder.() -> Unit> = emptyList(),
): MoveIntentV2RequestMutation.Data = MoveIntentV2RequestMutation.Data(OctopusFakeResolver) {
  moveIntentRequest = buildMoveIntentMutationOutput {
    moveIntent = buildMoveIntent {
      id = TEST_MOVE_INTENT_ID
      this.homeQuotes = homeQuotes.map { buildMoveHomeQuote(it) }
      this.mtaQuotes = mtaQuotes.map { buildMoveMtaQuote(it) }
    }
    userError = null
  }
}

internal fun testMoveIntentQuotesFragment(
  homeQuotes: List<MoveHomeQuoteBuilder.() -> Unit>,
  mtaQuotes: List<MoveMtaQuoteBuilder.() -> Unit> = emptyList(),
): MoveIntentQuotesFragment = moveIntentRequestData(homeQuotes, mtaQuotes).moveIntentRequest.moveIntent!!
