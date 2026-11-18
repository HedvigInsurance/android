package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.navigation.ManualChargeSuccessKey
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Charged
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Failure
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Success
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Hosts [ChargedEffect] the way `ManualChargeDestination` does: the composed state comes from a lifecycle-aware
 * collection of the same flow the effect is given.
 */
@OptIn(ExperimentalTestApi::class)
class ChargedEffectTest {
  private val swish = PayinAccount.SwishPayin(phoneNumber = "0701234567", isPending = false, isDefault = true)
  private val info = ManualChargeInfo(
    chargeId = "missedChargeId",
    missedDueDate = LocalDate(2026, 9, 27),
    amountDue = UiMoney(399.0, UiCurrencyCode.SEK),
    currentMethods = listOf(swish),
    primaryPayinMethod = swish,
    showCancellationWarning = true,
  )
  private val successKey = ManualChargeSuccessKey(
    showCancellationWarning = true,
    primaryPayinProvider = PaymentProvider.Swish,
  )

  private val lifecycleOwner = TestLifecycleOwner()
  private val uiStates = MutableStateFlow<ManualChargeUiState>(Success(info))
  private val onChargedCalls = mutableListOf<ManualChargeSuccessKey>()
  private var recompositionTrigger by mutableIntStateOf(0)

  @Before
  fun setUp() {
    // collectAsStateWithLifecycle observes the lifecycle on Dispatchers.Main, which the JVM has no platform
    // implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `one charged state calls back once, across recomposition and re-collection`() = runComposeUiTest {
    showEffect()
    setState(Success(info, payButtonLoading = true))
    setState(Charged(NavigationRequest.of(info)))
    assertThat(onChargedCalls).containsExactly(successKey)

    runOnIdle { recompositionTrigger++ }
    waitForIdle()
    assertThat(onChargedCalls).containsExactly(successKey)

    setLifecycleState(Lifecycle.State.CREATED)
    setLifecycleState(Lifecycle.State.RESUMED)
    assertThat(onChargedCalls).containsExactly(successKey)
  }

  @Test
  fun `a charge that lands while the screen is stopped still calls back`() = runComposeUiTest {
    showEffect()
    setState(Success(info, payButtonLoading = true))
    setLifecycleState(Lifecycle.State.CREATED)

    setState(Charged(NavigationRequest.of(info)))

    assertThat(onChargedCalls).containsExactly(successKey)
  }

  @Test
  fun `a charge that lands before the loading state is composed still calls back once`() = runComposeUiTest {
    showEffect()

    setState(Charged(NavigationRequest.of(info)))

    assertThat(onChargedCalls).containsExactly(successKey)
  }

  @Test
  fun `a failed charge does not call back`() = runComposeUiTest {
    showEffect()
    setState(Success(info, payButtonLoading = true))

    setState(Failure(ErrorMessage("Insufficient funds")))

    assertThat(onChargedCalls).isEmpty()
  }

  @Test
  fun `two screens sharing the ViewModel call back once for one charge`() = runComposeUiTest {
    showEffect(screens = 2)
    setState(Success(info, payButtonLoading = true))

    setState(Charged(NavigationRequest.of(info)))

    assertThat(onChargedCalls).containsExactly(successKey)
  }

  private fun ComposeUiTest.showEffect(screens: Int = 1) {
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        val uiState by uiStates.collectAsStateWithLifecycle()
        // A new callback instance on every trigger, so the effect's call site cannot be skipped.
        val onCharged = remember(recompositionTrigger) { { key: ManualChargeSuccessKey -> onChargedCalls += key } }
        repeat(screens) { ChargedEffect(uiState, uiStates, onCharged) }
      }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.setState(state: ManualChargeUiState) {
    runOnIdle { uiStates.value = state }
    waitForIdle()
  }

  private fun ComposeUiTest.setLifecycleState(state: Lifecycle.State) {
    runOnIdle { lifecycleOwner.registry.currentState = state }
    waitForIdle()
  }
}

private class TestLifecycleOwner : LifecycleOwner {
  val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
    currentState = Lifecycle.State.RESUMED
  }
  override val lifecycle: Lifecycle
    get() = registry
}
