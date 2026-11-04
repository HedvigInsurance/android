package com.hedvig.android.feature.movingflow.storage

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.data.AddonId
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.ApartmentState.IsAvailableForStudentState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState
import com.hedvig.android.feature.movingflow.data.MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo
import com.hedvig.android.feature.movingflow.testMoveAddonQuote
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMoveIntentFragment
import com.hedvig.android.feature.movingflow.testMoveIntentQuotesFragment
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MovingFlowRepositoryTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testFolder = TemporaryFolder()

  private fun TestScope.repository(): MovingFlowRepository = MovingFlowRepository(
    MovingFlowStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = CoroutineScope(backgroundScope.coroutineContext),
      ),
    ),
  )

  private suspend fun MovingFlowRepository.state(): MovingFlowState = movingFlowState().first()!!

  @Test
  fun `there is no state before a flow is initiated`() = runTest {
    val repository = repository()

    assertThat(repository.movingFlowState().first()).isNull()
  }

  @Test
  fun `edits without an ongoing flow do nothing`() = runTest {
    val repository = repository()

    repository.updateWithHousingType(HousingType.Villa)
    val result = repository.updateWithPropertyInput(LocalDate(2026, 2, 1), "street", "12345", 50, 1, false)

    assertThat(result).isNull()
    assertThat(repository.movingFlowState().first()).isNull()
  }

  @Test
  fun `initiating a flow stores a fresh state for the chosen address`() = runTest {
    val repository = repository()

    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.TERMINATION)

    val state = repository.state()
    assertThat(state.moveFromAddressId).isEqualTo("addressId")
    assertThat(state.movingSource).isEqualTo(MovingSource.TERMINATION)
    assertThat(state.housingType).isNull()
    assertThat(state.propertyState).isNull()
  }

  @Test
  fun `picking a housing type selects its prepared property state`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.OTHER)

    repository.updateWithHousingType(HousingType.ApartmentOwn)
    assertThat(repository.state().housingType).isEqualTo(HousingType.ApartmentOwn)
    assertThat(repository.state().propertyState)
      .isNotNull().isInstanceOf<ApartmentState>()
      .prop(ApartmentState::apartmentType).isEqualTo(ApartmentState.ApartmentType.BRF)

    repository.updateWithHousingType(HousingType.Villa)
    assertThat(repository.state().propertyState).isNotNull().isInstanceOf<HouseState>()
  }

  @Test
  fun `property input fills in address, date and apartment details`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.OTHER)
    repository.updateWithHousingType(HousingType.ApartmentRent)

    val returned = repository.updateWithPropertyInput(
      movingDate = LocalDate(2026, 2, 1),
      address = "New street 1",
      postalCode = "12345",
      squareMeters = 55,
      numberCoInsured = 2,
      isStudent = true,
    )

    val state = repository.state()
    assertThat(returned).isNotNull().prop(MovingFlowState::addressInfo).isEqualTo(state.addressInfo)
    assertThat(state.addressInfo).isEqualTo(MovingFlowState.AddressInfo("New street 1", "12345"))
    assertThat(state.movingDateState.selectedMovingDate).isEqualTo(LocalDate(2026, 2, 1))
    val apartment = state.propertyState as ApartmentState
    assertThat(apartment.squareMetersState.selectedSquareMeters).isEqualTo(55)
    assertThat(apartment.numberCoInsuredState.selectedNumberCoInsured).isEqualTo(2)
    assertThat(apartment.isAvailableForStudentState).isEqualTo(IsAvailableForStudentState.Available(true))
  }

  @Test
  fun `student choice is ignored when the apartment is not available for students`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(
      testMoveIntentFragment(isApartmentAvailableForStudent = false),
      "addressId",
      MovingSource.OTHER,
    )
    repository.updateWithHousingType(HousingType.ApartmentRent)

    repository.updateWithPropertyInput(LocalDate(2026, 2, 1), "street", "12345", 55, 0, isStudent = true)

    assertThat((repository.state().propertyState as ApartmentState).isAvailableForStudentState)
      .isEqualTo(IsAvailableForStudentState.NotAvailable)
  }

  @Test
  fun `house input is stored on a house`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.OTHER)
    repository.updateWithHousingType(HousingType.Villa)
    val garage = ExtraBuildingInfo(area = 20, type = "GARAGE", displayName = "Garage", hasWaterConnected = false)

    repository.updateWithHouseInput(
      yearOfConstruction = 1990,
      ancillaryArea = 15,
      numberOfBathrooms = 2,
      isSublet = true,
      extraBuildings = listOf(garage),
    )

    val house = repository.state().propertyState as HouseState
    assertThat(house.yearOfConstruction).isEqualTo(1990)
    assertThat(house.ancillaryArea).isEqualTo(15)
    assertThat(house.numberOfBathrooms).isEqualTo(2)
    assertThat(house.isSublet).isEqualTo(true)
    assertThat(house.extraBuildingTypesState.selectedExtraBuildingTypes).containsExactly(garage)
  }

  @Test
  fun `house input on an apartment leaves the state untouched`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.OTHER)
    repository.updateWithHousingType(HousingType.ApartmentRent)
    val before = repository.state()

    repository.updateWithHouseInput(1990, 15, 2, true, emptyList())

    assertThat(repository.state().propertyState).isEqualTo(before.propertyState)
  }

  @Test
  fun `quotes, preselected quote and addon exclusions are stored`() = runTest {
    val repository = repository()
    repository.initiateNewMovingFlow(testMoveIntentFragment(), "addressId", MovingSource.OTHER)

    repository.updateWithMoveIntentQuotes(
      testMoveIntentQuotesFragment(
        homeQuotes = listOf(
          testMoveHomeQuote(id = "basic", addons = listOf(testMoveAddonQuote("travel"))),
          testMoveHomeQuote(id = "max", addons = listOf(testMoveAddonQuote("travel"), testMoveAddonQuote("other"))),
        ),
      ),
    )
    repository.updatePreselectedHomeQuoteId("max")
    repository.changeHomeAddonExclusion(AddonId("travel"), exclude = true)

    val state = repository.state()
    assertThat(state.lastSelectedHomeQuoteId).isEqualTo("max")
    val exclusions = state.movingFlowQuotes!!.homeQuotes.associate { homeQuote ->
      homeQuote.id to homeQuote.relatedAddonQuotes.associate { it.addonId.id to it.isExcludedByUser }
    }
    assertThat(exclusions).isEqualTo(
      mapOf(
        "basic" to mapOf("travel" to true),
        "max" to mapOf("travel" to true, "other" to false),
      ),
    )

    repository.changeHomeAddonExclusion(AddonId("travel"), exclude = false)
    assertThat(
      repository.state().movingFlowQuotes!!.homeQuotes.flatMap { it.relatedAddonQuotes }.none { it.isExcludedByUser },
    ).isEqualTo(true)
  }
}
