package com.hedvig.android.feature.movingflow.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.data.MovingFlowState.NumberCoInsuredState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.ApartmentType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.MoveExtraBuildingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState.SquareMetersState
import com.hedvig.android.feature.movingflow.testMaxMovingDate
import com.hedvig.android.feature.movingflow.testMinMovingDate
import com.hedvig.android.feature.movingflow.testMoveAddonQuote
import com.hedvig.android.feature.movingflow.testMoveAddress
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMoveIntentFragment
import com.hedvig.android.feature.movingflow.testMoveIntentQuotesFragment
import com.hedvig.android.feature.movingflow.testMovingFlowState
import com.hedvig.android.feature.movingflow.testMovingFlowStateWithPropertyInput
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Test

class MovingFlowStateTest {
  @Test
  fun `number of co-insured suggestion is capped to the max`() {
    val state = NumberCoInsuredState(maxNumberCoInsured = 2, suggestedNumberCoInsured = 5)

    assertThat(state.allowedNumberCoInsuredRange).isEqualTo(0..2)
    assertThat(state.selectedNumberCoInsured).isEqualTo(2)
  }

  @Test
  fun `a missing max number of co-insured falls back to a sane upper bound`() {
    val state = NumberCoInsuredState(maxNumberCoInsured = null, suggestedNumberCoInsured = 3)

    assertThat(state.allowedNumberCoInsuredRange).isEqualTo(0..99)
    assertThat(state.selectedNumberCoInsured).isEqualTo(3)
  }

  @Test
  fun `square meters start unselected with a range from 1 to the max`() {
    assertThat(SquareMetersState(maxSquareMeters = 150)).isEqualTo(
      SquareMetersState(allowedSquareMetersRange = 1..150, selectedSquareMeters = null),
    )
    assertThat(SquareMetersState(maxSquareMeters = null).allowedSquareMetersRange).isEqualTo(1..999)
  }

  @Test
  fun `fromFragments builds an untouched state for the chosen address`() {
    val fragment = testMoveIntentFragment(
      addresses = listOf(
        testMoveAddress(id = "first", suggestedNumberCoInsured = 0),
        testMoveAddress(id = "second", suggestedNumberCoInsured = 3),
      ),
    )

    val state = MovingFlowState.fromFragments(fragment, null, "second", MovingSource.TERMINATION)

    assertThat(state.id).isEqualTo(fragment.id)
    assertThat(state.moveFromAddressId).isEqualTo("second")
    assertThat(state.movingSource).isEqualTo(MovingSource.TERMINATION)
    assertThat(state.housingType).isNull()
    assertThat(state.propertyState).isNull()
    assertThat(state.movingFlowQuotes).isNull()
    assertThat(state.lastSelectedHomeQuoteId).isNull()
    assertThat(state.addressInfo).isEqualTo(MovingFlowState.AddressInfo(null, null))
    assertThat(state.movingDateState).isEqualTo(
      MovingFlowState.MovingDateState(
        selectedMovingDate = null,
        allowedMovingDateRange = testMinMovingDate..testMaxMovingDate,
      ),
    )
    assertThat(state.mapOfPropertyStates.getValue(HousingType.Villa).numberCoInsuredState.selectedNumberCoInsured)
      .isEqualTo(3)
  }

  @Test
  fun `fromFragments falls back to the first address when the chosen one is unknown`() {
    val fragment = testMoveIntentFragment(
      addresses = listOf(
        testMoveAddress(id = "first", suggestedNumberCoInsured = 2),
        testMoveAddress(id = "second", suggestedNumberCoInsured = 4),
      ),
    )

    val state = MovingFlowState.fromFragments(fragment, null, "unknown", MovingSource.OTHER)

    assertThat(state.mapOfPropertyStates.getValue(HousingType.Villa).numberCoInsuredState.selectedNumberCoInsured)
      .isEqualTo(2)
  }

  @Test
  fun `fromFragments prepares a property state for every housing type`() {
    val fragment = testMoveIntentFragment(
      maxHouseNumberCoInsured = 6,
      maxHouseSquareMeters = 400,
      maxApartmentNumberCoInsured = 3,
      maxApartmentSquareMeters = 150,
      isApartmentAvailableForStudent = true,
    )

    val propertyStates = testMovingFlowState(moveIntentFragment = fragment).mapOfPropertyStates

    assertThat(propertyStates.keys).isEqualTo(HousingType.entries.toSet())
    assertThat(propertyStates.getValue(HousingType.ApartmentRent))
      .isInstanceOf<ApartmentState>()
      .prop(ApartmentState::apartmentType).isEqualTo(ApartmentType.RENT)
    assertThat(propertyStates.getValue(HousingType.ApartmentOwn))
      .isInstanceOf<ApartmentState>()
      .prop(ApartmentState::apartmentType).isEqualTo(ApartmentType.BRF)
    val apartment = propertyStates.getValue(HousingType.ApartmentRent) as ApartmentState
    assertThat(apartment.numberCoInsuredState.allowedNumberCoInsuredRange).isEqualTo(0..3)
    assertThat(apartment.squareMetersState.allowedSquareMetersRange).isEqualTo(1..150)
    assertThat(apartment.isAvailableForStudentState).isEqualTo(IsAvailableForStudentState.Available(false))

    val house = propertyStates.getValue(HousingType.Villa) as HouseState
    assertThat(house.numberCoInsuredState.allowedNumberCoInsuredRange).isEqualTo(0..6)
    assertThat(house.squareMetersState.allowedSquareMetersRange).isEqualTo(1..400)
    assertThat(house.extraBuildingTypesState.allowedExtraBuildingTypes)
      .containsExactly(MoveExtraBuildingType(type = "GARAGE", displayName = "Garage"))
    assertThat(house.extraBuildingTypesState.selectedExtraBuildingTypes).isEqualTo(emptyList())
    assertThat(house.isSublet).isEqualTo(false)
  }

  @Test
  fun `student option is unavailable unless the backend explicitly allows it`() {
    for (availableForStudent in listOf(false, null)) {
      val state = testMovingFlowState(
        moveIntentFragment = testMoveIntentFragment(isApartmentAvailableForStudent = availableForStudent),
      )
      val apartment = state.mapOfPropertyStates.getValue(HousingType.ApartmentRent) as ApartmentState
      assertThat(apartment.isAvailableForStudentState).isEqualTo(IsAvailableForStudentState.NotAvailable)
    }
  }

  @Test
  fun `state survives a json round trip`() {
    val state = testMovingFlowStateWithPropertyInput(HousingType.ApartmentRent).copy(
      movingFlowQuotes = testMoveIntentQuotesFragment(
        homeQuotes = listOf(testMoveHomeQuote(id = "quote", addons = listOf(testMoveAddonQuote("addon")))),
      ).toMovingFlowQuotes(),
      lastSelectedHomeQuoteId = "quote",
    )

    val encoded = Json.encodeToString(state)
    val decoded = Json.decodeFromString<MovingFlowState>(encoded)

    // A decoded LocalDate range is a plain ClosedRange, which never equals the LocalDateRange it was encoded from,
    // so the round trip is compared through its encoded form.
    assertThat(Json.encodeToString(decoded)).isEqualTo(encoded)
    assertThat(decoded.movingDateState.allowedMovingDateRange.start)
      .isEqualTo(state.movingDateState.allowedMovingDateRange.start)
    assertThat(decoded.movingDateState.allowedMovingDateRange.endInclusive)
      .isEqualTo(state.movingDateState.allowedMovingDateRange.endInclusive)
  }

  @Test
  fun `state persisted before the moving source existed decodes as OTHER`() {
    val state = testMovingFlowState(movingSource = MovingSource.TERMINATION)
    val encodedWithoutSource = JsonObject(
      Json.encodeToJsonElement(MovingFlowState.serializer(), state).jsonObject - "movingSource",
    ).toString()

    val decoded = Json.decodeFromString<MovingFlowState>(encodedWithoutSource)

    assertThat(decoded.movingSource).isEqualTo(MovingSource.OTHER)
    assertThat(decoded.id).isEqualTo(state.id)
  }

  @Test
  fun `closed ranges serialize as start and end`() {
    val serializer = ClosedRangeSerializer(LocalDate.serializer())
    val range = LocalDate(2026, 1, 1)..LocalDate(2026, 12, 31)

    val encoded = Json.encodeToString(serializer, range)

    assertThat(encoded).isEqualTo("""{"start":"2026-01-01","end":"2026-12-31"}""")
    val decoded = Json.decodeFromString(serializer, encoded)
    assertThat(decoded.start).isEqualTo(range.start)
    assertThat(decoded.endInclusive).isEqualTo(range.endInclusive)
  }
}
