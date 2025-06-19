package com.hedvig.android.crosssells

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.CrossSell.CrossSellType.ACCIDENT
import com.hedvig.android.data.contract.CrossSell.CrossSellType.HOME
import com.hedvig.android.data.contract.android.iconRes
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Small
import com.hedvig.android.design.system.hedvig.CrossSellDragHandle
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.placeholder.fade
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.design.system.hedvig.placeholder.shimmer
import com.hedvig.android.placeholder.PlaceholderHighlight
import hedvig.resources.R

data class CrossSellSheetData(
  val recommendedCrossSell: RecommendedCrossSell?,
  val otherCrossSells: List<CrossSell>,
)

data class RecommendedCrossSell(
  val crossSell: CrossSell,
  val bannerText: String,
  val buttonText: String?,
  val discountText: String?,
)

@Composable
fun CrossSellSheet(state: HedvigBottomSheetState<CrossSellSheetData>, onCrossSellClick: (String) -> Unit) {
  val dragHandle: @Composable (() -> Unit)? =
    if (state.data?.recommendedCrossSell != null) {
      {
        CrossSellDragHandle(
          text = state.data?.recommendedCrossSell?.bannerText,
          contentPadding = PaddingValues(horizontal = 16.dp),
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
      )
    },
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CrossSellsSheetContent(
  recommendedCrossSell: RecommendedCrossSell?,
  otherCrossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  dismissSheet: () -> Unit,
) {
  if (recommendedCrossSell != null) {
    RecommendationSection(recommendedCrossSell, onCrossSellClick)
  }
  if (otherCrossSells.isNotEmpty() && recommendedCrossSell != null) {
    Spacer(Modifier.height(48.dp))
  }
  if (otherCrossSells.isNotEmpty()) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      stringResource(R.string.CROSS_SELL_SUBTITLE),
      Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(24.dp))
    CrossSellsSection(
      showNotificationBadge = false,
      crossSells = otherCrossSells,
      onCrossSellClick = onCrossSellClick,
      withSubHeader = false,
    )
  }
  Spacer(Modifier.height(24.dp))
  HedvigButton(
    text = stringResource(R.string.general_close_button),
    onClick = dismissSheet,
    enabled = true,
    buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
    modifier = Modifier.fillMaxSize(),
  )
  Spacer(Modifier.height(8.dp))
  Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
}

@Composable
private fun RecommendationSection(
  recommendedCrossSell: RecommendedCrossSell,
  onCrossSellClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    Spacer(Modifier.height(48.dp))
    Box {
      Image(
        painter = painterResource(id = recommendedCrossSell.crossSell.type.iconRes()),
        contentDescription = EmptyContentDescription,
        modifier = Modifier
          .size(140.dp),
      )
      if (recommendedCrossSell.discountText != null) {
        HighlightLabel(
          labelText = recommendedCrossSell.discountText,
          size = HighlightLabelDefaults.HighLightSize.Small,
          color = HighlightLabelDefaults.HighlightColor.Green(HighlightLabelDefaults.HighlightShade.LIGHT),
          modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp),
        )
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigText(
      text = recommendedCrossSell.crossSell.title,
    )
    HedvigText(
      recommendedCrossSell.crossSell.subtitle,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
    Spacer(Modifier.height(48.dp))
    HedvigButton(
      text = recommendedCrossSell.buttonText ?: stringResource(R.string.CROSS_SELL_BUTTON),
      onClick = {
        onCrossSellClick(recommendedCrossSell.crossSell.storeUrl)
      },
      enabled = true,
      Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(12.dp))
    val bottomLabelText = if (recommendedCrossSell.discountText != null) {
      stringResource(R.string.CROSS_SELL_LABEL_LIMITED_OFFER)
    } else {
      stringResource(R.string.CROSS_SELL_LABEL)
    }
    HedvigText(
      text = bottomLabelText,
      style = HedvigTheme.typography.finePrint,
      color = HedvigTheme.colorScheme.textSecondaryTranslucent,
    )
  }
}

@Composable
fun CrossSellsSection(
  showNotificationBadge: Boolean,
  crossSells: List<CrossSell>,
  onCrossSellClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  withSubHeader: Boolean = true,
) {
  Column(modifier) {
    if (withSubHeader) {
      CrossSellsSubHeaderWithDivider(showNotificationBadge)
    }
    for ((index, crossSell) in crossSells.withIndex()) {
      CrossSellItem(crossSell, onCrossSellClick)
      if (index != crossSells.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun CrossSellItemPlaceholder(modifier: Modifier = Modifier) {
  Column(modifier) {
    CrossSellsSubHeaderWithDivider(false)
    CrossSellItem(
      crossSellTitle = "HHHH",
      crossSellSubtitle = "HHHHHHHH\nHHHHHHHHHHH",
      storeUrl = "",
      type = CrossSell.CrossSellType.HOME,
      onCrossSellClick = {},
      isLoading = true,
      modifier = Modifier,
    )
  }
}

@Composable
private fun CrossSellsSubHeaderWithDivider(showNotificationBadge: Boolean) {
  Column {
    NotificationSubheading(
      text = stringResource(R.string.insurance_tab_cross_sells_title),
      showNotification = showNotificationBadge,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CrossSellItem(crossSell: CrossSell, onCrossSellClick: (String) -> Unit, modifier: Modifier = Modifier) {
  CrossSellItem(
    crossSellTitle = crossSell.title,
    crossSellSubtitle = crossSell.subtitle,
    storeUrl = crossSell.storeUrl,
    type = crossSell.type,
    onCrossSellClick = onCrossSellClick,
    modifier = modifier,
    isLoading = false,
  )
}

@Composable
private fun CrossSellItem(
  crossSellTitle: String,
  crossSellSubtitle: String,
  storeUrl: String,
  type: CrossSell.CrossSellType,
  onCrossSellClick: (String) -> Unit,
  isLoading: Boolean,
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
    Image(
      painter = painterResource(id = type.iconRes()),
      contentDescription = EmptyContentDescription,
      modifier = Modifier
        .size(48.dp)
        .hedvigPlaceholder(
          visible = isLoading,
          shape = HedvigTheme.shapes.cornerSmall,
          highlight = PlaceholderHighlight.fade(),
        ),
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
      },
      buttonSize = Small,
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
private fun NotificationSubheading(text: String, showNotification: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // We want the notification to stick until we leave the screen, even after we've "cleared" it.
    var stickyShowNotification by remember { mutableStateOf(showNotification) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
          stickyShowNotification = false
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
    AnimatedVisibility(stickyShowNotification) {
      Row {
        Canvas(Modifier.size(8.dp)) {
          drawCircle(Color.Red)
        }
        Spacer(Modifier.width(8.dp))
      }
    }
    HedvigText(text = text)
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellsSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column {
        CrossSellsSheetContent(
          recommendedCrossSell =
            RecommendedCrossSell(
              crossSell = CrossSell(
                "rh",
                "Car Insurance",
                "For you and your car",
                "",
                CrossSell.CrossSellType.CAR,
              ),
              bannerText = "50% discount the first year",
              buttonText = "Explore offer",
              discountText = "-50%",
            ),
          otherCrossSells = listOf(
            CrossSell(
              "rf",
              "Pet insurance",
              "For your dog or cat",
              "",
              ACCIDENT,
            ),
          ),
          onCrossSellClick = {},
          dismissSheet = {},
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
        true,
        List(2) { CrossSell("id", "title", "subtitle", "storeUrl", HOME) },
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellItemPlaceholder() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CrossSellItemPlaceholder()
    }
  }
}
