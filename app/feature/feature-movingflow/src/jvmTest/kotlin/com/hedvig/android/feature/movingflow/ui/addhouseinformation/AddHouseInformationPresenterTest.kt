package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
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
import com.hedvig.android.feature.movingflow.FakeMoveIntentRequestTransport
import com.hedvig.android.feature.movingflow.InMemoryPreferencesDataStore
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
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
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.test.FrameTestContext
import com.hedvig.android.molecule.test.MoleculePresenterTestContext
import com.hedvig.android.molecule.test.test
import com.hedvig.android.molecule.test.testFrames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
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

private typealias CoverageStepRequest = NavigationRequest<ChoseCoverageLevelAndDeductibleStep>

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

  private val coverageStep = ChoseCoverageLevelAndDeductibleStep(TEST_MOVE_INTENT_ID)

  private fun presenter(repository: MovingFlowRepository) =
    AddHouseInformationPresenter(TEST_MOVE_INTENT_ID, repository, apolloClient)

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

    presenter(repository).test(AddHouseInformationUiState.Loading) {
      val content = awaitContent()
      content.addressInput.yearOfConstruction.updateValue(1850)

      sendEvent(AddHouseInformationEvent.Submit)
      awaitCondition { content.addressInput.ancillaryArea.validationError != null }

      assertThat(content.addressInput.yearOfConstruction.validationError)
        .isEqualTo(AddHouseInformationValidationError.InvalidYearOfConstruction.TooEarly)
      assertThat(content.addressInput.ancillaryArea.validationError)
        .isEqualTo(AddHouseInformationValidationError.MissingAncillaryArea)
      assertThat(content.next).isNull()
      cancelAndIgnoreRemainingEvents()
    }
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
    presenter(repository).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      val next = awaitNext()
      assertThat(next.target).isEqualTo(coverageStep)
      assertThat(next.isPending).isTrue()
      cancelAndIgnoreRemainingEvents()
    }

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
    presenter(repository).test(AddHouseInformationUiState.Loading) {
      awaitContent().fillInValidHouseInfo()

      sendEvent(AddHouseInformationEvent.Submit)
      val failure = awaitFailure()
      assertThat(failure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      sendEvent(AddHouseInformationEvent.DismissSubmissionError)
      assertThat(awaitItem()).isInstanceOf<Content>().prop(Content::submittingInfoFailure).isNull()
    }
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

  private val requestTransport = FakeMoveIntentRequestTransport()
  private val quotes = moveIntentRequestData(homeQuotes = listOf(testMoveHomeQuote(id = "quote")))

  private suspend fun inMemoryRepository(
    dataStore: InMemoryPreferencesDataStore = InMemoryPreferencesDataStore(),
  ): MovingFlowRepository {
    val storage = MovingFlowStorage(dataStore)
    storage.setMovingFlowState(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )
    return MovingFlowRepository(storage)
  }

  private fun framePresenter(repository: MovingFlowRepository) =
    AddHouseInformationPresenter(TEST_MOVE_INTENT_ID, repository, requestTransport.apolloClient())

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

  /** Answers the request in flight with quotes and returns the step offered for it. */
  private fun AddHouseInformationFrames.succeed(): CoverageStepRequest {
    requestTransport.responses.trySend(quotes)
    frame()
    val next = content.next!!
    assertThat(next.target).isEqualTo(coverageStep)
    assertThat(next.isPending).isTrue()
    return next
  }

  @Test
  fun `frames, a success offers the coverage step once and enables the form again`() = runTest {
    val repository = inMemoryRepository()
    framePresenter(repository).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      assertThat(requestTransport.inputs).containsExactly(expectedRequest.moveIntentRequestInput)
      assertThat(content.next).isNull()

      val next = succeed()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
      val stored = repository.movingFlowState().first()
      assertThat(stored?.movingFlowQuotes?.homeQuotes?.map { it.id }).isEqualTo(listOf("quote"))

      // The platform takes the step; later states keep the same, taken request and offer nothing new.
      assertThat(next.tryConsume()).isTrue()
      frame()
      frame()
      assertThat(content.next).isEqualTo(next)
      assertThat(observed.mapNotNull { (it as? Content)?.next }.distinct()).containsExactly(next)
      assertThat(requestTransport.inputs).hasSize(1)
    }
  }

  @Test
  fun `frames, a failure offers nothing, and a retry after dismissing sends the request again`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.next).isNull()

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

      succeed()
    }
  }

  @Test
  fun `frames, an equal retry handled before the frame after a failure is sent`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      // The failure and the retry are both handled before the next frame composes the presenter.
      requestTransport.responses.trySend(null)
      runCurrent()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).containsExactly(
        expectedRequest.moveIntentRequestInput,
        expectedRequest.moveIntentRequestInput,
      )
      assertThat(content.isLoadingNextStep).isTrue()

      succeed()
      assertThat(content.isLoadingNextStep).isFalse()
    }
  }

  @Test
  fun `frames, a continue while the request is in flight with the same input sends nothing more`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()

      val next = succeed()
      frame()
      assertThat(observed.mapNotNull { (it as? Content)?.next }.distinct()).containsExactly(next)
    }
  }

  @Test
  fun `frames, a continue with changed input while the request is in flight is ignored and cancels nothing`() =
    runTest {
      val repository = inMemoryRepository()
      framePresenter(repository).testFrames(AddHouseInformationUiState.Loading) {
        continueUntilInFlight()
        content.addressInput.ancillaryArea.updateValue(30)
        emit(AddHouseInformationEvent.Submit)
        frame()
        assertThat(requestTransport.inputs).containsExactly(expectedRequest.moveIntentRequestInput)
        assertThat(requestTransport.cancelled).isEqualTo(0)
        assertThat(content.isLoadingNextStep).isTrue()
        // The stored input stays the one the quotes in flight are for.
        val stored = repository.movingFlowState().first()?.propertyState as MovingFlowState.PropertyState.HouseState
        assertThat(stored.ancillaryArea).isEqualTo(15)

        succeed()
      }
    }

  @Test
  fun `frames, two continues in one frame send one request`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      showFilledInForm()
      emit(AddHouseInformationEvent.Submit, AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isTrue()

      val next = succeed()
      frame()
      assertThat(observed.mapNotNull { (it as? Content)?.next }.distinct()).containsExactly(next)
    }
  }

  @Test
  fun `frames, two continues handled in one frame write the input once and send one request`() = runTest {
    val dataStore = InMemoryPreferencesDataStore()
    framePresenter(inMemoryRepository(dataStore)).testFrames(AddHouseInformationUiState.Loading) {
      showFilledInForm()
      val writesBefore = dataStore.writes
      emit(AddHouseInformationEvent.Submit, AddHouseInformationEvent.Submit)
      frame()
      assertThat(dataStore.writes - writesBefore).isEqualTo(1)
      assertThat(requestTransport.inputs).hasSize(1)
    }
  }

  @Test
  fun `frames, a second continue during a slow write is not sent after the first request failed`() = runTest {
    val dataStore = InMemoryPreferencesDataStore()
    val repository = inMemoryRepository(dataStore)
    framePresenter(repository).testFrames(AddHouseInformationUiState.Loading) {
      showFilledInForm()
      val writes = Channel<Unit>(Channel.UNLIMITED)
      dataStore.gate = writes
      emit(AddHouseInformationEvent.Submit, AddHouseInformationEvent.Submit)
      frame()
      // The input is stored before the request is sent.
      assertThat(requestTransport.inputs).isEmpty()
      writes.trySend(Unit)
      frame()
      val stored = repository.movingFlowState().first()?.propertyState as MovingFlowState.PropertyState.HouseState
      assertThat(stored.ancillaryArea).isEqualTo(15)
      assertThat(requestTransport.inputs).containsExactly(expectedRequest.moveIntentRequestInput)

      // The request fails fast, before any further write lands.
      requestTransport.responses.trySend(null)
      frame()
      writes.trySend(Unit)
      writes.trySend(Unit)
      frame()
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)
      assertThat(content.isLoadingNextStep).isFalse()
    }
  }

  @Test
  fun `frames, a continue whose stored flow is gone when the input is written sends nothing`() = runTest {
    val dataStore = InMemoryPreferencesDataStore()
    framePresenter(inMemoryRepository(dataStore)).testFrames(AddHouseInformationUiState.Loading) {
      showFilledInForm()
      val writes = Channel<Unit>(Channel.UNLIMITED)
      dataStore.gate = writes
      emit(AddHouseInformationEvent.Submit)
      frame()
      dataStore.clear()
      writes.trySend(Unit)
      frame()
      frame()
      assertThat(requestTransport.inputs).isEmpty()
      assertThat(latest).isEqualTo(AddHouseInformationUiState.MissingOngoingMovingFlow)
    }
  }

  @Test
  fun `frames, a continue before the flow loaded is dropped`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      emit(AddHouseInformationEvent.Submit)
      frame()
      frame()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.next).isNull()
      assertThat(requestTransport.inputs).isEmpty()
    }
  }

  @Test
  fun `frames, a continue while the failure is shown sends the request and keeps the failure shown`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      assertThat(content.isLoadingNextStep).isTrue()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)
      assertThat(content.next).isNull()
    }
  }

  @Test
  fun `frames, a continue while the offered step is not taken yet is ignored`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      val next = succeed()

      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.next).isEqualTo(next)
      assertThat(next.isPending).isTrue()
    }
  }

  @Test
  fun `frames, continuing again after the step was taken requests the quotes again and offers a new step`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      val first = succeed()
      assertThat(first.tryConsume()).isTrue()

      // Back from the coverage step, or a tap while it is still animating in, then continue.
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      assertThat(content.next).isEqualTo(first)
      val second = succeed()
      assertThat(second).isNotEqualTo(first)
    }
  }

  @Test
  fun `frames, a restart before the flow loaded shows the form`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      restart()
      frame()
      frame()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.next).isNull()
    }
  }

  @Test
  fun `frames, a restart replaces input typed but not continued with the stored input`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
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
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      restart()
      frame()
      frame()
      assertThat(requestTransport.cancelled).isEqualTo(1)
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.next).isNull()
      assertThat(content.addressInput.ancillaryArea.value).isEqualTo(15)
      assertThat(requestTransport.inputs).hasSize(1)

      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(2)
      assertThat(content.isLoadingNextStep).isTrue()
      succeed()
    }
  }

  @Test
  fun `frames, a restart after a failure drops the failure`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      requestTransport.responses.trySend(null)
      frame()
      assertThat(content.submittingInfoFailure).isEqualTo(SubmittingInfoFailure.NetworkFailure)

      restart()
      frame()
      frame()
      assertThat(content.submittingInfoFailure).isNull()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(content.next).isNull()
    }
  }

  @Test
  fun `frames, a restart after the step was taken keeps it taken and offers nothing new`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      val next = succeed()
      assertThat(next.tryConsume()).isTrue()

      restart()
      frame()
      frame()
      assertThat(content.next).isEqualTo(next)
      assertThat(next.isPending).isFalse()
      assertThat(content.isLoadingNextStep).isFalse()
      assertThat(requestTransport.inputs).hasSize(1)
    }
  }

  @Test
  fun `frames, a restart while the step is offered and not taken keeps offering the same step`() = runTest {
    framePresenter(inMemoryRepository()).testFrames(AddHouseInformationUiState.Loading) {
      continueUntilInFlight()
      val next = succeed()

      restart()
      frame()
      frame()
      assertThat(content.next).isEqualTo(next)
      assertThat(next.isPending).isTrue()
      emit(AddHouseInformationEvent.Submit)
      frame()
      assertThat(requestTransport.inputs).hasSize(1)
    }
  }

  private suspend fun AddHouseInformationTestContext.awaitNext(): CoverageStepRequest {
    while (true) {
      val next = (awaitItem() as Content).next
      if (next != null) return next
    }
  }

  private suspend fun AddHouseInformationTestContext.awaitFailure(): SubmittingInfoFailure {
    while (true) {
      val content = awaitItem() as Content
      if (content.submittingInfoFailure != null && !content.isLoadingNextStep) {
        assertThat(content.shouldDisableInput).isEqualTo(true)
        assertThat(content.next).isNull()
        return content.submittingInfoFailure
      }
    }
  }
}
