package com.hedvig.android.feature.claimtriaging.claimentrypointoptions

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
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.molecule.test.test
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import org.junit.Test

class ClaimEntryPointOptionsPresenterTest {
  private val testEntryPointId = EntryPointId("test-entry-point")

  @Test
  fun `initial state shows entry point options from constructor`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
      EntryPointOption(EntryPointOptionId("2"), "Option 2"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointOptionsUiState(options)) {
      val state = awaitItem()
      assertThat(state.entryPointOptions).isEqualTo(options)
      assertThat(state.selectedEntryPointOption).isNull()
      assertThat(state.haveTriedContinuingWithoutSelection).isFalse()
      assertThat(state.isLoading).isFalse()
      assertThat(state.canContinue).isTrue()
    }
  }

  @Test
  fun `selecting an entry point option updates the selected option`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
      EntryPointOption(EntryPointOptionId("2"), "Option 2"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointOptionsUiState(options)) {
      assertThat(awaitItem().selectedEntryPointOption).isNull()

      sendEvent(ClaimEntryPointOptionsEvent.SelectEntryPointOption(options[0]))

      assertThat(awaitItem().selectedEntryPointOption).isEqualTo(options[0])
    }
  }

  @Test
  fun `selecting an entry point option clears the continue without selection error`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        haveTriedContinuingWithoutSelection = true,
      ),
    ) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()

      sendEvent(ClaimEntryPointOptionsEvent.SelectEntryPointOption(options[0]))

      val state = awaitItem()
      assertThat(state.selectedEntryPointOption).isEqualTo(options[0])
      assertThat(state.haveTriedContinuingWithoutSelection).isFalse()
    }
  }

  @Test
  fun `continue without selection sets validation error`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    presenter.test(ClaimEntryPointOptionsUiState(options)) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isFalse()

      sendEvent(ClaimEntryPointOptionsEvent.ContinueWithoutSelection)

      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()
    }
  }

  @Test
  fun `starting claim flow when no option selected does nothing`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, repository)

    presenter.test(ClaimEntryPointOptionsUiState(options)) {
      awaitItem()

      sendEvent(ClaimEntryPointOptionsEvent.StartClaimFlow)

      expectNoEvents()
    }
  }

  @Test
  fun `starting claim flow when already loading does nothing`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, repository)

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        selectedEntryPointOption = options[0],
        isLoading = true,
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointOptionsEvent.StartClaimFlow)

      expectNoEvents()
    }
  }

  @Test
  fun `starting claim flow shows loading state`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, repository)

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        selectedEntryPointOption = options[0],
      ),
    ) {
      assertThat(awaitItem().isLoading).isFalse()

      sendEvent(ClaimEntryPointOptionsEvent.StartClaimFlow)

      val loadingState = awaitItem()
      assertThat(loadingState.isLoading).isTrue()
      assertThat(loadingState.canContinue).isFalse()

      repository.startClaimFlowResponse.add(
        ClaimFlowStep.ClaimSuccessStep(FlowId("test")).right(),
      )

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `starting claim flow successfully sets next step`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, repository)

    val expectedStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test-flow"))

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        selectedEntryPointOption = options[0],
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointOptionsEvent.StartClaimFlow)
      awaitItem()

      repository.startClaimFlowResponse.add(expectedStep.right())

      val successState = awaitItem()
      assertThat(successState.isLoading).isFalse()
      assertThat(successState.nextStep).isEqualTo(expectedStep)
    }
  }

  @Test
  fun `starting claim flow with error shows error message`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, repository)

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        selectedEntryPointOption = options[0],
      ),
    ) {
      awaitItem()

      sendEvent(ClaimEntryPointOptionsEvent.StartClaimFlow)
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
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        startClaimErrorMessage = "Some error",
      ),
    ) {
      assertThat(awaitItem().startClaimErrorMessage).isNotNull()

      sendEvent(ClaimEntryPointOptionsEvent.DismissStartClaimError)

      assertThat(awaitItem().startClaimErrorMessage).isNull()
    }
  }

  @Test
  fun `handling next step navigation clears next step`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())
    val nextStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test"))

    presenter.test(
      ClaimEntryPointOptionsUiState(
        entryPointOptions = options,
        nextStep = nextStep,
      ),
    ) {
      assertThat(awaitItem().nextStep).isNotNull()

      sendEvent(ClaimEntryPointOptionsEvent.HandledNextStepNavigation)

      assertThat(awaitItem().nextStep).isNull()
    }
  }

  @Test
  fun `state preservation works correctly when going back`() = runTest {
    val options = listOf(
      EntryPointOption(EntryPointOptionId("1"), "Option 1"),
      EntryPointOption(EntryPointOptionId("2"), "Option 2"),
    )
    val presenter = ClaimEntryPointOptionsPresenter(testEntryPointId, options, FakeClaimFlowRepository())

    val preservedState = ClaimEntryPointOptionsUiState(
      entryPointOptions = options,
      selectedEntryPointOption = options[1],
      haveTriedContinuingWithoutSelection = false,
    )

    presenter.test(preservedState) {
      val initialState = awaitItem()
      assertThat(initialState.selectedEntryPointOption).isEqualTo(options[1])
      assertThat(initialState.entryPointOptions).isEqualTo(options)
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
