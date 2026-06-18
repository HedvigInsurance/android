package com.hedvig.android.app.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.benasher44.uuid.Uuid
import com.hedvig.android.app.AndroidAppHost
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseKey
import com.hedvig.android.feature.addon.purchase.navigation.addonPurchaseEntries
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowKey
import com.hedvig.android.feature.change.tier.navigation.changeTierEntries
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.chat.navigation.cbmChatEntries
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.feature.chip.id.navigation.chipIdEntries
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.claim.details.navigation.claimDetailsEntries
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryKey
import com.hedvig.android.feature.claimhistory.nav.claimHistoryEntries
import com.hedvig.android.feature.connect.payment.connectPaymentEntries
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountKey
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountEntries
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredEntries
import com.hedvig.android.feature.forever.navigation.foreverEntries
import com.hedvig.android.feature.help.center.helpCenterEntries
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.homeEntries
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.imageviewer.navigation.imageViewerEntries
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurance.certificate.navigation.insuranceEvidenceEntries
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.insuranceEntries
import com.hedvig.android.feature.login.navigation.loginEntries
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.movingFlowEntries
import com.hedvig.android.feature.payments.navigation.paymentsEntries
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountKey
import com.hedvig.android.feature.payoutaccount.navigation.payoutAccountEntries
import com.hedvig.android.feature.profile.navigation.ContactInfoKey
import com.hedvig.android.feature.profile.tab.profileEntries
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceEntries
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateEntries
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.compose.removeAllOf
import com.hedvig.feature.claim.chat.navigation.ClaimChatKey
import com.hedvig.feature.claim.chat.navigation.claimChatEntries
import com.hedvig.feature.remove.addons.RemoveAddonsKey
import com.hedvig.feature.remove.addons.removeAddonsEntries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Registers every in-app destination on the [EntryProviderScope]. Decomposed into per-domain
 * sub-builders so each tab/flow's wiring is readable in isolation; the cross-feature `navigateToX`
 * closures are derived once here and threaded down to the sub-builders that need them. All wiring
 * stays in `:app` because feature `*Key`s live in feature *impl* modules and the feature-isolation
 * build rule blocks one feature from referencing another's key.
 */
internal fun EntryProviderScope<HedvigNavKey>.hedvigEntryProvider(
  backstack: BackstackController,
  scope: CoroutineScope,
  windowSizeClass: WindowSizeClass,
  memberIdService: MemberIdService,
  globalSnackBarState: GlobalSnackBarState,
  externalNavigator: ExternalNavigator,
  androidAppHost: AndroidAppHost,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
) {
  val shouldShowRequestPermissionRationale: (String) -> Boolean = androidAppHost::shouldShowPermissionRationale
  val navigateToConnectPayment: () -> Unit = { backstack.add(TrustlyKey) }
  val navigateToPayoutAccount: () -> Unit = { backstack.add(PayoutAccountKey) }
  val navigateToInbox: () -> Unit = { backstack.add(InboxKey) }
  val navigateToNewConversation: () -> Unit = { backstack.add(ChatKey(Uuid.randomUUID().toString())) }
  val navigateToConversation: (String) -> Unit = { conversationId -> backstack.add(ChatKey(conversationId)) }
  val navigateToMovingFlow: () -> Unit = { backstack.add(SelectContractForMovingKey) }
  val onNavigateToImageViewer: (String, String) -> Unit = { imageUrl, cacheKey ->
    backstack.add(ImageViewerKey(imageUrl, cacheKey))
  }
  addLoginEntries(backstack, hedvigBuildConstants, openUrl, externalNavigator, scope, memberIdService)
  addHomeEntries(
    backstack = backstack,
    hedvigBuildConstants = hedvigBuildConstants,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    externalNavigator = externalNavigator,
    imageLoader = imageLoader,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToNewConversation = navigateToNewConversation,
    navigateToConversation = navigateToConversation,
    navigateToInbox = navigateToInbox,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToPayoutAccount = navigateToPayoutAccount,
  )
  addInsuranceEntries(
    backstack = backstack,
    windowSizeClass = windowSizeClass,
    imageLoader = imageLoader,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    externalNavigator = externalNavigator,
    navigateToNewConversation = navigateToNewConversation,
    navigateToMovingFlow = navigateToMovingFlow,
  )
  foreverEntries()
  addPaymentsEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToPayoutAccount = navigateToPayoutAccount,
    navigateToNewConversation = navigateToNewConversation,
  )
  addProfileEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    hedvigBuildConstants = hedvigBuildConstants,
    languageService = languageService,
    externalNavigator = externalNavigator,
    openUrl = openUrl,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToPayoutAccount = navigateToPayoutAccount,
    navigateToNewConversation = navigateToNewConversation,
  )
  addChatEntries(
    backstack = backstack,
    hedvigBuildConstants = hedvigBuildConstants,
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToNewConversation = navigateToNewConversation,
  )
  addSharedFlowEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    imageLoader = imageLoader,
    openUrl = openUrl,
    externalNavigator = externalNavigator,
    navigateToNewConversation = navigateToNewConversation,
    navigateToInbox = navigateToInbox,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addLoginEntries(
  backstack: BackstackController,
  hedvigBuildConstants: HedvigBuildConstants,
  openUrl: (String) -> Unit,
  externalNavigator: ExternalNavigator,
  scope: CoroutineScope,
  memberIdService: MemberIdService,
) {
  loginEntries(
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
}

private fun EntryProviderScope<HedvigNavKey>.addHomeEntries(
  backstack: BackstackController,
  hedvigBuildConstants: HedvigBuildConstants,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
  navigateToConversation: (String) -> Unit,
  navigateToInbox: () -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
) {
  homeEntries(
    nestedEntries = {
      addNestedHomeEntries(
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
    navigateToChipIdScreen = { backstack.add(ChipIdKey()) },
    openAppSettings = externalNavigator::openAppSettings,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    imageLoader = imageLoader,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addNestedHomeEntries(
  backstack: BackstackController,
  appPackageId: String,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
  navigateToConversation: (String) -> Unit,
) {
  claimChatEntries(
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
    clearClaimEntryPoints = {
      backstack.removeAllOf<InboxKey>()
    },
  )
  claimDetailsEntries(
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    appPackageId = appPackageId,
    navigateToConversation = { conversationId -> navigateToConversation(conversationId) },
    backstack = backstack,
    applicationId = appPackageId,
  )
  travelCertificateEntries(
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
  insuranceEvidenceEntries(
    backstack = backstack,
    applicationId = appPackageId,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addInsuranceEntries(
  backstack: BackstackController,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Unit,
  externalNavigator: ExternalNavigator,
  navigateToNewConversation: () -> Unit,
  navigateToMovingFlow: () -> Unit,
) {
  insuranceEntries(
    nestedEntries = {
      terminateInsuranceEntries(
        windowSizeClass = windowSizeClass,
        backstack = backstack,
        onNavigateToNewConversation = navigateToNewConversation,
        openUrl = openUrl,
        openPlayStore = externalNavigator::tryOpenPlayStore,
        navigateToInsurances = {
          backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
          backstack.selectTopLevel(TopLevelTab.Insurances)
        },
        navigateToMovingFlow = {
          backstack.navigateAndPopUpTo<TerminateInsuranceKey>(SelectContractForMovingKey, inclusive = true)
        },
        closeTerminationFlow = {
          backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
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
}

private fun EntryProviderScope<HedvigNavKey>.addPaymentsEntries(
  backstack: BackstackController,
  globalSnackBarState: GlobalSnackBarState,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  navigateToNewConversation: () -> Unit,
) {
  paymentsEntries(
    backstack = backstack,
    navigateToConnectPayment = navigateToConnectPayment,
    navigateToPayoutAccount = navigateToPayoutAccount,
    openConversation = navigateToNewConversation,
  )
  payoutAccountEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    navigateToConnectPayment = navigateToConnectPayment,
  )
  connectPaymentEntries(backstack = backstack)
}

private fun EntryProviderScope<HedvigNavKey>.addProfileEntries(
  backstack: BackstackController,
  globalSnackBarState: GlobalSnackBarState,
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  externalNavigator: ExternalNavigator,
  openUrl: (String) -> Unit,
  navigateToConnectPayment: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  navigateToNewConversation: () -> Unit,
) {
  profileEntries(
    settingsDestinationNestedGraphs = {
      deleteAccountEntries(backstack)
    },
    nestedEntries = {
      claimHistoryEntries(
        backstack = backstack,
        navigateToClaimDetails = { claimId -> backstack.add(ClaimDetailsKey(claimId)) },
      )
    },
    globalSnackBarState = globalSnackBarState,
    backstack = backstack,
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
}

private fun EntryProviderScope<HedvigNavKey>.addChatEntries(
  backstack: BackstackController,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
) {
  cbmChatEntries(
    hedvigBuildConstants = hedvigBuildConstants,
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToClaimDetails = { claimId ->
      logcat { "Navigating to claim details from chat" }
      backstack.add(ClaimDetailsKey(claimId))
    },
    onNavigateToImageViewer = onNavigateToImageViewer,
    onNavigateToNewConversation = navigateToNewConversation,
    navigateToClaimChat = {
      backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = false))
    },
    backstack = backstack,
  )
}

/**
 * Cross-cutting flows reachable from more than one tab: addon purchase/removal, tier changes,
 * co-insured editing, moving, chip-id, the help center and the full-screen image viewer.
 */
private fun EntryProviderScope<HedvigNavKey>.addSharedFlowEntries(
  backstack: BackstackController,
  globalSnackBarState: GlobalSnackBarState,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  externalNavigator: ExternalNavigator,
  navigateToNewConversation: () -> Unit,
  navigateToInbox: () -> Unit,
) {
  addonPurchaseEntries(
    backstack = backstack,
    onNavigateToNewConversation = navigateToNewConversation,
    onNavigateToChangeTier = { contractId ->
      backstack.add(StartTierFlowKey(insuranceId = contractId))
    },
  )
  changeTierEntries(
    backstack = backstack,
    onNavigateToNewConversation = navigateToNewConversation,
  )
  chipIdEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    goHome = {
      backstack.popUpTo<ChipIdKey>(inclusive = true)
      backstack.selectTopLevel(TopLevelTab.Home)
    },
  )
  movingFlowEntries(
    backstack = backstack,
    goToChat = navigateToNewConversation,
  )
  editCoInsuredEntries(backstack)
  helpCenterEntries(
    backstack = backstack,
    onNavigateToNewConversation = navigateToNewConversation,
    onNavigateToInbox = navigateToInbox,
    openUrl = openUrl,
    tryToDialPhone = externalNavigator::tryToDialPhone,
    imageLoader = imageLoader,
  )
  imageViewerEntries(backstack, imageLoader)
  removeAddonsEntries(backstack = backstack)
}
