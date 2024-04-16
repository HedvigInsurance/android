package com.hedvig.android.app.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import coil.ImageLoader
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.chat.navigation.chatGraph
import com.hedvig.android.feature.claim.details.navigation.claimDetailsGraph
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import com.hedvig.android.feature.claimtriaging.claimTriagingDestinations
import com.hedvig.android.feature.connect.payment.adyen.connectAdyenPaymentGraph
import com.hedvig.android.feature.connect.payment.connectPaymentGraph
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountDestination
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountGraph
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredGraph
import com.hedvig.android.feature.forever.navigation.foreverGraph
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.helpCenterGraph
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.insurance.insuranceGraph
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.feature.odyssey.navigation.claimFlowGraph
import com.hedvig.android.feature.odyssey.navigation.navigateToClaimFlowDestination
import com.hedvig.android.feature.odyssey.navigation.terminalClaimFlowStepDestinations
import com.hedvig.android.feature.payments.navigation.paymentsGraph
import com.hedvig.android.feature.profile.tab.profileGraph
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceGraphDestination
import com.hedvig.android.feature.terminateinsurance.navigation.terminateInsuranceGraph
import com.hedvig.android.feature.travelcertificate.navigation.travelCertificateGraph
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.app.BuildConfig
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popBackStack
import com.kiwi.navigationcompose.typed.popUpTo

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  externalNavigator: ExternalNavigator,
  finishApp: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openUrl: (String) -> Unit,
  onOpenEmailApp: () -> Unit,
  imageLoader: ImageLoader,
  market: Market,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val density = LocalDensity.current
  val navigator: Navigator = rememberNavigator(hedvigAppState.navController, finishApp)

  val navigateToConnectPayment = {
    when (market) {
      Market.SE -> hedvigAppState.navController.navigate(AppDestination.ConnectPayment)
      Market.NO,
      Market.DK,
      -> hedvigAppState.navController.navigate(AppDestination.ConnectPaymentAdyen)
    }
  }

  NavHost(
    navController = hedvigAppState.navController,
    startDestination = createRoutePattern<HomeDestination.Graph>(),
    route = RootGraph.route,
    modifier = modifier,
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    loginGraph(
      navigator = navigator,
      appVersionName = hedvigBuildConstants.appVersionName,
      urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
      openUrl = openUrl,
      onOpenEmailApp = onOpenEmailApp,
      startLoggedInActivity = hedvigAppState::navigateToLoggedIn,
    )
    homeGraph(
      nestedGraphs = {
        nestedHomeGraphs(
          density = density,
          hedvigAppState = hedvigAppState,
          hedvigBuildConstants = hedvigBuildConstants,
          context = context,
          navigator = navigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          externalNavigator = externalNavigator,
          imageLoader = imageLoader,
          openUrl = openUrl,
        )
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onStartChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat())
        }
      },
      onStartClaim = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(AppDestination.ClaimsFlow) }
      },
      navigateToClaimDetails = { backStackEntry, claimId ->
        with(navigator) { backStackEntry.navigate(AppDestination.ClaimDetails(claimId)) }
      },
      navigateToPayinScreen = navigateToConnectPayment,
      navigateToMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) { backStackEntry.navigate(AppDestination.CoInsuredAddInfo(contractId)) }
      },
      navigateToHelpCenter = { backStackEntry ->
        with(navigator) { backStackEntry.navigate(HelpCenterDestination) }
      },
      openAppSettings = { externalNavigator.openAppSettings(context) },
      openUrl = openUrl,
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navigator = navigator,
          navController = hedvigAppState.navController,
          openChat = { backStackEntry ->
            with(navigator) {
              backStackEntry.navigate(AppDestination.Chat())
            }
          },
          openUrl = openUrl,
          openPlayStore = { externalNavigator.tryOpenPlayStore(context) },
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          navigateToInsurances = { navOptions ->
            hedvigAppState.navController.navigate(InsurancesDestination.Graph, navOptions)
          },
          closeTerminationFlow = {
            /**
             * If we fail to pop the backstack including TerminateInsuranceGraphDestination here it means we were deep
             * linked into this screen only, and they do not wish to continue with the flow they were deep linked to.
             * The right way to handle this is to simply finish the app as per the docs:
             * https://developer.android.com/guide/navigation/backstack#handle-failure
             */
            if (!hedvigAppState.navController.popBackStack<TerminateInsuranceGraphDestination>(inclusive = true)) {
              finishApp()
            }
          },
        )
      },
      navigator = navigator,
      openUrl = openUrl,
      openChat = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat())
        }
      },
      startMovingFlow = { backStackEntry ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.ChangeAddress)
        }
      },
      startTerminationFlow = { backStackEntry: NavBackStackEntry, data: CancelInsuranceData ->
        with(navigator) {
          backStackEntry.navigate(
            TerminateInsuranceGraphDestination(insuranceId = data.contractId),
          )
        }
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      imageLoader = imageLoader,
      startEditCoInsured = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.CoInsuredAddOrRemove(contractId))
        }
      },
      startEditCoInsuredAddMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.CoInsuredAddInfo(contractId))
        }
      },
    )
    foreverGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    paymentsGraph(
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigateToConnectPayment = navigateToConnectPayment,
      languageService = languageService,
      hedvigBuildConstants = hedvigBuildConstants,
    )
    profileGraph(
      nestedGraphs = {},
      settingsDestinationNestedGraphs = {
        deleteAccountGraph(hedvigDeepLinkContainer, navigator)
      },
      navigator = navigator,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      navigateToConnectPayment = navigateToConnectPayment,
      navigateToAddMissingInfo = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.CoInsuredAddInfo(contractId))
        }
      },
      navigateToDeleteAccountFeature = { backStackEntry: NavBackStackEntry ->
        with(navigator) { backStackEntry.navigate(DeleteAccountDestination) }
      },
      openAppSettings = { externalNavigator.openAppSettings(context) },
      openUrl = openUrl,
    )
    chatGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      openUrl = openUrl,
      navigator = navigator,
    )
    connectPaymentGraph(
      navigator = navigator,
      market = market,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigateToAdyenConnectPayment = {
        navigator.navigateUnsafe(
          AppDestination.ConnectPaymentAdyen,
          navOptions {
            popUpTo(createRoutePattern<AppDestination.ConnectPayment>()) {
              inclusive = true
            }
          },
        )
      },
    )
    editCoInsuredGraph(
      navigateUp = navigator::navigateUp,
      navController = hedvigAppState.navController,
    )
    connectAdyenPaymentGraph(navigator)
    helpCenterGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigator = navigator,
      onNavigateToQuickLink = { backStackEntry, quickLinkDestination ->
        val destination = when (quickLinkDestination) {
          QuickLinkDestination.OuterDestination.QuickLinkChangeAddress -> AppDestination.ChangeAddress
          is QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo ->
            AppDestination.CoInsuredAddInfo(quickLinkDestination.contractId)

          is QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove ->
            AppDestination.CoInsuredAddOrRemove(quickLinkDestination.contractId)

          QuickLinkDestination.OuterDestination.QuickLinkConnectPayment -> AppDestination.ConnectPayment
          QuickLinkDestination.OuterDestination.QuickLinkTermination -> TerminateInsuranceGraphDestination(null)
          QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate -> AppDestination.TravelCertificate
        }
        with(navigator) {
          backStackEntry.navigate(destination)
        }
      },
      openChat = { backStackEntry, chatContext ->
        with(navigator) {
          backStackEntry.navigate(AppDestination.Chat(chatContext))
        }
      },
    )
  }
}

private fun NavGraphBuilder.nestedHomeGraphs(
  density: Density,
  hedvigAppState: HedvigAppState,
  hedvigBuildConstants: HedvigBuildConstants,
  context: Context,
  navigator: Navigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  claimDetailsGraph(
    navigator = navigator,
    imageLoader = imageLoader,
    openUrl = openUrl,
    navigateUp = navigator::navigateUp,
    appPackageId = hedvigBuildConstants.appId,
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat())
      }
    },
    applicationId = BuildConfig.APPLICATION_ID,
  )
  changeAddressGraph(
    navController = hedvigAppState.navController,
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat())
      }
    },
    openUrl = openUrl,
  )
  travelCertificateGraph(
    density = density,
    navController = hedvigAppState.navController,
    applicationId = BuildConfig.APPLICATION_ID,
  )
  claimFlowGraph(
    windowSizeClass = hedvigAppState.windowSizeClass,
    navigator = navigator,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    navigateToTriaging = {
      navigator.navigateUnsafe(ClaimTriagingDestination.ClaimGroups)
    },
    openAppSettings = {
      externalNavigator.openAppSettings(context)
    },
    closeClaimFlow = {
      hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
    },
    nestedGraphs = {
      claimTriagingDestinations(
        navigator = navigator,
        windowSizeClass = hedvigAppState.windowSizeClass,
        startClaimFlow = { backStackEntry, claimFlowStep: ClaimFlowStep ->
          navigator.navigateToClaimFlowDestination(backStackEntry, claimFlowStep.toClaimFlowDestination())
        },
        closeClaimFlow = {
          hedvigAppState.navController.popBackStack<AppDestination.ClaimsFlow>(inclusive = true)
        },
      )
    },
    openUrl = openUrl,
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(AppDestination.Chat())
      }
    },
    imageLoader = imageLoader,
    appPackageId = hedvigBuildConstants.appId,
  )
  terminalClaimFlowStepDestinations(
    navigator = navigator,
    openPlayStore = {
      navigator.popBackStack()
      externalNavigator.tryOpenPlayStore(context)
    },
    openChat = { backStackEntry ->
      with(navigator) {
        backStackEntry.navigate(destination = AppDestination.Chat()) {
          popUpTo<HomeDestination.Home>()
        }
      }
    },
  )
}
