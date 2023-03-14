package com.hedvig.android.odyssey.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.android.navigation.compose.typed.animatedNavigation
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingDestination
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingViewModel
import com.hedvig.android.odyssey.step.claimsummary.ClaimSummaryDestination
import com.hedvig.android.odyssey.step.dateofoccurrence.DateOfOccurrenceDestination
import com.hedvig.android.odyssey.step.dateofoccurrencepluslocation.DateOfOccurrencePlusLocationDestination
import com.hedvig.android.odyssey.step.location.LocationDestination
import com.hedvig.android.odyssey.step.manualhandling.ManualHandlingDestination
import com.hedvig.android.odyssey.step.phonenumber.PhoneNumberDestination
import com.hedvig.android.odyssey.step.singleitem.SingleItemDestination
import com.hedvig.android.odyssey.step.singleitempayout.SingleItemPayoutDestination
import com.kiwi.navigationcompose.typed.createRoutePattern
import org.koin.androidx.compose.koinViewModel

internal fun NavGraphBuilder.claimFlowGraph(
  windowSizeClass: WindowSizeClass,
  density: Density,
  navController: NavHostController,
  imageLoader: ImageLoader,
  navigateUp: () -> Boolean,
  openChat: () -> Unit,
  finishClaimFlow: () -> Unit,
) {
  animatedNavigation<Destinations.ClaimFlow>(
    startDestination = createRoutePattern<ClaimFlowDestination.StartStep>(),
    enterTransition = { MotionDefaults.sharedXAxisEnter(density) },
    exitTransition = { MotionDefaults.sharedXAxisExit(density) },
    popEnterTransition = { MotionDefaults.sharedXAxisPopEnter(density) },
    popExitTransition = { MotionDefaults.sharedXAxisPopExit(density) },
  ) {
    animatedComposable<ClaimFlowDestination.StartStep> {
      // todo
    }
    animatedComposable<ClaimFlowDestination.AudioRecording> {
      val viewModel: AudioRecordingViewModel = koinViewModel()
      AudioRecordingDestination(viewModel, emptyList())
    }
    animatedComposable<ClaimFlowDestination.ClaimSummary> {
//      val viewModel: ClaimSummaryViewModel = koinViewModel()
      ClaimSummaryDestination(imageLoader = imageLoader)
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrence> {
//      val viewModel: DateOfOccurrenceViewModel = koinViewModel()
      DateOfOccurrenceDestination()
    }
    animatedComposable<ClaimFlowDestination.DateOfOccurrencePlusLocation> {
//      val viewModel: DateOfOccurrencePlusLocationViewModel = koinViewModel()
      DateOfOccurrencePlusLocationDestination(
        imageLoader = imageLoader,
      )
    }
    animatedComposable<ClaimFlowDestination.Location> {
//      val viewModel: LocationViewModel = koinViewModel()
      LocationDestination()
    }
    animatedComposable<ClaimFlowDestination.PhoneNumber> {
//      val viewModel: PhoneNumberViewModel = koinViewModel()
      PhoneNumberDestination()
    }
    animatedComposable<ClaimFlowDestination.SingleItem> {
//      val viewModel: SingleItemViewModel = koinViewModel()
      SingleItemDestination(
        imageLoader = imageLoader,
      )
    }
    // Result destinations, not recoverable
    animatedComposable<ClaimFlowDestination.SingleItemPayout> {
      SingleItemPayoutDestination()
    }
    animatedComposable<ClaimFlowDestination.ManualHandling> {
      BackHandler { finishClaimFlow() }
//      val viewModel: ManualHandlingViewModel = koinViewModel()
      ManualHandlingDestination(
        navigateUp = finishClaimFlow,
      )
    }
    animatedComposable<ClaimFlowDestination.UnknownScreen> {
      BackHandler { finishClaimFlow() }
      Box(contentAlignment = Alignment.Center) {
        Text("UnknownScreenDestination")
      }
//      UnknownScreenDestination( // todo this destination
//        windowSizeClass = windowSizeClass,
//        openChat = openChat,
//        navigateBack = finishClaimFlow,
//      )
    }
  }
}
