package com.hedvig.android.feature.claimtriaging.claimgroups

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
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOptionId
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.type.FlowClaimItemBrandInput
import octopus.type.FlowClaimItemModelInput
import org.junit.Rule
import org.junit.Test

class ClaimGroupsPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `initial state starts loading and fetches claim groups`() = runTest {
    val claimGroups = listOf(
      ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
      ClaimGroup(ClaimGroupId("2"), "Group 2", listOf()),
    )
    val responseTurbine = Turbine<Either<ErrorMessage, List<ClaimGroup>>>()
    val presenter = ClaimGroupsPresenter({ responseTurbine.awaitItem() }, FakeClaimFlowRepository())

    presenter.test(ClaimGroupsUiState()) {
      val loadingState = awaitItem()
      assertThat(loadingState.isLoading).isTrue()

      responseTurbine.add(claimGroups.right())

      val loadedState = awaitItem()
      assertThat(loadedState.claimGroups).isEqualTo(claimGroups)
      assertThat(loadedState.isLoading).isFalse()
      assertThat(loadedState.selectedClaimGroup).isNull()
    }
  }

  @Test
  fun `state preservation skips loading when claim groups already present`() = runTest {
    val claimGroups = listOf(
      ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
    )
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    val preservedState = ClaimGroupsUiState(
      claimGroups = claimGroups,
      selectedClaimGroup = claimGroups[0],
      isLoading = false,
    )

    presenter.test(preservedState) {
      val state = awaitItem()
      // State should be preserved without loading
      assertThat(state.claimGroups).isEqualTo(claimGroups)
      assertThat(state.selectedClaimGroup).isEqualTo(claimGroups[0])
      assertThat(state.isLoading).isFalse()
    }
  }

  @Test
  fun `loading claim groups with error shows error message`() = runTest {
    val responseTurbine = Turbine<Either<ErrorMessage, List<ClaimGroup>>>()
    val presenter = ClaimGroupsPresenter({ responseTurbine.awaitItem() }, FakeClaimFlowRepository())

    presenter.test(ClaimGroupsUiState()) {
      awaitItem() // loading state

      responseTurbine.add(ErrorMessage("Network error").left())

      val errorState = awaitItem()
      assertThat(errorState.chipLoadingErrorMessage).isEqualTo("Network error")
      assertThat(errorState.isLoading).isFalse()
      assertThat(errorState.canContinue).isFalse()
    }
  }

  @Test
  fun `retry loading claim groups fetches again`() = runTest {
    val claimGroups = listOf(
      ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
    )
    val responseTurbine = Turbine<Either<ErrorMessage, List<ClaimGroup>>>()
    val presenter = ClaimGroupsPresenter({ responseTurbine.awaitItem() }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        chipLoadingErrorMessage = "Previous error",
        isLoading = false,
      ),
    ) {
      awaitItem() // initial state

      sendEvent(ClaimGroupsEvent.LoadClaimGroups)

      val loadingState = awaitItem()
      assertThat(loadingState.isLoading).isTrue()

      responseTurbine.add(claimGroups.right())

      val loadedState = awaitItem()
      assertThat(loadedState.claimGroups).isEqualTo(claimGroups)
      assertThat(loadedState.chipLoadingErrorMessage).isNull()
      assertThat(loadedState.isLoading).isFalse()
    }
  }

  @Test
  fun `selecting a claim group updates the selected claim group`() = runTest {
    val claimGroups = listOf(
      ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
      ClaimGroup(ClaimGroupId("2"), "Group 2", listOf()),
    )
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = claimGroups,
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().selectedClaimGroup).isNull()

      sendEvent(ClaimGroupsEvent.SelectClaimGroup(claimGroups[0]))

      assertThat(awaitItem().selectedClaimGroup).isEqualTo(claimGroups[0])
    }
  }

  @Test
  fun `selecting a claim group clears the continue without selection error`() = runTest {
    val claimGroups = listOf(
      ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
    )
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = claimGroups,
        haveTriedContinuingWithoutSelection = true,
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()

      sendEvent(ClaimGroupsEvent.SelectClaimGroup(claimGroups[0]))

      val state = awaitItem()
      assertThat(state.selectedClaimGroup).isEqualTo(claimGroups[0])
      assertThat(state.haveTriedContinuingWithoutSelection).isFalse()
    }
  }

  @Test
  fun `continue without selection sets validation error`() = runTest {
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isFalse()

      sendEvent(ClaimGroupsEvent.ContinueWithoutSelection)

      assertThat(awaitItem().haveTriedContinuingWithoutSelection).isTrue()
    }
  }

  @Test
  fun `starting claim flow when already loading does nothing`() = runTest {
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, repository)

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        selectedClaimGroup = ClaimGroup(ClaimGroupId("1"), "Group 1", listOf()),
        isLoading = true,
      ),
    ) {
      awaitItem()

      sendEvent(ClaimGroupsEvent.StartClaimFlow)

      expectNoEvents()
    }
  }

  @Test
  fun `starting claim flow shows loading state`() = runTest {
    val claimGroup = ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, repository)

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(claimGroup),
        selectedClaimGroup = claimGroup,
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().isLoading).isFalse()

      sendEvent(ClaimGroupsEvent.StartClaimFlow)

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
    val claimGroup = ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, repository)

    val expectedStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test-flow"))

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(claimGroup),
        selectedClaimGroup = claimGroup,
        isLoading = false,
      ),
    ) {
      awaitItem()

      sendEvent(ClaimGroupsEvent.StartClaimFlow)
      awaitItem() // loading state

      repository.startClaimFlowResponse.add(expectedStep.right())

      val successState = awaitItem()
      assertThat(successState.isLoading).isFalse()
      assertThat(successState.nextStep).isEqualTo(expectedStep)
    }
  }

  @Test
  fun `starting claim flow with error shows error message`() = runTest {
    val claimGroup = ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())
    val repository = FakeClaimFlowRepository()
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, repository)

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(claimGroup),
        selectedClaimGroup = claimGroup,
        isLoading = false,
      ),
    ) {
      awaitItem()

      sendEvent(ClaimGroupsEvent.StartClaimFlow)
      awaitItem() // loading state

      repository.startClaimFlowResponse.add(ErrorMessage("Network error").left())

      val errorState = awaitItem()
      assertThat(errorState.isLoading).isFalse()
      assertThat(errorState.startClaimErrorMessage).isEqualTo("Network error")
      assertThat(errorState.canContinue).isFalse()
    }
  }

  @Test
  fun `dismissing start claim error clears error message`() = runTest {
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        startClaimErrorMessage = "Some error",
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().startClaimErrorMessage).isNotNull()

      sendEvent(ClaimGroupsEvent.DismissStartClaimError)

      assertThat(awaitItem().startClaimErrorMessage).isNull()
    }
  }

  @Test
  fun `handling next step navigation clears next step`() = runTest {
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())
    val nextStep = ClaimFlowStep.ClaimSuccessStep(FlowId("test"))

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        nextStep = nextStep,
        isLoading = false,
      ),
    ) {
      assertThat(awaitItem().nextStep).isNotNull()

      sendEvent(ClaimGroupsEvent.HandledNextStepNavigation)

      assertThat(awaitItem().nextStep).isNull()
    }
  }

  @Test
  fun `canContinue is true when not loading and no errors`() = runTest {
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        isLoading = false,
        chipLoadingErrorMessage = null,
        startClaimErrorMessage = null,
        nextStep = null,
      ),
    ) {
      assertThat(awaitItem().canContinue).isTrue()
    }
  }

  @Test
  fun `canContinue is false when loading`() = runTest {
    val presenter = ClaimGroupsPresenter({ error("Should not be called") }, FakeClaimFlowRepository())

    presenter.test(
      ClaimGroupsUiState(
        claimGroups = listOf(ClaimGroup(ClaimGroupId("1"), "Group 1", listOf())),
        isLoading = true,
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
