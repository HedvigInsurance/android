package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.movingflow.AddHouseInformationKey
import com.hedvig.android.feature.movingflow.ChoseCoverageLevelAndDeductibleKey
import com.hedvig.android.feature.movingflow.EnterNewAddressKey
import com.hedvig.android.feature.movingflow.FakeMoveIntentRequestTransport
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.InMemoryPreferencesDataStore
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
import com.hedvig.android.feature.movingflow.TestBackstack
import com.hedvig.android.feature.movingflow.data.HousingType
import com.hedvig.android.feature.movingflow.moveIntentRequestData
import com.hedvig.android.feature.movingflow.navigateToCoverageStep
import com.hedvig.android.feature.movingflow.storage.MovingFlowRepository
import com.hedvig.android.feature.movingflow.storage.MovingFlowStorage
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMovingFlowStateWithPropertyInput
import com.hedvig.android.navigation.common.HedvigNavKey
import hedvig.resources.Res
import hedvig.resources.SAVE_AND_CONTINUE_BUTTON_LABEL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.getString
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * The house information destination with its real ViewModel and presenter, a stored flow that is ready to continue
 * and a fake request transport, wired to the same backstack call as its entry.
 */
@OptIn(ExperimentalTestApi::class)
class AddHouseInformationDestinationTest {
  private val continueText = runBlocking { getString(Res.string.SAVE_AND_CONTINUE_BUTTON_LABEL) }
  private val flowStack: List<HedvigNavKey> = listOf(
    HousingTypeKey(TEST_MOVE_INTENT_ID),
    EnterNewAddressKey(TEST_MOVE_INTENT_ID),
    AddHouseInformationKey(TEST_MOVE_INTENT_ID),
  )
  private val coverageKey = ChoseCoverageLevelAndDeductibleKey(TEST_MOVE_INTENT_ID)
  private val backstack = TestBackstack(flowStack.toMutableList())
  private val transport = FakeMoveIntentRequestTransport()
  private val lifecycleOwner = TestLifecycleOwner()

  // Cleared after each test, so no presenter keeps running into the next test class.
  private val viewModelStore = ViewModelStore()

  @Before
  fun setUp() {
    // The ViewModel runs its presenter on Dispatchers.Main with a frame clock that waits in real time, and the
    // navigation effect waits for the resumed lifecycle on Dispatchers.Main.
    Dispatchers.setMain(Dispatchers.Unconfined)
  }

  @After
  fun tearDown() {
    viewModelStore.clear()
    Dispatchers.resetMain()
  }

  private fun readyToContinueRepository(): MovingFlowRepository = runBlocking {
    val storage = MovingFlowStorage(InMemoryPreferencesDataStore())
    storage.setMovingFlowState(
      testMovingFlowStateWithPropertyInput(HousingType.Villa, movingSource = MovingSource.TERMINATION),
    )
    MovingFlowRepository(storage).apply {
      updateWithHouseInput(
        yearOfConstruction = 1990,
        ancillaryArea = 15,
        numberOfBathrooms = 2,
        isSublet = false,
        extraBuildings = emptyList(),
      )
    }
  }

  private fun ComposeUiTest.showDestination() {
    val repository = readyToContinueRepository()
    val factory = viewModelFactory {
      initializer { AddHouseInformationViewModel(TEST_MOVE_INTENT_ID, repository, transport.apolloClient()) }
    }
    val viewModel = ViewModelProvider.create(viewModelStore, factory)[AddHouseInformationViewModel::class]
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        HedvigTheme {
          AddHouseInformationDestination(
            viewModel = viewModel,
            navigateUp = {},
            popBackstack = {},
            exitFlow = {},
            navigateToCoverageStep = { backstack.navigateToCoverageStep(it) },
          )
        }
      }
    }
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { onAllNodesWithText(continueText).fetchSemanticsNodes().isNotEmpty() }
  }

  /** Lets the presenter run for several of its frames, for checks that something did not happen. */
  private fun ComposeUiTest.settle() {
    Thread.sleep(SETTLE_MILLIS)
    waitForIdle()
  }

  @Test
  fun `continue requests the quotes and pushes the coverage step once`() = runComposeUiTest {
    showDestination()
    onNodeWithText(continueText).performClick()
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { transport.inputs.isNotEmpty() }
    transport.responses.trySend(moveIntentRequestData(homeQuotes = listOf(testMoveHomeQuote(id = "quote"))))
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { backstack.entries.size > flowStack.size }
    settle()
    assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    assertThat(transport.inputs).hasSize(1)
  }

  @Test
  fun `a continue while the screen is not resumed is dropped`() = runComposeUiTest {
    showDestination()
    lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    onNodeWithText(continueText).performClick()
    settle()
    assertThat(transport.inputs).isEmpty()

    lifecycleOwner.registry.currentState = Lifecycle.State.RESUMED
    onNodeWithText(continueText).performClick()
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { transport.inputs.isNotEmpty() }
  }

  @Test
  fun `quotes that arrive while the screen is not resumed push the coverage step once it resumes`() = runComposeUiTest {
    showDestination()
    onNodeWithText(continueText).performClick()
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { transport.inputs.isNotEmpty() }
    lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    transport.responses.trySend(moveIntentRequestData(homeQuotes = listOf(testMoveHomeQuote(id = "quote"))))
    settle()
    assertThat(backstack.entries).isEqualTo(flowStack)

    lifecycleOwner.registry.currentState = Lifecycle.State.RESUMED
    waitUntil(timeoutMillis = TIMEOUT_MILLIS) { backstack.entries.size > flowStack.size }
    settle()
    assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
  }

  private companion object {
    const val TIMEOUT_MILLIS = 5_000L
    const val SETTLE_MILLIS = 300L
  }
}

private class TestLifecycleOwner : LifecycleOwner {
  val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
    currentState = Lifecycle.State.RESUMED
  }
  override val lifecycle: Lifecycle
    get() = registry
}
