package com.hedvig.android.feature.odyssey.step.summary

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Dimension
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiNullableMoney
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.feature.odyssey.data.ClaimFlowStep
import com.hedvig.android.feature.odyssey.navigation.ItemModel
import com.hedvig.android.feature.odyssey.navigation.ItemProblem
import com.hedvig.android.feature.odyssey.navigation.LocationOption
import com.hedvig.android.feature.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode
import java.time.format.DateTimeFormatter

@Composable
internal fun ClaimSummaryDestination(
  viewModel: ClaimSummaryViewModel,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateBack: () -> Unit,
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
    windowSizeClass = windowSizeClass,
    imageLoader = imageLoader,
    showedError = viewModel::showedError,
    submitSummary = viewModel::submitSummary,
    navigateBack = navigateBack,
  )
}

@Composable
private fun ClaimSummaryScreen(
  uiState: ClaimSummaryUiState,
  windowSizeClass: WindowSizeClass,
  imageLoader: ImageLoader,
  showedError: () -> Unit,
  submitSummary: () -> Unit,
  navigateBack: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateBack,
    topAppBarText = stringResource(R.string.claims_item_screen_title),
    isLoading = uiState.claimSummaryStatusUiState.isLoading,
    errorSnackbarState = ErrorSnackbarState(
      uiState.claimSummaryStatusUiState.hasError,
      showedError,
    ),
    itemsColumnHorizontalAlignment = Alignment.CenterHorizontally,
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(100.dp))
    ItemIcon(uiState.claimSummaryInfoUiState, imageLoader, sideSpacingModifier)
    Spacer(Modifier.height(20.dp))
    uiState.claimSummaryInfoUiState.claimTypeTitle?.let { claimTypeTitle ->
      Text(
        text = claimTypeTitle,
        style = MaterialTheme.typography.titleLarge,
        modifier = sideSpacingModifier,
      )
      Spacer(Modifier.height(4.dp))
    }
    CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
      uiState.claimSummaryInfoUiState.dateOfIncident?.let { dateOfIncident ->
        Row(modifier = sideSpacingModifier, verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CalendarToday, null)
          Spacer(Modifier.width(8.dp))
          Text(dateOfIncident.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy", getLocale())))
        }
        Spacer(Modifier.height(4.dp))
      }
      uiState.claimSummaryInfoUiState.locationOption?.let { locationOption ->
        Row(modifier = sideSpacingModifier, verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocationOn, null)
          Spacer(Modifier.width(8.dp))
          Text(locationOption.displayName)
        }
        Spacer(Modifier.height(4.dp))
      }
    }
    Spacer(Modifier.height(36.dp))
    ItemDetailsText(uiState.claimSummaryInfoUiState, sideSpacingModifier)
    Spacer(Modifier.height(20.dp))
    Spacer(Modifier.weight(1f))
    LargeContainedTextButton(
      onClick = submitSummary,
      enabled = uiState.canSubmit,
      text = stringResource(R.string.CONFIRM_AND_PROCEED_BUTTON),
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(20.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun ItemIcon(
  uiState: ClaimSummaryInfoUiState,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val density = LocalDensity.current
  val sizeInPx = with(density) { Dimension(145.dp.roundToPx()) }
  SubcomposeAsyncImage(
    model = ImageRequest.Builder(context)
      .data(uiState.imageUrl)
      .size(sizeInPx, sizeInPx)
      .build(),
    contentDescription = null,
    imageLoader = imageLoader,
  ) {
    Crossfade(
      targetState = painter.state,
      label = "imageCrossfade",
    ) {
      Box(
        modifier = modifier.size(145.dp),
        contentAlignment = Alignment.Center,
        propagateMinConstraints = true,
      ) {
        when (it) {
          is AsyncImagePainter.State.Success -> this@SubcomposeAsyncImage.SubcomposeAsyncImageContent()
          is AsyncImagePainter.State.Error -> {}
          AsyncImagePainter.State.Empty,
          is AsyncImagePainter.State.Loading,
          -> {
            Box(
              Modifier.placeholder(
                visible = true,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.medium,
                highlight = PlaceholderHighlight.fade(Color.Gray),
              ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ItemDetailsText(
  uiState: ClaimSummaryInfoUiState,
  modifier: Modifier = Modifier,
) {
  val itemDetailsText = formatItemDetailsText(
    itemType = uiState.itemType,
    dateOfPurchase = uiState.dateOfPurchase,
    priceOfPurchase = uiState.priceOfPurchase,
    itemProblems = uiState.itemProblems,
  )
  if (itemDetailsText != null) {
    Text(
      text = itemDetailsText,
      textAlign = TextAlign.Center,
      modifier = modifier,
    )
  }
}

@Composable
private fun formatItemDetailsText(
  itemType: ClaimSummaryInfoUiState.ItemType?,
  dateOfPurchase: LocalDate?,
  priceOfPurchase: UiNullableMoney?,
  itemProblems: List<ItemProblem>,
): String? {
  val paidText = run {
    if (priceOfPurchase?.amount == null) return@run null
    buildString {
      append(
        stringResource(
          R.string.SUMMARY_PURCHASE_PRICE_DESCRIPTION,
          priceOfPurchase.amount!!.toInt(),
        ),
      )
      append(" ")
      append(priceOfPurchase.currencyCode)
    }
  }
  val purchasedAtText = run {
    if (dateOfPurchase == null) return@run null
    buildString {
      append(
        stringResource(
          R.string.SUMMARY_PURCHASE_DATE_DESCRIPTION,
          dateOfPurchase.toJavaLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy", getLocale())),
        ),
      )
    }
  }
  val itemProblemsText = run {
    if (itemProblems.isEmpty()) return@run null
    stringResource(
      R.string.SUMMARY_SELECTED_PROBLEM_DESCRIPTION,
      itemProblems.joinToString { it.displayName },
    )
  }
  if (itemType == null && paidText == null && itemProblemsText == null) return null
  return buildString {
    if (itemType != null) {
      LocalConfiguration.current
      val resources = LocalContext.current.resources
      append(itemType.displayName(resources))
    }
    if (paidText != null) {
      appendLine()
      append(paidText)
    }
    if (purchasedAtText != null) {
      appendLine()
      append(purchasedAtText)
    }
    if (itemProblemsText != null) {
      appendLine()
      append(itemProblemsText)
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimSummaryScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimSummaryScreen(
        ClaimSummaryUiState(
          claimSummaryInfoUiState = ClaimSummaryInfoUiState(
            imageUrl = "https://fdn2.gsmarena.com/vv/bigpic/apple-iphone-14-pro.jpg",
            claimTypeTitle = "Broken Phone",
            dateOfIncident = LocalDate.parse("2023-03-24"),
            locationOption = LocationOption(
              value = "IN_HOME_COUNTRY",
              displayName = "In Sweden",
            ),
            itemType = ClaimSummaryInfoUiState.ItemType.Model(
              itemModel = ItemModel.Known(
                displayName = "Apple iPhone 14 Pro",
                imageUrl = "https://fdn2.gsmarena.com/vv/bigpic/apple-iphone-14-pro.jpg",
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
          ),
          claimSummaryStatusUiState = ClaimSummaryStatusUiState(
            isLoading = false,
            hasError = false,
            nextStep = null,
          ),
        ),
        WindowSizeClass.calculateForPreview(),
        rememberPreviewImageLoader(),
        {},
        {},
        {},
      )
    }
  }
}
