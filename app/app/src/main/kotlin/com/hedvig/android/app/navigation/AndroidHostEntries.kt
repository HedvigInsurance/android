package com.hedvig.android.app.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseKey
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowKey
import com.hedvig.android.feature.chat.navigation.cbmChatEntries
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.feature.claim.chat.navigation.ClaimChatKey
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.claim.details.navigation.claimDetailsEntries
import com.hedvig.android.feature.claimhistory.nav.ClaimHistoryKey
import com.hedvig.android.feature.connect.payment.connectPaymentEntries
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddInfoKey
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.forever.navigation.foreverEntries
import com.hedvig.android.feature.help.center.navigation.HelpCenterKey
import com.hedvig.android.feature.home.home.navigation.homeEntries
import com.hedvig.android.feature.imageviewer.navigation.imageViewerEntries
import com.hedvig.android.feature.insurance.certificate.navigation.InsuranceEvidenceKey
import com.hedvig.android.feature.insurance.certificate.navigation.insuranceEvidenceEntries
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.insuranceEntries
import com.hedvig.android.feature.login.navigation.loginEntries
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.onboarding.navigation.onboardingEntries
import com.hedvig.android.feature.payin.account.navigation.PayinMethodDetailsKey
import com.hedvig.android.feature.payin.account.navigation.SelectPrimaryPayinMethodKey
import com.hedvig.android.feature.payments.navigation.ManualChargeKey
import com.hedvig.android.feature.payments.navigation.paymentsEntries
import com.hedvig.android.feature.profile.navigation.ContactInfoKey
import com.hedvig.android.feature.profile.navigation.UsageDataKey
import com.hedvig.android.feature.profile.tab.profileEntries
import com.hedvig.android.feature.remove.addons.RemoveAddonsKey
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateEntries
import com.hedvig.android.language.LanguageService
import com.hedvig.android.logger.logcat
import com.hedvig.android.memberquickactions.InnerHelpCenterDestination
import com.hedvig.android.memberquickactions.QuickLinkDestination
import com.hedvig.android.memberquickactions.toNavKey
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.add
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Registers the destinations of the features only Android can render, the host half of
 * [hedvigEntryProvider]. A closure that [context] provides is taken from it and never rebuilt here; closures
 * it does not provide are built here.
 */
internal fun EntryProviderScope<HedvigNavKey>.androidHostEntries(context: HostEntryContext) {
  addLoginEntries(
    context.backstack,
    context.hedvigBuildConstants,
    context.openUrl,
    context.externalNavigator,
    context.scope,
    context.memberIdService,
  )
  addHomeEntries(
    backstack = context.backstack,
    hedvigBuildConstants = context.hedvigBuildConstants,
    externalNavigator = context.externalNavigator,
    imageLoader = context.imageLoader,
    openUrl = context.openUrl,
    openCrossSellUrl = context.openCrossSellUrl,
    onNavigateToImageViewer = context.onNavigateToImageViewer,
    navigateToNewConversation = context.navigateToNewConversation,
    navigateToConversation = context.navigateToConversation,
    navigateToInbox = context.navigateToInbox,
    navigateToPayinAccount = context.navigateToPayinAccount,
    navigateToPayoutAccount = context.navigateToPayoutAccount,
    navigateToAddonPurchaseFlow = context.navigateToAddonPurchaseFlow,
  )
  addInsuranceEntries(
    backstack = context.backstack,
    imageLoader = context.imageLoader,
    openUrl = context.openUrl,
    openCrossSellUrl = context.openCrossSellUrl,
    navigateToNewConversation = context.navigateToNewConversation,
    navigateToAddonPurchaseFlow = context.navigateToAddonPurchaseFlow,
    navigateToMovingFlowFromInsurance = {
      context.navigateToMovingFlow(MovingSource.INSURANCE)
    },
  )
  foreverEntries()
  addPaymentsEntries(
    backstack = context.backstack,
    navigateToPayinAccount = context.navigateToPayinAccount,
    navigateToPayoutAccount = context.navigateToPayoutAccount,
    navigateToNewConversation = context.navigateToNewConversation,
    changePayinMethod = context.changePayinMethod,
  )
  addProfileEntries(
    backstack = context.backstack,
    globalSnackBarState = context.globalSnackBarState,
    hedvigBuildConstants = context.hedvigBuildConstants,
    languageService = context.languageService,
    externalNavigator = context.externalNavigator,
    openUrl = context.openUrl,
    openPrivacyPolicy = context.openPrivacyPolicy,
    navigateToPayinAccount = context.navigateToPayinAccount,
    navigateToPayoutAccount = context.navigateToPayoutAccount,
    navigateToNewConversation = context.navigateToNewConversation,
    navigateToTravelCertificate = context.navigateToTravelCertificate,
    onResetOnboardingForDebug = context.onResetOnboardingForDebug,
  )
  addChatEntries(
    backstack = context.backstack,
    hedvigBuildConstants = context.hedvigBuildConstants,
    imageLoader = context.imageLoader,
    openUrl = context.openUrl,
    openCrossSellUrl = context.openCrossSellUrl,
    onNavigateToImageViewer = context.onNavigateToImageViewer,
    navigateToNewConversation = context.navigateToNewConversation,
  )
  imageViewerEntries(context.backstack, context.imageLoader)
  onboardingEntries(
    backstack = context.backstack,
    imageLoader = context.imageLoader,
    openCrossSellUrl = context.openCrossSellUrl,
    openPrivacyPolicy = context.openPrivacyPolicy,
    navigateToChipId = { contractId -> context.backstack.add(ChipIdKey(contractId)) },
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
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
  navigateToConversation: (String) -> Unit,
  navigateToInbox: () -> Unit,
  navigateToPayinAccount: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>, AddonBannerSource) -> Unit,
) {
  homeEntries(
    nestedEntries = {
      addNestedHomeEntries(
        backstack = backstack,
        appPackageId = hedvigBuildConstants.appPackageId,
        imageLoader = imageLoader,
        openUrl = openUrl,
        onNavigateToImageViewer = onNavigateToImageViewer,
        navigateToConversation = navigateToConversation,
        navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
      )
    },
    backstack = backstack,
    onNavigateToInbox = navigateToInbox,
    onNavigateToNewConversation = navigateToNewConversation,
    navigateToClaimDetails = { claimId -> backstack.add(ClaimDetailsKey(claimId)) },
    navigateToConnectPayment = navigateToPayinAccount,
    navigateToManualCharge = { backstack.add(ManualChargeKey) },
    navigateToConnectPayout = navigateToPayoutAccount,
    navigateToContactInfo = { backstack.add(ContactInfoKey) },
    navigateToMissingInfo = { contractId: String, type: CoInsuredFlowType ->
      backstack.add(CoInsuredAddInfoKey(contractId, type))
    },
    navigateToHelpCenter = { backstack.add(HelpCenterKey) },
    navigateToQuickLink = { destination ->
      when (destination) {
        is QuickLinkDestination.OuterDestination -> backstack.add(destination.toNavKey())

        // FirstVet is handled by feature-home before reaching here.
        is InnerHelpCenterDestination -> error("Inner quick-link destinations are routed by the feature")
      }
    },
    navigateToClaimChat = { resumeClaim ->
      backstack.add(
        ClaimChatKey(
          messageId = null,
          isDevelopmentFlow = false,
          resumeClaim = resumeClaim,
        ),
      )
    },
    navigateToChipIdScreen = { backstack.add(ChipIdKey()) },
    navigateToUsageData = { backstack.add(UsageDataKey) },
    openAppSettings = externalNavigator::openAppSettings,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    imageLoader = imageLoader,
    navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addNestedHomeEntries(
  backstack: BackstackController,
  appPackageId: String,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToConversation: (String) -> Unit,
  navigateToAddonPurchaseFlow: (List<String>, AddonBannerSource) -> Unit,
) {
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
      navigateToAddonPurchaseFlow(ids, AddonBannerSource.TRAVEL_CERTIFICATES)
    },
  )
  insuranceEvidenceEntries(
    backstack = backstack,
    applicationId = appPackageId,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addInsuranceEntries(
  backstack: BackstackController,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Boolean,
  navigateToNewConversation: () -> Unit,
  navigateToAddonPurchaseFlow: (List<String>, AddonBannerSource) -> Unit,
  navigateToMovingFlowFromInsurance: () -> Unit,
) {
  insuranceEntries(
    nestedEntries = {},
    backstack = backstack,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    onNavigateToNewConversation = navigateToNewConversation,
    startMovingFlow = navigateToMovingFlowFromInsurance,
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
    onNavigateToStartChangeTier = { contractId: String, isPaymentProtection: Boolean ->
      backstack.add(StartTierFlowKey(insuranceId = contractId, isPaymentProtection = isPaymentProtection))
    },
    startEditCoInsuredAddMissingInfo = { contractId: String ->
      backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoInsured))
    },
    startEditCoOwnersAddMissingInfo = { contractId: String ->
      backstack.add(CoInsuredAddInfoKey(contractId, CoInsuredFlowType.CoOwners))
    },
    onNavigateToAddonPurchaseFlow = { insuranceIds, availableAddon, source ->
      backstack.add(
        AddonPurchaseKey(
          insuranceIds.map(ContractId::id),
          availableAddon?.displayName,
          source,
        ),
      )
    },
    onNavigateToRemoveAddon = { contractId, addonVariant ->
      backstack.add(RemoveAddonsKey(contractId, addonVariant))
    },
    navigateToUpgradeAddon = { contractId, _ ->
      navigateToAddonPurchaseFlow(listOfNotNull(contractId?.id), AddonBannerSource.CONTRACT_DETAIL)
    },
    navigateToChipIdScreen = { contractId -> backstack.add(ChipIdKey(contractId)) },
  )
}

private fun EntryProviderScope<HedvigNavKey>.addPaymentsEntries(
  backstack: BackstackController,
  navigateToPayinAccount: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  navigateToNewConversation: () -> Unit,
  changePayinMethod: () -> Unit,
) {
  paymentsEntries(
    backstack = backstack,
    navigateToPayinAccount = navigateToPayinAccount,
    navigateToPayinMethodDetails = { method -> backstack.add(PayinMethodDetailsKey(method)) },
    navigateToPayoutAccount = navigateToPayoutAccount,
    navigateToSelectPrimaryPayinMethod = { backstack.add(SelectPrimaryPayinMethodKey) },
    openConversation = navigateToNewConversation,
  )
  connectPaymentEntries(
    backstack = backstack,
    changePaymentMethod = changePayinMethod,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addProfileEntries(
  backstack: BackstackController,
  globalSnackBarState: GlobalSnackBarState,
  hedvigBuildConstants: HedvigBuildConstants,
  languageService: LanguageService,
  externalNavigator: ExternalNavigator,
  openUrl: (String) -> Unit,
  openPrivacyPolicy: () -> Unit,
  navigateToPayinAccount: () -> Unit,
  navigateToPayoutAccount: () -> Unit,
  navigateToNewConversation: () -> Unit,
  navigateToTravelCertificate: () -> Unit,
  onResetOnboardingForDebug: () -> Unit,
) {
  profileEntries(
    settingsDestinationNestedGraphs = {},
    nestedEntries = {},
    globalSnackBarState = globalSnackBarState,
    backstack = backstack,
    hedvigBuildConstants = hedvigBuildConstants,
    navigateToConnectPayment = navigateToPayinAccount,
    navigateToConnectPayout = navigateToPayoutAccount,
    navigateToAddMissingInfo = { contractId: String, type: CoInsuredFlowType ->
      backstack.add(CoInsuredAddInfoKey(contractId, type))
    },
    navigateToDeleteAccountFeature = { backstack.add(DeleteAccountKey) },
    navigateToClaimHistory = { backstack.add(ClaimHistoryKey) },
    openAppSettings = externalNavigator::openAppSettings,
    onNavigateToNewConversation = navigateToNewConversation,
    onNavigateToTravelCertificate = navigateToTravelCertificate,
    onNavigateToInsuranceEvidence = { backstack.add(InsuranceEvidenceKey) },
    openUrl = openUrl,
    openPrivacyPolicy = openPrivacyPolicy,
    navigateToChipId = { backstack.add(ChipIdKey()) },
    languageService = languageService,
    onResetOnboardingForDebug = onResetOnboardingForDebug,
  )
}

private fun EntryProviderScope<HedvigNavKey>.addChatEntries(
  backstack: BackstackController,
  hedvigBuildConstants: HedvigBuildConstants,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Boolean,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
) {
  cbmChatEntries(
    hedvigBuildConstants = hedvigBuildConstants,
    imageLoader = imageLoader,
    openUrl = openUrl,
    openCrossSellUrl = openCrossSellUrl,
    onNavigateToClaimDetails = { claimId ->
      logcat { "Navigating to claim details from chat" }
      backstack.add(ClaimDetailsKey(claimId))
    },
    onNavigateToImageViewer = onNavigateToImageViewer,
    onNavigateToNewConversation = navigateToNewConversation,
    navigateToClaimChat = { resumeClaim ->
      backstack.add(ClaimChatKey(messageId = null, isDevelopmentFlow = false, resumeClaim = resumeClaim))
    },
    backstack = backstack,
  )
}
