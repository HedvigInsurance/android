package com.hedvig.android.feature.help.center.ui

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.help.center.data.QuickLinkDestination
import com.hedvig.android.feature.help.center.helpCenterGraph
import com.hedvig.android.feature.help.center.navigation.HelpCenterDestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import platform.UIKit.UIViewController

@Suppress("unused", "FunctionName") // Used from iOS
fun HelpCenterViewController(
  onNavigateUp: () -> Unit,
  onNavigateToInbox: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  onNavigateToQuickLink: (QuickLinkDestination.OuterDestination) -> Unit,
  openUrl: (String) -> Unit,
  tryToDialPhone: (String) -> Unit,
): UIViewController {
  return ComposeUIViewController {
    HedvigTheme {
      val navController = rememberNavController()
      NavHost(
        navController = navController,
        startDestination = HelpCenterDestination,
      ) {
        helpCenterGraph(
          hedvigDeepLinkContainer = NoOpHedvigDeepLinkContainer,
          navController = navController,
          onNavigateUp = onNavigateUp,
          onNavigateToQuickLink = onNavigateToQuickLink,
          onNavigateToInbox = onNavigateToInbox,
          onNavigateToNewConversation = onNavigateToNewConversation,
          openUrl = openUrl,
          tryToDialPhone = tryToDialPhone,
        )
      }
    }
  }
}

private object NoOpHedvigDeepLinkContainer : HedvigDeepLinkContainer {
  override val home: List<String> = emptyList()
  override val helpCenter: List<String> = emptyList()
  override val helpCenterCommonTopic: List<String> = emptyList()
  override val helpCenterQuestion: List<String> = emptyList()
  override val insurances: List<String> = emptyList()
  override val claimFlow: List<String> = emptyList()
  override val contractWithoutContractId: List<String> = emptyList()
  override val contract: List<String> = emptyList()
  override val editCoInsuredWithoutContractId: List<String> = emptyList()
  override val editCoInsured: List<String> = emptyList()
  override val terminateInsurance: List<String> = emptyList()
  override val forever: List<String> = emptyList()
  override val profile: List<String> = emptyList()
  override val connectPayment: List<String> = emptyList()
  override val directDebit: List<String> = emptyList()
  override val eurobonus: List<String> = emptyList()
  override val payments: List<String> = emptyList()
  override val deleteAccount: List<String> = emptyList()
  override val contactInfo: List<String> = emptyList()
  override val chat: List<String> = emptyList()
  override val inbox: List<String> = emptyList()
  override val conversation: List<String> = emptyList()
  override val travelAddon: List<String> = emptyList()
  override val travelAddonWithContractId: List<String> = emptyList()
  override val carAddon: List<String> = emptyList()
  override val carAddonWithContractId: List<String> = emptyList()
  override val travelCertificate: List<String> = emptyList()
  override val changeTierWithoutContractId: List<String> = emptyList()
  override val changeTierWithContractId: List<String> = emptyList()
  override val claimDetails: List<String> = emptyList()
  override val insuranceEvidence: List<String> = emptyList()
  override val moveContract: List<String> = emptyList()
}
