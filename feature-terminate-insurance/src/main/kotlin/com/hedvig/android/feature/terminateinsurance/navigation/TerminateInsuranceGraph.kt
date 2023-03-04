package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceViewModel
import com.hedvig.android.feature.terminateinsurance.ui.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSuccessDestination
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
  navigateUp: () -> Boolean,
  finishTerminationFlow: () -> Unit,
) {
  animatedNavigation<Destinations.TerminateInsurance>(
    startDestination = createRoutePattern<TerminateInsuranceDestinations.TerminationDate>(),
  ) {
    animatedComposable<TerminateInsuranceDestinations.TerminationDate> { navBackStackEntry ->
      val viewModel: TerminateInsuranceViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry) {
        parametersOf(insuranceId)
      }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToSuccessScreen = {
          navController.navigate(
            TerminateInsuranceDestinations.TerminationSuccess,
            navOptions {
              popUpTo<Destinations.TerminateInsurance>()
            },
          )
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    animatedComposable<TerminateInsuranceDestinations.TerminationSuccess> {
      TerminationSuccessDestination(
        windowSizeClass = windowSizeClass,
        // Can't use navigateUp() from non-starting destination while it's the only one in the backstack due to not
        // having a fixed startDestination. This may be hard to achieve our this mixed compose-activity situation.
        // https://issuetracker.google.com/issues/271549886
        navigateBack = finishTerminationFlow,
      )
    }
  }
}
