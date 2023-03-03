package com.hedvig.android.feature.cancelinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.hedvig.android.feature.cancelinsurance.CancelInsuranceViewModel
import com.hedvig.android.feature.cancelinsurance.InsuranceId
import com.hedvig.android.feature.cancelinsurance.ui.result.CancellationSuccessDestination
import com.hedvig.android.feature.cancelinsurance.ui.terminationdate.TerminationDateDestination
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
  object CancelInsurance : Destinations
}

internal sealed interface CancelInsuranceDestinations : Destination {
  @Serializable
  object TerminationDate : CancelInsuranceDestinations

  @Serializable
  object TerminationSuccess : CancelInsuranceDestinations
}

internal fun NavGraphBuilder.cancelInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
  navigateUp: () -> Boolean,
) {
  navigation<Destinations.CancelInsurance>(
    startDestination = createRoutePattern<CancelInsuranceDestinations.TerminationDate>(),
  ) {
    composable<CancelInsuranceDestinations.TerminationDate> { navBackStackEntry ->
      val viewModel: CancelInsuranceViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry) {
        parametersOf(insuranceId)
      }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToSuccessScreen = {
          navController.navigate(
            route = CancelInsuranceDestinations.TerminationSuccess,
            navOptions = navOptions {
              popUpTo<Destinations.CancelInsurance>()
            },
          )
        },
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
    composable<CancelInsuranceDestinations.TerminationSuccess> {
      CancellationSuccessDestination(
        windowSizeClass = windowSizeClass,
        navigateBack = { navController.navigateUp() || navigateUp() },
      )
    }
  }
}
