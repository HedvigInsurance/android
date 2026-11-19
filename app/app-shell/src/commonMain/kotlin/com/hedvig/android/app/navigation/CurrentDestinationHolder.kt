package com.hedvig.android.app.navigation

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.navigation.common.HedvigNavKey
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped source of truth for the destination currently on top of the rendered stack, published
 * as a [StateFlow] so non-Composable consumers (e.g.
 * [com.hedvig.android.app.notification.senders.ChatNotificationSender], which runs on the FCM/binder
 * thread) can read it safely. Written by `ReportCurrentDestinationEffect`.
 *
 * This is intentionally non-persistent: a process death wipes it, which is the desired behavior —
 * the suppression it powers only matters while the app is resumed, and over-showing a notification is
 * preferable to wrongly hiding one.
 */
@SingleIn(AppScope::class)
@Inject
class CurrentDestinationHolder {
  private val currentDestinationState = MutableStateFlow<HedvigNavKey?>(null)
  val currentDestination: StateFlow<HedvigNavKey?> = currentDestinationState.asStateFlow()

  fun update(destination: HedvigNavKey?) {
    currentDestinationState.value = destination
  }
}
