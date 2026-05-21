package com.hedvig.android.feature.insurances.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.insurances.data.AvailableAddon
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.HomePickerDialog
import com.hedvig.android.feature.insurances.insurance.InsuranceDestination
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailDestination
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailViewModel
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsDestination
import com.hedvig.android.feature.insurances.terminatedcontracts.TerminatedContractsViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import java.net.URLDecoder
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.insuranceGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  navController: NavController,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  startMovingFlow: () -> Unit,
  onNavigateToStartChangeTier: (contractId: String) -> Unit,
  startTerminationFlow: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  startEditCoInsured: (contractId: String) -> Unit,
  startEditCoOwners: (contractId: String) -> Unit,
  startEditCoInsuredAddMissingInfo: (contractId: String) -> Unit,
  startEditCoOwnersAddMissingInfo: (contractId: String) -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<ContractId>, AvailableAddon?) -> Unit,
  onNavigateToRemoveAddon: (ContractId?, AddonVariant?) -> Unit,
  navigateToUpgradeAddon: (ContractId?, AddonVariant?) -> Unit,
  onNavigateToApartmentPurchase: (productName: String) -> Unit,
  onNavigateToCarPurchase: (productName: String) -> Unit,
  onNavigateToPetPurchase: () -> Unit,
  onNavigateToHousePurchase: (productName: String) -> Unit,
) {
  navgraph<InsurancesDestination.Graph>(
    startDestination = InsurancesDestination.Insurances::class,
  ) {
    nestedGraphs()
    navdestination<InsurancesDestination.Insurances>(
      deepLinks = navDeepLinks(
        hedvigDeepLinkContainer.insurances,
        hedvigDeepLinkContainer.contractWithoutContractId,
      ),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      var showHomePicker by rememberSaveable { mutableStateOf(false) }
      val viewModel: InsuranceViewModel = koinViewModel()
      InsuranceDestination(
        viewModel = viewModel,
        onInsuranceCardClick = dropUnlessResumed { contractId: String ->
          navController.navigate(InsurancesDestinations.InsuranceContractDetail(contractId))
        },
        onCrossSellClick = dropUnlessResumed { url: String ->
          val decoded = try {
            URLDecoder.decode(url, "UTF-8")
          } catch (_: Exception) {
            url
          }
          val lower = decoded.lowercase()
          when {
            "fritidshusforsakring" in lower || "vacation-home" in lower -> {
              onNavigateToHousePurchase("SE_VACATION_HOME")
            }

            "villaforsakring" in lower || "home-insurance/house" in lower -> {
              onNavigateToHousePurchase("SE_HOUSE")
            }

            "bostadsratt" in lower || "home-insurance/homeowner" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
            }

            "hyresratt" in lower || "home-insurance/rental" in lower -> {
              onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
            }

            "hemforsakring" in lower || "home-insurance" in lower -> {
              showHomePicker = true
            }

            "car-insurance" in lower || "bilforsakring" in lower -> {
              onNavigateToCarPurchase("SE_CAR")
            }

            "pet-insurance" in lower || "djurforsakring" in lower -> {
              onNavigateToPetPurchase()
            }

            else -> {
              openUrl(url)
            }
          }
        },
        navigateToCancelledInsurances = dropUnlessResumed {
          navController.navigate(InsurancesDestinations.TerminatedInsurances)
        },
        onNavigateToMovingFlow = dropUnlessResumed { startMovingFlow() },
        imageLoader = imageLoader,
        onNavigateToAddonPurchaseFlow = dropUnlessResumed { ids: List<ContractId> ->
          onNavigateToAddonPurchaseFlow(ids, null)
        },
      )
      if (showHomePicker) {
        HomePickerDialog(
          onDismiss = { showHomePicker = false },
          onSelectApartmentRent = {
            showHomePicker = false
            onNavigateToApartmentPurchase("SE_APARTMENT_RENT")
          },
          onSelectApartmentBrf = {
            showHomePicker = false
            onNavigateToApartmentPurchase("SE_APARTMENT_BRF")
          },
          onSelectVilla = {
            showHomePicker = false
            onNavigateToHousePurchase("SE_HOUSE")
          },
        )
      }
    }
    navdestination<InsurancesDestinations.InsuranceContractDetail>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.contract),
    ) {
      val contractDetail = this
      val viewModel: ContractDetailViewModel = koinViewModel { parametersOf(contractDetail.contractId) }
      ContractDetailDestination(
        viewModel = viewModel,
        onEditCoInsuredClick = dropUnlessResumed { contractId: String -> startEditCoInsured(contractId) },
        onEditCoOwnersClick = dropUnlessResumed { contractId: String -> startEditCoOwners(contractId) },
        onMissingCoInsuredInfoClick = dropUnlessResumed { contractId: String ->
          startEditCoInsuredAddMissingInfo(contractId)
        },
        onMissingCoOwnersInfoClick = dropUnlessResumed { contractId: String ->
          startEditCoOwnersAddMissingInfo(contractId)
        },
        onChangeAddressClick = dropUnlessResumed { startMovingFlow() },
        onCancelInsuranceClick = dropUnlessResumed { cancelInsuranceData: CancelInsuranceData ->
          startTerminationFlow(cancelInsuranceData)
        },
        onNavigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        openUrl = openUrl,
        navigateUp = navController::navigateUp,
        navigateBack = navController::popBackStack,
        imageLoader = imageLoader,
        onChangeTierClick = dropUnlessResumed { contractId: String ->
          onNavigateToStartChangeTier(contractId)
        },
        navigateToRemoveAddon = onNavigateToRemoveAddon,
        navigateToUpgradeAddon = navigateToUpgradeAddon,
        navigateToAddAddon = { availableAddon ->
          onNavigateToAddonPurchaseFlow(listOf(availableAddon.relatedContractId), availableAddon)
        },
      )
    }
    navdestination<InsurancesDestinations.TerminatedInsurances> {
      val viewModel: TerminatedContractsViewModel = koinViewModel()
      TerminatedContractsDestination(
        viewModel = viewModel,
        navigateToContractDetail = dropUnlessResumed { contractId: String ->
          navController.navigate(InsurancesDestinations.InsuranceContractDetail(contractId))
        },
        navigateUp = navController::navigateUp,
        imageLoader = imageLoader,
      )
    }
  }
}
