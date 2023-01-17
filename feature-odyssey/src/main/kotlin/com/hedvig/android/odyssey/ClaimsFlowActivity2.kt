package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
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
import com.hedvig.android.odyssey.model.Claim
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.ui.AudioRecorder
import com.hedvig.android.odyssey.ui.AudioRecorderScreen
import com.hedvig.android.odyssey.ui.DateOfOccurence
import com.hedvig.android.odyssey.ui.HonestyPledge
import com.hedvig.android.odyssey.ui.Location
import com.hedvig.android.odyssey.ui.PhoneNumber
import com.hedvig.android.odyssey.ui.SingleItem
import com.hedvig.app.ui.compose.composables.ErrorDialog
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import okhttp3.internal.notify
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ClaimsFlowActivity2 : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val itemType = intent.getParcelableExtra<ItemType>(EXTRA_ITEM_TYPE)?.name ?: "PHONE"

    setContent {
      val viewModel: ClaimsFlowViewModel = getViewModel { parametersOf(itemType, "BROKEN") }
      val viewState by viewModel.viewState.collectAsState()

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
                  finish()
                },
                title = viewState.title,
              )
            }
          },
        ) { paddingValues ->

          FullScreenProgressOverlay(show = viewState.isLoading)

          if (viewState.errorMessage != null) {
            ErrorDialog(
              message = viewState.errorMessage,
              onDismiss = { viewModel.onDismissError() },
            )
          }

          if (viewState.resolution != Resolution.None) {
            when (viewState.resolution) {
              Resolution.ManualHandling -> TODO()
              Resolution.None -> TODO()
              is Resolution.SingleItemPayout -> TODO()
            }
          } else if (viewState.currentInput != null) {
            when (viewState.currentInput) {
              is Input.AudioRecording -> AudioRecorderScreen(viewModel)
              is Input.DateOfOccurrence -> DateOfOccurence(viewModel)
              is Input.Location -> Location(viewModel)
              is Input.PhoneNumber -> PhoneNumber(viewModel)
              is Input.SingleItem -> SingleItem(viewModel)
              Input.Unknown -> {}
              else -> {}
            }
          } else {
            HonestyPledge(viewModel)
          }

          if (viewState.shouldExit) {
            finish()
          }
        }
      }
    }
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
