package com.hedvig.android.feature.terminateinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceViewModel
import com.hedvig.android.feature.terminateinsurance.ui.TerminationDateDestination
import com.hedvig.android.feature.terminateinsurance.ui.TerminationSuccessDestination
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal sealed interface Destinations : Destination {
  @Serializable
  object TerminateInsurance : Destinations
}

internal sealed interface TerminateInsuranceDestinations : Destination {
  @Serializable
  object TerminationDate : TerminateInsuranceDestinations

  @Serializable
  object TerminationSuccess : TerminateInsuranceDestinations
}

internal fun NavGraphBuilder.terminateInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
  navigateUp: () -> Boolean,
  finishTerminationFlow: () -> Unit,
) {
  navigation<Destinations.TerminateInsurance>(
    startDestination = createRoutePattern<TerminateInsuranceDestinations.TerminationDate>(),
  ) {
    composable<TerminateInsuranceDestinations.TerminationDate> { navBackStackEntry ->
      val viewModel: TerminateInsuranceViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry) {
        parametersOf(insuranceId)
      }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToSuccessScreen = {
          navController.navigate(
            route = TerminateInsuranceDestinations.TerminationSuccess,
            navOptions = navOptions {
              popUpTo<Destinations.TerminateInsurance>()
            },
          )
        },
        navigateBack = {
          navController.navigateUp() || navigateUp()
        },
      )
    }
    composable<TerminateInsuranceDestinations.TerminationSuccess> {
      TerminationSuccessDestination(
        windowSizeClass = windowSizeClass,
        // Can't use navigateUp() from non-starting destination while it's the only one in the backstack due to:
        // https://issuetracker.google.com/issues/271549886
        navigateBack = finishTerminationFlow,
      )
    }
  }
}
