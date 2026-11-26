package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack

/**
 * Every screen of the termination flow. The flow does not always start on [TerminateInsuranceKey]: when the
 * insurance is already known, that step is replaced by the next one. Closing or replacing the flow therefore
 * looks for the first screen of the flow on top of the back stack rather than for a specific key.
 */
interface TerminationFlowKey : HedvigNavKey

/** Pops the whole termination flow on top of the back stack. */
fun Backstack.closeTerminationFlow() {
  val start = terminationFlowStartIndex() ?: return
  popUpToIndex(start - 1)
}

/** Replaces the whole termination flow on top of the back stack with [key]. */
fun Backstack.replaceTerminationFlow(key: HedvigNavKey) {
  val start = terminationFlowStartIndex() ?: entries.size
  Snapshot.withMutableSnapshot {
    entries.subList(start, entries.size).clear()
    entries.add(key)
  }
}

private fun Backstack.terminationFlowStartIndex(): Int? {
  if (entries.lastOrNull() !is TerminationFlowKey) return null
  val lastEntryBeforeFlow = entries.indexOfLast { it !is TerminationFlowKey }
  return lastEntryBeforeFlow + 1
}
