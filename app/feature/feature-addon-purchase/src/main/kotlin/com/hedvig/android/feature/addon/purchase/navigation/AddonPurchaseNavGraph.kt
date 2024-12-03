package com.hedvig.android.feature.addon.purchase.navigation
//
//import androidx.navigation.NavController
//import androidx.navigation.NavGraphBuilder
//import com.hedvig.android.navigation.compose.navdestination
//import com.hedvig.android.navigation.compose.navgraph
//import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
//
//import com.hedvig.android.navigation.core.Navigator
//
//import org.koin.androidx.compose.koinViewModel
//import org.koin.core.parameter.parametersOf
//
//fun NavGraphBuilder.addonPurchaseNavGraph(navigator: Navigator, navController: NavController) {
//  navgraph<AddonPurchaseGraphDestination>(
//    startDestination =
//  ) {
//
//  }
//
//
//  navgraph<ChooseTierGraphDestination>(
//    startDestination = ChooseTierDestination.SelectTierAndDeductible::class,
//    destinationNavTypeAware = ChooseTierGraphDestination,
//  ) {
//    navdestination<ChooseTierDestination.SelectTierAndDeductible> { backStackEntry ->
//      val chooseTierGraphDestination = navController
//        .getRouteFromBackStack<ChooseTierGraphDestination>(backStackEntry)
//      val viewModel: SelectCoverageViewModel = koinViewModel {
//        parametersOf(chooseTierGraphDestination.parameters)
//      }
//      SelectTierDestination(
//        viewModel = viewModel,
//        navigateUp = navigator::navigateUp,
//        navigateToSummary = { quote ->
//          navigator.navigateUnsafe(
//            ChooseTierDestination.Summary(
//              SummaryParameters(
//                quoteIdToSubmit = quote.id,
//                activationDate = chooseTierGraphDestination.parameters.activationDate,
//                insuranceId = chooseTierGraphDestination.parameters.insuranceId,
//              ),
//            ),
//          )
//        },
//        popBackStack = {
//          navigator.popBackStack()
//        },
//        navigateToComparison = { listOfQuotes, selectedTerms ->
//          navigator.navigateUnsafe(
//            ChooseTierDestination.Comparison(
//              ComparisonParameters(
//                termsIds = listOfQuotes.map {
//                  it.productVariant.termsVersion
//                },
//                selectedTermsVersion = selectedTerms,
//              ),
//            ),
//          )
//        },
//      )
//    }
