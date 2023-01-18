package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FullScreenProgressOverlay
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.appbar.TopAppBarWithClose
import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.ui.AudioRecorderScreen
import com.hedvig.android.odyssey.ui.DateOfOccurrenceAndLocation
import com.hedvig.android.odyssey.ui.HonestyPledge
import com.hedvig.android.odyssey.ui.Location
import com.hedvig.android.odyssey.ui.PayoutSummary
import com.hedvig.android.odyssey.ui.PhoneNumber
import com.hedvig.android.odyssey.ui.SingleItem
import com.hedvig.android.odyssey.ui.Success
import com.hedvig.app.ui.compose.composables.ErrorDialog
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ClaimsFlowActivity2 : ComponentActivity() {

  @OptIn(ExperimentalAnimationApi::class)
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
                  viewModel.onBack()
                },
                title = viewState.title,
              )
            }
          },
        ) { paddingValues ->

          BackHandler {
            viewModel.onBack()
          }

          FullScreenProgressOverlay(show = viewState.isLoading)

          if (viewState.resolution != Resolution.None) {
            Resolution(viewState, viewModel)
          } else {
            Input(viewState.currentInput, viewState.claimState, viewModel)
          }

          if (viewState.errorMessage != null) {
            ErrorDialog(
              message = viewState.errorMessage,
              onDismiss = { viewModel.onDismissError() },
            )
          }

          if (viewState.shouldExit) {
            finish()
          }
        }
      }
    }
  }

  @Composable
  private fun Input(
    input: Input?,
    claimState: ClaimState,
    viewModel: ClaimsFlowViewModel,
  ) {
    when (input) {
      is Input.AudioRecording -> AudioRecorderScreen(
        questions = input.questions,
        onAudioFile = viewModel::onAudioFile,
        onNext = viewModel::onNext,
      )
      // TODO How to combine multiple inputs
      is Input.DateOfOccurrence -> DateOfOccurrenceAndLocation(
        state = claimState,
        onDateOfOccurrence = viewModel::onDateOfOccurrence,
        onLocation = viewModel::onLocation,
        onNext = viewModel::onNext,
      )
      is Input.Location -> Location(viewModel)
      is Input.PhoneNumber -> PhoneNumber(viewModel)
      is Input.SingleItem -> SingleItem(
        state = claimState,
        input = input,
        onDateOfPurchase = viewModel::onDateOfPurchase,
        onTypeOfDamage = viewModel::onTypeOfDamage,
        onPurchasePrice = viewModel::onPurchasePrice,
        onNext = viewModel::onNext,
      )
      Input.Unknown -> {}
      else -> {}
    }
  }

  @Composable
  private fun Resolution(
    viewState: ViewState,
    viewModel: ClaimsFlowViewModel,
  ) {
    when (val resolution = viewState.resolution) {
      Resolution.ManualHandling -> Success(
        onExit = viewModel::onExit,
        onNext = viewModel::onNext,
      )
      Resolution.None -> TODO()
      is Resolution.SingleItemPayout -> PayoutSummary(
        resolution = resolution,
        onPayout = viewModel::openClaimAndPayout,
      )
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
