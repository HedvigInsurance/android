package com.hedvig.android.feature.onboarding.ui.bundle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.feature.onboarding.data.OnboardingCrossSell
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingStepButtons
import com.hedvig.android.feature.onboarding.ui.OnboardingStepHeader
import com.hedvig.android.feature.onboarding.ui.OnboardingStepScaffold
import com.hedvig.android.feature.onboarding.ui.progressFor
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import hedvig.resources.ONBOARDING_CONTINUE_TO_APP_BUTTON
import hedvig.resources.ONBOARDING_CROSS_SELL_SUBTITLE
import hedvig.resources.ONBOARDING_CROSS_SELL_TITLE
import hedvig.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingBundleViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
) : MoleculeViewModel<OnboardingBundleEvent, OnboardingBundleUiState>(
    initialState = OnboardingBundleUiState.Loading,
    presenter = OnboardingBundlePresenter(sessionStore, navigator),
  )

internal class OnboardingBundlePresenter(
  private val sessionStore: OnboardingSessionStore,
  private val navigator: OnboardingNavigator,
) : MoleculePresenter<OnboardingBundleEvent, OnboardingBundleUiState> {
  @Composable
  override fun MoleculePresenterScope<OnboardingBundleEvent>.present(
    lastState: OnboardingBundleUiState,
  ): OnboardingBundleUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (currentState is OnboardingBundleUiState.Content) return@LaunchedEffect
      currentState = OnboardingBundleUiState.Loading
      sessionStore.getOrFetchSession().fold(
        ifLeft = { currentState = OnboardingBundleUiState.Error },
        ifRight = { session ->
          currentState = OnboardingBundleUiState.Content(
            progress = session.progressFor(OnboardingStepId.BundleDiscount),
            crossSells = session.data.crossSells,
          )
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        OnboardingBundleEvent.Retry -> loadIteration++
        OnboardingBundleEvent.Close -> launch { navigator.exitOnboarding() }
        OnboardingBundleEvent.ContinueToApp -> launch { navigator.continueFrom(OnboardingStepId.BundleDiscount) }
      }
    }

    return currentState
  }
}

internal sealed interface OnboardingBundleUiState {
  data object Loading : OnboardingBundleUiState

  data object Error : OnboardingBundleUiState

  data class Content(
    val progress: OnboardingProgress,
    val crossSells: List<OnboardingCrossSell>,
  ) : OnboardingBundleUiState
}

internal sealed interface OnboardingBundleEvent {
  data object Retry : OnboardingBundleEvent

  data object Close : OnboardingBundleEvent

  data object ContinueToApp : OnboardingBundleEvent
}

@Composable
internal fun OnboardingBundleDestination(
  viewModel: OnboardingBundleViewModel,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingBundleUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = { viewModel.emit(OnboardingBundleEvent.Close) },
  ) {
    when (val state = uiState) {
      OnboardingBundleUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingBundleUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(OnboardingBundleEvent.Retry) },
        )
      }

      is OnboardingBundleUiState.Content -> {
        Spacer(Modifier.height(16.dp))
        OnboardingStepHeader(
          title = stringResource(Res.string.ONBOARDING_CROSS_SELL_TITLE),
          description = stringResource(Res.string.ONBOARDING_CROSS_SELL_SUBTITLE),
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        for ((index, crossSell) in state.crossSells.withIndex()) {
          if (index > 0) {
            Spacer(Modifier.height(8.dp))
          }
          OnboardingCrossSellRow(
            crossSell = crossSell,
            imageLoader = imageLoader,
            openUrl = openUrl,
          )
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.ONBOARDING_CONTINUE_TO_APP_BUTTON),
          onPrimaryClick = { viewModel.emit(OnboardingBundleEvent.ContinueToApp) },
        )
      }
    }
  }
}

@Composable
private fun OnboardingCrossSellRow(
  crossSell: OnboardingCrossSell,
  imageLoader: ImageLoader,
  openUrl: (String) -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    if (crossSell.pillowImageUrl != null) {
      val placeholder = crossSellPainterFallback()
      AsyncImage(
        model = crossSell.pillowImageUrl,
        contentDescription = null,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        modifier = Modifier.size(40.dp),
      )
    } else {
      Spacer(Modifier.size(40.dp))
    }
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
      HedvigText(crossSell.title, style = HedvigTheme.typography.bodySmall)
      HedvigText(
        crossSell.description,
        style = HedvigTheme.typography.bodySmall,
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
    HedvigButton(
      // TODO: Add "See price" / "Se pris" to Lokalise
      text = "See price",
      onClick = { openUrl(crossSell.storeUrl) },
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Small,
      modifier = Modifier.clip(CircleShape),
    )
  }
}
