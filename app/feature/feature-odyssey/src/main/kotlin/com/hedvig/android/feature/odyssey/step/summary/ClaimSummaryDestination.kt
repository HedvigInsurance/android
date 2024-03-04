package com.hedvig.android.feature.odyssey.step.summary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.PlayableAudioSource
import com.hedvig.android.audio.player.state.rememberAudioPlayer
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.FilesLazyVerticalGrid
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.ItemModel
import com.hedvig.android.data.claimflow.ItemProblem
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.data.claimflow.SubmittedContent
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

@Composable
internal fun ClaimSummaryDestination(
  viewModel: ClaimSummaryViewModel,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.claimSummaryStatusUiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  ClaimSummaryScreen(
    uiState = uiState,
    showedError = viewModel::showedError,
    submitSummary = viewModel::submitSummary,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    imageLoader = imageLoader,
    windowSizeClass = windowSizeClass,
  )
}

@Composable
private fun ClaimSummaryScreen(
  uiState: ClaimSummaryUiState,
  showedError: () -> Unit,
  submitSummary: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
  imageLoader: ImageLoader,
  windowSizeClass: WindowSizeClass,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
    topAppBarText = stringResource(R.string.claims_summary_screen_title),
    errorSnackbarState = ErrorSnackbarState(
      uiState.claimSummaryStatusUiState.hasError,
      showedError,
    ),
  ) { sideSpacingModifier ->
    var layoutHeight by remember { mutableIntStateOf(-1) }
    Layout(
      content = {
        BeforeGridContent(uiState = uiState)
        FilesLazyVerticalGrid(
          paddingValues = PaddingValues(horizontal = 8.dp),
          files = uiState.claimSummaryInfoUiState.files,
          imageLoader = imageLoader,
          onRemoveFile = null,
        )
        AfterGridContent(uiState = uiState, submitSummary = submitSummary)
      },
      modifier = sideSpacingModifier
        .fillMaxWidth()
        .weight(1f)
        .onSizeChanged { intSize -> layoutHeight = intSize.height }
        .verticalScroll(rememberScrollState()),
    ) { measurables, constraints ->
      val beforeGridPlaceable = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
      val afterGridPlaceable = measurables[2].measure(constraints.copy(minWidth = 0, minHeight = 0))
      val remainingHeightForGrid = layoutHeight - beforeGridPlaceable.height - afterGridPlaceable.height
      val actualGridHeight = remainingHeightForGrid.coerceAtLeast(160.dp.roundToPx())
      val gridPlaceable = measurables[1].measure(
        constraints.copy(minWidth = 0, minHeight = actualGridHeight, maxHeight = actualGridHeight),
      )
      layout(
        constraints.maxWidth,
        beforeGridPlaceable.height + gridPlaceable.height + afterGridPlaceable.height,
      ) {
        beforeGridPlaceable.place(0, 0)
        gridPlaceable.place(0, beforeGridPlaceable.height)
        afterGridPlaceable.place(0, beforeGridPlaceable.height + gridPlaceable.height)
      }
    }
  }
}

@Composable
private fun BeforeGridContent(uiState: ClaimSummaryUiState, modifier: Modifier = Modifier) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.moving_summary_scroll_Details))
    Spacer(Modifier.height(8.dp))
    val detailPairs = uiState.claimSummaryInfoUiState.itemDetailPairs(resources, getLocale())
    CompositionLocalProvider(
      LocalTextStyle provides MaterialTheme.typography.bodyLarge.copy(
        MaterialTheme.colorScheme.onSurfaceVariant,
      ),
    ) {
      Column(Modifier.fillMaxWidth()) {
        for ((left, right) in detailPairs) {
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              Text(text = left)
            },
            endSlot = {
              Text(text = right, textAlign = TextAlign.End)
            },
          )
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    Text(stringResource(R.string.claim_status_detail_uploaded_files_info_title))
    when (uiState.claimSummaryInfoUiState.submittedContent) {
      is SubmittedContent.Audio -> {
        val signedAudioUrl = SignedAudioUrl.fromSignedAudioUrlString(
          uiState.claimSummaryInfoUiState.submittedContent.audioURL,
        )
        Spacer(Modifier.height(8.dp))
        val audioPlayer = rememberAudioPlayer(
          playableAudioSource = PlayableAudioSource.RemoteUrl(signedAudioUrl),
        )
        HedvigAudioPlayer(audioPlayer = audioPlayer)
      }

      else -> {}
    }
    Spacer(Modifier.height(8.dp))
  }
}

@Composable
private fun AfterGridContent(uiState: ClaimSummaryUiState, submitSummary: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    VectorInfoCard(stringResource(R.string.CLAIMS_COMPLEMENT__CLAIM), Modifier.fillMaxWidth())
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.EMBARK_SUBMIT_CLAIM),
      onClick = submitSummary,
      isLoading = uiState.claimSummaryStatusUiState.isLoading,
      enabled = uiState.canSubmit,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Preview(device = "spec:width=1080px,height=1000px,dpi=440")
@HedvigMultiScreenPreview
@Composable
private fun PreviewClaimSummaryScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimSummaryScreen(
        ClaimSummaryUiState(
          claimSummaryInfoUiState = ClaimSummaryInfoUiState(
            claimTypeTitle = "Broken Phone",
            dateOfIncident = LocalDate.parse("2023-03-24"),
            locationOption = LocationOption(
              value = "IN_HOME_COUNTRY",
              displayName = "Sweden",
            ),
            itemType = ClaimSummaryInfoUiState.ItemType.Model(
              itemModel = ItemModel.Known(
                displayName = "Apple iPhone 14 Pro",
                itemTypeId = "PHONE",
                itemBrandId = "APPLE_IPHONE",
                itemModelId = "",
              ),
            ),
            dateOfPurchase = LocalDate.parse("2015-03-26"),
            priceOfPurchase = UiNullableMoney(
              amount = 3990.0,
              currencyCode = CurrencyCode.SEK,
            ),
            itemProblems = listOf(
              ItemProblem(displayName = "Other", itemProblemId = ""),
              ItemProblem(displayName = "Water", itemProblemId = ""),
            ),
            files = List(5) {
              UiFile(
                id = "$it",
                name = "$it",
                mimeType = "",
                path = "$it",
              )
            },
            submittedContent = null,
          ),
          claimSummaryStatusUiState = ClaimSummaryStatusUiState(
            isLoading = false,
            hasError = false,
            nextStep = null,
          ),
        ),
        {},
        {},
        {},
        {},
        imageLoader = rememberPreviewImageLoader(),
        windowSizeClass = WindowSizeClass.calculateForPreview(),
      )
    }
  }
}
