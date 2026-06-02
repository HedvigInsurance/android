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
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestinations
import com.hedvig.android.feature.profile.navigation.ProfileDestinations.Certificates
import com.hedvig.android.feature.profile.navigation.SettingsDestinations
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.entryTransitionMetadata
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<Destination>.profileGraph(
  settingsDestinationNestedGraphs: EntryProviderScope<Destination>.() -> Unit,
  nestedGraphs: EntryProviderScope<Destination>.() -> Unit,
  globalSnackBarState: GlobalSnackBarState,
  navigator: Navigator,
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
  navgraph(
    startDestination = ProfileDestination.Profile::class,
  ) {
    navdestination<ProfileDestination.Profile>(
      metadata = entryTransitionMetadata(MotionDefaults.fadeThroughEnter, MotionDefaults.fadeThroughExit),
    ) {
      val viewModel: ProfileViewModel = metroViewModel()
      ProfileDestination(
        navigateToEurobonus = dropUnlessResumed {
          navigator.navigate(ProfileDestinations.Eurobonus)
        },
        navigateToClaimHistory = dropUnlessResumed { navigateToClaimHistory() },
        navigateToContactInfo = dropUnlessResumed {
          navigator.navigate(ProfileDestination.ContactInfo)
        },
        navigateToAboutApp = dropUnlessResumed {
          navigator.navigate(ProfileDestinations.Information)
        },
        navigateToSettings = dropUnlessResumed {
          navigator.navigate(ProfileDestinations.SettingsGraph)
        },
        navigateToCertificates = dropUnlessResumed {
          navigator.navigate(Certificates)
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

    navdestination<ProfileDestinations.Eurobonus> {
      val viewModel: EurobonusViewModel = metroViewModel()
      EurobonusDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<ProfileDestination.ContactInfo> {
      val viewModel: ContactInfoViewModel = metroViewModel()
      ContactInfoDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        navigateUp = navigator::navigateUp,
        popBackStack = popBackStackOrFinish,
      )
    }
    navdestination<ProfileDestinations.Information> {
      val viewModel: AboutAppViewModel = metroViewModel()
      InformationDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = dropUnlessResumed {
          navigator.navigate(ProfileDestinations.Licenses)
        },
        navigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        hedvigBuildConstants = hedvigBuildConstants,
        languageService = languageService,
        openUrl = openUrl,
      )
    }
    navdestination<ProfileDestinations.Licenses> {
      LicensesDestination(
        onBackPressed = navigator::navigateUp,
      )
    }
    navdestination<Certificates> {
      val viewModel: CertificatesViewModel = metroViewModel()
      CertificatesDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToInsuranceEvidence = dropUnlessResumed { onNavigateToInsuranceEvidence() },
        onNavigateToTravelCertificate = dropUnlessResumed { onNavigateToTravelCertificate() },
      )
    }
    navgraph(
      startDestination = SettingsDestinations.Settings::class,
    ) {
      navdestination<SettingsDestinations.Settings> {
        val viewModel: SettingsViewModel = metroViewModel()
        SettingsDestination(
          viewModel = viewModel,
          navigateUp = navigator::navigateUp,
          openAppSettings = openAppSettings,
          onNavigateToDeleteAccountFeature = dropUnlessResumed { navigateToDeleteAccountFeature() },
        )
      }
      settingsDestinationNestedGraphs()
    }
    nestedGraphs()
  }
}
