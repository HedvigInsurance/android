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
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseKey
import com.hedvig.android.feature.addon.purchase.navigation.addonPurchaseNavGraph
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceKey
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowKey
import com.hedvig.android.feature.change.tier.navigation.changeTierGraph
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.chat.navigation.cbmChatGraph
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.feature.chip.id.navigation.chipIdGraph
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.claim.details.navigation.claimDetailsGraph
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryDestination
import com.hedvig.android.feature.claimhistory.nav.claimHistoryGraph
import com.hedvig.android.feature.connect.payment.connectPaymentGraph
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountKey
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountGraph
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
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
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.imageviewer.navigation.imageViewerGraph
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurance.certificate.navigation.insuranceEvidenceGraph
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.insuranceGraph
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.movingFlowGraph
import com.hedvig.android.feature.payments.navigation.paymentsGraph
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountKey
import com.hedvig.android.feature.payoutaccount.navigation.payoutAccountGraph
import com.hedvig.android.feature.profile.navigation.ContactInfoKey
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateGraph
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.HedvigNavDisplay
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigate
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.feature.claim.chat.ClaimChatKey
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

  val navigateToConnectPayment = { navigator.navigate(TrustlyKey) }
  val navigateToPayoutAccount = { navigator.navigate(PayoutAccountKey) }
  val navigateToInbox = { navigator.navigate(InboxKey) }
  val navigateToNewConversation = { navigator.navigate(ChatKey(Uuid.randomUUID().toString())) }
  val navigateToConversation = { conversationId: String -> navigator.navigate(ChatKey(conversationId)) }
  val navigateToMovingFlow = { navigator.navigate(SelectContractForMovingKey) }
  val onNavigateToImageViewer = { imageUrl: String, cacheKey: String ->
    navigator.navigate(ImageViewerKey(imageUrl, cacheKey))
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
      backStack = hedvigAppState.backStacks.backStack,
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
          backStack = hedvigAppState.backStacks.backStack,
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
      backStack = hedvigAppState.backStacks.backStack,
      onNavigateToInbox = navigateToInbox,
      onNavigateToNewConversation = navigateToNewConversation,
      navigateToClaimDetails = { claimId -> navigator.navigate(ClaimDetailsKey(claimId)) },
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToPayoutAccount,
      navigateToContactInfo = { navigator.navigate(ContactInfoKey) },
      navigateToMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navigator.navigate(CoInsuredAddInfoKey(contractId, type))
      },
      navigateToHelpCenter = { navigator.navigate(HelpCenterKey) },
      navigateToClaimChat = {
        navigator.navigate(ClaimChatKey(messageId = null, isDevelopmentFlow = false))
      },
      navigateToClaimChatInDevMode = {
        navigator.navigate(ClaimChatKey(messageId = null, isDevelopmentFlow = true))
      },
      navigateToChipIdScreen = { navigator.navigate(ChipIdKey()) },
      openAppSettings = externalNavigator::openAppSettings,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      imageLoader = imageLoader,
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          backStack = hedvigAppState.backStacks.backStack,
          onNavigateToNewConversation = navigateToNewConversation,
          openUrl = openUrl,
          openPlayStore = externalNavigator::tryOpenPlayStore,
          navigateToInsurances = {
            navigator.popUpTo<TerminateInsuranceKey>(inclusive = true)
            hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Insurances)
          },
          navigateToMovingFlow = {
            navigator.navigate<TerminateInsuranceKey>(SelectContractForMovingKey, inclusive = true)
          },
          closeTerminationFlow = {
            // If we fail to pop the backstack including TerminateInsuranceKey here it means we were deep
            //  linked into this screen only, and they do not wish to continue with the flow they were deep linked to.
            //  The right way to handle this is to simply finish the app as per the docs:
            //  https://developer.android.com/guide/navigation/backstack#handle-failure
            if (navigator.findLastOrNull<TerminateInsuranceKey>() != null) {
              navigator.popUpTo<TerminateInsuranceKey>(inclusive = true)
            } else {
              finishApp()
            }
          },
          redirectToChangeTierFlow = { idWithIntent ->
            navigator.navigate<TerminateInsuranceKey>(
              ChooseTierKey(
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
      backStack = hedvigAppState.backStacks.backStack,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      onNavigateToNewConversation = navigateToNewConversation,
      startMovingFlow = navigateToMovingFlow,
      startTerminationFlow = { data: CancelInsuranceData ->
        navigator.navigate(TerminateInsuranceKey(insuranceId = data.contractId))
      },
      imageLoader = imageLoader,
      startEditCoInsured = { contractId: String ->
        navigator.navigate(CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwners = { contractId: String ->
        navigator.navigate(EditCoInsuredTriageKey(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToStartChangeTier = { contractId: String ->
        navigator.navigate(StartTierFlowKey(insuranceId = contractId))
      },
      startEditCoInsuredAddMissingInfo = { contractId: String ->
        navigator.navigate(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
      },
      startEditCoOwnersAddMissingInfo = { contractId: String ->
        navigator.navigate(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoOwners))
      },
      onNavigateToAddonPurchaseFlow = { insuranceIds, availableAddon ->
        navigator.navigate(
          AddonPurchaseKey(
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
          AddonPurchaseKey(
            listOfNotNull(contractId?.id),
            null,
            AddonBannerSource.INSURANCES_TAB,
          ),
        )
      },
      navigateToChipIdScreen = { contractId -> navigator.navigate(ChipIdKey(contractId)) },
    )
    foreverGraph(
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    paymentsGraph(
      backStack = hedvigAppState.backStacks.backStack,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToPayoutAccount = navigateToPayoutAccount,
      openConversation = navigateToNewConversation,
    )
    payoutAccountGraph(
      backStack = hedvigAppState.backStacks.backStack,
      globalSnackBarState = globalSnackBarState,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateUp = navigator::navigateUp,
    )
    profileGraph(
      settingsDestinationNestedGraphs = {
        deleteAccountGraph(hedvigAppState.backStacks.backStack)
      },
      nestedGraphs = {
        claimHistoryGraph(
          navigateUp = navigator::navigateUp,
          navigateToClaimDetails = { claimId -> navigator.navigate(ClaimDetailsKey(claimId)) },
        )
      },
      globalSnackBarState = globalSnackBarState,
      backStack = hedvigAppState.backStacks.backStack,
      popBackStackOrFinish = popBackStackOrFinish,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToConnectPayout = navigateToPayoutAccount,
      navigateToAddMissingInfo = { contractId: String, type: CoInsuredFlowType ->
        navigator.navigate(CoInsuredAddInfoKey(contractId, type))
      },
      navigateToDeleteAccountFeature = { navigator.navigate(DeleteAccountKey) },
      navigateToClaimHistory = { navigator.navigate(ClaimHistoryDestination) },
      openAppSettings = externalNavigator::openAppSettings,
      onNavigateToNewConversation = navigateToNewConversation,
      onNavigateToTravelCertificate = { navigator.navigate(TravelCertificateKey) },
      onNavigateToInsuranceEvidence = { navigator.navigate(InsuranceEvidenceKey) },
      openUrl = openUrl,
      navigateToChipId = { navigator.navigate(ChipIdKey()) },
      languageService = languageService,
    )
    cbmChatGraph(
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      simpleVideoCache = simpleVideoCache,
      openUrl = openUrl,
      onNavigateToClaimDetails = { claimId ->
        logcat { "Navigating to claim details from chat" }
        navigator.navigate(ClaimDetailsKey(claimId))
      },
      onNavigateToImageViewer = onNavigateToImageViewer,
      backStack = hedvigAppState.backStacks.backStack,
    )
    addonPurchaseNavGraph(
      backStack = hedvigAppState.backStacks.backStack,
      popBackStack = popBackStackOrFinish,
      finishApp = finishApp,
      onNavigateToNewConversation = navigateToNewConversation,
      onNavigateToChangeTier = { contractId ->
        navigator.navigate(StartTierFlowKey(insuranceId = contractId))
      },
    )
    changeTierGraph(
      backStack = hedvigAppState.backStacks.backStack,
      onNavigateToNewConversation = navigateToNewConversation,
    )
    chipIdGraph(
      backStack = hedvigAppState.backStacks.backStack,
      globalSnackBarState = globalSnackBarState,
      navigateUp = navigator::navigateUp,
      popBackStackOrFinish = popBackStackOrFinish,
      goHome = {
        navigator.popUpTo<ChipIdKey>(inclusive = true)
        hedvigAppState.navigateToTopLevelGraph(TopLevelGraph.Home)
      },
    )
    movingFlowGraph(
      backStack = hedvigAppState.backStacks.backStack,
      goToChat = navigateToNewConversation,
    )
    connectPaymentGraph(backStack = hedvigAppState.backStacks.backStack)
    editCoInsuredGraph(hedvigAppState.backStacks.backStack)
    helpCenterGraph(
      backStack = hedvigAppState.backStacks.backStack,
      onNavigateUp = navigator::navigateUp,
      onNavigateToQuickLink = onNavigateToQuickLink@{ quickLinkDestination ->
        val destination: HedvigNavKey = when (quickLinkDestination) {
          QuickLinkChangeAddress -> {
            navigateToMovingFlow()
            return@onNavigateToQuickLink
          }

          is QuickLinkCoInsuredAddInfo -> {
            CoInsuredAddInfoKey(quickLinkDestination.contractId, CoInsuredFlowType.CoInsured)
          }

          is QuickLinkCoInsuredAddOrRemove -> {
            CoInsuredAddOrRemoveKey(quickLinkDestination.contractId, CoInsuredFlowType.CoInsured)
          }

          is QuickLinkCoOwnerAddInfo -> {
            CoInsuredAddInfoKey(quickLinkDestination.contractId, CoInsuredFlowType.CoOwners)
          }

          is QuickLinkCoOwnerAddOrRemove -> {
            CoInsuredAddOrRemoveKey(quickLinkDestination.contractId, CoInsuredFlowType.CoOwners)
          }

          QuickLinkConnectPayment -> {
            TrustlyKey
          }

          QuickLinkTermination -> {
            TerminateInsuranceKey(null)
          }

          QuickLinkTravelCertificate -> {
            TravelCertificateKey
          }

          QuickLinkChangeTier -> {
            StartTierFlowChooseInsuranceKey
          }

          ChooseInsuranceForEditCoInsured -> {
            EditCoInsuredTriageKey()
          }

          ChooseInsuranceForEditCoOwners -> {
            EditCoInsuredTriageKey(type = CoInsuredFlowType.CoOwners)
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
    imageViewerGraph(hedvigAppState.backStacks.backStack, imageLoader)
    removeAddonsNavGraph(navigator = navigator)
  }
}

private fun EntryProviderScope<HedvigNavKey>.nestedHomeGraphs(
  navigator: Navigator,
  backStack: MutableList<HedvigNavKey>,
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
    backStack = backStack,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    openAppSettings = externalNavigator::openAppSettings,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToClaimDetails = { claimId: String -> navigator.navigate(ClaimDetailsKey(claimId)) },
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
    backStack = backStack,
    applicationId = appPackageId,
  )
  travelCertificateGraph(
    backStack = backStack,
    applicationId = appPackageId,
    onNavigateToCoInsuredAddInfo = { contractId ->
      navigator.navigate(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
    },
    onNavigateToAddonPurchaseFlow = { ids ->
      navigator.navigate(
        AddonPurchaseKey(
          insuranceIds = ids,
          preselectedAddonDisplayName = null,
          source = AddonBannerSource.TRAVEL_CERTIFICATES,
        ),
      )
    },
  )
  insuranceEvidenceGraph(
    backStack = backStack,
    applicationId = appPackageId,
  )
}
