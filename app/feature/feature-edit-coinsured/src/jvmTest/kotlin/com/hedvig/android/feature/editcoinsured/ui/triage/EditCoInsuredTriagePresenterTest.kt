package com.hedvig.android.feature.editcoinsured.ui.triage

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.Assert
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.Reload
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.SelectInsurance
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Redirect
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import com.hedvig.android.molecule.test.FrameTestContext
import com.hedvig.android.molecule.test.testFrames
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * What [EditCoInsuredTriagePresenter] shows and which step it redirects to. The back stack each redirect and each
 * Continue leaves, per entry path, is in [EditCoInsuredTriageNavigationTest].
 */
internal class EditCoInsuredTriagePresenterTest {
  private val useCase = FakeGetInsurancesForEditCoInsuredUseCase()

  private fun presenter(insuranceId: String? = null, type: CoInsuredFlowType = CoInsuredFlowType.CoOwners) =
    EditCoInsuredTriagePresenter(useCase, insuranceId, type)

  private fun FrameTestContext<EditCoInsuredTriageEvent, EditCoInsuredTriageUiState>.load(
    result: Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>,
  ) {
    useCase.results.trySend(result)
    frame()
  }

  private fun picker(selected: InsuranceForEditOrAddCoInsured? = null) =
    Success(list = both, selected = selected, type = CoInsuredFlowType.CoOwners)

  private fun Assert<EditCoInsuredTriageUiState>.isPendingRedirectTo(insurance: InsuranceForEditOrAddCoInsured) =
    isInstanceOf<Redirect>().prop(Redirect::step).given { step ->
      assertThat(step.target).isEqualTo(insurance)
      assertThat(step.isPending).isTrue()
    }

  // Redirect after load

  @Test
  fun `one option redirects to it without showing the picker`() = runTest {
    presenter().testFrames(Loading) {
      load(listOf(addOrRemove).right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
      assertThat(observed.size).isEqualTo(2)
      assertThat(observed.first()).isEqualTo(Loading)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a contract among several redirects to it`() = runTest {
    presenter(insuranceId = "contract-b").testFrames(Loading) {
      load(both.right())
      assertThat(latest).isPendingRedirectTo(missingInfo)
      assertThat(observed.first()).isEqualTo(Loading)
      assertThat(observed.size).isEqualTo(2)
    }
  }

  @Test
  fun `the co-insured flow loads co-insured insurances and redirects to the contract`() = runTest {
    presenter(insuranceId = "contract-a", type = CoInsuredFlowType.CoInsured).testFrames(Loading) {
      load(both.right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoInsured)
    }
  }

  // The picker

  @Test
  fun `several options and no contract show the picker`() = runTest {
    presenter().testFrames(Loading) {
      load(both.right())
      assertThat(latest).isEqualTo(picker())
    }
  }

  @Test
  fun `a contract that is not among the options shows the picker with every option`() = runTest {
    presenter(insuranceId = "contract-unknown").testFrames(Loading) {
      load(both.right())
      assertThat(latest).isEqualTo(picker())
    }
  }

  @Test
  fun `one option that does not match the contract shows a picker of one`() = runTest {
    presenter(insuranceId = "contract-unknown").testFrames(Loading) {
      load(listOf(addOrRemove).right())
      assertThat(latest).isEqualTo(Success(listOf(addOrRemove), null, CoInsuredFlowType.CoOwners))
    }
  }

  @Test
  fun `no options show an empty picker`() = runTest {
    presenter().testFrames(Loading) {
      load(emptyList<InsuranceForEditOrAddCoInsured>().right())
      assertThat(latest).isEqualTo(Success(emptyList(), null, CoInsuredFlowType.CoOwners))
    }
  }

  @Test
  fun `selecting an option shows it selected`() = runTest {
    presenter().testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-b"))
      frame()
      assertThat(latest).isEqualTo(picker(selected = missingInfo))
    }
  }

  // Loading and failure

  @Test
  fun `a failed load shows the failure, and reload loads again`() = runTest {
    presenter().testFrames(Loading) {
      load(ErrorMessage().left())
      assertThat(latest).isEqualTo(Failure)
      emit(Reload)
      // The reload restarts the load effect in this frame; the effect shows loading in the next one.
      frame()
      frame()
      assertThat(latest).isEqualTo(Loading)
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(observed).containsExactly(Loading, Failure, Loading, picker())
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a reload that ends in one option redirects`() = runTest {
    presenter().testFrames(Loading) {
      load(ErrorMessage().left())
      emit(Reload)
      frame()
      load(listOf(addOrRemove).right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
    }
  }

  @Test
  fun `a reload from the picker shows loading and keeps the selection`() = runTest {
    presenter().testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-b"))
      frame()
      emit(Reload)
      // The reload restarts the load effect in this frame; the effect shows loading in the next one.
      frame()
      frame()
      assertThat(latest).isEqualTo(Loading)
      load(both.right())
      assertThat(latest).isEqualTo(picker(selected = missingInfo))
    }
  }

  @Test
  fun `a reload while loading starts a second load and the first result is used by it`() = runTest {
    presenter().testFrames(Loading) {
      emit(Reload)
      frame()
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }

  // After a redirect

  @Test
  fun `every event after a redirect is ignored`() = runTest {
    presenter().testFrames(Loading) {
      load(listOf(addOrRemove).right())
      val redirect = latest
      emit(Reload, SelectInsurance("contract-a"))
      frame()
      frame()
      assertThat(latest).isSameInstanceAs(redirect)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners)
    }
  }

  // Restart from the last state

  @Test
  fun `a restart while loading loads again and redirects`() = runTest {
    presenter().testFrames(Loading) {
      frame()
      restart()
      load(listOf(addOrRemove).right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a restart from the picker shows it, then loading, then the reloaded picker with the selection kept`() = runTest {
    presenter().testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-b"))
      frame()
      restart()
      frame()
      assertThat(latest).isEqualTo(Loading)
      load(both.right())
      assertThat(latest).isEqualTo(picker(selected = missingInfo))
      assertThat(observed).containsExactly(
        Loading,
        picker(),
        picker(selected = missingInfo),
        // The new collection receives the restored state again.
        picker(selected = missingInfo),
        Loading,
        picker(selected = missingInfo),
      )
    }
  }

  @Test
  fun `a restart from the picker that reloads one option redirects`() = runTest {
    presenter().testFrames(Loading) {
      load(both.right())
      restart()
      load(listOf(addOrRemove).right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
    }
  }

  @Test
  fun `a restart from the failure loads again`() = runTest {
    presenter().testFrames(Loading) {
      load(ErrorMessage().left())
      restart()
      frame()
      assertThat(latest).isEqualTo(Loading)
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(useCase.calls.size).isEqualTo(2)
    }
  }

  @Test
  fun `a restart before the redirect was performed keeps it pending without loading again`() = runTest {
    presenter().testFrames(Loading) {
      load(listOf(addOrRemove).right())
      val redirect = latest as Redirect
      restart()
      frame()
      assertThat(latest).isSameInstanceAs(redirect)
      assertThat(redirect.step.isPending).isTrue()
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a restart after the redirect was performed loads again and offers a new redirect`() = runTest {
    presenter().testFrames(Loading) {
      load(listOf(addOrRemove).right())
      val redirect = latest as Redirect
      assertThat(redirect.step.tryConsume()).isTrue()
      restart()
      frame()
      frame()
      assertThat(latest).isEqualTo(Loading)
      load(listOf(addOrRemove).right())
      assertThat(latest).isPendingRedirectTo(addOrRemove)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }
}

internal class FakeGetInsurancesForEditCoInsuredUseCase : GetInsurancesForEditCoInsuredUseCase {
  val calls = mutableListOf<CoInsuredFlowType>()
  val results = Channel<Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>>(Channel.UNLIMITED)

  override suspend fun invoke(type: CoInsuredFlowType): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>> {
    calls += type
    return results.receive()
  }
}

internal val addOrRemove = InsuranceForEditOrAddCoInsured(
  id = "contract-a",
  destination = EditCoInsuredDestination.ADD_OR_REMOVE,
  displayName = "Home insurance",
  exposureName = "Street 1",
  type = CoInsuredFlowType.CoOwners,
)
internal val missingInfo = InsuranceForEditOrAddCoInsured(
  id = "contract-b",
  destination = EditCoInsuredDestination.MISSING_INFO,
  displayName = "Home insurance",
  exposureName = "Street 2",
  type = CoInsuredFlowType.CoOwners,
)
internal val both = listOf(addOrRemove, missingInfo)
