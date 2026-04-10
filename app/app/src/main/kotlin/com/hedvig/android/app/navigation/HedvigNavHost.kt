package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.media3.datasource.cache.SimpleCache
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import coil3.ImageLoader
import com.benasher44.uuid.Uuid
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseGraphDestination
import com.hedvig.android.feature.addon.purchase.navigation.addonPurchaseNavGraph
import com.hedvig.android.feature.change.tier.navigation.ChooseTierGraphDestination
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceDestination
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowDestination
import com.hedvig.android.feature.change.tier.navigation.changeTierGraph
import com.hedvig.android.feature.chat.navigation.ChatDestination
import com.hedvig.android.feature.chat.navigation.ChatDestinations
import com.hedvig.android.feature.chat.navigation.cbmChatGraph
import com.hedvig.android.feature.chip.id.navigation.ChipIdGraphDestination
import com.hedvig.android.feature.chip.id.navigation.chipIdGraph
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailDestination
import com.hedvig.android.feature.claim.details.navigation.claimDetailsGraph
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryDestination
import com.hedvig.android.feature.claimhistory.nav.claimHistoryGraph
import com.hedvig.android.feature.connect.payment.connectPaymentGraph
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountDestination
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountGraph
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.CoInsuredAddInfo
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.CoInsuredAddOrRemove
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredDestination.EditCoInsuredTriage
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredGraph
import com.hedvig.android.feature.forever.navigation.foreverGraph
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkConnectPayment
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTermination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.feature.help.center.helpCenterGraph
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.imageviewer.navigation.ImageViewer
import com.hedvig.android.feature.imageviewer.navigation.imageViewerGraph
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceGraphDestination
import com.hedvig.android.feature.insurance.certificate.navigation.insuranceEvidenceGraph
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.insuranceGraph
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.feature.movingflow.SelectContractForMoving
import com.hedvig.android.feature.movingflow.movingFlowGraph
import com.hedvig.android.feature.payments.navigation.paymentsGraph
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountDestination
import com.hedvig.android.feature.payoutaccount.navigation.payoutAccountGraph
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceGraphDestination
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateGraphDestination
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateGraph
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.typedPopBackStack
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.feature.claim.chat.ClaimChatDestination
import com.hedvig.feature.claim.chat.claimChatGraph
import com.hedvig.feature.remove.addons.AddonRemoveGraphDestination
import com.hedvig.feature.remove.addons.removeAddonsNavGraph

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  globalSnackBarState: GlobalSnackBarState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  externalNavigator: ExternalNavigator,
  finishApp: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  simpleVideoCache: SimpleCache,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val density = LocalDensity.current
  val navController = hedvigAppState.navController

  val navigateToConnectPayment = {
    navController.navigate(TrustlyDestination)
  }
  val navigateToInbox = {
    navController.navigate(ChatDestination)
  }
  val popBackStackOrFinish = {
    if (!navController.popBackStack()) {
      finishApp()
    }
  }

  fun navigateToNewConversation(builder: (NavOptionsBuilder.() -> Unit)? = null) {
    navController.navigate(ChatDestinations.Chat(Uuid.randomUUID().toString()), builder ?: {})
  }

  val navigateToConversation = { conversationId: String ->
    navController.navigate(ChatDestinations.Chat(conversationId))
  }

  fun navigateToMovingFlow(navOptions: NavOptionsBuilder.() -> Unit = {}) {
    hedvigAppState.navController.navigate(SelectContractForMoving, navOptions)
  }

  fun navigateToChangeTier(navOptions: NavOptionsBuilder.() -> Unit = {}) {
    hedvigAppState.navController.navigate(SelectContractForMoving, navOptions)
  }

  val onNavigateToImageViewer = { imageUrl: String, cacheKey: String ->
    hedvigAppState.navController.navigate(ImageViewer(imageUrl, cacheKey))
  }

  NavHost(
    navController = navController,
    startDestination = HomeDestination.Graph::class,
    route = RootGraph::class,
    modifier = modifier,
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    loginGraph(
      navController = navController,
      appVersionName = hedvigBuildConstants.appVersionName,
      urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
      openUrl = openUrl,
      onOpenEmailApp = externalNavigator::openEmailApp,
      onNavigateToLoggedIn = hedvigAppState::navigateToLoggedIn,
    )
    homeGraph(
      nestedGraphs = {
        nestedHomeGraphs(
          hedvigAppState = hedvigAppState,
          hedvigBuildConstants = hedvigBuildConstants,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          navController = navController,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          externalNavigator = externalNavigator,
          imageLoader = imageLoader,
          openUrl = openUrl,
          onNavigateToImageViewer = onNavigateToImageViewer,
          navigateToNewConversation = ::navigateToNewConversation,
          navigateToConversation = navigateToConversation,
          finishApp = finishApp,
        )
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onNavigateToInbox = {
        navigateToInbox()
      },
      onNavigateToNewConversation = {
        navigateToNewConversation()
      },
      navigateToClaimDetails = { claimId ->
        navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
      },
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navController.navigate(CoInsuredAddInfo(contractId, type))
      },
      navigateToHelpCenter = {
        navController.navigate(HelpCenterDestination)
      },
      navigateToClaimChat = {
        navController.navigate(
          ClaimChatDestination(
            messageId = null,
            isDevelopmentFlow = false,
          ),
        )
      },
      navigateToClaimChatInDevMode = {
        navController.navigate(
          ClaimChatDestination(
            messageId = null,
            isDevelopmentFlow = true,
          ),
        )
      },
      openAppSettings = externalNavigator::openAppSettings,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      navController = navController,
      navigateToContactInfo = {
        navController.navigate(ProfileDestination.ContactInfo)
      },
      imageLoader = imageLoader,
      navigateToChipIdScreen = {
        navController.navigate(ChipIdGraphDestination())
      },
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navController = navController,
          onNavigateToNewConversation = {
            navigateToNewConversation()
          },
          openUrl = openUrl,
          openPlayStore = externalNavigator::tryOpenPlayStore,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          navigateToInsurances = { navOptionsBuilder ->
            navController.navigate(InsurancesDestination.Graph, navOptionsBuilder)
          },
          navigateToMovingFlow = {
            navigateToMovingFlow {
              typedPopUpTo<TerminateInsuranceGraphDestination> {
                inclusive = true
              }
            }
          },
          closeTerminationFlow = {
            // If we fail to pop the backstack including TerminateInsuranceGraphDestination here it means we were deep
            //  linked into this screen only, and they do not wish to continue with the flow they were deep linked to.
            //  The right way to handle this is to simply finish the app as per the docs:
            //  https://developer.android.com/guide/navigation/backstack#handle-failure
            if (!navController.typedPopBackStack<TerminateInsuranceGraphDestination>(inclusive = true)) {
              finishApp()
            }
          },
          redirectToChangeTierFlow = { idWithIntent ->
            navController.navigate(
              ChooseTierGraphDestination(
                InsuranceCustomizationParameters(
                  insuranceId = idWithIntent.first,
                  activationDate = idWithIntent.second.activationDate,
                  quoteIds = idWithIntent.second.quotes.map {
                    it.id
                  },
                ),
              ),
            ) {
              typedPopUpTo<TerminateInsuranceGraphDestination> {
                inclusive = true
              }
            }
          },
        )
      },
      navController = navController,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      onNavigateToNewConversation = {
        navigateToNewConversation()
      },
      startMovingFlow = ::navigateToMovingFlow,
      startTerminationFlow = { data: CancelInsuranceData ->
        navController.navigate(
          TerminateInsuranceGraphDestination(insuranceId = data.contractId),
        )
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      imageLoader = imageLoader,
      startEditCoInsured = { contractId: String ->
        navController.navigate(CoInsuredAddOrRemove(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwners = { contractId: String ->
        navController.navigate(EditCoInsuredTriage(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToStartChangeTier = { contractId: String ->
        navController.navigate(
          StartTierFlowDestination(
            insuranceId = contractId,
          ),
        )
      },
      startEditCoInsuredAddMissingInfo = { contractId: String ->
        navController.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwnersAddMissingInfo = { contractId: String ->
        navController.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToAddonPurchaseFlow = { insuranceIds, availableAddon ->
        navController.navigate(
          AddonPurchaseGraphDestination(
            insuranceIds.map(ContractId::id),
            availableAddon?.displayName,
            AddonBannerSource.INSURANCES_TAB,
          ),
        )
      },
      onNavigateToRemoveAddon = { contractId, addonVariant ->
        navController.navigate(
          AddonRemoveGraphDestination(
            contractId,
            addonVariant,
          ),
        )
      },
      navigateToUpgradeAddon = { contractId, addonVariant ->
        navController.navigate(
          AddonPurchaseGraphDestination(
            listOfNotNull(contractId?.id),
            null,
            AddonBannerSource.INSURANCES_TAB,
          ),
        )
      },
      navigateToChipIdScreen = { contractId ->
        navController.navigate(ChipIdGraphDestination(contractId))
      },
    )
    foreverGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    paymentsGraph(
      navController = navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToPayoutAccount = { navController.navigate(PayoutAccountDestination.Graph) },
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
      onOpenChat = ::navigateToNewConversation,
    )
    payoutAccountGraph(
      navController = navController,
      globalSnackBarState = globalSnackBarState,
      navigateUp = navController::navigateUp,
    )
    profileGraph(
      settingsDestinationNestedGraphs = {
        deleteAccountGraph(hedvigDeepLinkContainer, navController)
      },
      navController = navController,
      globalSnackBarState = globalSnackBarState,
      nestedGraphs = {
        claimHistoryGraph(
          navigateUp = navController::navigateUp,
          navigateToClaimDetails = { claimId ->
            navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
          },
        )
      },
      popBackStackOrFinish = popBackStackOrFinish,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToAddMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navController.navigate(CoInsuredAddInfo(contractId, type))
      },
      navigateToDeleteAccountFeature = {
        navController.navigate(DeleteAccountDestination)
      },
      navigateToClaimHistory = {
        navController.navigate(ClaimHistoryDestination)
      },
      openAppSettings = externalNavigator::openAppSettings,
      onNavigateToNewConversation = {
        navigateToNewConversation()
      },
      onNavigateToTravelCertificate = {
        navController.navigate(TravelCertificateGraphDestination)
      },
      onNavigateToInsuranceEvidence = {
        navController.navigate(InsuranceEvidenceGraphDestination)
      },
      openUrl = openUrl,
      navigateToChipId = {
        navController.navigate(ChipIdGraphDestination())
      },
    )
    cbmChatGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      simpleVideoCache = simpleVideoCache,
      openUrl = openUrl,
      onNavigateToClaimDetails = { claimId ->
        logcat { "Navigating to claim details from chat" }
        navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
      },
      onNavigateToImageViewer = onNavigateToImageViewer,
      navController = navController,
    )
    addonPurchaseNavGraph(
      navController = navController,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      onNavigateToNewConversation = ::navigateToNewConversation,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onNavigateToChangeTier = { contractId ->
        navController.navigate(
          StartTierFlowDestination(insuranceId = contractId),
        )
      },
    )
    changeTierGraph(
      navController = navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onNavigateToNewConversation = ::navigateToNewConversation,
    )
    chipIdGraph(
      navController = navController,
      globalSnackBarState = globalSnackBarState,
      navigateUp = navController::navigateUp,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      popBackStackOrFinish = popBackStackOrFinish,
      goHome = {
          navController.navigate(HomeDestination.Graph) {
            popUpTo(ChipIdGraphDestination::class) { inclusive = true }
          }
      }
    )
    movingFlowGraph(
      navController = navController,
      goToChat = ::navigateToNewConversation,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    connectPaymentGraph(
      navController = navController,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    editCoInsuredGraph(navController, hedvigDeepLinkContainer = hedvigDeepLinkContainer)
    helpCenterGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navController = navController,
      onNavigateToQuickLink = onNavigateToQuickLink@{ quickLinkDestination ->
        val destination: Destination = when (quickLinkDestination) {
          QuickLinkChangeAddress -> {
            navigateToMovingFlow()
            return@onNavigateToQuickLink
          }

          is QuickLinkCoInsuredAddInfo -> {
            CoInsuredAddInfo(quickLinkDestination.contractId, CoInsuredFlowType.CoInsured)
          }

          is QuickLinkCoInsuredAddOrRemove -> {
            CoInsuredAddOrRemove(quickLinkDestination.contractId, CoInsuredFlowType.CoInsured)
          }

          is QuickLinkCoOwnerAddInfo -> {
            CoInsuredAddInfo(quickLinkDestination.contractId, CoInsuredFlowType.CoOwners)
          }

          is QuickLinkCoOwnerAddOrRemove -> {
            CoInsuredAddOrRemove(quickLinkDestination.contractId, CoInsuredFlowType.CoOwners)
          }

          QuickLinkConnectPayment -> {
            TrustlyDestination
          }

          QuickLinkTermination -> {
            TerminateInsuranceGraphDestination(null)
          }

          QuickLinkTravelCertificate -> {
            TravelCertificateGraphDestination
          }

          QuickLinkChangeTier -> {
            StartTierFlowChooseInsuranceDestination
          }

          QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured -> {
            EditCoInsuredTriage()
          }

          QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners -> {
            EditCoInsuredTriage(type = CoInsuredFlowType.CoOwners)
          }
        }
        navController.navigate(destination)
      },
      onNavigateToNewConversation = {
        navigateToNewConversation()
      },
      onNavigateToInbox = {
        navigateToInbox()
      },
      openUrl = openUrl,
      tryToDialPhone = externalNavigator::tryToDialPhone,
      imageLoader = imageLoader,
    )
    imageViewerGraph(navController, imageLoader)
    removeAddonsNavGraph(
      navController = hedvigAppState.navController,
      onNavigateToNewConversation = {
        navigateToNewConversation()
      },
    )
  }
}

private fun NavGraphBuilder.nestedHomeGraphs(
  hedvigAppState: HedvigAppState,
  hedvigBuildConstants: HedvigBuildConstants,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  navController: NavController,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: ((NavOptionsBuilder.() -> Unit)?) -> Unit,
  navigateToConversation: (String) -> Unit,
  finishApp: () -> Unit,
) {
  claimChatGraph(
    navController = navController,
    hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    openAppSettings = externalNavigator::openAppSettings,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToClaimDetails = { claimId: String ->
      navController.navigate(ClaimDetailDestination.ClaimOverviewDestination(claimId))
    },
    tryOpenPlayStore = externalNavigator::tryOpenPlayStore,
    openUrl = openUrl,
    tryToDialPhone = externalNavigator::tryToDialPhone,
    imageLoader = imageLoader,
    appPackageId = hedvigBuildConstants.appPackageId,
    onNavigateToNewConversation = {
      navigateToNewConversation(null)
    },
    openPlayStore = externalNavigator::tryOpenPlayStore,
  )
  claimDetailsGraph(
    navController = navController,
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateUp = navController::navigateUp,
    appPackageId = hedvigBuildConstants.appPackageId,
    navigateToConversation = { conversationId ->
      navigateToConversation(conversationId)
    },
    applicationId = hedvigBuildConstants.appPackageId,
    hedvigDeepLinkContainer = hedvigDeepLinkContainer,
  )
  travelCertificateGraph(
    navController = navController,
    applicationId = hedvigBuildConstants.appPackageId,
    hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    onNavigateToCoInsuredAddInfo = { contractId ->
      navController.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoInsured))
    },
    onNavigateToAddonPurchaseFlow = { ids ->
      navController.navigate(
        AddonPurchaseGraphDestination(
          insuranceIds = ids,
          preselectedAddonDisplayName = null,
          source = AddonBannerSource.TRAVEL_CERTIFICATES,
        ),
      )
    },
  )
  insuranceEvidenceGraph(
    navController = navController,
    applicationId = hedvigBuildConstants.appPackageId,
    hedvigDeepLinkContainer = hedvigDeepLinkContainer,
  )
}
