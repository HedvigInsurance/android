package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
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
import com.hedvig.android.feature.profile.navigation.SettingsKey
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.NavSuiteSceneDecoratorStrategy
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.profileEntries(
  settingsDestinationNestedGraphs: EntryProviderScope<HedvigNavKey>.() -> Unit,
  nestedEntries: EntryProviderScope<HedvigNavKey>.() -> Unit,
  globalSnackBarState: GlobalSnackBarState,
  backstack: Backstack,
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
  entry<ProfileKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: ProfileViewModel = metroViewModel()
    ProfileDestination(
      navigateToEurobonus = dropUnlessResumed {
        backstack.add(EurobonusKey)
      },
      navigateToClaimHistory = dropUnlessResumed { navigateToClaimHistory() },
      navigateToContactInfo = dropUnlessResumed {
        backstack.add(ContactInfoKey)
      },
      navigateToAboutApp = dropUnlessResumed {
        backstack.add(InformationKey)
      },
      navigateToSettings = dropUnlessResumed {
        backstack.add(SettingsKey)
      },
      navigateToCertificates = dropUnlessResumed {
        backstack.add(CertificatesKey)
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

  entry<EurobonusKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: EurobonusViewModel = metroViewModel()
    EurobonusDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
    )
  }
  entry<ContactInfoKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: ContactInfoViewModel = metroViewModel()
    ContactInfoDestination(
      viewModel = viewModel,
      globalSnackBarState = globalSnackBarState,
      navigateUp = backstack::navigateUp,
      popBackstack = backstack::popBackstack,
    )
  }
  entry<InformationKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: AboutAppViewModel = metroViewModel()
    InformationDestination(
      viewModel = viewModel,
      onBackPressed = backstack::navigateUp,
      showOpenSourceLicenses = dropUnlessResumed {
        backstack.add(LicensesKey)
      },
      navigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
      hedvigBuildConstants = hedvigBuildConstants,
      languageService = languageService,
      openUrl = openUrl,
    )
  }
  entry<LicensesKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    LicensesDestination(
      onBackPressed = backstack::navigateUp,
    )
  }
  entry<CertificatesKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: CertificatesViewModel = metroViewModel()
    CertificatesDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onNavigateToInsuranceEvidence = dropUnlessResumed { onNavigateToInsuranceEvidence() },
      onNavigateToTravelCertificate = dropUnlessResumed { onNavigateToTravelCertificate() },
    )
  }
  entry<SettingsKey>(metadata = NavSuiteSceneDecoratorStrategy.showNavBar()) {
    val viewModel: SettingsViewModel = metroViewModel()
    SettingsDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      openAppSettings = openAppSettings,
      onNavigateToDeleteAccountFeature = dropUnlessResumed { navigateToDeleteAccountFeature() },
    )
  }
  settingsDestinationNestedGraphs()
  nestedEntries()
}
