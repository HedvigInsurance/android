package com.hedvig.android.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavEntry
import com.hedvig.android.design.system.hedvig.NotAvailableOnDesktop
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack

/**
 * Shows a key that has no registered entry as not available. Logs each such key at WARN the first time it is shown,
 * since the entry provider runs again on every recomposition.
 */
internal fun desktopFallbackEntry(backstack: Backstack): (HedvigNavKey) -> NavEntry<HedvigNavKey> {
  val loggedKeys = mutableSetOf<HedvigNavKey>()
  return { key ->
    if (loggedKeys.add(key)) {
      logcat(LogPriority.WARN) { "No entry is registered for $key, showing it as not available on desktop" }
    }
    NavEntry(key) { DesktopPlaceholder(backstack) }
  }
}

@Composable
private fun DesktopPlaceholder(backstack: Backstack) {
  Column(Modifier.fillMaxSize()) {
    TopAppBar(
      actionType = TopAppBarActionType.BACK,
      onActionClick = dropUnlessResumed { backstack.navigateUp() },
    )
    NotAvailableOnDesktop(Modifier.weight(1f))
  }
}
