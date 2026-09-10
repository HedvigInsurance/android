package com.hedvig.android.crosssells

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
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
import arrow.core.nonEmptyListOf
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.design.system.hedvig.BottomSheetStyle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigStepProgress
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighLightSize.Small
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightColor.Green
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults.HighlightShade
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.StepProgressItem
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.Campaign
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.placeholder.crossSellPainterFallback
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.A11Y_NUMBER_OF_ELIGIBLE_INSURANCES
import hedvig.resources.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_CURRENT_APPLIED_DISCOUNT
import hedvig.resources.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_NO_DISCOUNT
import hedvig.resources.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_ONE_INSURANCE
import hedvig.resources.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_THREE_OR_MORE
import hedvig.resources.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_TWO_INSURANCES
import hedvig.resources.CROSS_SELL_BANNER_TEXT
import hedvig.resources.CROSS_SELL_SUBTITLE
import hedvig.resources.CROSS_SELL_TITLE
import hedvig.resources.INSURANCE_ADDONS_SUBHEADING
import hedvig.resources.Res
import hedvig.resources.Res.plurals
import hedvig.resources.Res.string
import hedvig.resources.TALKBACK_OPEN_EXTERNAL_LINK
import hedvig.resources.cross_sell_see_price
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

data class CrossSellSheetData(
  val recommendedCrossSell: RecommendedCrossSell?,
  val otherCrossSells: List<CrossSell>,
  val recommendedAddon: RecommendedAddon?,
  val addons: List<AddonBannerInfo> = emptyList(),
) {
  val isEmpty: Boolean
    get() = recommendedCrossSell == null &&
      recommendedAddon == null &&
      otherCrossSells.isEmpty() &&
      addons.isEmpty()
}

data class RecommendedAddon(
  val id: String,
  val title: String,
  val buttonText: String,
  val description: String,
  val deepLink: String,
  val bannerText: String?,
  val benefits: List<String>,
  val pillowImageSmall: String,
  val pillowImageLarge: String,
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

@Composable
fun CrossSellBottomSheet(
  state: HedvigBottomSheetState<CrossSellSheetData>,
  onCrossSellClick: (String) -> Unit,
  onAddonClick: (eligibleInsuranceIds: List<String>) -> Unit,
  imageLoader: ImageLoader,
) {
  val dragHandle: @Composable (() -> Unit)? =
    if (state.data?.recommendedCrossSell != null || state.data?.recommendedAddon != null) {
      {
        CrossSellDragHandle(
          contentPadding = PaddingValues(horizontal = 16.dp),
          text = state.data?.recommendedCrossSell?.bannerText
            ?: state.data?.recommendedAddon?.bannerText
            ?: stringResource(string.CROSS_SELL_BANNER_TEXT),
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
        recommendedAddon = crossSellSheetData.recommendedAddon,
        addons = crossSellSheetData.addons,
        onCrossSellClick = onCrossSellClick,
        onAddonClick = onAddonClick,
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
  recommendedAddon: RecommendedAddon?,
  addons: List<AddonBannerInfo>,
  onCrossSellClick: (String) -> Unit,
  onAddonClick: (eligibleInsuranceIds: List<String>) -> Unit,
  dismissSheet: () -> Unit,
  imageLoader: ImageLoader,
) {
  Column {
    Column(
      verticalArrangement = Arrangement.spacedBy(40.dp),
      modifier = Modifier.padding(bottom = 24.dp),
    ) {
      if (recommendedAddon != null) {
        Column {
          Spacer(Modifier.height(48.dp))
          AddonRecommendationSection(
            recommendedAddon,
            onButtonClick = onCrossSellClick,
            dismissSheet = dismissSheet,
            imageLoader = imageLoader,
          )
        }
      } else if (recommendedCrossSell != null) {
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
          HedvigText(stringResource(string.CROSS_SELL_TITLE), Modifier.semantics { heading() })
          HedvigText(
            text = stringResource(string.CROSS_SELL_SUBTITLE),
            color = HedvigTheme.colorScheme.textSecondary,
          )
          Spacer(Modifier.height(24.dp))
          CrossSellsSection(
            crossSells = otherCrossSells,
            onCrossSellClick = onCrossSellClick,
            onSheetDismissed = dismissSheet,
            imageLoader = imageLoader,
            buttonSize = ButtonSize.Small,
          )
        }
      }
      if (addons.isNotEmpty()) {
        Column {
          Spacer(Modifier.height(24.dp))
          AddonsSection(
            addons = addons,
            onAddonClick = { eligibleInsuranceIds ->
              onAddonClick(eligibleInsuranceIds)
              dismissSheet()
            },
            imageLoader = imageLoader,
          )
        }
      }
    }
    HedvigButton(
      text = stringResource(string.general_close_button),
      onClick = dismissSheet,
      enabled = true,
      buttonStyle = ButtonStyle.Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun AddonRecommendationSection(
  recommendedAddon: RecommendedAddon,
  onButtonClick: (String) -> Unit,
  dismissSheet: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    val placeholder = crossSellPainterFallback(shape = HedvigTheme.shapes.cornerXXLarge)
    Box {
      AsyncImage(
        model = recommendedAddon.pillowImageLarge,
        contentDescription = EmptyContentDescription,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .size(140.dp),
      )
      Row(
        Modifier
          .align(Alignment.TopEnd)
          .padding(top = 8.dp),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .hedvigDropShadow(CircleShape)
            .size(30.dp)
            .background(HedvigTheme.colorScheme.fillNegative, CircleShape)
            .border(1.dp, HedvigTheme.colorScheme.borderPrimary, CircleShape),
        ) {
          Icon(
            imageVector = HedvigIcons.Plus,
            contentDescription = EmptyContentDescription,
            tint = HedvigTheme.colorScheme.fillPrimary,
            modifier = Modifier.size(24.dp),
          )
        }
        Spacer(Modifier.width(12.dp))
      }
    }
    Spacer(Modifier.height(24.dp))
    val headingDescription = stringResource(string.CROSS_SELL_TITLE) +
      ": ${recommendedAddon.title}"
    HedvigText(
      text = recommendedAddon.title,
      modifier = Modifier
        .clearAndSetSemantics {
          contentDescription = headingDescription
          heading()
        },
    )
    HedvigText(
      recommendedAddon.description,
      style = LocalTextStyle.current.copy(
        lineBreak = LineBreak.Heading,
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
      textAlign = TextAlign.Center,
    )
    if (recommendedAddon.benefits.isNotEmpty()) {
      Spacer(Modifier.height(32.dp))
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp),
      ) {
        for (benefit in recommendedAddon.benefits) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = HedvigIcons.Checkmark,
              contentDescription = EmptyContentDescription,
              modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            HedvigText(benefit)
          }
        }
      }
    }
    Spacer(Modifier.height(if (recommendedAddon.benefits.isEmpty()) 48.dp else 32.dp))
    HedvigButton(
      text = recommendedAddon.buttonText,
      onClick = {
        onButtonClick(recommendedAddon.deepLink)
        dismissSheet()
      },
      enabled = true,
      modifier = Modifier
        .fillMaxWidth(),
    )
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
    val headingDescription = stringResource(string.CROSS_SELL_TITLE) +
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
          plurals.A11Y_NUMBER_OF_ELIGIBLE_INSURANCES,
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
      onClickLabel = stringResource(string.TALKBACK_OPEN_EXTERNAL_LINK),
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
        model = recommendedCrossSell.crossSell.pillowImageLarge.src,
        contentDescription = recommendedCrossSell.crossSell.pillowImageLarge.description
          ?: EmptyContentDescription,
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
          size = Small,
          color = Green(HighlightShade.LIGHT),
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
  val firstStepTitle = stringResource(string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_ONE_INSURANCE)
  val firstStepSubtitle = stringResource(string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_NO_DISCOUNT)
  val stepOne = StepProgressItem(firstStepTitle, firstStepSubtitle)
  val secondStepTitle = stringResource(string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_TWO_INSURANCES)
  val secondStepSubtitle = stringResource(
    string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_SUBTITLE_CURRENT_APPLIED_DISCOUNT,
    "${bundleProgress.discountPercent}%",
  )
  val stepTwo = StepProgressItem(secondStepTitle, secondStepSubtitle)
  val thirdStepTitle = stringResource(string.BUNDLE_DISCOUNT_PROGRESS_SEGMENT_TITLE_THREE_OR_MORE)
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
  buttonSize: ButtonSize = ButtonSize.Medium,
) {
  Column(modifier) {
    for ((index, crossSell) in crossSells.withIndex()) {
      CrossSellItem(
        crossSell,
        onCrossSellClick,
        onSheetDismissed = onSheetDismissed,
        imageLoader = imageLoader,
        buttonSize = buttonSize,
      )
      if (index != crossSells.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun CrossSellItemPlaceholder(imageLoader: ImageLoader, modifier: Modifier = Modifier) {
  Column(modifier) {
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
      buttonText = "button",
    )
  }
}

@Composable
private fun CrossSellItem(
  crossSell: CrossSell,
  onCrossSellClick: (String) -> Unit,
  onSheetDismissed: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  buttonSize: ButtonSize = ButtonSize.Medium,
) {
  CrossSellItem(
    crossSellTitle = crossSell.title,
    crossSellSubtitle = crossSell.subtitle,
    storeUrl = crossSell.storeUrl,
    onCrossSellClick = onCrossSellClick,
    modifier = modifier,
    isLoading = false,
    imageLoader = imageLoader,
    crossSellImageAsset = crossSell.pillowImageSmall,
    onSheetDismissed = onSheetDismissed,
    buttonSize = buttonSize,
    buttonText = crossSell.buttonText,
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
  buttonText: String,
  modifier: Modifier = Modifier,
  buttonSize: ButtonSize = ButtonSize.Medium,
) {
  PillowRow(
    title = crossSellTitle,
    subtitle = crossSellSubtitle,
    pillowImage = crossSellImageAsset,
    buttonText = buttonText,
    onButtonClick = {
      onCrossSellClick(storeUrl)
      onSheetDismissed()
    },
    imageLoader = imageLoader,
    onButtonClickLabel = stringResource(string.TALKBACK_OPEN_EXTERNAL_LINK),
    isLoading = isLoading,
    modifier = modifier,
    buttonSize = buttonSize,
  )
}

/**
 * A pillow icon, a title/subtitle, and a trailing button in one row. The shape shared by the cross-sell
 * list and the home addons list. [pillowImage] is drawn at 48.dp, so pass the small pillow variant; it falls
 * back to a generic pillow placeholder when null.
 */
@Composable
fun PillowRow(
  title: String,
  subtitle: String,
  pillowImage: ImageAsset?,
  buttonText: String,
  onButtonClick: () -> Unit,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  onButtonClickLabel: String? = null,
  buttonStyle: ButtonStyle = ButtonStyle.Secondary,
  isLoading: Boolean = false,
  buttonSize: ButtonSize = ButtonSize.Medium,
  pillow: (@Composable () -> Unit)? = null,
) {
  val description = "$title $subtitle"
  Row(
    modifier = modifier
      .heightIn(64.dp)
      .semantics(true) {
        contentDescription = description
      },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (pillow != null) {
      pillow()
    } else {
      AsyncImage(
        model = pillowImage?.src,
        contentDescription = pillowImage?.description ?: EmptyContentDescription,
        placeholder = crossSellPainterFallback(),
        error = crossSellPainterFallback(),
        fallback = crossSellPainterFallback(),
        imageLoader = imageLoader,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(48.dp),
      )
    }
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
        text = title,
        style = HedvigTheme.typography.bodySmall,
        modifier = Modifier.hedvigPlaceholder(
          visible = isLoading,
          highlight = PlaceholderHighlight.shimmer(),
          shape = HedvigTheme.shapes.cornerXSmall,
        ),
      )
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = subtitle,
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
      text = buttonText,
      onClick = onButtonClick,
      onClickLabel = onButtonClickLabel,
      buttonSize = buttonSize,
      buttonStyle = buttonStyle,
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
  text: String? = stringResource(string.CROSS_SELL_BANNER_TEXT),
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
              ImageAsset("", "", ""),
              buttonText = "button",
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
              ImageAsset("", "", ""),
              buttonText = "button",
            ),
          ).takeIf { case != TripleCase.FIRST }.orEmpty(),
          addons = listOf(
            AddonBannerInfo(
              title = "Car Plus",
              description = "Extended car coverage",
              labels = listOf(),
              eligibleInsurancesIds = nonEmptyListOf("id"),
              flowType = FlowType.APP_CAR_PLUS,
            ),
          ).takeIf { case != TripleCase.FIRST }.orEmpty(),
          onCrossSellClick = {},
          onAddonClick = {},
          dismissSheet = {},
          imageLoader = rememberPreviewImageLoader(),
          recommendedAddon = null,
        )
      }
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
            ImageAsset("", "", ""),
            buttonText = "button",
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

@HedvigPreview
@Composable
private fun PreviewRecommendedAddon(
  @PreviewParameter(TripleBooleanCollectionPreviewParameterProvider::class) case: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellsSheetContent(
        imageLoader = rememberPreviewImageLoader(),
        recommendedCrossSell = null,
        otherCrossSells = emptyList(),
        recommendedAddon = RecommendedAddon(
          id = "ifsf",
          title = "Addon title",
          buttonText = "Check the addon",
          description = "Best addon in the world",
          deepLink = "deep",
          bannerText = "Add extra safety when traveling",
          benefits = listOf(
            "Travel up to 60 days in a row",
            "Delayed bags and flights covered",
            "Applies to your whole household",
          ),
          pillowImageSmall = "src",
          pillowImageLarge = "src",
        ),
        addons = emptyList(),
        onCrossSellClick = {},
        onAddonClick = {},
        dismissSheet = {},
      )
    }
  }
}
