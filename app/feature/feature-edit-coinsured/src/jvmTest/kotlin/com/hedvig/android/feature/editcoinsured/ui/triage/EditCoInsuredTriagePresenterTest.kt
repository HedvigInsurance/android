package com.hedvig.android.feature.editcoinsured.ui.triage

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.data.EditCoInsuredDestination
import com.hedvig.android.feature.editcoinsured.data.GetInsurancesForEditCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoOwnersTriageDeepLinkKey
import com.hedvig.android.feature.editcoinsured.ui.TestBackstack
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.OnContinueWithSelected
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.Reload
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageEvent.SelectInsurance
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Failure
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Loading
import com.hedvig.android.feature.editcoinsured.ui.triage.EditCoInsuredTriageUiState.Success
import com.hedvig.android.molecule.test.FrameTestContext
import com.hedvig.android.molecule.test.testFrames
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Every navigation of [EditCoInsuredTriagePresenter], asserted as the back stack it leaves for each way the triage
 * screen is reached. The stand-in keys play the screens below it; the deep link stacks are what the app shell lands:
 * Home then the key in our own task (neither triage key has a deep link ancestry), the key alone in another app's task.
 */
internal class EditCoInsuredTriagePresenterTest {
  private val useCase = FakeGetInsurancesForEditCoInsuredUseCase()

  private val addOrRemove = InsuranceForEditOrAddCoInsured(
    id = "contract-a",
    destination = EditCoInsuredDestination.ADD_OR_REMOVE,
    displayName = "Home insurance",
    exposureName = "Street 1",
    type = CoInsuredFlowType.CoOwners,
  )
  private val missingInfo = InsuranceForEditOrAddCoInsured(
    id = "contract-b",
    destination = EditCoInsuredDestination.MISSING_INFO,
    displayName = "Home insurance",
    exposureName = "Street 2",
    type = CoInsuredFlowType.CoOwners,
  )
  private val both = listOf(addOrRemove, missingInfo)

  private val triageKey = EditCoInsuredTriageKey(contractId = null, type = CoInsuredFlowType.CoOwners)
  private val inAppPush: List<HedvigNavKey> = listOf(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, triageKey)
  private val deepLinkKey = EditCoOwnersTriageDeepLinkKey(contractId = "contract-b")
  private val deepLinkInOwnTask: List<HedvigNavKey> = listOf(HomeStandIn, deepLinkKey)
  private val deepLinkInForeignTask: List<HedvigNavKey> = listOf(deepLinkKey)

  private fun presenter(
    backstack: TestBackstack,
    insuranceId: String? = null,
    type: CoInsuredFlowType = CoInsuredFlowType.CoOwners,
  ) = EditCoInsuredTriagePresenter(useCase, backstack, insuranceId, type)

  private fun FrameTestContext<EditCoInsuredTriageEvent, EditCoInsuredTriageUiState>.load(
    result: Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>,
  ) {
    useCase.results.trySend(result)
    frame()
  }

  private fun picker(selected: InsuranceForEditOrAddCoInsured? = null) =
    Success(list = both, selected = selected, type = CoInsuredFlowType.CoOwners)

  // Redirect after load

  @Test
  fun `one option, pushed in app, replaces the triage screen with the option's step`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners),
      )
      assertThat(observed).containsExactly(Loading)
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a deep link in our own task with a matching contract among several replaces the triage screen`() = runTest {
    val backstack = TestBackstack(deepLinkInOwnTask.toMutableList())
    presenter(backstack, insuranceId = "contract-b").testFrames(Loading) {
      load(both.right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        CoInsuredAddInfoKey("contract-b", CoInsuredFlowType.CoOwners),
      )
      assertThat(observed).containsExactly(Loading)
    }
  }

  @Test
  fun `a deep link in another app's task leaves the option's step alone on the stack`() = runTest {
    val backstack = TestBackstack(deepLinkInForeignTask.toMutableList())
    presenter(backstack, insuranceId = "contract-b").testFrames(Loading) {
      load(both.right())
      assertThat(backstack.entries).containsExactly(CoInsuredAddInfoKey("contract-b", CoInsuredFlowType.CoOwners))
      assertThat(observed).containsExactly(Loading)
    }
  }

  @Test
  fun `the co-insured deep link in our own task replaces its triage key`() = runTest {
    val coInsuredKey = EditCoInsuredTriageKey(contractId = "contract-a", type = CoInsuredFlowType.CoInsured)
    val backstack = TestBackstack(mutableListOf(HomeStandIn, coInsuredKey))
    presenter(backstack, insuranceId = "contract-a", type = CoInsuredFlowType.CoInsured).testFrames(Loading) {
      load(both.right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners),
      )
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoInsured)
    }
  }

  @Test
  fun `the step key takes its flow type from the insurance, not from the triage key`() = runTest {
    val coInsuredKey = EditCoInsuredTriageKey(contractId = null, type = CoInsuredFlowType.CoInsured)
    val backstack = TestBackstack(mutableListOf(HomeStandIn, coInsuredKey))
    presenter(backstack, type = CoInsuredFlowType.CoInsured).testFrames(Loading) {
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries.last()).isEqualTo(CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners))
    }
  }

  // The picker

  @Test
  fun `several options and no contract show the picker without navigating`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(backstack.entries).isEqualTo(inAppPush)
    }
  }

  @Test
  fun `a contract that is not among the options shows the picker with every option`() = runTest {
    val backstack = TestBackstack(deepLinkInOwnTask.toMutableList())
    presenter(backstack, insuranceId = "contract-unknown").testFrames(Loading) {
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(backstack.entries).isEqualTo(deepLinkInOwnTask)
    }
  }

  @Test
  fun `one option that does not match the contract shows a picker of one`() = runTest {
    val backstack = TestBackstack(deepLinkInOwnTask.toMutableList())
    presenter(backstack, insuranceId = "contract-unknown").testFrames(Loading) {
      load(listOf(addOrRemove).right())
      assertThat(latest).isEqualTo(Success(listOf(addOrRemove), null, CoInsuredFlowType.CoOwners))
      assertThat(backstack.entries).isEqualTo(deepLinkInOwnTask)
    }
  }

  @Test
  fun `no options show an empty picker`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(emptyList<InsuranceForEditOrAddCoInsured>().right())
      assertThat(latest).isEqualTo(Success(emptyList(), null, CoInsuredFlowType.CoOwners))
      assertThat(backstack.entries).isEqualTo(inAppPush)
    }
  }

  @Test
  fun `continue with a selection replaces the triage screen with the selected option's step`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-b"))
      frame()
      assertThat(latest).isEqualTo(picker(selected = missingInfo))
      emit(OnContinueWithSelected)
      frame()
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        CoInsuredAddInfoKey("contract-b", CoInsuredFlowType.CoOwners),
      )
      assertThat(latest).isEqualTo(picker(selected = missingInfo))
    }
  }

  @Test
  fun `continue from the deep link picker in another app's task leaves the step alone`() = runTest {
    val backstack = TestBackstack(deepLinkInForeignTask.toMutableList())
    presenter(backstack, insuranceId = "contract-unknown").testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-a"), OnContinueWithSelected)
      frame()
      assertThat(backstack.entries).containsExactly(CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners))
    }
  }

  @Test
  fun `continue without a selection does nothing`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      emit(OnContinueWithSelected)
      frame()
      assertThat(backstack.entries).isEqualTo(inAppPush)
    }
  }

  @Test
  fun `continue before the picker has loaded is dropped`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      emit(OnContinueWithSelected)
      frame()
      load(both.right())
      assertThat(backstack.entries).isEqualTo(inAppPush)
    }
  }

  @Test
  fun `two continues in one frame push the step twice`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-a"))
      frame()
      emit(OnContinueWithSelected, OnContinueWithSelected)
      frame()
      val step = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
      assertThat(backstack.entries).containsExactly(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, step, step)
    }
  }

  @Test
  fun `a continue after the first was performed pushes the step again`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      emit(SelectInsurance("contract-a"), OnContinueWithSelected)
      frame()
      emit(OnContinueWithSelected)
      frame()
      val step = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
      assertThat(backstack.entries).containsExactly(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, step, step)
    }
  }

  // Loading and failure

  @Test
  fun `a failed load shows the failure, and reload loads again`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
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
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(ErrorMessage().left())
      emit(Reload)
      frame()
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries.last()).isEqualTo(CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners))
      assertThat(backstack.entries.size).isEqualTo(inAppPush.size)
    }
  }

  @Test
  fun `a reload from the picker shows loading and keeps the selection`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
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
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      emit(Reload)
      frame()
      load(both.right())
      assertThat(latest).isEqualTo(picker())
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }

  // Restart from the last state

  @Test
  fun `a restart while loading loads again and redirects`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      frame()
      restart()
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners),
      )
      assertThat(useCase.calls).containsExactly(CoInsuredFlowType.CoOwners, CoInsuredFlowType.CoOwners)
    }
  }

  @Test
  fun `a restart from the picker shows it, then loading, then the reloaded picker with the selection kept`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
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
      assertThat(backstack.entries).isEqualTo(inAppPush)
    }
  }

  @Test
  fun `a restart from the picker that reloads one option redirects`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(both.right())
      restart()
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries.last()).isEqualTo(CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners))
      assertThat(backstack.entries.size).isEqualTo(inAppPush.size)
    }
  }

  @Test
  fun `a restart from the failure loads again`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
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
  fun `a restart after a redirect loads and redirects again, appending a second step`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(listOf(addOrRemove).right())
      restart()
      load(listOf(addOrRemove).right())
      val step = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
      assertThat(backstack.entries).containsExactly(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, step, step)
    }
  }

  @Test
  fun `a reload after a redirect loads and redirects again`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      load(listOf(addOrRemove).right())
      emit(Reload)
      frame()
      load(listOf(addOrRemove).right())
      val step = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
      assertThat(backstack.entries).containsExactly(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, step, step)
    }
  }

  // The stack the redirect runs on

  @Test
  fun `a load that lands after the flow's tab was parked pushes the step onto the shown tab`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      frame()
      // The member switches to Home: the Insurances run is parked and only Home's run is rendered.
      backstack.entries.clear()
      backstack.entries.add(HomeStandIn)
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners),
      )
    }
  }

  @Test
  fun `a co-owners deep link opened above an earlier co-insured triage pops to the earlier one`() = runTest {
    val backstack = TestBackstack(mutableListOf(HomeStandIn, triageKey, ContractDetailStandIn, deepLinkKey))
    presenter(backstack, insuranceId = "contract-b").testFrames(Loading) {
      load(both.right())
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        CoInsuredAddInfoKey("contract-b", CoInsuredFlowType.CoOwners),
      )
    }
  }

  @Test
  fun `nothing navigates while loading or after a failure`() = runTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    presenter(backstack).testFrames(Loading) {
      frame()
      load(ErrorMessage().left())
      emit(OnContinueWithSelected)
      frame()
      assertThat(backstack.entries).isEqualTo(inAppPush)
      assertThat(observed).containsExactly(Loading, Failure)
    }
  }
}

private class FakeGetInsurancesForEditCoInsuredUseCase : GetInsurancesForEditCoInsuredUseCase {
  val calls = mutableListOf<CoInsuredFlowType>()
  val results = Channel<Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>>(Channel.UNLIMITED)

  override suspend fun invoke(type: CoInsuredFlowType): Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>> {
    calls += type
    return results.receive()
  }
}

private data object HomeStandIn : HedvigNavKey

private data object InsurancesStandIn : HedvigNavKey

private data object ContractDetailStandIn : HedvigNavKey
