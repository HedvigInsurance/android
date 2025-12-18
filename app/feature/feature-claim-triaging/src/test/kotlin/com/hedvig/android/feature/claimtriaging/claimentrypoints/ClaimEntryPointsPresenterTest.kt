package com.hedvig.android.feature.claimtriaging.claimentrypoints

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.data.claimflow.model.FlowId
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.molecule.test.test
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import org.junit.Test

class ClaimEntryPointsPresenterTest {
  @Test
  fun `initial state shows entry points from constructor`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
      EntryPoint(EntryPointId("2"), "Entry Point 2", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointsUiState(entryPoints)) {
      val state = awaitItem()
      assertThat(state.entryPoints).isEqualTo(entryPoints)
      assertThat(state.selectedEntryPoint).isNull()
      assertThat(state.haveTriedContinuingWithoutSelection).isFalse()
      assertThat(state.isLoading).isFalse()
      assertThat(state.canContinue).isTrue()
    }
  }

  @Test
  fun `selecting an entry point updates the selected entry point`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
      EntryPoint(EntryPointId("2"), "Entry Point 2", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointsUiState(entryPoints)) {
      assertThat(awaitItem().selectedEntryPoint).isNull()

      sendEvent(ClaimEntryPointsEvent.SelectEntryPoint(entryPoints[0]))

      assertThat(awaitItem().selectedEntryPoint).isEqualTo(entryPoints[0])
    }
  }

  @Test
  fun `selecting an entry point clears the continue without selection error`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        haveTriedContinuingWithoutSelection = true,
      ),
    ) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()

      sendEvent(ClaimEntryPointsEvent.SelectEntryPoint(entryPoints[0]))

      val state = awaitItem()
      assertThat(state.selectedEntryPoint).isEqualTo(entryPoints[0])
      assertThat(state.haveTriedContinuingWithoutSelection).isFalse()
    }
  }

  @Test
  fun `continue without selection sets validation error`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointsUiState(entryPoints)) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isFalse()

      sendEvent(ClaimEntryPointsEvent.ContinueWithoutSelection)

      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()
    }
  }

  @Test
  fun `starting claim flow when no entry point selected does nothing`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointsPresenter(entryPoints, repository)

    presenter.test(ClaimEntryPointsUiState(entryPoints)) {
      awaitItem()

      sendEvent(ClaimEntryPointsEvent.StartClaimFlow)

      // Should not call repository since no entry point is selected
      expectNoEvents()
    }
  }

  @Test
  fun `starting claim flow when already loading does nothing`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointsPresenter(entryPoints, repository)

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        selectedEntryPoint = entryPoints[0],
        isLoading = true,
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointsEvent.StartClaimFlow)

      // Should not change state since already loading
      expectNoEvents()
    }
  }

  @Test
  fun `starting claim flow shows loading state`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointsPresenter(entryPoints, repository)

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        selectedEntryPoint = entryPoints[0],
      ),
    ) {
      assertThat(awaitItem().isLoading).isFalse()

      sendEvent(ClaimEntryPointsEvent.StartClaimFlow)

      val loadingState = awaitItem()
      assertThat(loadingState.isLoading).isTrue()
      assertThat(loadingState.canContinue).isFalse()

      // Provide response to complete the test
      repository.startClaimFlowResponse.add(
        ClaimFlowStep.ClaimSuccessStep(FlowId("test")).right(),
      )

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `starting claim flow successfully sets next step`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointsPresenter(entryPoints, repository)

    val expectedStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test-flow"))

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        selectedEntryPoint = entryPoints[0],
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointsEvent.StartClaimFlow)

      // Skip loading state
      awaitItem()

      repository.startClaimFlowResponse.add(expectedStep.right())

      val successState = awaitItem()
      assertThat(successState.isLoading).isFalse()
      assertThat(successState.nextStep).isEqualTo(expectedStep)
    }
  }

  @Test
  fun `starting claim flow with error shows error message`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointsPresenter(entryPoints, repository)

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        selectedEntryPoint = entryPoints[0],
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointsEvent.StartClaimFlow)

      // Skip loading state
      awaitItem()

      repository.startClaimFlowResponse.add(ErrorMessage("Network error").left())

      val errorState = awaitItem()
      assertThat(errorState.isLoading).isFalse()
      assertThat(errorState.startClaimErrorMessage).isEqualTo("Network error")
      assertThat(errorState.canContinue).isFalse()
    }
  }

  @Test
  fun `dismissing start claim error clears error message`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        startClaimErrorMessage = "Some error",
      ),
    ) {
      assertThat(awaitItem().startClaimErrorMessage).isNotNull()

      sendEvent(ClaimEntryPointsEvent.DismissStartClaimError)

      assertThat(awaitItem().startClaimErrorMessage).isNull()
    }
  }

  @Test
  fun `handling next step navigation clears next step`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())
    val nextStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test"))

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        nextStep = nextStep,
      ),
    ) {
      assertThat(awaitItem().nextStep).isNotNull()

      sendEvent(ClaimEntryPointsEvent.HandledNextStepNavigation)

      assertThat(awaitItem().nextStep).isNull()
    }
  }

  @Test
  fun `state preservation works correctly when going back`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
      EntryPoint(EntryPointId("2"), "Entry Point 2", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    // Simulate state that would be preserved from a previous session
    val preservedState = ClaimEntryPointsUiState(
      entryPoints = entryPoints,
      selectedEntryPoint = entryPoints[1],
      haveTriedContinuingWithoutSelection = false,
    )

    presenter.test(preservedState) {
      val initialState = awaitItem()
      // State should be preserved
      assertThat(initialState.selectedEntryPoint).isEqualTo(entryPoints[1])
      assertThat(initialState.entryPoints).isEqualTo(entryPoints)
    }
  }

  @Test
  fun `canContinue is false when loading`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        isLoading = true,
      ),
    ) {
      assertThat(awaitItem().canContinue).isFalse()
    }
  }

  @Test
  fun `canContinue is false when there is an error message`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        startClaimErrorMessage = "Error",
      ),
    ) {
      assertThat(awaitItem().canContinue).isFalse()
    }
  }

  @Test
  fun `canContinue is false when there is a next step`() = runTest {
    val entryPoints = listOf(
      EntryPoint(EntryPointId("1"), "Entry Point 1", null),
    )
    val presenter = ClaimEntryPointsPresenter(entryPoints, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointsUiState(
        entryPoints = entryPoints,
        nextStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test")),
      ),
    ) {
      assertThat(awaitItem().canContinue).isFalse()
    }
  }
}

private class FakeClaimFlowRepository : ClaimFlowRepository {
  val startClaimFlowResponse = Turbine<Either<ErrorMessage, ClaimFlowStep>>()

  override suspend fun startClaimFlow(
    entryPointId: EntryPointId?,
    entryPointOptionId: EntryPointOptionId?,
  ): Either<ErrorMessage, ClaimFlowStep> {
    return startClaimFlowResponse.awaitItem()
  }

  override suspend fun submitAudioRecording(flowId: FlowId, audioFile: File): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitAudioUrl(flowId: FlowId, audioUrl: AudioUrl): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitDateOfOccurrence(dateOfOccurrence: LocalDate?): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitLocation(location: String?): Either<ErrorMessage, ClaimFlowStep> = error("Not implemented")

  override suspend fun submitFreeTextInsteadOfAudio(freeText: String): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitContract(contract: String?): Either<ErrorMessage, ClaimFlowStep> = error("Not implemented")

  override suspend fun submitDateOfOccurrenceAndLocation(
    dateOfOccurrence: LocalDate?,
    location: String?,
  ): Either<ErrorMessage, ClaimFlowStep> = error("Not implemented")

  override suspend fun submitPhoneNumber(phoneNumber: String): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitSingleItem(
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    customName: String?,
    itemProblemIds: List<String>?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep> = error("Not implemented")

  override suspend fun submitSingleItemCheckout(amount: Double): Either<ErrorMessage, Unit> = error("Not implemented")

  override suspend fun submitSummary(
    dateOfOccurrence: LocalDate?,
    itemBrandInput: FlowClaimItemBrandInput?,
    itemModelInput: FlowClaimItemModelInput?,
    itemProblemIds: List<String>?,
    location: String?,
    purchaseDate: LocalDate?,
    purchasePrice: Double?,
  ): Either<ErrorMessage, ClaimFlowStep> = error("Not implemented")

  override suspend fun submitUrgentEmergency(isUrgentEmergency: Boolean): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")

  override suspend fun submitFiles(fileIds: List<String>): Either<ErrorMessage, ClaimFlowStep> =
    error("Not implemented")
}
