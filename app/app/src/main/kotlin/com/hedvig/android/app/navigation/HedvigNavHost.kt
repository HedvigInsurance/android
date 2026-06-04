package com.hedvig.android.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.media3.datasource.cache.SimpleCache
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneDecoratorStrategy
import androidx.navigation3.ui.NavDisplay
import coil3.ImageLoader
import com.benasher44.uuid.Uuid
import com.hedvig.android.auth.MemberIdService
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
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryKey
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
import com.hedvig.android.navigation.common.TopLevelGraph
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.entryDecorators
import com.hedvig.android.navigation.compose.findLastOrNull
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popBackstack
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.feature.claim.chat.ClaimChatKey
import com.hedvig.feature.claim.chat.claimChatGraph
import com.hedvig.feature.remove.addons.RemoveAddonsKey
import com.hedvig.feature.remove.addons.removeAddonsNavGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HedvigNavHost(
  backstack: BackstackController,
  windowSizeClass: WindowSizeClass,
  memberIdService: MemberIdService,
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
  sharedTransitionScope: SharedTransitionScope? = null,
  sceneDecoratorStrategies: List<SceneDecoratorStrategy<HedvigNavKey>> = emptyList(),
) {
  val scope = rememberCoroutineScope()

  val navigateToConnectPayment: () -> Unit = { backstack.add(TrustlyKey) }
  val navigateToPayoutAccount: () -> Unit = { backstack.add(PayoutAccountKey) }
  val navigateToInbox: () -> Unit = { backstack.add(InboxKey) }
  val navigateToNewConversation: () -> Unit = { backstack.add(ChatKey(Uuid.randomUUID().toString())) }
  val navigateToConversation: (String) -> Unit = { conversationId -> backstack.add(ChatKey(conversationId)) }
  val navigateToMovingFlow: () -> Unit = { backstack.add(SelectContractForMovingKey) }
  val onNavigateToImageViewer: (String, String) -> Unit = { imageUrl, cacheKey ->
    backstack.add(ImageViewerKey(imageUrl, cacheKey))
  }
  val popBackstackOrFinish = {
    if (!backstack.popBackstack()) {
      finishApp()
    }
    Unit
  }

  val density = LocalDensity.current
  val retainedContentKeys = { backstack.allLiveContentKeys }
  val popTransitionSpec: AnimatedContentTransitionScope<Scene<HedvigNavKey>>.() -> ContentTransform = {
    val fromTab = backstack.owningTopLevelGraphForContentKey(initialState.entries.lastOrNull()?.contentKey)
    val toTab = backstack.owningTopLevelGraphForContentKey(targetState.entries.lastOrNull()?.contentKey)
    if (shouldFadeThrough(fromTab, toTab)) {
      MotionDefaults.fadeThroughEnter togetherWith MotionDefaults.fadeThroughExit
    } else {
      MotionDefaults.sharedXAxisPopEnter(density) togetherWith MotionDefaults.sharedXAxisPopExit(density)
    }
  }
  NavDisplay(
    backStack = backstack.entries,
    modifier = modifier,
    onBack = {
      if (!backstack.handleBack()) {
        finishApp()
      }
    },
    entryDecorators = entryDecorators(retainedContentKeys),
    sharedTransitionScope = sharedTransitionScope,
    sceneDecoratorStrategies = sceneDecoratorStrategies,
    transitionSpec = {
      val fromTab = backstack.owningTopLevelGraphForContentKey(initialState.entries.lastOrNull()?.contentKey)
      val toTab = backstack.owningTopLevelGraphForContentKey(targetState.entries.lastOrNull()?.contentKey)
      if (shouldFadeThrough(fromTab, toTab)) {
        MotionDefaults.fadeThroughEnter togetherWith MotionDefaults.fadeThroughExit
      } else {
        MotionDefaults.sharedXAxisEnter(density) togetherWith MotionDefaults.sharedXAxisExit(density)
      }
    },
    popTransitionSpec = popTransitionSpec,
    predictivePopTransitionSpec = { popTransitionSpec() },
    entryProvider = entryProvider(
      builder = fun EntryProviderScope<HedvigNavKey>.() {
        loginGraph(
          backstack = backstack,
          appVersionName = hedvigBuildConstants.appVersionName,
          urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
          openUrl = openUrl,
          onOpenEmailApp = externalNavigator::openEmailApp,
          onNavigateToLoggedIn = {
            scope.launch {
              backstack.setLoggedIn(memberIdService.getMemberId().first())
            }
          },
        )
        homeGraph(
          nestedGraphs = {
            nestedHomeGraphs(
              backstack = backstack,
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
          backstack = backstack,
          onNavigateToInbox = navigateToInbox,
          onNavigateToNewConversation = navigateToNewConversation,
          navigateToClaimDetails = { claimId -> backstack.add(ClaimDetailsKey(claimId)) },
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToConnectPayout = navigateToPayoutAccount,
          navigateToContactInfo = { backstack.add(ContactInfoKey) },
          navigateToMissingInfo = { contractId: String, type: CoInsuredFlowType ->
            backstack.add(CoInsuredAddInfoKey(contractId, type))
          },
          navigateToHelpCenter = { backstack.add(HelpCenterKey) },
          navigateToClaimChat = {
            backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = false))
          },
          navigateToClaimChatInDevMode = {
            backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = true))
          },
          navigateToChipIdScreen = { backstack.add(ChipIdKey()) },
          openAppSettings = externalNavigator::openAppSettings,
          openUrl = openUrl,
          openCrossSellUrl = openCrossSellUrl,
          imageLoader = imageLoader,
        )
        insuranceGraph(
          nestedGraphs = {
            terminateInsuranceGraph(
              windowSizeClass = windowSizeClass,
              backstack = backstack,
              onNavigateToNewConversation = navigateToNewConversation,
              openUrl = openUrl,
              openPlayStore = externalNavigator::tryOpenPlayStore,
              navigateToInsurances = {
                backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
                backstack.selectTopLevel(TopLevelGraph.Insurances)
              },
              navigateToMovingFlow = {
                backstack.navigateAndPopUpTo<TerminateInsuranceKey>(SelectContractForMovingKey, inclusive = true)
              },
              closeTerminationFlow = {
                // If we fail to pop the backstack including TerminateInsuranceKey here it means we were deep
                //  linked into this screen only, and they do not wish to continue with the flow they were deep linked to.
                //  The right way to handle this is to simply finish the app as per the docs:
                //  https://developer.android.com/guide/navigation/backstack#handle-failure
                if (backstack.findLastOrNull<TerminateInsuranceKey>() != null) {
                  backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
                } else {
                  finishApp()
                }
              },
              redirectToChangeTierFlow = { idWithIntent ->
                backstack.navigateAndPopUpTo<TerminateInsuranceKey>(
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
          backstack = backstack,
          openUrl = openUrl,
          openCrossSellUrl = openCrossSellUrl,
          onNavigateToNewConversation = navigateToNewConversation,
          startMovingFlow = navigateToMovingFlow,
          startTerminationFlow = { data: CancelInsuranceData ->
            backstack.add(TerminateInsuranceKey(insuranceId = data.contractId))
          },
          imageLoader = imageLoader,
          startEditCoInsured = { contractId: String ->
            backstack.add(CoInsuredAddOrRemoveKey(contractId, CoInsuredFlowType.CoInsured))
          },
          startEditCoOwners = { contractId: String ->
            backstack.add(EditCoInsuredTriageKey(contractId, CoInsuredFlowType.CoOwners))
          },
          onNavigateToStartChangeTier = { contractId: String ->
            backstack.add(StartTierFlowKey(insuranceId = contractId))
          },
          startEditCoInsuredAddMissingInfo = { contractId: String ->
            backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
          },
          startEditCoOwnersAddMissingInfo = { contractId: String ->
            backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoOwners))
          },
          onNavigateToAddonPurchaseFlow = { insuranceIds, availableAddon ->
            backstack.add(
              AddonPurchaseKey(
                insuranceIds.map(ContractId::id),
                availableAddon?.displayName,
                AddonBannerSource.INSURANCES_TAB,
              ),
            )
          },
          onNavigateToRemoveAddon = { contractId, addonVariant ->
            backstack.add(RemoveAddonsKey(contractId, addonVariant))
          },
          navigateToUpgradeAddon = { contractId, _ ->
            backstack.add(
              AddonPurchaseKey(
                listOfNotNull(contractId?.id),
                null,
                AddonBannerSource.INSURANCES_TAB,
              ),
            )
          },
          navigateToChipIdScreen = { contractId -> backstack.add(ChipIdKey(contractId)) },
        )
        foreverGraph(
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
        )
        paymentsGraph(
          backstack = backstack,
          languageService = languageService,
          hedvigBuildConstants = hedvigBuildConstants,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToPayoutAccount = navigateToPayoutAccount,
          openConversation = navigateToNewConversation,
        )
        payoutAccountGraph(
          backstack = backstack,
          globalSnackBarState = globalSnackBarState,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateUp = backstack::navigateUp,
        )
        profileGraph(
          settingsDestinationNestedGraphs = {
            deleteAccountGraph(backstack)
          },
          nestedGraphs = {
            claimHistoryGraph(
              navigateUp = backstack::navigateUp,
              navigateToClaimDetails = { claimId -> backstack.add(ClaimDetailsKey(claimId)) },
            )
          },
          globalSnackBarState = globalSnackBarState,
          backstack = backstack,
          popBackstackOrFinish = popBackstackOrFinish,
          hedvigBuildConstants = hedvigBuildConstants,
          navigateToConnectPayment = navigateToConnectPayment,
          navigateToConnectPayout = navigateToPayoutAccount,
          navigateToAddMissingInfo = { contractId: String, type: CoInsuredFlowType ->
            backstack.add(CoInsuredAddInfoKey(contractId, type))
          },
          navigateToDeleteAccountFeature = { backstack.add(DeleteAccountKey) },
          navigateToClaimHistory = { backstack.add(ClaimHistoryKey) },
          openAppSettings = externalNavigator::openAppSettings,
          onNavigateToNewConversation = navigateToNewConversation,
          onNavigateToTravelCertificate = { backstack.add(TravelCertificateKey) },
          onNavigateToInsuranceEvidence = { backstack.add(InsuranceEvidenceKey) },
          openUrl = openUrl,
          navigateToChipId = { backstack.add(ChipIdKey()) },
          languageService = languageService,
        )
        cbmChatGraph(
          hedvigBuildConstants = hedvigBuildConstants,
          imageLoader = imageLoader,
          simpleVideoCache = simpleVideoCache,
          openUrl = openUrl,
          onNavigateToClaimDetails = { claimId ->
            logcat { "Navigating to claim details from chat" }
            backstack.add(ClaimDetailsKey(claimId))
          },
          onNavigateToImageViewer = onNavigateToImageViewer,
          backstack = backstack,
        )
        addonPurchaseNavGraph(
          backstack = backstack,
          popBackstack = popBackstackOrFinish,
          finishApp = finishApp,
          onNavigateToNewConversation = navigateToNewConversation,
          onNavigateToChangeTier = { contractId ->
            backstack.add(StartTierFlowKey(insuranceId = contractId))
          },
        )
        changeTierGraph(
          backstack = backstack,
          onNavigateToNewConversation = navigateToNewConversation,
        )
        chipIdGraph(
          backstack = backstack,
          globalSnackBarState = globalSnackBarState,
          navigateUp = backstack::navigateUp,
          popBackstackOrFinish = popBackstackOrFinish,
          goHome = {
            backstack.popUpTo<ChipIdKey>(inclusive = true)
            backstack.selectTopLevel(TopLevelGraph.Home)
          },
        )
        movingFlowGraph(
          backstack = backstack,
          goToChat = navigateToNewConversation,
        )
        connectPaymentGraph(backstack = backstack)
        editCoInsuredGraph(backstack)
        helpCenterGraph(
          backstack = backstack,
          onNavigateUp = backstack::navigateUp,
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
            backstack.add(destination)
          },
          onNavigateToNewConversation = navigateToNewConversation,
          onNavigateToInbox = navigateToInbox,
          openUrl = openUrl,
          tryToDialPhone = externalNavigator::tryToDialPhone,
          imageLoader = imageLoader,
        )
        imageViewerGraph(backstack, imageLoader)
        removeAddonsNavGraph(backstack = backstack)
      },
    ),
  )
}

private fun EntryProviderScope<HedvigNavKey>.nestedHomeGraphs(
  backstack: Backstack,
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
    backstack = backstack,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    openAppSettings = externalNavigator::openAppSettings,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToClaimDetails = { claimId: String -> backstack.add(ClaimDetailsKey(claimId)) },
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
    navigateUp = backstack::navigateUp,
    appPackageId = appPackageId,
    navigateToConversation = { conversationId -> navigateToConversation(conversationId) },
    backstack = backstack,
    applicationId = appPackageId,
  )
  travelCertificateGraph(
    backstack = backstack,
    applicationId = appPackageId,
    onNavigateToCoInsuredAddInfo = { contractId ->
      backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
    },
    onNavigateToAddonPurchaseFlow = { ids ->
      backstack.add(
        AddonPurchaseKey(
          insuranceIds = ids,
          preselectedAddonDisplayName = null,
          source = AddonBannerSource.TRAVEL_CERTIFICATES,
        ),
      )
    },
  )
  insuranceEvidenceGraph(
    backstack = backstack,
    applicationId = appPackageId,
  )
}
