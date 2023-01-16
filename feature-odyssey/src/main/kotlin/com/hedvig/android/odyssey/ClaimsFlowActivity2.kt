package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.appbar.TopAppBarWithClose
import com.hedvig.android.odyssey.ui.PayoutSummary
import com.hedvig.android.odyssey.ui.Success
import com.hedvig.app.ui.compose.composables.ErrorDialog
import com.hedvig.common.designsystem.BlurredFullScreenProgressOverlay
import com.hedvig.common.designsystem.TextProgressOverlay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ClaimsFlowActivity2 : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val itemType = intent.getParcelableExtra<ItemType>(EXTRA_ITEM_TYPE)?.name ?: "PHONE"

    setContent {
      val navController = rememberNavHostController(LocalContext.current)

      val viewModel: ClaimsFlowViewModel = getViewModel { parametersOf(itemType, "BROKEN") }
      val viewState by viewModel.viewState.collectAsState()
      val automationClaimScreens = createAutomationClaimScreens(viewModel)

      HedvigTheme {
        Scaffold(
          topBar = {
            if (viewState.isLastScreen) {
              TopAppBarWithClose(
                onClick = { finish() },
                title = viewState.title,
              )
            } else {
              TopAppBarWithBack(
                onClick = {
                  if (!navController.popBackStack()) {
                    finish()
                  }
                },
                title = viewState.title,
              )
            }
          },
        ) { paddingValues ->
          NavHostWithScreens(
            navController = navController,
            automationClaimScreens = automationClaimScreens,
            modifier = Modifier
              .padding(paddingValues)
              .fillMaxSize(),
          )
        }

        FullScreenProgressOverlay(show = viewState.isLoading)

        if (viewState.errorMessage != null) {
          ErrorDialog(
            message = viewState.errorMessage,
            onDismiss = { viewModel.onDismissError() },
          )
        }

        if (viewState.shouldExit) {
          finish()
        }

        LaunchedEffect(viewState.id) {
          lifecycleScope.launch {
            val routerHistory = navController.routerHistory()
            val claim = viewState.claim ?: return@launch
            val route = getNextRoute(claim, routerHistory, automationClaimScreens)
            route?.let { navController.navigate(it) } ?: finish()
          }
        }
      }
    }
  }

  private fun getNextRoute(
    claim: Claim,
    routerHistory: List<String?>,
    screens: List<AutomationClaimScreen>,
  ): String? {
    val remainingPaths = screens
      .filter { it.isApplicable(claim.inputs, claim.resolutions) }
      .filter { !routerHistory.contains(it.path) }
      .map { it.path }

    return remainingPaths.firstOrNull { remaining ->
      val remainingParts = remaining.split("_PLUS_").toSet()
      routerHistory.none { history ->
        val historyParts = history?.split("_PLUS_")?.toSet() ?: emptySet()
        remainingParts.intersect(historyParts).isNotEmpty()
      }
    }
  }

  private fun NavHostController.routerHistory(): List<String?> = backQueue.map { it.destination.route }

  @Composable
  private fun rememberNavHostController(context: Context) = remember {
    NavHostController(context).apply {
      navigatorProvider.addNavigator(ComposeNavigator())
      navigatorProvider.addNavigator(DialogNavigator())
    }
  }

  @Composable
  private fun NavHostWithScreens(
    navController: NavHostController,
    automationClaimScreens: List<AutomationClaimScreen>,
    modifier: Modifier,
  ) {
    val graph = navController
      .createGraph("honesty-pledge") {
        automationClaimScreens.map { screen ->
          composable(screen.path, content = screen.content)
        }
      }

    NavHost(navController, graph, modifier)
  }

  companion object {
    private const val EXTRA_ITEM_TYPE = "EXTRA_ITEM_TYPE"

    fun newInstance(
      context: Context,
      itemType: ItemType? = null,
    ): Intent {
      return Intent(context, ClaimsFlowActivity2::class.java)
        .putExtra(EXTRA_ITEM_TYPE, itemType)
    }
  }

  @Parcelize
  @JvmInline
  value class ItemType(val name: String) : Parcelable
}
