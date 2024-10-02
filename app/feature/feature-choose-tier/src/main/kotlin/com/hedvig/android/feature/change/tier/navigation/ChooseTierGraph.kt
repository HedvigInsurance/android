package com.hedvig.android.feature.change.tier.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonDestination
import com.hedvig.android.feature.change.tier.ui.comparison.ComparisonViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageViewModel
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectTierDestination
import com.hedvig.android.feature.change.tier.ui.stepsummary.ChangeTierSummaryDestination
import com.hedvig.android.feature.change.tier.ui.stepsummary.SummaryViewModel
import com.hedvig.android.feature.change.tier.ui.sucess.SuccessScreen
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.changeTierGraph(
  navigator: Navigator,
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
  openUrl: (String) -> Unit, // todo: maybe needed for comparison for now?
) {
  navgraph<ChooseTierGraphDestination>(
    startDestination = ChooseTierDestination.SelectTierAndDeductible::class,
  ) {
    navdestination<ChooseTierDestination.SelectTierAndDeductible> { backStackEntry ->
      val chooseTierGraphDestination = navController
        .getRouteFromBackStack<ChooseTierGraphDestination>(backStackEntry)
      val viewModel: SelectCoverageViewModel = koinViewModel {
        parametersOf(chooseTierGraphDestination.parameters)
      }
      SelectTierDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToSummary = { quote ->
          navigator.navigateUnsafe(ChooseTierDestination.Summary(quote.id)) // todo: Unsafe???
        },
        navigateToComparison = { listOfQuotes ->
          navigator.navigateUnsafe(ChooseTierDestination.Comparison(listOfQuotes.map { it.id })) // todo: Unsafe???
        },
      )
    }

    navdestination<ChooseTierDestination.Comparison> { _ ->
      val viewModel: ComparisonViewModel = koinViewModel {
        parametersOf(this.quoteIds)
      }
      ComparisonDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }

    navdestination<ChooseTierDestination.Summary> { backStackEntry ->
      val viewModel: SummaryViewModel = koinViewModel {
        parametersOf(quoteIdToSubmit)
      }
      ChangeTierSummaryDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToNewConversation = {
          onNavigateToNewConversation(backStackEntry)
        },
        openUrl = openUrl,
      )
    }

    navdestination<ChooseTierDestination.ChangingTierSuccess> { _ ->
      SuccessScreen(
        activationDate,
        navigateUp = navigator::navigateUp,
      )
    }
  }
}
