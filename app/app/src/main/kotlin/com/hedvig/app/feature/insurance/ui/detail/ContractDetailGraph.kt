package com.hedvig.app.feature.insurance.ui.detail

import androidx.compose.ui.unit.Density
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.contractDetailGraph(
  density: Density,
  navigator: Navigator,
  onEditCoInsuredClick: (NavBackStackEntry) -> Unit,
  onChangeAddressClick: (NavBackStackEntry) -> Unit,
  imageLoader: ImageLoader,
) {
  animatedComposable<AppDestination.ContractDetail>(
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) { backStackEntry ->
    val contractDetail = this
    val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
    val coverageViewModel: CoverageViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
    ContractDetailDestination(
      viewModel = viewModel,
      coverageViewModel = coverageViewModel,
      imageLoader = imageLoader,
      onEditCoInsuredClick = { onEditCoInsuredClick(backStackEntry) },
      onChangeAddressClick = { onChangeAddressClick(backStackEntry) },
      navigateUp = navigator::navigateUp,
    )
  }
}
