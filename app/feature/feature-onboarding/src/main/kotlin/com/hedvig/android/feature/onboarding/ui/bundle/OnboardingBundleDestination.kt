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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Dp
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
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.autoScrollingMarquee
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.onboarding.data.OnboardingCrossSell
import com.hedvig.android.feature.onboarding.data.OnboardingSessionStore
import com.hedvig.android.feature.onboarding.navigation.OnboardingNavigator
import com.hedvig.android.feature.onboarding.navigation.OnboardingStepId
import com.hedvig.android.feature.onboarding.ui.OnboardingProgress
import com.hedvig.android.feature.onboarding.ui.OnboardingProgressBarAnimation
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
import hedvig.resources.ONBOARDING_SEE_PRICE_BUTTON
import hedvig.resources.Res
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class OnboardingBundleViewModel(
  sessionStore: OnboardingSessionStore,
  navigator: OnboardingNavigator,
  val progressBarAnimation: OnboardingProgressBarAnimation,
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
  OnboardingBundleScreen(
    uiState = uiState,
    progressAnimation = viewModel.progressBarAnimation,
    imageLoader = imageLoader,
    navigateUp = navigateUp,
    onClose = { viewModel.emit(OnboardingBundleEvent.Close) },
    onRetry = { viewModel.emit(OnboardingBundleEvent.Retry) },
    onContinueToApp = { viewModel.emit(OnboardingBundleEvent.ContinueToApp) },
    openUrl = openUrl,
  )
}

@Composable
private fun OnboardingBundleScreen(
  uiState: OnboardingBundleUiState,
  progressAnimation: OnboardingProgressBarAnimation,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  onClose: () -> Unit,
  onRetry: () -> Unit,
  onContinueToApp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  OnboardingStepScaffold(
    progress = (uiState as? OnboardingBundleUiState.Content)?.progress,
    showBackButton = true,
    onBackClick = navigateUp,
    onCloseClick = onClose,
    progressAnimation = progressAnimation,
  ) {
    when (val state = uiState) {
      OnboardingBundleUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced()
      }

      OnboardingBundleUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
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
        EqualHeightColumn(rowSpacing = 8.dp) {
          for (crossSell in state.crossSells) {
            OnboardingCrossSellRow(
              crossSell = crossSell,
              imageLoader = imageLoader,
              openUrl = openUrl,
            )
          }
        }
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        OnboardingStepButtons(
          primaryText = stringResource(Res.string.ONBOARDING_CONTINUE_TO_APP_BUTTON),
          onPrimaryClick = onContinueToApp,
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
      .padding(horizontal = 16.dp, vertical = 8.dp),
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
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(48.dp),
      )
    } else {
      Spacer(Modifier.size(48.dp))
    }
    Spacer(Modifier.width(16.dp))
    Column(Modifier.weight(1f)) {
      HedvigText(crossSell.title, style = HedvigTheme.typography.bodySmall)
      HedvigText(
        crossSell.description,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.autoScrollingMarquee(),
      )
    }
    Spacer(Modifier.width(16.dp))
    HedvigButton(
      text = stringResource(Res.string.ONBOARDING_SEE_PRICE_BUTTON),
      onClick = { openUrl(crossSell.storeUrl) },
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      buttonSize = ButtonDefaults.ButtonSize.Small,
      modifier = Modifier.clip(CircleShape),
    )
  }
}

/**
 * Lays its children out in a vertical column where every child is given the height of the tallest
 * child, with each child's own content centred within its cell. One measure pass, so the heights are
 * uniform on the first frame. [rowSpacing] is inserted between children.
 */
@Composable
private fun EqualHeightColumn(rowSpacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  Layout(content = content, modifier = modifier) { measurables, constraints ->
    val placeables = measurables.map { it.measure(constraints.copy(minHeight = 0)) }
    val rowHeight = placeables.maxOfOrNull(Placeable::height) ?: 0
    val spacingPx = rowSpacing.roundToPx()
    val totalHeight = rowHeight * placeables.size + spacingPx * (placeables.size - 1).coerceAtLeast(0)
    layout(constraints.maxWidth, totalHeight) {
      var y = 0
      placeables.forEach { placeable ->
        placeable.place(0, y + (rowHeight - placeable.height) / 2)
        y += rowHeight + spacingPx
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOnboardingBundleScreen(
  @PreviewParameter(OnboardingBundleUiStateProvider::class) uiState: OnboardingBundleUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      OnboardingBundleScreen(
        uiState = uiState,
        progressAnimation = remember { OnboardingProgressBarAnimation() },
        imageLoader = rememberPreviewImageLoader(),
        navigateUp = {},
        onClose = {},
        onRetry = {},
        onContinueToApp = {},
        openUrl = {},
      )
    }
  }
}

private class OnboardingBundleUiStateProvider : CollectionPreviewParameterProvider<OnboardingBundleUiState>(
  listOf(
    OnboardingBundleUiState.Loading,
    OnboardingBundleUiState.Error,
    OnboardingBundleUiState.Content(
      progress = OnboardingProgress(totalSteps = 5, currentIndex = 1),
      crossSells = listOf(
        OnboardingCrossSell(
          id = "home",
          title = "Home Insurance",
          description = "For you, your family and your home",
          storeUrl = "https://www.hedvig.com/se/forsakringar/hemforsakring",
          pillowImageUrl = null,
        ),
        OnboardingCrossSell(
          id = "pet",
          title = "Pet Insurance",
          description = "For your dog or cat",
          storeUrl = "https://www.hedvig.com/se/forsakringar/djurforsakring",
          pillowImageUrl = null,
        ),
        OnboardingCrossSell(
          id = "car",
          title = "Car insurance",
          description = "For you and your car",
          storeUrl = "https://www.hedvig.com/se/forsakringar/bilforsakring",
          pillowImageUrl = null,
        ),
        OnboardingCrossSell(
          id = "vacation",
          title = "Vacation Home Insurance",
          description = "For your cottage or cabin",
          storeUrl = "https://www.hedvig.com/se/forsakringar/fritidshusforsakring",
          pillowImageUrl = null,
        ),
        OnboardingCrossSell(
          id = "ppi",
          title = "Payment Protection Insurance",
          description = "For you if you get unemployed",
          storeUrl = "https://www.hedvig.com/se/forsakringar/inkomstforsakring",
          pillowImageUrl = null,
        ),
      ),
    ),
  ),
)
