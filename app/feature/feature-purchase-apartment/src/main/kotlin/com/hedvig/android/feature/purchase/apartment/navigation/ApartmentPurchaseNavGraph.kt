package com.hedvig.android.feature.purchase.apartment.navigation

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Failure
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Form
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.SelectTier
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Signing
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Success
import com.hedvig.android.feature.purchase.apartment.navigation.ApartmentPurchaseDestination.Summary
import com.hedvig.android.feature.purchase.apartment.ui.failure.PurchaseFailureDestination
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormDestination
import com.hedvig.android.feature.purchase.apartment.ui.form.ApartmentFormViewModel
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierDestination
import com.hedvig.android.feature.purchase.apartment.ui.offer.SelectTierViewModel
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningDestination
import com.hedvig.android.feature.purchase.apartment.ui.sign.SigningViewModel
import com.hedvig.android.feature.purchase.apartment.ui.success.PurchaseSuccessDestination
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryDestination
import com.hedvig.android.feature.purchase.apartment.ui.summary.PurchaseSummaryViewModel
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.apartmentPurchaseNavGraph(
  navController: NavController,
  popBackStack: () -> Unit,
  finishApp: () -> Unit,
  crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) {
  navgraph<ApartmentPurchaseGraphDestination>(
    startDestination = Form::class,
  ) {
    navdestination<Form> { backStackEntry ->
      val graphRoute = navController
        .getRouteFromBackStack<ApartmentPurchaseGraphDestination>(backStackEntry)
      val viewModel: ApartmentFormViewModel = koinViewModel {
        parametersOf(graphRoute.productName)
      }
      ApartmentFormDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { popBackStack() },
        onOffersReceived = { shopSessionId, offers ->
          navController.navigate(
            SelectTier(
              SelectTierParameters(
                shopSessionId = shopSessionId,
                offers = offers.offers.map { offer ->
                  TierOfferData(
                    offerId = offer.offerId,
                    tierDisplayName = offer.tierDisplayName,
                    tierDescription = offer.tierDescription,
                    grossAmount = offer.grossPrice.amount,
                    grossCurrencyCode = offer.grossPrice.currencyCode.name,
                    netAmount = offer.netPrice.amount,
                    netCurrencyCode = offer.netPrice.currencyCode.name,
                    usps = offer.usps,
                    exposureDisplayName = offer.exposureDisplayName,
                    deductibleDisplayName = offer.deductibleDisplayName,
                    hasDiscount = offer.hasDiscount,
                  )
                },
                productDisplayName = offers.productDisplayName,
              ),
            ),
          )
        },
      )
    }

    navdestination<SelectTier>(SelectTier) { backStackEntry ->
      val route = backStackEntry.toRoute<SelectTier>()
      val viewModel: SelectTierViewModel = koinViewModel {
        parametersOf(route.params)
      }
      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        onContinueToSummary = { params -> navController.navigate(Summary(params)) },
      )
    }

    navdestination<Summary>(Summary) { backStackEntry ->
      val route = backStackEntry.toRoute<Summary>()
      val viewModel: PurchaseSummaryViewModel = koinViewModel {
        parametersOf(route.params)
      }
      PurchaseSummaryDestination(
        viewModel = viewModel,
        navigateUp = dropUnlessResumed { navController.popBackStack() },
        navigateToSigning = { params -> navController.navigate(Signing(params)) },
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
      )
    }

    navdestination<Signing>(Signing) { backStackEntry ->
      val route = backStackEntry.toRoute<Signing>()
      val viewModel: SigningViewModel = koinViewModel {
        parametersOf(route.params)
      }
      SigningDestination(
        viewModel = viewModel,
        navigateToSuccess = { startDate ->
          crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully(
            CrossSellInfoType.Purchase,
          )
          navController.navigate(Success(startDate)) {
            typedPopUpTo<ApartmentPurchaseGraphDestination>({ inclusive = true })
          }
        },
        navigateToFailure = dropUnlessResumed { navController.navigate(Failure) },
      )
    }

    navdestination<Failure> {
      PurchaseFailureDestination(
        onRetry = dropUnlessResumed { navController.popBackStack() },
        close = dropUnlessResumed {
          if (!navController.typedPopBackStack<ApartmentPurchaseGraphDestination>(inclusive = true)) finishApp()
        },
      )
    }
  }

  navdestination<Success> { backStackEntry ->
    val route = backStackEntry.toRoute<Success>()
    PurchaseSuccessDestination(
      startDate = route.startDate,
      close = dropUnlessResumed {
        if (!navController.popBackStack()) finishApp()
      },
    )
  }
}
