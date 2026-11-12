package com.hedvig.android.app.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.benasher44.uuid.Uuid
import com.hedvig.android.app.AppHost
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseKey
import com.hedvig.android.feature.addon.purchase.navigation.addonPurchaseEntries
import com.hedvig.android.feature.change.tier.navigation.ChooseTierKey
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowKey
import com.hedvig.android.feature.change.tier.navigation.changeTierEntries
import com.hedvig.android.feature.chat.navigation.ChatKey
import com.hedvig.android.feature.chat.navigation.InboxKey
import com.hedvig.android.feature.chip.id.navigation.ChipIdKey
import com.hedvig.android.feature.chip.id.navigation.chipIdEntries
import com.hedvig.android.feature.claim.chat.navigation.claimChatEntries
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailsKey
import com.hedvig.android.feature.claimhistory.nav.claimHistoryEntries
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyKey
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountEntries
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredEntries
import com.hedvig.android.feature.help.center.helpCenterEntries
import com.hedvig.android.feature.imageviewer.navigation.ImageViewerKey
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.SelectContractForMovingKey
import com.hedvig.android.feature.movingflow.movingFlowEntries
import com.hedvig.android.feature.onboarding.data.ResetOnboardingSeenUseCase
import com.hedvig.android.feature.payin.account.navigation.PayinAccountKey
import com.hedvig.android.feature.payin.account.navigation.payinAccountEntries
import com.hedvig.android.feature.payoutaccount.navigation.PayoutAccountKey
import com.hedvig.android.feature.payoutaccount.navigation.payoutAccountEntries
import com.hedvig.android.feature.remove.addons.removeAddonsEntries
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceKey
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceEntries
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateKey
import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.add
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import com.hedvig.android.navigation.compose.popUpTo
import com.hedvig.android.navigation.compose.removeAllOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Registers every in-app destination on the [EntryProviderScope]: the entries of features every host can
 * render here, the rest through [hostEntries]. Decomposed into per-domain sub-builders so each tab/flow's
 * wiring is readable in isolation. The shared cross-feature `navigateToX` closures are derived once here and
 * threaded down to the sub-builders that need them, and to [hostEntries] through [HostEntryContext]. All
 * wiring stays out of the features because feature `*Key`s live in feature *impl* modules and the
 * feature-isolation build rule blocks one feature from referencing another's key.
 */
internal fun EntryProviderScope<HedvigNavKey>.hedvigEntryProvider(
  backstack: BackstackController,
  scope: CoroutineScope,
  windowSizeClass: WindowSizeClass,
  memberIdService: MemberIdService,
  globalSnackBarState: GlobalSnackBarState,
  externalNavigator: ExternalNavigator,
  appHost: AppHost,
  openUrl: (String) -> Unit,
  openCrossSellUrl: (String) -> Boolean,
  imageLoader: ImageLoader,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  resetOnboardingSeenUseCase: ResetOnboardingSeenUseCase,
  hostEntries: EntryProviderScope<HedvigNavKey>.(HostEntryContext) -> Unit,
) {
  val shouldShowRequestPermissionRationale: (String) -> Boolean = appHost::shouldShowPermissionRationale
  val navigateToTrustly: () -> Unit = { backstack.add(TrustlyKey()) }
  val navigateToPayinAccount: () -> Unit = { backstack.add(PayinAccountKey) }
  val navigateToPayoutAccount: () -> Unit = { backstack.add(PayoutAccountKey) }
  val navigateToInbox: () -> Unit = { backstack.add(InboxKey) }
  val navigateToNewConversation: () -> Unit = { backstack.add(ChatKey(Uuid.randomUUID().toString())) }
  val navigateToConversation: (String) -> Unit = { conversationId -> backstack.add(ChatKey(conversationId)) }
  val navigateToTravelCertificate: () -> Unit = { backstack.add(TravelCertificateKey) }
  val navigateToAddonPurchaseFlow: (List<String>, AddonBannerSource) -> Unit = { ids, source ->
    backstack.add(
      AddonPurchaseKey(
        insuranceIds = ids,
        preselectedAddonDisplayName = null,
        source = source,
      ),
    )
  }
  val navigateToMovingFlow: (MovingSource) -> Unit = { source -> backstack.add(SelectContractForMovingKey(source)) }
  val onNavigateToImageViewer: (String, String) -> Unit = { imageUrl, cacheKey ->
    backstack.add(ImageViewerKey(imageUrl, cacheKey))
  }
  val openPrivacyPolicy: () -> Unit = { openUrl(privacyPolicyInAppUrl(languageService.getLanguage())) }
  val onResetOnboardingForDebug: () -> Unit = {
    scope.launch {
      resetOnboardingSeenUseCase.invoke()
      // Debug-only feedback for non-production builds; deliberately not translated.
      globalSnackBarState.show("Onboarding reset. Background and foreground the app to see it.")
    }
  }
  val changePayinMethod: () -> Unit = {
    when (val target = changePayinMethodTarget(backstack.entries)) {
      is ChangePayinMethodTarget.PopTo -> backstack.popUpToIndex(target.index)
      is ChangePayinMethodTarget.Reseed -> backstack.reseed(target.stack)
      ChangePayinMethodTarget.PopOne -> backstack.popBackstack()
    }
  }
  addHomeEntries(
    backstack = backstack,
    appPackageId = hedvigBuildConstants.appPackageId,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    externalNavigator = externalNavigator,
    imageLoader = imageLoader,
    openUrl = openUrl,
    onNavigateToImageViewer = onNavigateToImageViewer,
    navigateToNewConversation = navigateToNewConversation,
  )
  addInsuranceEntries(
    backstack = backstack,
    windowSizeClass = windowSizeClass,
    imageLoader = imageLoader,
    openUrl = openUrl,
    externalNavigator = externalNavigator,
    navigateToNewConversation = navigateToNewConversation,
  )
  payoutAccountEntries(
    backstack = backstack,
    navigateToTrustly = navigateToTrustly,
  )
  payinAccountEntries(
    backstack = backstack,
    hedvigBuildConstants = hedvigBuildConstants,
    navigateToTrustly = navigateToTrustly,
    returnToPayinOverview = changePayinMethod,
  )
  addProfileEntries(backstack = backstack)
  addSharedFlowEntries(
    backstack = backstack,
    globalSnackBarState = globalSnackBarState,
    imageLoader = imageLoader,
    openUrl = openUrl,
    externalNavigator = externalNavigator,
    navigateToNewConversation = navigateToNewConversation,
    navigateToInbox = navigateToInbox,
  )
  hostEntries(
    HostEntryContext(
      backstack = backstack,
      scope = scope,
      memberIdService = memberIdService,
      globalSnackBarState = globalSnackBarState,
      externalNavigator = externalNavigator,
      imageLoader = imageLoader,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
      openUrl = openUrl,
      openCrossSellUrl = openCrossSellUrl,
      navigateToTrustly = navigateToTrustly,
      navigateToPayinAccount = navigateToPayinAccount,
      navigateToPayoutAccount = navigateToPayoutAccount,
      navigateToInbox = navigateToInbox,
      navigateToNewConversation = navigateToNewConversation,
      navigateToConversation = navigateToConversation,
      navigateToTravelCertificate = navigateToTravelCertificate,
      navigateToAddonPurchaseFlow = navigateToAddonPurchaseFlow,
      navigateToMovingFlow = navigateToMovingFlow,
      onNavigateToImageViewer = onNavigateToImageViewer,
      openPrivacyPolicy = openPrivacyPolicy,
      onResetOnboardingForDebug = onResetOnboardingForDebug,
      changePayinMethod = changePayinMethod,
    ),
  )
}

/**
 * What [hedvigEntryProvider] hands to the host's own entries: the dependencies those entries need and the
 * cross-feature closures derived once in [hedvigEntryProvider]. A closure this context provides is taken from
 * it and never rebuilt by a host; closures it does not provide are built in the host's entries. A field is
 * removed when its last host-side consumer moves into the shared provider.
 */
class HostEntryContext internal constructor(
  val backstack: BackstackController,
  val scope: CoroutineScope,
  val memberIdService: MemberIdService,
  val globalSnackBarState: GlobalSnackBarState,
  val externalNavigator: ExternalNavigator,
  val imageLoader: ImageLoader,
  val languageService: LanguageService,
  val hedvigBuildConstants: HedvigBuildConstants,
  val openUrl: (String) -> Unit,
  val openCrossSellUrl: (String) -> Boolean,
  val navigateToTrustly: () -> Unit,
  val navigateToPayinAccount: () -> Unit,
  val navigateToPayoutAccount: () -> Unit,
  val navigateToInbox: () -> Unit,
  val navigateToNewConversation: () -> Unit,
  val navigateToConversation: (String) -> Unit,
  val navigateToTravelCertificate: () -> Unit,
  val navigateToAddonPurchaseFlow: (List<String>, AddonBannerSource) -> Unit,
  val navigateToMovingFlow: (MovingSource) -> Unit,
  val onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  val openPrivacyPolicy: () -> Unit,
  val onResetOnboardingForDebug: () -> Unit,
  val changePayinMethod: () -> Unit,
)

/**
 * The app-specific privacy notice, per language. Mirrors the "Privacy policy (Hedvig app)" link on the
 * profile "about app" screen; used in `feature-onboarding` and UsageData destination in profile.
 */
private fun privacyPolicyInAppUrl(language: Language): String = when (language) {
  Language.SV_SE -> "https://www.hedvig.com/se/hedvig/integritetsnotis-appen"
  Language.EN_SE -> "https://www.hedvig.com/se-en/hedvig/privacy-notice-app"
}

/** The home tab's nested destinations that every host can render. */
private fun EntryProviderScope<HedvigNavKey>.addHomeEntries(
  backstack: BackstackController,
  appPackageId: String,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateToNewConversation: () -> Unit,
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
}

/** The insurances tab's nested destinations that every host can render. */
private fun EntryProviderScope<HedvigNavKey>.addInsuranceEntries(
  backstack: BackstackController,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  externalNavigator: ExternalNavigator,
  navigateToNewConversation: () -> Unit,
) {
  terminateInsuranceEntries(
    windowSizeClass = windowSizeClass,
    backstack = backstack,
    imageLoader = imageLoader,
    onNavigateToNewConversation = navigateToNewConversation,
    openUrl = openUrl,
    openPlayStore = externalNavigator::tryOpenPlayStore,
    navigateToInsurances = {
      backstack.popUpTo<TerminateInsuranceKey>(inclusive = true)
      backstack.selectTopLevel(TopLevelTab.Insurances)
    },
    navigateToMovingFlow = {
      backstack.navigateAndPopUpTo<TerminateInsuranceKey>(
        SelectContractForMovingKey(MovingSource.TERMINATION),
        inclusive = true,
      )
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
}

/** The profile tab's nested destinations that every host can render. */
private fun EntryProviderScope<HedvigNavKey>.addProfileEntries(backstack: BackstackController) {
  deleteAccountEntries(backstack)
  claimHistoryEntries(
    backstack = backstack,
    navigateToClaimDetails = { claimId -> backstack.add(ClaimDetailsKey(claimId)) },
  )
}

/**
 * Cross-cutting flows reachable from more than one tab: addon purchase/removal, tier changes,
 * co-insured editing, moving, chip-id and the help center.
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
  removeAddonsEntries(backstack = backstack)
}
