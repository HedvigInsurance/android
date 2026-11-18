package com.hedvig.android.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.hedvig.android.molecule.public.NavigationRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * Calls [navigate] with the target of [request] if no one has taken the request yet, so each request is performed
 * once however often it is recomposed, however many times its composition is created, and however many screens show
 * the same state.
 */
@Composable
fun <T : Any> NavigationEffect(request: NavigationRequest<T>?, navigate: (T) -> Unit) {
  val currentNavigate by rememberUpdatedState(navigate)
  LaunchedEffect(request) {
    if (request != null && request.tryConsume()) {
      currentNavigate(request.target)
    }
  }
}

/**
 * While [active], collects [requests] whatever the lifecycle, and calls [navigate] with the target of the first
 * request it sees if no one has taken it yet. For an outcome that must be acted on while the screen is stopped:
 * [requests] is read straight from the ViewModel, not from state composed while the screen is visible.
 */
@Composable
fun <T : Any> NavigationEffect(requests: Flow<NavigationRequest<T>?>, active: Boolean, navigate: (T) -> Unit) {
  val currentNavigate by rememberUpdatedState(navigate)
  LaunchedEffect(requests, active) {
    if (!active) return@LaunchedEffect
    val request = requests.filterNotNull().first()
    if (request.tryConsume()) {
      currentNavigate(request.target)
    }
  }
}
