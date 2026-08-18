package com.hedvig.android.navigation.compose

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntryDecorator
import com.hedvig.android.navigation.common.HedvigNavKey

@Composable
fun entryDecorators(retainedContentKeys: () -> Set<Any>): List<NavEntryDecorator<HedvigNavKey>> {
  return listOf(
    rememberRetainedSaveableStateHolderNavEntryDecorator(retainedContentKeys),
    rememberRetainedViewModelStoreNavEntryDecorator(retainedContentKeys),
  )
}
