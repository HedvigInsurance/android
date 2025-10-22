package com.hedvig.android.crosssells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.BottomSheetStyle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigStepProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.StepProgressItem
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.R

data class CrossSellSheetData(
  val recommendedCrossSell: RecommendedCrossSell?,
  val otherCrossSells: List<CrossSell>,
)

data class RecommendedCrossSell(
  val crossSell: CrossSell,
  val bannerText: String,
  val buttonText: String,
  val discountText: String?,
  val buttonDescription: String,
  val backgroundPillowImages: Pair<String, String>?,
  val bundleProgress: BundleProgress?,
)

data class BundleProgress(
  val numberOfEligibleContracts: Int,
  val discountPercent: Int,
)

/**
 * Floating bottom sheet option
 * todo: Look into using this again when we can control the scrim composable and can add a gradient brush there instead
 */
@Composable
fun CrossSellFloatingBottomSheet(
  state: HedvigBottomSheetState<CrossSellSheetData>,
  onCrossSellClick: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  HedvigBottomSheet(
    hedvigBottomSheetState = state,
    dragHandle = {
      CrossSellDragHandle(
        text = state.data?.recommendedCrossSell?.bannerText,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .clip(HedvigTheme.shapes.cornerXLargeTop),
      )
    },
    style = BottomSheetStyle(
      transparentBackground = true,
      automaticallyScrollableContent = false,
      scrimColor = HedvigTheme.colorScheme.scrim.copy(alpha = 0.72f),
    ),
    contentPadding = PaddingValues(horizontal = 0.dp),
    sheetPadding = PaddingValues(horizontal = 0.dp),
    content = { crossSellSheetData ->
      CrossSellsFloatingSheetContent(
        recommendedCrossSell = crossSellSheetData.recommendedCrossSell,
        otherCrossSells = crossSellSheetData.otherCrossSells,
        onCrossSellClick = onCrossSellClick,
        dismissSheet = { state.dismiss() },
        imageLoader,
      )
    },
  )
}

@Composable
fun CrossSellBottomSheet(
  state: HedvigBottomSheetState<CrossSellSheetData>,
  onCrossSellClick: (String) -> Unit,
  imageLoader: ImageLoader,
) {
  val dragHandle: @Composable (() -> Unit)? =
    if (state.data?.recommendedCrossSell != null) {
      {
        CrossSellDragHandle(
          contentPadding = PaddingValues(horizontal = 16.dp),
          text = state.data?.recommendedCrossSell?.bannerText,
        )
      }
    } else {
      null
    }
  HedvigBottomSheet(
    hedvigBottomSheetState = state,
    dragHandle = dragHandle,
    content = { crossSellSheetData ->
      CrossSellsSheetContent(
        recommendedCrossSell = crossSellSheetData.recommendedCrossSell,
        otherCrossSells = crossSellSheetData.otherCrossSells,
        onCrossSellClick = onCrossSellClick,
        dismissSheet = { state.dismiss() },
        imageLoader,
      )
    },
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun CrossSellsSheetContent(
  recommendedCrossSell: RecommendedCrossSell?,
  otherCrossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  dismissSheet: () -> Unit,
  imageLoader: ImageLoader,
) {
  Column {
    Column(
      verticalArrangement = Arrangement.spacedBy(40.dp),
      modifier = Modifier.padding(bottom = 24.dp),
    ) {
      if (recommendedCrossSell != null) {
        Column {
          Spacer(Modifier.height(48.dp))
          RecommendationSection(
            recommendedCrossSell,
            onCrossSellClick,
            dismissSheet = dismissSheet,
            imageLoader = imageLoader,
          )
        }
      }
      if (otherCrossSells.isNotEmpty()) {
        Column {
          Spacer(Modifier.height(24.dp))
          HedvigText(stringResource(R.string.CROSS_SELL_SUBTITLE), Modifier.semantics { heading() })
          Spacer(Modifier.height(24.dp))
          CrossSellsSection(
            crossSells = otherCrossSells,
            onCrossSellClick = onCrossSellClick,
            withSubHeader = false,
            onSheetDismissed = dismissSheet,
            imageLoader = imageLoader,
          )
        }
      }
    }
    HedvigButton(
      text = stringResource(R.string.general_close_button),
      onClick = dismissSheet,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun CrossSellsFloatingSheetContent(
  recommendedCrossSell: RecommendedCrossSell?,
  otherCrossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  dismissSheet: () -> Unit,
  imageLoader: ImageLoader,
) {
  Column(
    Modifier.padding(horizontal = 16.dp),
  ) {
    Surface(
      shape = HedvigTheme.shapes.cornerXLargeBottom,
      modifier = Modifier.weight(1f, fill = false),
    ) {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp)
          .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
      ) {
        if (recommendedCrossSell != null) {
          Column {
            Spacer(Modifier.height(48.dp))
            RecommendationSection(
              recommendedCrossSell,
              onCrossSellClick,
              dismissSheet = dismissSheet,
              imageLoader = imageLoader,
            )
          }
        }
        if (otherCrossSells.isNotEmpty()) {
          Column {
            Spacer(Modifier.height(24.dp))
            HedvigText(stringResource(R.string.CROSS_SELL_SUBTITLE), Modifier.semantics { heading() })
            Spacer(Modifier.height(24.dp))
            CrossSellsSection(
              crossSells = otherCrossSells,
              onCrossSellClick = onCrossSellClick,
              withSubHeader = false,
              onSheetDismissed = dismissSheet,
              imageLoader = rememberPreviewImageLoader(),
            )
          }
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    Surface(
      shape = HedvigTheme.shapes.cornerLarge,
    ) {
      HedvigButton(
        text = stringResource(R.string.general_close_button),
        onClick = dismissSheet,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun RecommendationSection(
  recommendedCrossSell: RecommendedCrossSell,
  onCrossSellClick: (String) -> Unit,
  dismissSheet: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    StackedPillows(recommendedCrossSell, imageLoader)
    Spacer(Modifier.height(24.dp))
    val headingDescription = stringResource(R.string.CROSS_SELL_TITLE) +
      ": ${recommendedCrossSell.crossSell.title}"
    HedvigText(
      text = recommendedCrossSell.crossSell.title,
      modifier = Modifier
        .clearAndSetSemantics {
          contentDescription = headingDescription
          heading()
        },
    )
    HedvigText(
      recommendedCrossSell.crossSell.subtitle,
      style = LocalTextStyle.current.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(48.dp))
    if (recommendedCrossSell.bundleProgress != null) {
      val stepProgressItems = getHedvigStepProgressData(recommendedCrossSell.bundleProgress)
      val dataDescription =
        stepProgressItems.joinToString(separator = "; ") { item -> "${item.title} - ${item.subtitle}" }
      val description = "$dataDescription; " +
        pluralStringResource(
          R.plurals.A11Y_NUMBER_OF_ELIGIBLE_INSURANCES,
          recommendedCrossSell.bundleProgress.numberOfEligibleContracts,
          recommendedCrossSell.bundleProgress.numberOfEligibleContracts,
        )
      HedvigStepProgress(
        steps = stepProgressItems,
        numberOfActivatedSteps = recommendedCrossSell.bundleProgress.numberOfEligibleContracts,
        modifier = Modifier.clearAndSetSemantics {
          contentDescription = description
        },
      )
      Spacer(Modifier.height(16.dp))
    }
    HedvigButton(
      text = recommendedCrossSell.buttonText,
      onClick = {
        onCrossSellClick(recommendedCrossSell.crossSell.storeUrl)
        dismissSheet()
      },
      onClickLabel = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .semantics {
          contentDescription = recommendedCrossSell.crossSell.title
          // without it really is not very clear what price do we want to show here
          // since there is a lot of things said between the heading and the button
        },
    )
    Spacer(Modifier.height(12.dp))
    HedvigText(
      text = recommendedCrossSell.buttonDescription,
      style = HedvigTheme.typography.label,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
  }
}

@Composable
private fun StackedPillows(recommendedCrossSell: RecommendedCrossSell, imageLoader: ImageLoader) {
  Row(
    horizontalArrangement = Arrangement.spacedBy((-43).dp, Alignment.CenterHorizontally),
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.semantics(mergeDescendants = true) {},
  ) {
    val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerXXLarge)
    val backgroundPillowImages = recommendedCrossSell.backgroundPillowImages
    if (backgroundPillowImages != null) {
      AsyncImage(
        model = backgroundPillowImages.first,
        contentDescription = EmptyContentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(96.dp),
      )
    }
    Box(Modifier.zIndex(1f)) {
      AsyncImage(
        model = recommendedCrossSell.crossSell.pillowImage.src,
        contentDescription = recommendedCrossSell.crossSell.pillowImage.description ?: EmptyContentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(140.dp),
      )
      if (recommendedCrossSell.discountText != null) {
        HighlightLabel(
          labelText = recommendedCrossSell.discountText,
          size = HighlightLabelDefaults.HighLightSize.Small,
          color = HighlightLabelDefaults.HighlightColor.Green(HighlightLabelDefaults.HighlightShade.LIGHT),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = 16.dp),
        )
      }
    }
    if (backgroundPillowImages != null) {
      AsyncImage(
        model = backgroundPillowImages.second,
        contentDescription = EmptyContentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(96.dp),
      )
    }
  }
}

@Composable
private fun getHedvigStepProgressData(bundleProgress: BundleProgress): List<StepProgressItem> {
  val firstStepTitle = stringResource(R.string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_ONE_INSURANCE)
  val firstStepSubtitle = stringResource(R.string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_NO_DISCOUNT)
  val stepOne = StepProgressItem(firstStepTitle, firstStepSubtitle)
  val secondStepTitle = stringResource(R.string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_TWO_INSURANCES)
  val secondStepSubtitle = stringResource(
    R.string
      .BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_CURRENT_APPLIED_DISCOUNT,
    "${bundleProgress.discountPercent}%",
  )
  val stepTwo = StepProgressItem(secondStepTitle, secondStepSubtitle)
  val thirdStepTitle = stringResource(R.string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_THREE_OR_MORE)
  val stepThree = StepProgressItem(thirdStepTitle, secondStepSubtitle)
  return listOf(stepOne, stepTwo, stepThree)
}

@Composable
fun CrossSellsSection(
  crossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  onSheetDismissed: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  withSubHeader: Boolean = true,
) {
  Column(modifier) {
    if (withSubHeader) {
      CrossSellsSubHeaderWithDivider()
    }
    for ((index, crossSell) in crossSells.withIndex()) {
      CrossSellItem(crossSell, onCrossSellClick, onSheetDismissed = onSheetDismissed, imageLoader = imageLoader)
      if (index != crossSells.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun CrossSellItemPlaceholder(imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  Column(modifier) {
    CrossSellsSubHeaderWithDivider()
    CrossSellItem(
      crossSellTitle = "HHHH",
      crossSellSubtitle = "HHHHHHHH\nHHHHHHHHHHH",
      storeUrl = "",
      crossSellImageAsset = ImageAsset("", "", ""),
      onCrossSellClick = {},
      imageLoader = imageLoader,
      isLoading = true,
      modifier = Modifier,
      onSheetDismissed = {},
    )
  }
}

@Composable
private fun CrossSellsSubHeaderWithDivider() {
  Column {
    NotificationSubheading(
      text = stringResource(R.string.insurance_tab_cross_sells_title),
      modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CrossSellItem(
  crossSell: CrossSell,
  onCrossSellClick: (String) -> Unit,
  onSheetDismissed: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  CrossSellItem(
    crossSellTitle = crossSell.title,
    crossSellSubtitle = crossSell.subtitle,
    storeUrl = crossSell.storeUrl,
    onCrossSellClick = onCrossSellClick,
    modifier = modifier,
    isLoading = false,
    imageLoader = imageLoader,
    crossSellImageAsset = crossSell.pillowImage,
    onSheetDismissed = onSheetDismissed,
  )
}

@Composable
private fun CrossSellItem(
  crossSellTitle: String,
  crossSellSubtitle: String,
  storeUrl: String,
  crossSellImageAsset: ImageAsset?,
  onCrossSellClick: (String) -> Unit,
  isLoading: Boolean,
  imageLoader: ImageLoader,
  onSheetDismissed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val description = "$crossSellTitle $crossSellSubtitle"
  Row(
    modifier = modifier
      .heightIn(64.dp)
      .semantics(true) {
        contentDescription = description
      },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    AsyncImage(
      model = crossSellImageAsset?.src,
      contentDescription = crossSellImageAsset?.description ?: EmptyContentDescription,
      placeholder = crossSellPainterFallback(),
      error = crossSellPainterFallback(),
      fallback = crossSellPainterFallback(),
      imageLoader = imageLoader,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .size(48.dp),
    )
    Spacer(Modifier.width(16.dp))
    Column(
      modifier = Modifier
        .weight(1f)
        .semantics {
          hideFromAccessibility()
        },
      verticalArrangement = Arrangement.Center,
    ) {
      HedvigText(
        text = crossSellTitle,
        style = HedvigTheme.typography.bodySmall,
        modifier = Modifier.hedvigPlaceholder(
          visible = isLoading,
          highlight = PlaceholderHighlight.shimmer(),
          shape = HedvigTheme.shapes.cornerXSmall,
        ),
      )
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = crossSellSubtitle,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
        modifier = Modifier.hedvigPlaceholder(
          visible = isLoading,
          shape = HedvigTheme.shapes.cornerSmall,
          highlight = PlaceholderHighlight.shimmer(),
        ),
      )
    }
    Spacer(Modifier.width(16.dp))
    HedvigButton(
      text = stringResource(R.string.cross_sell_get_price),
      onClick = {
        onCrossSellClick(storeUrl)
        onSheetDismissed()
      },
      onClickLabel = stringResource(R.string.TALKBACK_OPEN_EXTERNAL_LINK),
      buttonSize = ButtonDefaults.ButtonSize.Medium,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier.hedvigPlaceholder(
        visible = isLoading,
        shape = HedvigTheme.shapes.cornerSmall,
        highlight = PlaceholderHighlight.shimmer(),
      ),
      enabled = !isLoading,
    )
  }
}

@Composable
private fun NotificationSubheading(text: String, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    HedvigText(text = text)
  }
}

@Composable
private fun CrossSellDragHandle(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues? = null,
  text: String? = stringResource(R.string.CROSS_SELL_BANNER_TEXT),
) {
  val direction = LocalLayoutDirection.current
  Box(
    modifier
      .fillMaxWidth()
      .height(40.dp)
      .layout { measurable, constraints ->
        // M3 sheet does not allow us to "break out" of the content padding so we do it through a layout
        val paddingStart = contentPadding?.calculateStartPadding(direction)?.roundToPx() ?: 0
        val paddingEnd = contentPadding?.calculateEndPadding(direction)?.roundToPx() ?: 0
        val adjustedConstraints = constraints.copy(
          maxWidth = constraints.maxWidth + paddingEnd + paddingStart,
          minWidth = constraints.minWidth + paddingEnd + paddingStart,
        )
        val placeable = measurable.measure(adjustedConstraints)
        layout(placeable.width, placeable.height) {
          placeable.place(0, 0)
        }
      }
      .background(color = HedvigTheme.colorScheme.signalGreenFill),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        HedvigIcons.Campaign,
        contentDescription = EmptyContentDescription,
        tint = HedvigTheme.colorScheme.signalGreenElement,
        modifier = Modifier.size(20.dp),
      )
      if (text != null) {
        Spacer(Modifier.width(8.dp))
        HedvigText(
          text,
          fontSize = HedvigTheme.typography.label.fontSize,
          color = HedvigTheme.colorScheme.signalGreenText,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellsSheetContent(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) case: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        CrossSellsSheetContent(
          recommendedCrossSell = RecommendedCrossSell(
            crossSell = CrossSell(
              "rh",
              "Car Insurance",
              "For you and your car",
              "",
              ImageAsset("", "", ""),
            ),
            bannerText = "50% discount the first year",
            buttonText = "Explore offer",
            discountText = "-50%",
            buttonDescription = "Limited time offer",
            backgroundPillowImages = ("ds" to "ds"),
            bundleProgress = BundleProgress(1, 15),
          ).takeIf { case != TripleCase.THIRD },
          otherCrossSells = listOf(
            CrossSell(
              "rf",
              "Pet insurance",
              "For your dog or cat",
              "",
              ImageAsset("", "", ""),
            ),
          ).takeIf { case != TripleCase.FIRST }.orEmpty(),
          onCrossSellClick = {},
          dismissSheet = {},
          imageLoader = rememberPreviewImageLoader(),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellsFloatingSheetContent(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) case: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellsFloatingSheetContent(
        RecommendedCrossSell(
          crossSell = CrossSell(
            "rh",
            "Car Insurance",
            "For you and your car",
            "",
            ImageAsset("", "", ""),
          ),
          bannerText = "50% discount the first year",
          buttonText = "Explore offer",
          discountText = "-50%",
          buttonDescription = "Limited time offer",
          backgroundPillowImages = ("ds" to "ds"),
          bundleProgress = BundleProgress(1, 15),
        ).takeIf { case != TripleCase.THIRD },
        listOf(
          CrossSell(
            "id",
            "title",
            "subtitle",
            "",
            ImageAsset("", "", ""),
          ),
        ).takeIf { case != TripleCase.FIRST }.orEmpty(),
        {},
        {},
        imageLoader = rememberPreviewImageLoader(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellsSection() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellsSection(
        List(2) {
          CrossSell(
            "id",
            "title",
            "subtitle",
            "storeUrl",
            ImageAsset("", "", ""),
          )
        },
        {},
        {},
        rememberPreviewImageLoader(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellItemPlaceholder() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellItemPlaceholder(rememberPreviewImageLoader())
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellDragHandle() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellDragHandle()
    }
  }
}
