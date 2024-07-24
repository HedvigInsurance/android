package com.hedvig.android.feature.changeaddress.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.changeaddress.destination.ChangeAddressResultDestination
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressDestination
import com.hedvig.android.feature.changeaddress.destination.enternewaddress.EnterNewAddressViewModel
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationDestination
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationViewModel
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferDestination
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferViewModel
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeDestination
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeViewModel
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.AddressResult
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.EnterNewAddress
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.EnterVillaInformation
import com.hedvig.android.feature.changeaddress.navigation.ChangeAddressDestination.Offer
import com.hedvig.android.navigation.core.AppDestination.ChangeAddress
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import com.kiwi.navigationcompose.typed.popUpTo
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navigation<ChangeAddress>(
    startDestination = createRoutePattern<ChangeAddressDestination.SelectHousingType>(),
  ) {
    composable<ChangeAddressDestination.SelectHousingType> { _ ->
      val viewModel: SelectHousingTypeViewModel = koinViewModel()
      SelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        navigateToEnterNewAddressDestination = { params ->
          navController.navigate(EnterNewAddress(params))
        },
      )
    }

    composable<EnterNewAddress> { _ ->
      val viewModel: EnterNewAddressViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      EnterNewAddressDestination(
        viewModel = viewModel,
        onNavigateToVillaInformationDestination = { newAddressParameters ->
          navController.navigate(EnterVillaInformation(newAddressParameters))
        },
        navigateUp = navController::navigateUp,
        onNavigateToOfferDestination = { movingParameters ->
          navController.navigate(Offer(movingParameters))
        },
      )
    }

    composable<EnterVillaInformation> { _ ->
      val viewModel: EnterVillaInformationViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      EnterVillaInformationDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onNavigateToOfferDestination = { params ->
          navController.navigate(Offer(params))
        },
      )
    }

    composable<Offer> { backStackEntry ->
      val viewModel: ChangeAddressOfferViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
        navigateUp = navController::navigateUp,
        onChangeAddressResult = { movingDate ->
          navController.navigate(AddressResult(movingDate)) {
            popUpTo<ChangeAddress> {
              inclusive = true
            }
          }
        },
        openUrl = openUrl,
      )
    }
  }
  composable<AddressResult> {
    ChangeAddressResultDestination(
      movingDate = movingDate,
      popBackstack = navController::popBackStack,
    )
  }
}
