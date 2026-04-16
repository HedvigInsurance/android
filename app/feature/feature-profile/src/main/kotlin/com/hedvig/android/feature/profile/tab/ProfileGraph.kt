package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import com.hedvig.android.design.system.hedvig.motion.MotionDefaults
import com.hedvig.android.feature.profile.aboutapp.AboutAppDestination
import com.hedvig.android.feature.profile.aboutapp.AboutAppViewModel
import com.hedvig.android.feature.profile.aboutapp.LicensesDestination
import com.hedvig.android.feature.profile.certificates.CertificatesDestination
import com.hedvig.android.feature.profile.certificates.CertificatesViewModel
import com.hedvig.android.feature.profile.contactinfo.ContactInfoDestination
import com.hedvig.android.feature.profile.contactinfo.ContactInfoViewModel
import com.hedvig.android.feature.profile.eurobonus.EurobonusDestination
import com.hedvig.android.feature.profile.eurobonus.EurobonusViewModel
import com.hedvig.android.feature.profile.legal.LegalInfoDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestinations
import com.hedvig.android.feature.profile.navigation.ProfileDestinations.Certificates
import com.hedvig.android.feature.profile.navigation.SettingsDestinations
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.language.LanguageService
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.profileGraph(
  settingsDestinationNestedGraphs: NavGraphBuilder.() -> Unit,
  nestedGraphs: NavGraphBuilder.() -> Unit,
  globalSnackBarState: GlobalSnackBarState,
  navController: NavController,
  popBackStackOrFinish: () -> Unit,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String, CoInsuredFlowType) -> Unit,
  navigateToDeleteAccountFeature: () -> Unit,
  navigateToClaimHistory: () -> Unit,
  openAppSettings: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  openUrl: (String) -> Unit,
  navigateToChipId: () -> Unit,
  languageService: LanguageService
) {
  navgraph<ProfileDestination.Graph>(
    startDestination = ProfileDestination.Profile::class,
  ) {
    navdestination<ProfileDestination.Profile>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.profile),
      enterTransition = { MotionDefaults.fadeThroughEnter },
      exitTransition = { MotionDefaults.fadeThroughExit },
    ) {
      val viewModel: ProfileViewModel = koinViewModel()
      ProfileDestination(
        navigateToEurobonus = dropUnlessResumed {
          navController.navigate(ProfileDestinations.Eurobonus)
        },
        navigateToClaimHistory = dropUnlessResumed { navigateToClaimHistory() },
        navigateToContactInfo = dropUnlessResumed {
          navController.navigate(ProfileDestination.ContactInfo)
        },
        navigateToAboutApp = dropUnlessResumed {
          navController.navigate(ProfileDestinations.AboutApp)
        },
        navigateToSettings = dropUnlessResumed {
          navController.navigate(ProfileDestinations.SettingsGraph)
        },
        navigateToCertificates = dropUnlessResumed {
          navController.navigate(Certificates)
        },
        navigateToConnectPayment = dropUnlessResumed { navigateToConnectPayment() },
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
        navigateToLegalInfo = dropUnlessResumed {
          navController.navigate(ProfileDestinations.Legal)
        },
      )
    }

    navdestination<ProfileDestinations.Legal> {
      LegalInfoDestination(
        openUrl = openUrl,
        navigateUp = navController::navigateUp,
        languageService = languageService
      )
    }

    navdestination<ProfileDestinations.Eurobonus>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.eurobonus),
    ) {
      val viewModel: EurobonusViewModel = koinViewModel()
      EurobonusDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
      )
    }
    navdestination<ProfileDestination.ContactInfo>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.contactInfo),
    ) {
      val viewModel: ContactInfoViewModel = koinViewModel()
      ContactInfoDestination(
        viewModel = viewModel,
        globalSnackBarState = globalSnackBarState,
        navigateUp = navController::navigateUp,
        popBackStack = popBackStackOrFinish,
      )
    }
    navdestination<ProfileDestinations.AboutApp> {
      val viewModel: AboutAppViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navController::navigateUp,
        showOpenSourceLicenses = dropUnlessResumed {
          navController.navigate(ProfileDestinations.Licenses)
        },
        navigateToNewConversation = dropUnlessResumed { onNavigateToNewConversation() },
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
    navdestination<ProfileDestinations.Licenses> {
      LicensesDestination(
        onBackPressed = navController::navigateUp,
      )
    }
    navdestination<Certificates> {
      val viewModel: CertificatesViewModel = koinViewModel()
      CertificatesDestination(
        viewModel = viewModel,
        navigateUp = navController::navigateUp,
        onNavigateToInsuranceEvidence = dropUnlessResumed { onNavigateToInsuranceEvidence() },
        onNavigateToTravelCertificate = dropUnlessResumed { onNavigateToTravelCertificate() },
      )
    }
    navgraph<ProfileDestinations.SettingsGraph>(
      startDestination = SettingsDestinations.Settings::class,
    ) {
      navdestination<SettingsDestinations.Settings> {
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsDestination(
          viewModel = viewModel,
          navigateUp = navController::navigateUp,
          openAppSettings = openAppSettings,
          onNavigateToDeleteAccountFeature = dropUnlessResumed { navigateToDeleteAccountFeature() },
        )
      }
      settingsDestinationNestedGraphs()
    }
    nestedGraphs()
  }
}
