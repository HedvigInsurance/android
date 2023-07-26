package com.hedvig.android.feature.businessmodel

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator

fun NavGraphBuilder.businessModelGraph(
  navigator: Navigator,
  windowSizeClass: WindowSizeClass,
) {
  animatedComposable<AppDestination.BusinessModel> {
    BusinessModelScreen(
      navigateUp = navigator::navigateUp,
      windowSizeClass = windowSizeClass,
    )
  }
}
