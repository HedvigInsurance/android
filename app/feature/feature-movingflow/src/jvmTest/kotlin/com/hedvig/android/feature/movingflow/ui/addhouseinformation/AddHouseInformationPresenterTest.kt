package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Optional
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import com.hedvig.android.feature.movingflow.ChoseCoverageLevelAndDeductibleKey
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
import com.hedvig.android.feature.movingflow.TestBackstack
import com.hedvig.android.feature.movingflow.awaitCondition
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.data.MovingFlowState
import com.hedvig.android.feature.movingflow.moveIntentRequestData
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMovingFlowStateWithPropertyInput
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.builder.Data
import octopus.builder.buildMoveIntentMutationOutput
import octopus.builder.buildUserError
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.type.MoveExtraBuildingInput
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveIntentSourceInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToHouseInput
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

private typealias AddHouseInformationTestContext = MoleculePresenterTestContext<*, AddHouseInformationUiState>

@OptIn(ApolloExperimental::class)
class AddHouseInformationPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @get:Rule
  val testFolder = TemporaryFolder()

  private val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  private val expectedRequest = MoveIntentV2RequestMutation(
    intentId = TEST_MOVE_INTENT_ID,
    moveIntentRequestInput = MoveIntentRequestInput(
      moveToAddress = MoveToAddressInput(street = "New street 1", postalCode = "12345"),
      moveFromAddressId = "addressId",
      movingDate = LocalDate(2026, 2, 1),
      numberCoInsured = 1,
      squareMeters = 120,
      house = Optional.present(
        MoveToHouseInput(
          ancillaryArea = 15,
          yearOfConstruction = 1990,
          numberOfBathrooms = 2,
          isSubleted = true,
          extraBuildings = listOf(MoveExtraBuildingInput(area = 20, type = "GARAGE", hasWaterConnected = true)),
        ),
      ),
      source = Optional.present(MoveIntentSourceInput.TERMINATION),
    ),
    addonsFlagOn = true,
  )

  private suspend fun TestScope.repositoryWith(state: MovingFlowState?): MovingFlowRepository {
    val storage = MovingFlowStorage(
      TestPreferencesDataStore(
        datastoreTestFileDirectory = testFolder.newFolder("datastoreTempFolder"),
        coroutineScope = CoroutineScope(backgroundScope.coroutineContext),
      ),
    )
    if (state != null) storage.setMovingFlowState(state)
    return MovingFlowRepository(storage)
  }

  private fun presenter(repository: MovingFlowRepository, backstack: TestBackstack = TestBackstack()) =
    AddHouseInformationPresenter(TEST_MOVE_INTENT_ID, repository, apolloClient, backstack)

  private suspend fun AddHouseInformationTestContext.awaitContent(): Content {
    assertThat(awaitItem()).isEqualTo(AddHouseInformationUiState.Loading)
    return awaitItem() as Content
  }

  private fun Content.fillInValidHouseInfo() {
    addressInput.yearOfConstruction.updateValue(1990)
    addressInput.ancillaryArea.updateValue(15)
    addressInput.numberOfBathrooms.updateValue(2)
    addressInput.isSublet.updateValue(true)
    addressInput.extraBuildings.updateValue(
      listOf(
        MovingFlowState.PropertyState.HouseState.ExtraBuildingTypesState.ExtraBuildingInfo(
          area = 20,
          type = "GARAGE",
          displayName = "Garage",
          hasWaterConnected = true,
        ),
      ),
    )
  }

  @Test
  fun `without an ongoing flow the flow is reported missing`() = runTest {
    presenter(repositoryWith(null)).test(AddHouseInformationUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(AddHouseInformationUiState.Loading)
      assertThat(awaitItem()).isEqualTo(AddHouseInformationUiState.MissingOngoingMovingFlow)
    }
  }

  @Test
  fun `a flow for an apartment has no house information to add`() = runTest {
    val repository = repositoryWith(testMovingFlowStateWithPropertyInput(HousingType.ApartmentRent))

    presenter(repository).test(AddHouseInformationUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(AddHouseInformationUiState.Loading)
      assertThat(awaitItem()).isEqualTo(AddHouseInformationUiState.MissingOngoingMovingFlow)
    }
  }

  @Test
  fun `a house flow starts with empty, unvalidated inputs`() = runTest {
    val repository = repositoryWith(testMovingFlowStateWithPropertyInput(HousingType.Villa))

    presenter(repository).test(AddHouseInformationUiState.Loading) {
      val content = awaitContent()
      assertThat(content.moveFromAddressId).isEqualTo(TEST_MOVE_INTENT_ID)
      assertThat(content.isLoadingNextStep).isEqualTo(false)
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.shouldDisableInput).isEqualTo(false)
      with(content.addressInput) {
        assertThat(yearOfConstruction.value).isNull()
        assertThat(yearOfConstruction.validationError).isNull()
        assertThat(ancillaryArea.value).isNull()
        assertThat(numberOfBathrooms.value).isEqualTo(1)
        assertThat(isSublet.value).isEqualTo(false)
        assertThat(possibleExtraBuildingTypes.map { it.type }).containsExactly("GARAGE")
        assertThat(extraBuildings.value).isEmpty()
      }
    }
  }

  @Test
  fun `submitting incomplete info shows validation errors and stays on the screen`() = runTest {
    val repository = repositoryWith(testMovingFlowStateWithPropertyInput(HousingType.Villa))
    val backstack = TestBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      val content = awaitContent()
      content.addressInput.yearOfConstruction.updateValue(1850)

      sendEvent(AddHouseInformationEvent.Submit)
      awaitCondition { content.addressInput.ancillaryArea.validationError != null }

      assertThat(content.addressInput.yearOfConstruction.validationError)
        .isEqualTo(AddHouseInformationValidationError.InvalidYearOfConstruction.TooEarly)
      assertThat(content.addressInput.ancillaryArea.validationError)
        .isEqualTo(AddHouseInformationValidationError.MissingAncillaryArea)
      cancelAndIgnoreRemainingEvents()
    }
    assertThat(backstack.entries).isEqualTo(emptyList())
  }

  @Test
  fun `a missing year of construction is reported as missing`() = runTest {
    val repository = repositoryWith(testMovingFlowStateWithPropertyInput(HousingType.Villa))

    presenter(repository).test(AddHouseInformationUiState.Loading) {
      val content = awaitContent()
      content.addressInput.ancillaryArea.updateValue(15)

      sendEvent(AddHouseInformationEvent.Submit)
      awaitCondition { content.addressInput.yearOfConstruction.validationError != null }

      assertThat(content.addressInput.yearOfConstruction.validationError)
        .isEqualTo(AddHouseInformationValidationError.InvalidYearOfConstruction.Missing)
      assertThat(content.addressInput.ancillaryArea.validationError).isNull()
    }
  }

  @Test
  fun `submitting valid info requests quotes, stores them and continues to coverage selection`() = runTest {
    apolloClient.registerTestResponse(
      expectedRequest,
      moveIntentRequestData(homeQuotes = listOf(testMoveHomeQuote(id = "quote"))),
    )
    val repository = repositoryWith(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )
    val backstack = TestBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      awaitCondition { backstack.entries.isNotEmpty() }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).containsExactly(ChoseCoverageLevelAndDeductibleKey(TEST_MOVE_INTENT_ID))
    val state = repository.movingFlowState().first()
    assertThat(state?.movingFlowQuotes?.homeQuotes?.map { it.id }).isEqualTo(listOf("quote"))
    val house = state?.propertyState as MovingFlowState.PropertyState.HouseState
    assertThat(house.yearOfConstruction).isEqualTo(1990)
    assertThat(house.ancillaryArea).isEqualTo(15)
  }

  @Test
  fun `a network failure is shown and can be dismissed`() = runTest {
    apolloClient.registerTestResponse(expectedRequest, errors = listOf(Error.Builder("error").build()))
    val repository = repositoryWith(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )
    val backstack = TestBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      val failure = awaitFailure()
      assertThat(failure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      sendEvent(AddHouseInformationEvent.DismissSubmissionError)
      assertThat(awaitItem()).isInstanceOf<Content>().prop(Content::submittingInfoFailure).isNull()
    }
    assertThat(backstack.entries).isEqualTo(emptyList())
  }

  @Test
  fun `a user error is shown with its message`() = runTest {
    apolloClient.registerTestResponse(
      expectedRequest,
      MoveIntentV2RequestMutation.Data(OctopusFakeResolver) {
        moveIntentRequest = buildMoveIntentMutationOutput {
          moveIntent = null
          userError = buildUserError { message = "Address not supported" }
        }
      },
    )
    val repository = repositoryWith(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )

    presenter(repository).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)

      assertThat(awaitFailure()).isEqualTo(SubmittingInfoFailure.UserError("Address not supported"))
    }
  }

  private suspend fun AddHouseInformationTestContext.awaitFailure(): SubmittingInfoFailure {
    while (true) {
      val content = awaitItem() as Content
      if (content.submittingInfoFailure != null && !content.isLoadingNextStep) {
        assertThat(content.shouldDisableInput).isEqualTo(true)
        return content.submittingInfoFailure
      }
    }
  }
}
