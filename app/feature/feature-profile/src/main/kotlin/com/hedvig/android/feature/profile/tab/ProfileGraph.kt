package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.aboutapp.InformationDestination
import com.hedvig.android.feature.profile.aboutapp.LicensesDestination
import com.hedvig.android.feature.profile.certificates.CertificatesDestination
import com.hedvig.android.feature.profile.certificates.CertificatesViewModel
import com.hedvig.android.feature.profile.contactinfo.ContactInfoDestination
import com.hedvig.android.feature.profile.contactinfo.ContactInfoViewModel
import com.hedvig.android.feature.profile.eurobonus.EurobonusDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.navigation.CertificatesKey
import com.hedvig.android.feature.profile.navigation.ContactInfoKey
import com.hedvig.android.feature.profile.navigation.EurobonusKey
import com.hedvig.android.feature.profile.navigation.InformationKey
import com.hedvig.android.feature.profile.navigation.LicensesKey
import com.hedvig.android.feature.profile.navigation.ProfileKey
import com.hedvig.android.feature.profile.navigation.SettingsGraphKey
import com.hedvig.android.feature.profile.navigation.SettingsKey
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navigateUp
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.profileGraph(
  settingsDestinationNestedGraphs: EntryProviderScope<HedvigNavKey>.() -> Unit,
  nestedGraphs: EntryProviderScope<HedvigNavKey>.() -> Unit,
  globalSnackBarState: GlobalSnackBarState,
  backStack: MutableList<HedvigNavKey>,
  popBackStackOrFinish: () -> Unit,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToConnectPayout: () -> Unit,
  navigateToAddMissingInfo: (contractId: String, CoInsuredFlowType) -> Unit,
  navigateToDeleteAccountFeature: () -> Unit,
  navigateToClaimHistory: () -> Unit,
  openAppSettings: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToChipId: () -> Unit,
  languageService: LanguageService,
) {
  entry<ProfileKey>(
    metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit),
  ) {
    val viewModel: ProfileViewModel = metroViewModel()
    ProfileDestination(
      navigateToEurobonus = dropUnlessResumed {
        backStack.add(EurobonusKey)
      },
      navigateToClaimHistory = dropUnlessResumed { navigateToClaimHistory() },
      navigateToContactInfo = dropUnlessResumed {
        backStack.add(ContactInfoKey)
      },
      navigateToAboutApp = dropUnlessResumed {
        backStack.add(InformationKey)
      },
      navigateToSettings = dropUnlessResumed {
        backStack.add(SettingsGraphKey)
      },
      navigateToCertificates = dropUnlessResumed {
        backStack.add(CertificatesKey)
      },
      navigateToConnectPayment = dropUnlessResumed { navigateToConnectPayment() },
      navigateToConnectPayout = dropUnlessResumed { navigateToConnectPayout() },
      navigateToAddMissingInfo = dropUnlessResumed { contractId: String, type: CoInsuredFlowType ->
        navigateToAddMissingInfo(contractId, type)
      },
      openAppSettings = openAppSettings,
      openUrl = openUrl,
      viewModel = viewModel,
      onNavigateToNewConversation = dropUnlessResumed {
        onNavigateToNewConversation()
      },
      navigateToChipId = navigateToChipId,
    )
  }

  entry<EurobonusKey> {
    val viewModel: EurobonusViewModel = metroViewModel()
    EurobonusDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
    )
  }
  entry<ContactInfoKey> {
    val viewModel: ContactInfoViewModel = metroViewModel()
    ContactInfoDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      navigateUp = backStack::navigateUp,
      popBackStack = popBackStackOrFinish,
    )
  }
  entry<InformationKey> {
    val viewModel: AboutAppViewModel = metroViewModel()
    InformationDestination(
      viewModel = viewModel,
      onBackPressed = backStack::navigateUp,
      showOpenSourceLicenses = dropUnlessResumed {
        backStack.add(LicensesKey)
      },
      navigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      hedvigBuildConstants = hedvigBuildConstants,
      languageService = languageService,
      openUrl = openUrl,
    )
  }
  entry<LicensesKey> {
    LicensesDestination(
      onBackPressed = backStack::navigateUp,
    )
  }
  entry<CertificatesKey> {
    val viewModel: CertificatesViewModel = metroViewModel()
    CertificatesDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onNavigateToInsuranceEvidence = dropUnlessResumed { onNavigateToInsuranceEvidence() },
      onNavigateToTravelCertificate = dropUnlessResumed { onNavigateToTravelCertificate() },
    )
  }
  entry<SettingsKey> {
    val viewModel: SettingsViewModel = metroViewModel()
    SettingsDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      openAppSettings = openAppSettings,
      onNavigateToDeleteAccountFeature = dropUnlessResumed { navigateToDeleteAccountFeature() },
    )
  }
  settingsDestinationNestedGraphs()
  nestedGraphs()
}
