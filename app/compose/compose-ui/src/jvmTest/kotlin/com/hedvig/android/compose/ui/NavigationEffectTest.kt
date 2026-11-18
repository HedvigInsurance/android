package com.hedvig.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * [NavigationEffect] against the two hand-written effects it replaces: a [SideEffect], and a [LaunchedEffect] keyed
 * on the target. Each double navigation is shown to happen with the hand-written effect and not with the block.
 */
@OptIn(ExperimentalTestApi::class)
class NavigationEffectTest {
  private fun interface Effect {
    @Composable
    fun Show(request: NavigationRequest<String>?, navigate: (String) -> Unit)
  }

  private val block = Effect { request, navigate -> NavigationEffect(request, navigate) }
  private val tapOffer = Effect { offer, navigate -> TapOfferNavigationEffect(offer, navigate) }
  private val sideEffect = Effect { request, navigate -> SideEffect { request?.target?.let(navigate) } }
  private val keyedOnTarget = Effect { request, navigate ->
    val currentNavigate by rememberUpdatedState(navigate)
    val target = request?.target
    LaunchedEffect(target) { target?.let(currentNavigate) }
  }

  private var request by mutableStateOf<NavigationRequest<String>?>(null)
  private var recompositionTrigger by mutableIntStateOf(0)
  private var shown by mutableStateOf(true)
  private var screens by mutableIntStateOf(1)

  /** The [recompositionTrigger] value of the composition whose callback navigated, once per navigation. */
  private val navigatedFrom = mutableListOf<Int>()
  private val lifecycleOwner = TestLifecycleOwner()

  @Before
  fun setUp() {
    // Waiting for a lifecycle state dispatches to Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun ComposeUiTest.show(effect: Effect) {
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        val composition = recompositionTrigger
        if (shown) {
          repeat(screens) {
            // A new callback on every recomposition, so the effect is recomposed rather than skipped.
            effect.Show(request) { navigatedFrom += composition }
          }
        }
      }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.setLifecycleState(state: Lifecycle.State) {
    runOnIdle { lifecycleOwner.registry.currentState = state }
    waitForIdle()
  }

  @Test
  fun `block, a request that arrives while started but not resumed is performed once on resume`() = runComposeUiTest {
    lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    show(block)
    val offered = NavigationRequest.of("step")
    request = offered
    waitForIdle()
    assertThat(navigatedFrom).isEmpty()
    assertThat(offered.isPending).isTrue()

    setLifecycleState(Lifecycle.State.RESUMED)
    recompositionTrigger++
    waitForIdle()
    assertThat(navigatedFrom).containsExactly(0)
    assertThat(offered.isPending).isFalse()
  }

  @Test
  fun `block, a request whose screen leaves before it resumes stays pending for the next screen showing it`() =
    runComposeUiTest {
      lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
      show(block)
      val offered = NavigationRequest.of("step")
      request = offered
      waitForIdle()
      shown = false
      waitForIdle()
      setLifecycleState(Lifecycle.State.RESUMED)
      assertThat(navigatedFrom).isEmpty()
      assertThat(offered.isPending).isTrue()

      recompositionTrigger++
      shown = true
      waitForIdle()
      assertThat(navigatedFrom).containsExactly(1)
    }

  /** The callback shape most entries use: it does nothing unless the screen is resumed. */
  private fun dropUnlessResumed(navigate: (String) -> Unit): (String) -> Unit = { target ->
    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) navigate(target)
  }

  private val ungated = Effect { request, navigate ->
    val currentNavigate by rememberUpdatedState(navigate)
    LaunchedEffect(request) {
      if (request != null && request.tryConsume()) currentNavigate(request.target)
    }
  }

  private fun ComposeUiTest.requestWhileStartedWithADroppingCallback(effect: Effect): NavigationRequest<String> {
    lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    setContent {
      CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
        effect.Show(request, dropUnlessResumed { navigatedFrom += 0 })
      }
    }
    val offered = NavigationRequest.of("step")
    request = offered
    waitForIdle()
    setLifecycleState(Lifecycle.State.RESUMED)
    return offered
  }

  @Test
  fun `block, a callback that drops calls while not resumed still navigates once the screen resumes`() =
    runComposeUiTest {
      requestWhileStartedWithADroppingCallback(block)
      assertThat(navigatedFrom).containsExactly(0)
    }

  @Test
  fun `taken before resumed, a callback that drops calls while not resumed loses the request`() = runComposeUiTest {
    val offered = requestWhileStartedWithADroppingCallback(ungated)
    assertThat(navigatedFrom).isEmpty()
    assertThat(offered.isPending).isFalse()
  }

  private fun ComposeUiTest.recomposeTwice(effect: Effect): List<Int> {
    show(effect)
    request = NavigationRequest.of("step")
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    return navigatedFrom
  }

  @Test
  fun `block, a request is performed once across recompositions with a new callback`() = runComposeUiTest {
    assertThat(recomposeTwice(block)).containsExactly(0)
  }

  @Test
  fun `side effect, a request is performed on every recomposition`() = runComposeUiTest {
    assertThat(recomposeTwice(sideEffect)).containsExactly(0, 1, 2)
  }

  private fun ComposeUiTest.recreateComposition(effect: Effect): List<Int> {
    show(effect)
    request = NavigationRequest.of("step")
    waitForIdle()
    shown = false
    waitForIdle()
    recompositionTrigger++
    shown = true
    waitForIdle()
    return navigatedFrom
  }

  @Test
  fun `block, a composition created again does not perform a request again`() = runComposeUiTest {
    assertThat(recreateComposition(block)).containsExactly(0)
  }

  @Test
  fun `keyed on the target, a composition created again performs the request again`() = runComposeUiTest {
    assertThat(recreateComposition(keyedOnTarget)).containsExactly(0, 1)
  }

  private fun ComposeUiTest.twoScreens(effect: Effect): List<Int> {
    screens = 2
    show(effect)
    request = NavigationRequest.of("step")
    waitForIdle()
    return navigatedFrom
  }

  @Test
  fun `block, two screens showing the same request perform it once`() = runComposeUiTest {
    assertThat(twoScreens(block)).containsExactly(0)
  }

  @Test
  fun `keyed on the target, two screens showing the same request perform it twice`() = runComposeUiTest {
    assertThat(twoScreens(keyedOnTarget)).containsExactly(0, 0)
  }

  @Test
  fun `block, a new request for an equal target is performed again`() = runComposeUiTest {
    show(block)
    request = NavigationRequest.of("step")
    waitForIdle()
    recompositionTrigger++
    request = NavigationRequest.of("step")
    waitForIdle()
    assertThat(navigatedFrom).containsExactly(0, 1)
  }

  @Test
  fun `block, no request and a request taken elsewhere perform nothing`() = runComposeUiTest {
    show(block)
    request = NavigationRequest.of("step").also { it.tryConsume() }
    waitForIdle()
    request = null
    waitForIdle()
    assertThat(navigatedFrom).isEmpty()
  }

  @Test
  fun `tap offer, an offer is performed once across recompositions with a new callback`() = runComposeUiTest {
    assertThat(recomposeTwice(tapOffer)).containsExactly(0)
  }

  @Test
  fun `tap offer, two screens showing the same offer perform it once`() = runComposeUiTest {
    assertThat(twoScreens(tapOffer)).containsExactly(0)
  }

  @Test
  fun `tap offer, an offer that reaches a screen that is not resumed is dropped, also once it resumes`() =
    runComposeUiTest {
      lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
      show(tapOffer)
      val offered = NavigationRequest.of("step")
      request = offered
      waitForIdle()
      assertThat(navigatedFrom).isEmpty()
      assertThat(offered.isPending).isFalse()

      setLifecycleState(Lifecycle.State.RESUMED)
      recompositionTrigger++
      waitForIdle()
      assertThat(navigatedFrom).isEmpty()
    }

  /**
   * A second tap in the frame before the navigation of the first one caps the screen below resumed: its offer reaches
   * a screen that is leaving, which is then covered and shown again when the member presses Back.
   */
  private fun ComposeUiTest.offerOnALeavingScreenThenBack(effect: Effect): List<Int> {
    lifecycleOwner.registry.currentState = Lifecycle.State.STARTED
    show(effect)
    request = NavigationRequest.of("step")
    waitForIdle()
    shown = false
    waitForIdle()
    setLifecycleState(Lifecycle.State.RESUMED)
    recompositionTrigger++
    shown = true
    waitForIdle()
    return navigatedFrom
  }

  @Test
  fun `tap offer, an offer made on a leaving screen does not navigate when the member comes back`() = runComposeUiTest {
    assertThat(offerOnALeavingScreenThenBack(tapOffer)).isEmpty()
  }

  @Test
  fun `block, an offer made on a leaving screen navigates when the member comes back`() = runComposeUiTest {
    assertThat(offerOnALeavingScreenThenBack(block)).containsExactly(1)
  }

  private val requests = MutableStateFlow<NavigationRequest<String>?>(null)
  private var active by mutableStateOf(true)
  private val flowNavigations = mutableListOf<String>()

  private fun ComposeUiTest.showFlow(screens: Int = 1, effect: @Composable (Flow<NavigationRequest<String>?>) -> Unit) {
    setContent {
      repeat(screens) { effect(requests) }
    }
    waitForIdle()
  }

  private fun ComposeUiTest.setRequest(request: NavigationRequest<String>?) {
    runOnIdle { requests.value = request }
    waitForIdle()
  }

  @Test
  fun `block flow, a request on the flow is performed once while active`() = runComposeUiTest {
    showFlow { NavigationEffect(it, active) { target -> flowNavigations += target } }
    setRequest(NavigationRequest.of("charged"))
    runOnIdle { active = false }
    waitForIdle()
    runOnIdle { active = true }
    waitForIdle()
    assertThat(flowNavigations).containsExactly("charged")
  }

  @Test
  fun `block flow, nothing is performed while inactive`() = runComposeUiTest {
    active = false
    showFlow { NavigationEffect(it, active) { target -> flowNavigations += target } }
    setRequest(NavigationRequest.of("charged"))
    assertThat(flowNavigations).isEmpty()
  }

  @Test
  fun `block flow, two screens collecting the same flow perform a request once`() = runComposeUiTest {
    showFlow(screens = 2) { NavigationEffect(it, active) { target -> flowNavigations += target } }
    setRequest(NavigationRequest.of("charged"))
    assertThat(flowNavigations).containsExactly("charged")
  }

  @Test
  fun `block flow, a taken request on the flow does not hide a later pending one`() = runComposeUiTest {
    showFlow { NavigationEffect(it, active) { target -> flowNavigations += target } }
    setRequest(NavigationRequest.of("taken").also { it.tryConsume() })
    setRequest(NavigationRequest.of("pending"))
    assertThat(flowNavigations).containsExactly("pending")
  }

  @Test
  fun `first on the flow, two screens collecting the same flow perform a request twice`() = runComposeUiTest {
    showFlow(screens = 2) { flow ->
      LaunchedEffect(flow) { flowNavigations += flow.filterNotNull().first().target }
    }
    setRequest(NavigationRequest.of("charged"))
    assertThat(flowNavigations).containsExactly("charged", "charged")
  }
}

private class TestLifecycleOwner : LifecycleOwner {
  val registry: LifecycleRegistry = LifecycleRegistry.createUnsafe(this).apply {
    currentState = Lifecycle.State.RESUMED
  }
  override val lifecycle: Lifecycle
    get() = registry
}
