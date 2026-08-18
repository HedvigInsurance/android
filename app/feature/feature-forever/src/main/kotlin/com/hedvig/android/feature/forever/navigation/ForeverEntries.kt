package com.hedvig.android.feature.forever.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.foreverEntries() {
  entry<ForeverKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: ForeverViewModel = metroViewModel()
    ForeverDestination(viewModel = viewModel)
  }
}
