package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.media3.datasource.cache.SimpleCache
import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.feature.chat.navigation.ChatDestinations.Chat
import com.hedvig.android.feature.chat.navigation.cbmChatGraph
import com.hedvig.android.feature.chip.id.navigation.ChipIdGraphDestination
import com.hedvig.android.feature.chip.id.navigation.chipIdGraph
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailDestination.ClaimOverviewDestination
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
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners
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
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.imageviewer.navigation.ImageViewer
import com.hedvig.android.feature.imageviewer.navigation.imageViewerGraph
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceGraphDestination
import com.hedvig.android.feature.insurance.certificate.navigation.insuranceEvidenceGraph
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.insuranceGraph
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.feature.movingflow.SelectContractForMoving
import com.hedvig.android.feature.movingflow.movingFlowGraph
import com.hedvig.android.feature.payments.navigation.paymentsGraph
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountDestination
import com.hedvig.android.feature.payoutaccount.navigation.payoutAccountGraph
import com.hedvig.android.feature.profile.navigation.ProfileDestination.ContactInfo
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceGraphDestination
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateGraphDestination
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateGraph
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.HedvigNavDisplay
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.feature.claim.chat.ClaimChatDestination
import com.hedvig.feature.claim.chat.claimChatGraph
import com.hedvig.feature.remove.addons.AddonRemoveGraphDestination
import com.hedvig.feature.remove.addons.removeAddonsNavGraph

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  globalSnackBarState: GlobalSnackBarState,
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
  val navigator = hedvigAppState.backStacks.navigator

  val navigateToConnectPayment = { navigator.navigate(TrustlyDestination) }
  val navigateToPayoutAccount = { navigator.navigate(PayoutAccountDestination.Graph) }
  val navigateToInbox = { navigator.navigate(ChatDestination) }
  val navigateToNewConversation = { navigator.navigate(Chat(Uuid.randomUUID().toString())) }
  val navigateToConversation = { conversationId: String -> navigator.navigate(Chat(conversationId)) }
  val navigateToMovingFlow = { navigator.navigate(SelectContractForMoving) }
  val onNavigateToImageViewer = { imageUrl: String, cacheKey: String ->
    navigator.navigate(ImageViewer(imageUrl, cacheKey))
  }
  val popBackStackOrFinish = {
    if (!navigator.popBackStack()) {
      finishApp()
    }
    Unit
  }

  val density = LocalDensity.current
  HedvigNavDisplay(
    backStack = hedvigAppState.backStacks.currentBackStack,
    onBack = { popBackStackOrFinish() },
    enterTransition = MotionDefaults.sharedXAxisEnter(density),
    exitTransition = MotionDefaults.sharedXAxisExit(density),
    popEnterTransition = MotionDefaults.sharedXAxisPopEnter(density),
    popExitTransition = MotionDefaults.sharedXAxisPopExit(density),
    modifier = modifier,
  ) {
    loginGraph(
      navigator = navigator,
      appVersionName = hedvigBuildConstants.appVersionName,
      urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
      openUrl = openUrl,
      onOpenEmailApp = externalNavigator::openEmailApp,
      onNavigateToLoggedIn = hedvigAppState::navigateToLoggedIn,
    )
    homeGraph(
      nestedGraphs = {
        nestedHomeGraphs(
          navigator = navigator,
          appPackageId = hedvigBuildConstants.appPackageId,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          externalNavigator = externalNavigator,
          imageLoader = imageLoader,
          openUrl = openUrl,
          onNavigateToImageViewer = onNavigateToImageViewer,
          navigateToNewConversation = navigateToNewConversation,
          navigateToConversation = navigateToConversation,
        )
      },
      navigator = navigator,
      onNavigateToInbox = navigateToInbox,
      onNavigateToNewConversation = navigateToNewConversation,
      navigateToClaimDetails = { claimId -> navigator.navigate(ClaimOverviewDestination(claimId)) },
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToPayoutAccount,
      navigateToContactInfo = { navigator.navigate(ContactInfo) },
      navigateToMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navigator.navigate(CoInsuredAddInfo(contractId, type))
      },
      navigateToHelpCenter = { navigator.navigate(HelpCenterDestination) },
      navigateToClaimChat = {
        navigator.navigate(ClaimChatDestination(messageId = null, isDevelopmentFlow = false))
      },
      navigateToClaimChatInDevMode = {
        navigator.navigate(ClaimChatDestination(messageId = null, isDevelopmentFlow = true))
      },
      navigateToChipIdScreen = { navigator.navigate(ChipIdGraphDestination()) },
      openAppSettings = externalNavigator::openAppSettings,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      imageLoader = imageLoader,
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navigator = navigator,
          onNavigateToNewConversation = navigateToNewConversation,
          openUrl = openUrl,
          openPlayStore = externalNavigator::tryOpenPlayStore,
          navigateToInsurances = {
            navigator.popUpTo<TerminateInsuranceGraphDestination>(inclusive = true)
            hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Insurances)
          },
          navigateToMovingFlow = {
            navigator.navigate<TerminateInsuranceGraphDestination>(SelectContractForMoving, inclusive = true)
          },
          closeTerminationFlow = {
            // If we fail to pop the backstack including TerminateInsuranceGraphDestination here it means we were deep
            //  linked into this screen only, and they do not wish to continue with the flow they were deep linked to.
            //  The right way to handle this is to simply finish the app as per the docs:
            //  https://developer.android.com/guide/navigation/backstack#handle-failure
            if (navigator.findLastOrNull<TerminateInsuranceGraphDestination>() != null) {
              navigator.popUpTo<TerminateInsuranceGraphDestination>(inclusive = true)
            } else {
              finishApp()
            }
          },
          redirectToChangeTierFlow = { idWithIntent ->
            navigator.navigate<TerminateInsuranceGraphDestination>(
              ChooseTierGraphDestination(
                InsuranceCustomizationParameters(
                  insuranceId = idWithIntent.first,
                  activationDate = idWithIntent.second.activationDate,
                  quoteIds = idWithIntent.second.quotes.map { it.id },
                ),
              ),
              inclusive = true,
            )
          },
        )
      },
      navigator = navigator,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      onNavigateToNewConversation = navigateToNewConversation,
      startMovingFlow = navigateToMovingFlow,
      startTerminationFlow = { data: CancelInsuranceData ->
        navigator.navigate(TerminateInsuranceGraphDestination(insuranceId = data.contractId))
      },
      imageLoader = imageLoader,
      startEditCoInsured = { contractId: String ->
        navigator.navigate(CoInsuredAddOrRemove(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwners = { contractId: String ->
        navigator.navigate(EditCoInsuredTriage(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToStartChangeTier = { contractId: String ->
        navigator.navigate(StartTierFlowDestination(insuranceId = contractId))
      },
      startEditCoInsuredAddMissingInfo = { contractId: String ->
        navigator.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwnersAddMissingInfo = { contractId: String ->
        navigator.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToAddonPurchaseFlow = { insuranceIds, availableAddon ->
        navigator.navigate(
          AddonPurchaseGraphDestination(
            insuranceIds.map(ContractId::id),
            availableAddon?.displayName,
            AddonBannerSource.INSURANCES_TAB,
          ),
        )
      },
      onNavigateToRemoveAddon = { contractId, addonVariant ->
        navigator.navigate(AddonRemoveGraphDestination(contractId, addonVariant))
      },
      navigateToUpgradeAddon = { contractId, _ ->
        navigator.navigate(
          AddonPurchaseGraphDestination(
            listOfNotNull(contractId?.id),
            null,
            AddonBannerSource.INSURANCES_TAB,
          ),
        )
      },
      navigateToChipIdScreen = { contractId -> navigator.navigate(ChipIdGraphDestination(contractId)) },
    )
    foreverGraph(
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    paymentsGraph(
      navigator = navigator,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToPayoutAccount = navigateToPayoutAccount,
      openConversation = navigateToNewConversation,
    )
    payoutAccountGraph(
      navigator = navigator,
      globalSnackBarState = globalSnackBarState,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateUp = navigator::navigateUp,
    )
    profileGraph(
      settingsDestinationNestedGraphs = {
        deleteAccountGraph(navigator)
      },
      nestedGraphs = {
        claimHistoryGraph(
          navigateUp = navigator::navigateUp,
          navigateToClaimDetails = { claimId -> navigator.navigate(ClaimOverviewDestination(claimId)) },
        )
      },
      globalSnackBarState = globalSnackBarState,
      navigator = navigator,
      popBackStackOrFinish = popBackStackOrFinish,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToPayoutAccount,
      navigateToAddMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navigator.navigate(CoInsuredAddInfo(contractId, type))
      },
      navigateToDeleteAccountFeature = { navigator.navigate(DeleteAccountDestination) },
      navigateToClaimHistory = { navigator.navigate(ClaimHistoryDestination) },
      openAppSettings = externalNavigator::openAppSettings,
      onNavigateToNewConversation = navigateToNewConversation,
      onNavigateToTravelCertificate = { navigator.navigate(TravelCertificateGraphDestination) },
      onNavigateToInsuranceEvidence = { navigator.navigate(InsuranceEvidenceGraphDestination) },
      openUrl = openUrl,
      navigateToChipId = { navigator.navigate(ChipIdGraphDestination()) },
      languageService = languageService,
    )
    cbmChatGraph(
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      simpleVideoCache = simpleVideoCache,
      openUrl = openUrl,
      onNavigateToClaimDetails = { claimId ->
        logcat { "Navigating to claim details from chat" }
        navigator.navigate(ClaimOverviewDestination(claimId))
      },
      onNavigateToImageViewer = onNavigateToImageViewer,
      navigator = navigator,
    )
    addonPurchaseNavGraph(
      navigator = navigator,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      onNavigateToNewConversation = navigateToNewConversation,
      onNavigateToChangeTier = { contractId ->
        navigator.navigate(StartTierFlowDestination(insuranceId = contractId))
      },
    )
    changeTierGraph(
      navigator = navigator,
      onNavigateToNewConversation = navigateToNewConversation,
    )
    chipIdGraph(
      navigator = navigator,
      globalSnackBarState = globalSnackBarState,
      navigateUp = navigator::navigateUp,
      popBackStackOrFinish = popBackStackOrFinish,
      goHome = {
        navigator.popUpTo<ChipIdGraphDestination>(inclusive = true)
        hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Home)
      },
    )
    movingFlowGraph(
      navigator = navigator,
      goToChat = navigateToNewConversation,
    )
    connectPaymentGraph(navigator = navigator)
    editCoInsuredGraph(navigator)
    helpCenterGraph(
      navigator = navigator,
      onNavigateUp = navigator::navigateUp,
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

          ChooseInsuranceForEditCoInsured -> {
            EditCoInsuredTriage()
          }

          ChooseInsuranceForEditCoOwners -> {
            EditCoInsuredTriage(type = CoInsuredFlowType.CoOwners)
          }
        }
        navigator.navigate(destination)
      },
      onNavigateToNewConversation = navigateToNewConversation,
      onNavigateToInbox = navigateToInbox,
      openUrl = openUrl,
      tryToDialPhone = externalNavigator::tryToDialPhone,
      imageLoader = imageLoader,
    )
    imageViewerGraph(navigator, imageLoader)
    removeAddonsNavGraph(navigator = navigator)
  }
}

private fun EntryProviderScope<Destination>.nestedHomeGraphs(
  navigator: Navigator,
  appPackageId: String,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
  navigateToConversation: (String) -> Unit,
) {
  claimChatGraph(
    navigator = navigator,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    openAppSettings = externalNavigator::openAppSettings,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToClaimDetails = { claimId: String -> navigator.navigate(ClaimOverviewDestination(claimId)) },
    tryOpenPlayStore = externalNavigator::tryOpenPlayStore,
    openUrl = openUrl,
    tryToDialPhone = externalNavigator::tryToDialPhone,
    imageLoader = imageLoader,
    appPackageId = appPackageId,
    onNavigateToNewConversation = navigateToNewConversation,
    openPlayStore = externalNavigator::tryOpenPlayStore,
  )
  claimDetailsGraph(
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateUp = navigator::navigateUp,
    appPackageId = appPackageId,
    navigateToConversation = { conversationId -> navigateToConversation(conversationId) },
    navigator = navigator,
    applicationId = appPackageId,
  )
  travelCertificateGraph(
    navigator = navigator,
    applicationId = appPackageId,
    onNavigateToCoInsuredAddInfo = { contractId ->
      navigator.navigate(CoInsuredAddInfo(contractId, CoInsuredFlowType.CoInsured))
    },
    onNavigateToAddonPurchaseFlow = { ids ->
      navigator.navigate(
        AddonPurchaseGraphDestination(
          insuranceIds = ids,
          preselectedAddonDisplayName = null,
          source = AddonBannerSource.TRAVEL_CERTIFICATES,
        ),
      )
    },
  )
  insuranceEvidenceGraph(
    navigator = navigator,
    applicationId = appPackageId,
  )
}
