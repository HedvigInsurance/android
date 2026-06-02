package com.hedvig.android.feature.forever.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.shared.foreverui.ui.ui.ForeverDestination
import com.hedvig.android.shared.foreverui.ui.ui.ForeverViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.foreverGraph(
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  navgraph(
    startDestination = ForeverKey::class,
  ) {
    navdestination<ForeverKey>(
      metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit),
    ) {
      val viewModel: ForeverViewModel = metroViewModel()
      ForeverDestination(
        viewModel = viewModel,
        languageService = languageService,
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
  }
}
