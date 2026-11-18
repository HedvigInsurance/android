package com.hedvig.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleDestroyedException
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.withResumed
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * Calls [navigate] with the target of [request] if no one has taken the request yet, so each request is performed
 * once however often it is recomposed, however many times its composition is created, and however many screens show
 * the same state. For requests that must not be lost: the outcome of a write and a redirect. A tap offer goes to
 * [TapOfferNavigationEffect] instead.
 *
 * The request is taken only while the screen is resumed, so a request that arrives during a transition or behind an
 * overlay waits for the screen to resume, and one whose screen leaves before that stays pending for whichever screen
 * shows it next. [navigate] must always navigate: it must not be wrapped in `dropUnlessResumed` or similar, since a
 * request it drops is taken and never performed.
 */
@Composable
fun <T : Any> NavigationEffect(request: NavigationRequest<T>?, navigate: (T) -> Unit) {
  val currentNavigate by rememberUpdatedState(navigate)
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(request, lifecycle) {
    if (request == null) return@LaunchedEffect
    try {
      lifecycle.withResumed {
        if (request.tryConsume()) currentNavigate(request.target)
      }
    } catch (_: LifecycleDestroyedException) {
      // The screen is gone for good, so the request stays pending.
    }
  }
}

/**
 * Calls [navigate] with the target of [offer] if the screen is resumed when the offer reaches it, once per offer, for
 * an offer the member makes with a tap and can repeat. An offer that reaches a screen that is not resumed is taken
 * without navigating: that screen is covered, leaving or in the background, and performing the offer when it is shown
 * again would navigate without a tap, such as when the member presses Back to it. The presenter sees the offer taken
 * and accepts the next tap. [navigate] must always navigate, as for [NavigationEffect].
 */
@Composable
fun <T : Any> TapOfferNavigationEffect(offer: NavigationRequest<T>?, navigate: (T) -> Unit) {
  val currentNavigate by rememberUpdatedState(navigate)
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  LaunchedEffect(offer, lifecycle) {
    if (offer == null || !offer.tryConsume()) return@LaunchedEffect
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) currentNavigate(offer.target)
  }
}

/**
 * While [active], collects [requests] whatever the lifecycle, and calls [navigate] with the target of the first pending
 * request it takes. For an outcome that must be acted on while the screen is stopped: [requests] is read straight from
 * the ViewModel, not from state composed while the screen is visible, so remember it rather than mapping a flow on
 * every recomposition, which would restart the collection.
 *
 * [navigate] must always navigate, as for the other overload.
 */
@Composable
fun <T : Any> NavigationEffect(requests: Flow<NavigationRequest<T>?>, active: Boolean, navigate: (T) -> Unit) {
  val currentNavigate by rememberUpdatedState(navigate)
  LaunchedEffect(requests, active) {
    if (!active) return@LaunchedEffect
    val request = requests.filterNotNull().filter { it.tryConsume() }.first()
    currentNavigate(request.target)
  }
}
