package com.hedvig.android.feature.chip.id.navigation

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.chip.id.ui.AddChipIdDestination
import com.hedvig.android.feature.chip.id.ui.AddChipIdViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.chipIdGraph(
  navigateUp: () -> Unit,
) {
  navgraph<ChipIdGraphDestination>(
    startDestination = ChipIdDestination.AddChipId::class,
  ) {
    navdestination<ChipIdDestination.AddChipId> { _ ->
      val viewModel: AddChipIdViewModel = koinViewModel()
      AddChipIdDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
      )
    }
  }
}
