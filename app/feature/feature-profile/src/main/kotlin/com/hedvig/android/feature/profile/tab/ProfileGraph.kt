package com.hedvig.android.feature.profile.tab

import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
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
import com.hedvig.android.feature.profile.navigation.ProfileDestination
import com.hedvig.android.feature.profile.navigation.ProfileDestinations
import com.hedvig.android.feature.profile.navigation.ProfileDestinations.Certificates
import com.hedvig.android.feature.profile.navigation.SettingsDestinations
import com.hedvig.android.feature.profile.settings.SettingsDestination
import com.hedvig.android.feature.profile.settings.SettingsViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.profileGraph(
  nestedGraphs: NavGraphBuilder.() -> Unit,
  settingsDestinationNestedGraphs: NavGraphBuilder.() -> Unit,
  navigator: Navigator,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  hedvigBuildConstants: HedvigBuildConstants,
  navigateToConnectPayment: () -> Unit,
  navigateToAddMissingInfo: (contractId: String) -> Unit,
  navigateToDeleteAccountFeature: () -> Unit,
  navigateToClaimHistory: () -> Unit,
  openAppSettings: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  openUrl: (String) -> Unit,
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
        navigateToEurobonus = {
          navigator.navigate(ProfileDestinations.Eurobonus)
        },
        navigateToClaimHistory = navigateToClaimHistory,
        navigateToContactInfo = {
          navigator.navigate(ProfileDestination.ContactInfo)
        },
        navigateToAboutApp = {
          navigator.navigate(ProfileDestinations.AboutApp)
        },
        navigateToSettings = {
          navigator.navigate(ProfileDestinations.SettingsGraph)
        },
        navigateToCertificates = {
          navigator.navigate(Certificates)
        },
        navigateToConnectPayment = navigateToConnectPayment,
        navigateToAddMissingInfo = { contractId ->
          navigateToAddMissingInfo(contractId)
        },
        openAppSettings = openAppSettings,
        openUrl = openUrl,
        viewModel = viewModel,
        onNavigateToNewConversation = {
          onNavigateToNewConversation()
        },
      )
    }
    navdestination<ProfileDestinations.Eurobonus>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.eurobonus),
    ) {
      val viewModel: EurobonusViewModel = koinViewModel()
      EurobonusDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<ProfileDestination.ContactInfo>(
      deepLinks = navDeepLinks(hedvigDeepLinkContainer.contactInfo),
    ) {
      val viewModel: ContactInfoViewModel = koinViewModel()
      ContactInfoDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
      )
    }
    navdestination<ProfileDestinations.AboutApp> {
      val viewModel: AboutAppViewModel = koinViewModel()
      AboutAppDestination(
        viewModel = viewModel,
        onBackPressed = navigator::navigateUp,
        showOpenSourceLicenses = {
          navigator.navigate(ProfileDestinations.Licenses)
        },
        navigateToNewConversation = { onNavigateToNewConversation() },
        hedvigBuildConstants = hedvigBuildConstants,
      )
    }
    navdestination<ProfileDestinations.Licenses> {
      LicensesDestination(
        onBackPressed = navigator::navigateUp,
      )
    }
    navdestination<Certificates> {
      val viewModel: CertificatesViewModel = koinViewModel()
      CertificatesDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToInsuranceEvidence = onNavigateToInsuranceEvidence,
        onNavigateToTravelCertificate = onNavigateToTravelCertificate,
      )
    }
    navgraph<ProfileDestinations.SettingsGraph>(
      startDestination = SettingsDestinations.Settings::class,
    ) {
      navdestination<SettingsDestinations.Settings> {
        val viewModel: SettingsViewModel = koinViewModel()
        SettingsDestination(
          viewModel = viewModel,
          navigateUp = navigator::navigateUp,
          openAppSettings = openAppSettings,
          onNavigateToDeleteAccountFeature = { navigateToDeleteAccountFeature() },
        )
      }
      settingsDestinationNestedGraphs()
    }
    nestedGraphs()
  }
}
