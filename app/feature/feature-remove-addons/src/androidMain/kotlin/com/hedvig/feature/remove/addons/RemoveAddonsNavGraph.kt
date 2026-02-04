package com.hedvig.feature.remove.addons

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.compose.typed.getRouteFromBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonDestination
import com.hedvig.feature.remove.addons.ui.SelectInsuranceToRemoveAddonViewModel

@Serializable
data class AddonRemoveGraphDestination(
  val params: AddonRemoveGraphDestinationParams?
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<AddonRemoveGraphDestinationParams>())
  }
}

@Serializable
data class AddonRemoveGraphDestinationParams(
  val insuranceId: String,
  val addonId: String?,
)

internal sealed interface AddonRemoveDestination {
  @Serializable
  data object ChooseInsuranceDestination : AddonRemoveDestination, Destination

  @Serializable
  data class ChooseAddonDestination(
    val params: AddonRemoveGraphDestinationParams?
  ) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<AddonRemoveGraphDestinationParams>())
    }
  }


  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : AddonRemoveDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : AddonRemoveDestination, Destination
}

@Serializable
internal data class SummaryParameters(
  val contractId: String,
   val addonIds: List<String>,
  // val productVariant: ProductVariant,
  //val chosenAddonsIds: List<String>,
  val activationDate: LocalDate,
  val notificationMessage: String?,
)

fun NavGraphBuilder.removeAddonsNavGraph(
  navigator: Navigator,
  navController: NavController,
  onNavigateToNewConversation: (NavBackStackEntry) -> Unit,
)  {
  navgraph<AddonRemoveGraphDestination>(
    startDestination = AddonRemoveDestination.ChooseInsuranceDestination::class,
  ) {
    navdestination<AddonRemoveDestination.ChooseInsuranceDestination> { backStackEntry ->
      val graphDestination = navController
        .getRouteFromBackStack<AddonRemoveGraphDestination>(backStackEntry)
      if (graphDestination.params!=null) {
        LaunchedEffect(Unit) {
          navigator.navigateUnsafe(AddonRemoveDestination.ChooseAddonDestination(
            graphDestination.params)) {
            typedPopUpTo<AddonRemoveDestination.ChooseInsuranceDestination>(
              { inclusive = true })
          }
        }
      } else {
        val viewModel: SelectInsuranceToRemoveAddonViewModel = koinViewModel() //TODO
        SelectInsuranceToRemoveAddonDestination(
          viewModel = viewModel,
          navigateUp = navigator::navigateUp,
          navigateToChooseAddon = { chosenInsuranceId: String ->
            navigator.navigateUnsafe(AddonRemoveDestination.ChooseAddonDestination(
              AddonRemoveGraphDestinationParams(
                insuranceId = chosenInsuranceId,
                addonId = null
              )
            ))
          },
        )
      }
    }

    navdestination<AddonRemoveDestination.ChooseAddonDestination> { backStackEntry ->
      //TODO
    }

    navdestination<AddonRemoveDestination.Summary> { backStackEntry ->
      //TODO
    }

    navdestination<AddonRemoveDestination.SubmitFailure> { backStackEntry ->
      //TODO
    }

    navdestination<AddonRemoveDestination.SubmitSuccess> { backStackEntry ->
      //TODO
    }
  }
}
