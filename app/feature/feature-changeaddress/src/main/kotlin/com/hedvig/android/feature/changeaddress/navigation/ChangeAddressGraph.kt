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
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.AppDestination.ChangeAddress
import com.kiwi.navigationcompose.typed.navigate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeAddressGraph(
  navController: NavController,
  navigator: Navigator,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit,
) {
  navgraph<AppDestination.ChangeAddress>(
    startDestination = ChangeAddressDestination.SelectHousingType::class,
  ) {
    navdestination<ChangeAddressDestination.SelectHousingType> { _ ->
      val viewModel: SelectHousingTypeViewModel = koinViewModel()
      SelectHousingTypeDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToEnterNewAddressDestination = { params ->
          navigator.navigateUnsafe(ChangeAddressDestination.EnterNewAddress(params))
        },
      )
    }

    navdestination<ChangeAddressDestination.EnterNewAddress> { _ ->
      val viewModel: EnterNewAddressViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      EnterNewAddressDestination(
        viewModel = viewModel,
        onNavigateToVillaInformationDestination = { newAddressParameters ->
          with(navigator) {
            navBackStackEntry.navigate(EnterVillaInformation(newAddressParameters))
          }
        },
        navigateUp = navigator::navigateUp,
        onNavigateToOfferDestination = { movingParameters ->
          navigator.navigateUnsafe(Offer(movingParameters))
        },
      )
    }

    navdestination<ChangeAddressDestination.EnterVillaInformation> { _ ->
      val viewModel: EnterVillaInformationViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      EnterVillaInformationDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToOfferDestination = { params ->
          navigator.navigateUnsafe(Offer(params))
        },
      )
    }

    navdestination<ChangeAddressDestination.Offer> { backStackEntry ->
      val viewModel: ChangeAddressOfferViewModel = koinViewModel { parametersOf(previousDestinationParameters) }
      ChangeAddressOfferDestination(
        viewModel = viewModel,
        onNavigateToNewConversation = { onNavigateToNewConversation(backStackEntry) },
        navigateUp = navigator::navigateUp,
        onChangeAddressResult = { movingDate ->
          navigator.navigateUnsafe(AddressResult(movingDate)) {
            typedPopUpTo<ChangeAddress> {
              inclusive = true
            }
          }
        },
        openUrl = openUrl,
      )
    }
  }
  navdestination<ChangeAddressDestination.AddressResult>(
    ChangeAddressDestination.AddressResult,
  ) { navBackStackEntry ->
    ChangeAddressResultDestination(
      movingDate = movingDate,
      popBackstack = navigator::popBackStack,
    )
  }
}
