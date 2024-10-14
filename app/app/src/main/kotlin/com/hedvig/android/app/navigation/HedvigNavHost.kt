package com.hedvig.android.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import coil.ImageLoader
import com.benasher44.uuid.Uuid
import com.hedvig.android.app.ui.HedvigAppState
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.toClaimFlowDestination
import com.hedvig.android.feature.change.tier.navigation.ChooseTierGraphDestination
import com.hedvig.android.feature.change.tier.navigation.InsuranceCustomizationParameters
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowChooseInsuranceDestination
import com.hedvig.android.feature.change.tier.navigation.StartTierFlowDestination
import com.hedvig.android.feature.change.tier.navigation.changeTierGraph
import com.hedvig.android.feature.changeaddress.navigation.changeAddressGraph
import com.hedvig.android.feature.chat.navigation.ChatDestination
import com.hedvig.android.feature.chat.navigation.ChatDestinations
import com.hedvig.android.feature.chat.navigation.cbmChatGraph
import com.hedvig.android.feature.claim.details.navigation.claimDetailsGraph
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import com.hedvig.android.feature.claimtriaging.claimTriagingDestinations
import com.hedvig.android.feature.connect.payment.connectPaymentGraph
import com.hedvig.android.feature.connect.payment.trustly.ui.TrustlyDestination
import com.hedvig.android.feature.deleteaccount.navigation.DeleteAccountDestination
import com.hedvig.android.feature.deleteaccount.navigation.deleteAccountGraph
import com.hedvig.android.feature.editcoinsured.navigation.editCoInsuredGraph
import com.hedvig.android.feature.forever.navigation.foreverGraph
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeAddress
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkChangeTier
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkConnectPayment
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTermination
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate
import com.hedvig.android.feature.help.center.helpCenterGraph
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.feature.home.home.navigation.HomeDestination
import com.hedvig.android.feature.home.home.navigation.homeGraph
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.navigation.InsurancesDestination
import com.hedvig.android.feature.insurances.navigation.insuranceGraph
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
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.activity.ExternalNavigator
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.typedPopUpTo
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.AppDestination.ChangeAddress
import com.hedvig.android.navigation.core.AppDestination.ClaimDetails
import com.hedvig.android.navigation.core.AppDestination.CoInsuredAddInfo
import com.hedvig.android.navigation.core.AppDestination.CoInsuredAddOrRemove
import com.hedvig.android.navigation.core.AppDestination.TravelCertificate
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator

@Composable
internal fun HedvigNavHost(
  hedvigAppState: HedvigAppState,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
  externalNavigator: ExternalNavigator,
  finishApp: () -> Unit,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openUrl: (String) -> Unit,
  imageLoader: ImageLoader,
  market: Market,
  languageService: LanguageService,
  hedvigBuildConstants: HedvigBuildConstants,
  modifier: Modifier = Modifier,
) {
  LocalConfiguration.current
  val density = LocalDensity.current
  val navigator: Navigator = rememberNavigator(hedvigAppState.navController, finishApp)

  val navigateToConnectPayment = {
    hedvigAppState.navController.navigate(TrustlyDestination)
  }
  val navigateToInbox = { backStackEntry: NavBackStackEntry ->
    with(navigator) {
      backStackEntry.navigate(ChatDestination)
    }
  }

  fun navigateToNewConversation(backStackEntry: NavBackStackEntry, builder: (NavOptionsBuilder.() -> Unit)? = null) {
    with(navigator) {
      backStackEntry.navigate(ChatDestinations.Chat(Uuid.randomUUID().toString()), builder ?: {})
    }
  }

  val navigateToConversation = { backStackEntry: NavBackStackEntry, conversationId: String ->
    with(navigator) {
      backStackEntry.navigate(ChatDestinations.Chat(conversationId))
    }
  }

  NavHost(
    navController = hedvigAppState.navController,
    startDestination = HomeDestination.Graph::class,
    route = RootGraph::class,
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
      onOpenEmailApp = externalNavigator::openEmailApp,
      onNavigateToLoggedIn = hedvigAppState::navigateToLoggedIn,
    )
    homeGraph(
      nestedGraphs = {
        nestedHomeGraphs(
          density = density,
          hedvigAppState = hedvigAppState,
          hedvigBuildConstants = hedvigBuildConstants,
          navigator = navigator,
          shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
          externalNavigator = externalNavigator,
          imageLoader = imageLoader,
          openUrl = openUrl,
          navigateToNewConversation = ::navigateToNewConversation,
          navigateToConversation = navigateToConversation,
        )
      },
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      onNavigateToInbox = { backStackEntry ->
        navigateToInbox(backStackEntry)
      },
      onNavigateToNewConversation = { backStackEntry ->
        navigateToNewConversation(backStackEntry)
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
      openAppSettings = externalNavigator::openAppSettings,
      openUrl = openUrl,
      navigator = navigator,
    )
    changeTierGraph(
      navigator = navigator,
      navController = hedvigAppState.navController,
      applicationId = hedvigBuildConstants.appId,
    )
    insuranceGraph(
      nestedGraphs = {
        terminateInsuranceGraph(
          windowSizeClass = hedvigAppState.windowSizeClass,
          navigator = navigator,
          navController = hedvigAppState.navController,
          onNavigateToNewConversation = { backStackEntry ->
            navigateToNewConversation(backStackEntry)
          },
          openUrl = openUrl,
          openPlayStore = externalNavigator::tryOpenPlayStore,
          hedvigDeepLinkContainer = hedvigDeepLinkContainer,
          navigateToInsurances = { navOptionsBuilder ->
            hedvigAppState.navController.navigate(InsurancesDestination.Graph, navOptionsBuilder)
          },
          navigateToMovingFlow = { backStackEntry ->
            with(navigator) {
              backStackEntry.navigate(
                destination = AppDestination.ChangeAddress,
              ) {
                typedPopUpTo<TerminateInsuranceGraphDestination> {
                  inclusive = true
                }
              }
            }
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
          redirectToChangeTierFlow = { backStackEntry, idWithIntent ->
            with(navigator) {
              backStackEntry.navigate(
                destination = ChooseTierGraphDestination(
                  InsuranceCustomizationParameters(
                    insuranceId = idWithIntent.first,
                    activationDateEpochDays = idWithIntent.second.activationDate.toEpochDays(),
                    currentTierLevel = idWithIntent.second.currentTierLevel,
                    currentTierName = idWithIntent.second.currentTierName,
                    quoteIds = idWithIntent.second.quotes.map {
                      it.id
                    },
                  ),
                ),
              ) {
                typedPopUpTo<TerminateInsuranceGraphDestination> {
                  inclusive = true
                }
              }
            }
          },
        )
      },
      navigator = navigator,
      openUrl = openUrl,
      onNavigateToNewConversation = { backStackEntry ->
        navigateToNewConversation(backStackEntry)
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
      onNavigateToStartChangeTier = { backStackEntry: NavBackStackEntry, contractId: String ->
        with(navigator) {
          backStackEntry.navigate(
            destination =
              StartTierFlowDestination(
                insuranceId = contractId,
              ),
          )
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
      openAppSettings = externalNavigator::openAppSettings,
      openUrl = openUrl,
      onNavigateToNewConversation = { backStackEntry ->
        navigateToNewConversation(backStackEntry)
      },
    )
    cbmChatGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      hedvigBuildConstants = hedvigBuildConstants,
      imageLoader = imageLoader,
      openUrl = openUrl,
      onNavigateToClaimDetails = { claimId ->
        logcat { "Navigating to claim details from chat" }
        hedvigAppState.navController.navigate(ClaimDetails(claimId))
      },
      navigator = navigator,
    )
    connectPaymentGraph(
      navigator = navigator,
      market = market,
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
    )
    editCoInsuredGraph(navigator)
    helpCenterGraph(
      hedvigDeepLinkContainer = hedvigDeepLinkContainer,
      navigator = navigator,
      onNavigateToQuickLink = { backStackEntry, quickLinkDestination ->
        val destination: Destination = when (quickLinkDestination) {
          QuickLinkChangeAddress -> ChangeAddress
          is QuickLinkCoInsuredAddInfo ->
            CoInsuredAddInfo(quickLinkDestination.contractId)

          is QuickLinkCoInsuredAddOrRemove ->
            CoInsuredAddOrRemove(quickLinkDestination.contractId)

          QuickLinkConnectPayment -> TrustlyDestination
          QuickLinkTermination -> TerminateInsuranceGraphDestination(null)
          QuickLinkTravelCertificate -> TravelCertificate
          QuickLinkChangeTier -> StartTierFlowChooseInsuranceDestination
        }
        with(navigator) {
          backStackEntry.navigate(destination)
        }
      },
      onNavigateToNewConversation = { backStackEntry ->
        navigateToNewConversation(backStackEntry)
      },
      onNavigateToInbox = { backStackEntry ->
        navigateToInbox(backStackEntry)
      },
      openUrl = openUrl,
    )
  }
}

private fun NavGraphBuilder.nestedHomeGraphs(
  density: Density,
  hedvigAppState: HedvigAppState,
  hedvigBuildConstants: HedvigBuildConstants,
  navigator: Navigator,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  externalNavigator: ExternalNavigator,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
  navigateToNewConversation: (NavBackStackEntry, (NavOptionsBuilder.() -> Unit)?) -> Unit,
  navigateToConversation: (NavBackStackEntry, String) -> Unit,
) {
  claimDetailsGraph(
    navigator = navigator,
    imageLoader = imageLoader,
    openUrl = openUrl,
    navigateUp = navigator::navigateUp,
    appPackageId = hedvigBuildConstants.appId,
    navigateToConversation = { backStackEntry, conversationId ->
      navigateToConversation(backStackEntry, conversationId)
    },
    applicationId = hedvigBuildConstants.appId,
  )
  changeAddressGraph(
    navigator = navigator,
    onNavigateToNewConversation = { backStackEntry ->
      navigateToNewConversation(backStackEntry, null)
    },
    openUrl = openUrl,
  )
  travelCertificateGraph(
    density = density,
    navigator = navigator,
    applicationId = hedvigBuildConstants.appId,
  )
  claimFlowGraph(
    windowSizeClass = hedvigAppState.windowSizeClass,
    navigator = navigator,
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    navigateToTriaging = {
      navigator.navigateUnsafe(ClaimTriagingDestination.ClaimGroups)
    },
    openAppSettings = externalNavigator::openAppSettings,
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
    onNavigateToNewConversation = { backStackEntry ->
      navigateToNewConversation(backStackEntry, null)
    },
    imageLoader = imageLoader,
    appPackageId = hedvigBuildConstants.appId,
  )
  terminalClaimFlowStepDestinations(
    navigator = navigator,
    openPlayStore = {
      navigator.popBackStack()
      externalNavigator.tryOpenPlayStore()
    },
    onNavigateToNewConversation = { backStackEntry ->
      navigateToNewConversation(backStackEntry) {
        typedPopUpTo<HomeDestination.Home>()
      }
    },
  )
}
