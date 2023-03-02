package com.hedvig.android.feature.cancelinsurance.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import com.hedvig.android.feature.cancelinsurance.CancelInsuranceViewModel
import com.hedvig.android.feature.cancelinsurance.InsuranceId
import com.hedvig.android.feature.cancelinsurance.ui.result.CancellationSuccessDestination
import com.hedvig.android.feature.cancelinsurance.ui.terminationdate.TerminationDateDestination
import com.hedvig.android.navigation.compose.typed.ext.composable
import com.hedvig.android.navigation.compose.typed.ext.popUpTo
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal sealed interface Destinations : Destination {
  @Serializable
  object CancelInsurance : Destinations
}

internal sealed interface CancelInsuranceDestinations : Destination {
  @Serializable
  data class TerminationDate(
    val insuranceId: InsuranceId,
  ) : CancelInsuranceDestinations

  @Serializable
  object TerminationSuccess : CancelInsuranceDestinations
}

internal fun NavGraphBuilder.cancelInsuranceGraph(
  windowSizeClass: WindowSizeClass,
  navController: NavHostController,
  insuranceId: InsuranceId,
) {
  navigation<Destinations.CancelInsurance>(
    startDestination = createRoutePattern<CancelInsuranceDestinations.TerminationDate>(),
  ) {
    composable<CancelInsuranceDestinations.TerminationDate>(
      extraNavArgumentConfiguration = mapOf(
        "insuranceId" to { defaultValue = Json.encodeToString(insuranceId) },
      ),
    ) { navBackStackEntry ->
      val viewModel: CancelInsuranceViewModel = koinViewModel(viewModelStoreOwner = navBackStackEntry) {
        parametersOf(this.insuranceId)
      }
      TerminationDateDestination(
        viewModel = viewModel,
        windowSizeClass = windowSizeClass,
        navigateToSuccessScreen = {
          navController.navigate(
            route = CancelInsuranceDestinations.TerminationSuccess,
            navOptions = navOptions {
              popUpTo(Destinations.CancelInsurance)
            },
          )
        },
        navigateBack = navController::popBackStack,
      )
    }
    composable<CancelInsuranceDestinations.TerminationSuccess> {
      CancellationSuccessDestination()
    }
  }
}
