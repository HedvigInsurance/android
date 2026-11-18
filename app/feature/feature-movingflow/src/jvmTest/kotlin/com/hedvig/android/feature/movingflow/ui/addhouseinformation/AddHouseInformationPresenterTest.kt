package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
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
import com.hedvig.android.feature.movingflow.AddHouseInformationKey
import com.hedvig.android.feature.movingflow.ChoseCoverageLevelAndDeductibleKey
import com.hedvig.android.feature.movingflow.EnterNewAddressKey
import com.hedvig.android.feature.movingflow.FakeMoveIntentRequestTransport
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.InMemoryPreferencesDataStore
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
import com.hedvig.android.molecule.test.FrameTestContext
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import com.hedvig.android.navigation.common.HedvigNavKey
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

private typealias AddHouseInformationFrames = FrameTestContext<AddHouseInformationEvent, AddHouseInformationUiState>

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

  /** The stack the flow runs on when this step is shown: housing type, new address, then this step. */
  private val flowStack: List<HedvigNavKey> = listOf(
    HousingTypeKey(TEST_MOVE_INTENT_ID),
    EnterNewAddressKey(TEST_MOVE_INTENT_ID),
    AddHouseInformationKey(TEST_MOVE_INTENT_ID),
  )
  private val coverageKey = ChoseCoverageLevelAndDeductibleKey(TEST_MOVE_INTENT_ID)

  private fun flowBackstack() = TestBackstack(flowStack.toMutableList())

  private fun presenter(repository: MovingFlowRepository, backstack: TestBackstack = flowBackstack()) =
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
    val backstack = flowBackstack()

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
    assertThat(backstack.entries).isEqualTo(flowStack)
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
    val backstack = flowBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      awaitCondition { backstack.entries.size > flowStack.size }
      cancelAndIgnoreRemainingEvents()
    }

    assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
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
    val backstack = flowBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      val failure = awaitFailure()
      assertThat(failure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      sendEvent(AddHouseInformationEvent.DismissSubmissionError)
      assertThat(awaitItem()).isInstanceOf<Content>().prop(Content::submittingInfoFailure).isNull()
    }
    assertThat(backstack.entries).isEqualTo(flowStack)
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

    val backstack = flowBackstack()

    presenter(repository, backstack).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)

      assertThat(awaitFailure()).isEqualTo(SubmittingInfoFailure.UserError("Address not supported"))
    }
    assertThat(backstack.entries).isEqualTo(flowStack)
  }

  private val requestTransport = FakeMoveIntentRequestTransport()
  private val quotes = moveIntentRequestData(homeQuotes = listOf(testMoveHomeQuote(id = "quote")))

  private suspend fun inMemoryRepository(): MovingFlowRepository {
    val storage = MovingFlowStorage(InMemoryPreferencesDataStore())
    storage.setMovingFlowState(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )
    return MovingFlowRepository(storage)
  }

  private fun framePresenter(repository: MovingFlowRepository, backstack: TestBackstack) =
    AddHouseInformationPresenter(TEST_MOVE_INTENT_ID, repository, requestTransport.apolloClient(), backstack)

  private val AddHouseInformationFrames.content: Content
    get() = latest as Content

  private fun AddHouseInformationFrames.showFilledInForm() {
    frame()
    frame()
    content.fillInValidHouseInfo()
  }

  private fun AddHouseInformationFrames.continueUntilInFlight() {
    showFilledInForm()
    emit(AddHouseInformationEvent.Submit)
    frame()
    assertThat(content.isLoadingNextStep).isTrue()
  }

  @Test
  fun `frames, a success pushes the coverage step once on top of the flow and enables the form again`() = runTest {
    val repository = inMemoryRepository()
    val backstack = flowBackstack()
    framePresenter(repository, backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      assertThat(requestTransport.inputs).containsExactly(expectedRequest.moveIntentRequestInput)
      assertThat(backstack.entries).isEqualTo(flowStack)

      requestTransport.responses.trySend(quotes)
      runCurrent()
      // The step is pushed as soon as the response lands, before the presenter produces its next state.
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
      assertThat(content.isLoadingNextStep).isTrue()
      frame()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
      frame()
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
      assertThat(requestTransport.inputs).hasSize(1)
      val stored = repository.movingFlowState().first()
      assertThat(stored?.movingFlowQuotes?.homeQuotes?.map { it.id }).isEqualTo(listOf("quote"))
    }
  }

  @Test
  fun `frames, a failure keeps the back stack, and a retry after dismissing sends the request again`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(backstack.entries).isEqualTo(flowStack)

      emit(AddHouseInformationEvent.DismissSubmissionError)
      frame()
      assertThat(content.submittingInfoFailure).isNull()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(content.isLoadingNextStep).isTrue()
      assertThat(requestTransport.inputs).containsExactly(
        expectedRequest.moveIntentRequestInput,
        expectedRequest.moveIntentRequestInput,
      )

      requestTransport.responses.trySend(quotes)
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    }
  }

  @Test
  fun `frames, an equal retry handled before the frame after a failure is never sent and loads forever`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      // The failure and the retry are both handled before the next frame composes the presenter.
      requestTransport.responses.trySend(null)
      runCurrent()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      // Nothing is in flight, yet the step stays loading, and a later continue is absorbed as well.
      emit(AddHouseInformationEvent.DismissSubmissionError)
      frame()
      frame()
      assertThat(content.isLoadingNextStep).isTrue()
      emit(AddHouseInformationEvent.Submit)
      frame()
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()
      assertThat(backstack.entries).isEqualTo(flowStack)
    }
  }

  @Test
  fun `frames, a continue while the request is in flight with the same input sends nothing more`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()

      requestTransport.responses.trySend(quotes)
      frame()
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    }
  }

  @Test
  fun `frames, a continue with changed input while the request is in flight cancels it and sends the new one`() =
    runTest {
      val backstack = flowBackstack()
      framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
        continueUntilInFlight()
        content.addressInput.ancillaryArea.updateValue(30)
        emit(AddHouseInformationEvent.Submit)
        frame()
        assertThat(requestTransport.inputs).hasSize(2)
        assertThat(requestTransport.cancelled).isEqualTo(1)
        assertThat(requestTransport.inputs.last().house.getOrThrow()?.ancillaryArea).isEqualTo(30)

        requestTransport.responses.trySend(quotes)
        frame()
        frame()
        assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
      }
    }

  @Test
  fun `frames, two continues in one frame send one request`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      showFilledInForm()
      emit(AddHouseInformationEvent.Submit, AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()

      requestTransport.responses.trySend(quotes)
      frame()
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    }
  }

  @Test
  fun `frames, a continue before the flow loaded is dropped`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      emit(AddHouseInformationEvent.Submit)
      frame()
      frame()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(requestTransport.inputs).isEmpty()
      assertThat(backstack.entries).isEqualTo(flowStack)
    }
  }

  @Test
  fun `frames, a continue while the failure is shown sends the request and keeps the failure shown`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      assertThat(content.isLoadingNextStep).isTrue()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)
      assertThat(backstack.entries).isEqualTo(flowStack)
    }
  }

  @Test
  fun `frames, continuing again after coming back from the coverage step requests and pushes it again`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(quotes)
      frame()
      backstack.entries.removeLast()

      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      requestTransport.responses.trySend(quotes)
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    }
  }

  @Test
  fun `frames, a continue right after the coverage step was pushed pushes it a second time`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(quotes)
      frame()

      // The member taps continue again while the coverage step is still animating in.
      emit(AddHouseInformationEvent.Submit)
      frame()
      requestTransport.responses.trySend(quotes)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey + coverageKey)
    }
  }

  @Test
  fun `frames, a restart before the flow loaded shows the form`() = runTest {
    framePresenter(inMemoryRepository(), flowBackstack()).testFrames(AddHouseInformationUiState.Loading) {
      restart()
      frame()
      frame()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
    }
  }

  @Test
  fun `frames, a restart replaces input typed but not continued with the stored input`() = runTest {
    framePresenter(inMemoryRepository(), flowBackstack()).testFrames(AddHouseInformationUiState.Loading) {
      frame()
      frame()
      content.addressInput.ancillaryArea.updateValue(15)
      frame()
      assertThat(content.addressInput.ancillaryArea.value).isEqualTo(15)

      restart()
      frame()
      frame()
      assertThat(content.addressInput.ancillaryArea.value).isNull()
    }
  }

  @Test
  fun `frames, a restart while the request is in flight cancels it and shows the form ready to continue`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      restart()
      frame()
      frame()
      assertThat(requestTransport.cancelled).isEqualTo(1)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.addressInput.ancillaryArea.value).isEqualTo(15)
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(backstack.entries).isEqualTo(flowStack)

      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      requestTransport.responses.trySend(quotes)
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    }
  }

  @Test
  fun `frames, a restart after a failure drops the failure`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      restart()
      frame()
      frame()
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(backstack.entries).isEqualTo(flowStack)
    }
  }

  @Test
  fun `frames, a restart after the coverage step was pushed does not push it again`() = runTest {
    val backstack = flowBackstack()
    framePresenter(inMemoryRepository(), backstack).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(quotes)
      frame()

      restart()
      frame()
      frame()
      assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(requestTransport.inputs).hasSize(1)
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
