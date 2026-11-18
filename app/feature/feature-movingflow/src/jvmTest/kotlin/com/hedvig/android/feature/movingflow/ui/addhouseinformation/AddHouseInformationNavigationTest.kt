package com.hedvig.android.feature.movingflow.ui.addhouseinformation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.hedvig.android.feature.movingflow.AddHouseInformationKey
import com.hedvig.android.feature.movingflow.ChoseCoverageLevelAndDeductibleKey
import com.hedvig.android.feature.movingflow.EnterNewAddressKey
import com.hedvig.android.feature.movingflow.HousingTypeKey
import com.hedvig.android.feature.movingflow.TEST_MOVE_INTENT_ID
import com.hedvig.android.feature.movingflow.TestBackstack
import com.hedvig.android.feature.movingflow.compose.BooleanInput
import com.hedvig.android.feature.movingflow.compose.ConstrainedNumberInput
import com.hedvig.android.feature.movingflow.compose.ListInput
import com.hedvig.android.feature.movingflow.compose.NoopValidator
import com.hedvig.android.feature.movingflow.compose.ValidatedInput
import com.hedvig.android.feature.movingflow.navigateToCoverageStep
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content
import com.hedvig.android.feature.movingflow.ui.addhouseinformation.AddHouseInformationUiState.Content.SubmittingInfoFailure
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * [AddHouseInformationNavigationEffect] wired to the same backstack call as the house information entry, on the stack
 * the flow has when this step shows.
 */
@OptIn(ExperimentalTestApi::class)
class AddHouseInformationNavigationTest {
  private val flowStack: List<HedvigNavKey> = listOf(
    HousingTypeKey(TEST_MOVE_INTENT_ID),
    EnterNewAddressKey(TEST_MOVE_INTENT_ID),
    AddHouseInformationKey(TEST_MOVE_INTENT_ID),
  )
  private val coverageKey = ChoseCoverageLevelAndDeductibleKey(TEST_MOVE_INTENT_ID)
  private val backstack = TestBackstack(flowStack.toMutableList())
  private val step = ChoseCoverageLevelAndDeductibleStep(TEST_MOVE_INTENT_ID)
  private val content = content()

  /** Each call builds new inputs, so two contents are never equal, as when the stored flow emits again. */
  private fun content(next: NavigationRequest<ChoseCoverageLevelAndDeductibleStep>? = null) = Content(
    moveFromAddressId = TEST_MOVE_INTENT_ID,
    addressInput = AddressInput(
      yearOfConstruction = ValidatedInput(1990, NoopValidator()),
      ancillaryArea = ValidatedInput(15, NoopValidator()),
      numberOfBathrooms = ConstrainedNumberInput(2, 1..10),
      isSublet = BooleanInput(false),
      possibleExtraBuildingTypes = emptyList(),
      extraBuildings = ListInput(emptyList()),
    ),
    isLoadingNextStep = false,
    submittingInfoFailure = null,
    next = next,
  )

  private var uiState by mutableStateOf<AddHouseInformationUiState>(content)
  private var recompositionTrigger by mutableIntStateOf(0)

  @Before
  fun setUp() {
    // The effect waits for the resumed lifecycle on Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun ComposeUiTest.showEffect() {
    setContent {
      recompositionTrigger
      AddHouseInformationNavigationEffect(
        uiState = uiState,
        navigateToCoverageStep = { backstack.navigateToCoverageStep(it) },
      )
    }
    waitForIdle()
  }

  @Test
  fun `content without a next step navigates nowhere`() = runComposeUiTest {
    showEffect()
    assertThat(backstack.entries).isEqualTo(flowStack)
  }

  @Test
  fun `the coverage step is pushed once on top of the flow, and pushed again when offered again`() = runComposeUiTest {
    showEffect()
    val offered = NavigationRequest.of(step)
    uiState = content(next = offered)
    waitForIdle()
    // The presenter keeps the taken step in the states it emits until the next continue.
    uiState = content(next = offered)
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
    assertThat(offered.isPending).isFalse()

    // Back from the coverage step, then continue again.
    backstack.entries.removeLast()
    uiState = content(next = NavigationRequest.of(step))
    waitForIdle()
    assertThat(backstack.entries).isEqualTo(flowStack + coverageKey)
  }

  @Test
  fun `a step that was taken before is not pushed when shown again`() = runComposeUiTest {
    val taken = NavigationRequest.of(step).also { it.tryConsume() }
    uiState = content(next = taken)
    showEffect()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).isEqualTo(flowStack)
  }

  @Test
  fun `loading, a missing flow, loading the next step and a failure navigate nowhere`() = runComposeUiTest {
    showEffect()
    for (state in listOf(
      AddHouseInformationUiState.Loading,
      AddHouseInformationUiState.MissingOngoingMovingFlow,
      content.copy(isLoadingNextStep = true),
      content.copy(submittingInfoFailure = SubmittingInfoFailure.NetworkFailure),
    )) {
      uiState = state
      waitForIdle()
    }
    assertThat(backstack.entries).isEqualTo(flowStack)
  }
}
