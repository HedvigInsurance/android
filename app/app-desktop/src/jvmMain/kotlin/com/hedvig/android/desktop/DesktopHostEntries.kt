package com.hedvig.android.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import com.hedvig.android.design.system.hedvig.NotAvailableOnDesktop
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack

/**
 * The host half of the entry provider on desktop: every key the shared provider can put on the back stack but does
 * not register, shown as not available.
 */
internal fun EntryProviderScope<HedvigNavKey>.desktopHostEntries(backstack: Backstack) {
  entry<ChatKey> { DesktopPlaceholder(backstack) }
  entry<InboxKey> { DesktopPlaceholder(backstack) }
  entry<TrustlyKey> { DesktopPlaceholder(backstack) }
  entry<ImageViewerKey> { DesktopPlaceholder(backstack) }
  entry<ClaimDetailsKey> { DesktopPlaceholder(backstack) }
}

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
