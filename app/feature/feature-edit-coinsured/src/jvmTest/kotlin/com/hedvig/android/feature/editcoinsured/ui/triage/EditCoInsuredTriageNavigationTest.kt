package com.hedvig.android.feature.editcoinsured.ui.triage

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.editcoinsured.data.InsuranceForEditOrAddCoInsured
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoOwnersTriageDeepLinkKey
import com.hedvig.android.feature.editcoinsured.navigation.navigateToTriageStep
import com.hedvig.android.feature.editcoinsured.ui.TestBackstack
import com.hedvig.android.navigation.common.HedvigNavKey
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The triage destination with a real ViewModel on a fake load, wired to the same back stack call as its entries, on the
 * stack each entry path leaves: an in-app push, a deep link in our own task (Home then the key, since neither triage
 * key has a deep link ancestry), and a deep link standing alone in another app's task.
 */
@OptIn(ExperimentalTestApi::class)
internal class EditCoInsuredTriageNavigationTest {
  private val main = UnconfinedTestDispatcher()
  private val useCase = FakeGetInsurancesForEditCoInsuredUseCase()
  private val lifecycleOwner = TestLifecycleOwner()
  private var shown by mutableStateOf(true)
  private var recompositionTrigger by mutableIntStateOf(0)

  /** The contract of each navigation, tagged with the [recompositionTrigger] value of the composition that ran it. */
  private val navigations = mutableListOf<Pair<String, Int>>()

  private val triageKey = EditCoInsuredTriageKey(contractId = null, type = CoInsuredFlowType.CoOwners)
  private val inAppPush: List<HedvigNavKey> = listOf(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, triageKey)
  private val deepLinkKey = EditCoOwnersTriageDeepLinkKey(contractId = "contract-b")
  private val addOrRemoveStep = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
  private val missingInfoStep = CoInsuredAddInfoKey("contract-b", CoInsuredFlowType.CoOwners)

  @Before
  fun setUp() {
    // The ViewModel's frames and the effect's wait for the resumed lifecycle run on Dispatchers.Main.
    Dispatchers.setMain(main)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun ComposeUiTest.show(
    backstack: TestBackstack,
    insuranceId: String? = null,
    type: CoInsuredFlowType = CoInsuredFlowType.CoOwners,
  ): EditCoInsuredTriageViewModel {
    val viewModel = EditCoInsuredTriageViewModel(useCase, insuranceId, type)
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        HedvigTheme {
          val composition = recompositionTrigger
          if (shown) {
            EditCoInsuredTriageDestination(
              viewModel = viewModel,
              navigateUp = {},
              // A new callback on every recomposition, so the effect is recomposed rather than skipped.
              navigateToStep = {
                navigations += it.id to composition
                backstack.navigateToTriageStep(it)
              },
            )
          }
        }
      }
    }
    settle()
    return viewModel
  }

  /** Lets the ViewModel produce its frames, which wait on virtual time, and the UI catch up. */
  private fun ComposeUiTest.settle() {
    repeat(4) {
      main.scheduler.advanceTimeBy(FRAME_MILLIS)
      main.scheduler.runCurrent()
      waitForIdle()
    }
  }

  private fun ComposeUiTest.load(result: Either<ErrorMessage, List<InsuranceForEditOrAddCoInsured>>) {
    useCase.results.trySend(result)
    settle()
  }

  private fun ComposeUiTest.setLifecycleState(state: Lifecycle.State) {
    runOnIdle { lifecycleOwner.registry.currentState = state }
    settle()
  }

  private fun ComposeUiTest.clickContinue() {
    onNodeWithText(continueText).performClick()
    settle()
  }

  // Redirect after load, per entry path

  @Test
  fun `pushed in app, one option replaces the triage screen with its step, once`() = runComposeUiTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    show(backstack)
    load(listOf(addOrRemove).right())
    recompositionTrigger++
    settle()
    assertThat(
      backstack.entries,
    ).containsExactly(HomeStandIn, InsurancesStandIn, ContractDetailStandIn, addOrRemoveStep)
    assertThat(navigations).containsExactly("contract-a" to 0)
  }

  @Test
  fun `a deep link in our own task replaces the triage key with the contract's step`() = runComposeUiTest {
    val backstack = TestBackstack(mutableListOf(HomeStandIn, deepLinkKey))
    show(backstack, insuranceId = "contract-b")
    load(both.right())
    assertThat(backstack.entries).containsExactly(HomeStandIn, missingInfoStep)
    assertThat(navigations).containsExactly("contract-b" to 0)
  }

  @Test
  fun `a deep link in another app's task leaves the contract's step alone on the stack`() = runComposeUiTest {
    val backstack = TestBackstack(mutableListOf(deepLinkKey))
    show(backstack, insuranceId = "contract-b")
    load(both.right())
    assertThat(backstack.entries).containsExactly(missingInfoStep)
  }

  @Test
  fun `the co-insured deep link in our own task replaces its triage key, with the insurance's flow type`() =
    runComposeUiTest {
      val coInsuredKey = EditCoInsuredTriageKey(contractId = "contract-a", type = CoInsuredFlowType.CoInsured)
      val backstack = TestBackstack(mutableListOf(HomeStandIn, coInsuredKey))
      show(backstack, insuranceId = "contract-a", type = CoInsuredFlowType.CoInsured)
      load(both.right())
      assertThat(backstack.entries).containsExactly(HomeStandIn, addOrRemoveStep)
    }

  @Test
  fun `a co-owners deep link opened above an earlier co-insured triage pops to the earlier one`() = runComposeUiTest {
    val backstack = TestBackstack(mutableListOf(HomeStandIn, triageKey, ContractDetailStandIn, deepLinkKey))
    show(backstack, insuranceId = "contract-b")
    load(both.right())
    assertThat(backstack.entries).containsExactly(HomeStandIn, missingInfoStep)
  }

  // When the redirect is performed

  @Test
  fun `a redirect that lands while the screen is not resumed shows the spinner and waits for the resume`() =
    runComposeUiTest {
      val backstack = TestBackstack(inAppPush.toMutableList())
      lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
      show(backstack)
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries).isEqualTo(inAppPush)
      onAllNodesWithText("Street 1").assertCountEquals(0)
      onAllNodesWithText(continueText).assertCountEquals(0)

      setLifecycleState(Lifecycle.State.RESUMED)
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        addOrRemoveStep,
      )
      assertThat(navigations.size).isEqualTo(1)
    }

  @Test
  fun `a redirect that lands while the flow's tab is parked is performed on the flow's stack when it is shown again`() =
    runComposeUiTest {
      val backstack = TestBackstack(inAppPush.toMutableList())
      show(backstack)
      // The member switches to Home: the flow's run is parked, so its screen leaves composition.
      shown = false
      val flowRun = backstack.entries.toList()
      backstack.entries.clear()
      backstack.entries.add(HomeStandIn)
      settle()
      load(listOf(addOrRemove).right())
      assertThat(backstack.entries).containsExactly(HomeStandIn)

      // The member returns to the tab.
      backstack.entries.clear()
      backstack.entries.addAll(flowRun)
      shown = true
      settle()
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        addOrRemoveStep,
      )
      assertThat(navigations.size).isEqualTo(1)
    }

  @Test
  fun `loading, a failure and the picker navigate nowhere`() = runComposeUiTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    val viewModel = show(backstack)
    load(Either.Left(ErrorMessage()))
    viewModel.emit(EditCoInsuredTriageEvent.Reload)
    settle()
    load(both.right())
    onNodeWithText("Street 1").assertExists()
    assertThat(backstack.entries).isEqualTo(inAppPush)
    assertThat(navigations).isEmpty()
  }

  // Continue from the picker

  @Test
  fun `continue with a selection replaces the triage screen with the selected step`() = runComposeUiTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    val viewModel = show(backstack)
    load(both.right())
    viewModel.emit(EditCoInsuredTriageEvent.SelectInsurance("contract-b"))
    settle()
    clickContinue()
    assertThat(backstack.entries).containsExactly(
      HomeStandIn,
      InsurancesStandIn,
      ContractDetailStandIn,
      missingInfoStep,
    )
  }

  @Test
  fun `continue from the deep link picker in another app's task leaves the step alone`() = runComposeUiTest {
    val backstack = TestBackstack(mutableListOf(deepLinkKey))
    val viewModel = show(backstack, insuranceId = "contract-unknown")
    load(both.right())
    viewModel.emit(EditCoInsuredTriageEvent.SelectInsurance("contract-a"))
    settle()
    clickContinue()
    assertThat(backstack.entries).containsExactly(addOrRemoveStep)
  }

  @Test
  fun `continue without a selection does nothing`() = runComposeUiTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    show(backstack)
    load(both.right())
    clickContinue()
    assertThat(backstack.entries).isEqualTo(inAppPush)
  }

  @Test
  fun `a second continue once the leaving screen is no longer resumed pushes nothing`() = runComposeUiTest {
    val backstack = TestBackstack(inAppPush.toMutableList())
    val viewModel = show(backstack)
    load(both.right())
    viewModel.emit(EditCoInsuredTriageEvent.SelectInsurance("contract-a"))
    settle()
    clickContinue()
    // The display caps the leaving screen below resumed while the transition runs.
    setLifecycleState(Lifecycle.State.STARTED)
    clickContinue()
    assertThat(backstack.entries).containsExactly(
      HomeStandIn,
      InsurancesStandIn,
      ContractDetailStandIn,
      addOrRemoveStep,
    )
  }

  @Test
  fun `a second continue before the leaving screen stops being resumed still pushes the step again`() =
    runComposeUiTest {
      val backstack = TestBackstack(inAppPush.toMutableList())
      val viewModel = show(backstack)
      load(both.right())
      viewModel.emit(EditCoInsuredTriageEvent.SelectInsurance("contract-a"))
      settle()
      clickContinue()
      clickContinue()
      assertThat(backstack.entries).containsExactly(
        HomeStandIn,
        InsurancesStandIn,
        ContractDetailStandIn,
        addOrRemoveStep,
        addOrRemoveStep,
      )
    }

  private companion object {
    const val FRAME_MILLIS = 20L
    val continueText: String = runBlocking { getString(Res.string.general_continue_button) }
  }
}

private class TestLifecycleOwner : LifecycleOwner {
  val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
    currentState = Lifecycle.State.RESUMED
  }
  override val lifecycle: Lifecycle
    get() = registry
}

private data object HomeStandIn : HedvigNavKey

private data object InsurancesStandIn : HedvigNavKey

private data object ContractDetailStandIn : HedvigNavKey
