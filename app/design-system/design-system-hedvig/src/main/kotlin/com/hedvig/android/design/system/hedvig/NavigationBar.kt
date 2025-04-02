package com.hedvig.android.design.system.hedvig

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Density
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.icon.ForeverFilled
import com.hedvig.android.design.system.hedvig.icon.ForeverOutline
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.PaymentFilled
import com.hedvig.android.design.system.hedvig.icon.PaymentOutline
import com.hedvig.android.design.system.hedvig.icon.ProfileFilled
import com.hedvig.android.design.system.hedvig.icon.ProfileOutline
import com.hedvig.android.design.system.hedvig.icon.ShieldFilled
import com.hedvig.android.design.system.hedvig.icon.ShieldOutline
import com.hedvig.android.design.system.hedvig.internal.MappedInteractionSource
import com.hedvig.android.design.system.hedvig.tokens.NavigationBarTokens
import com.hedvig.android.design.system.hedvig.tokens.NavigationRailTokens
import com.hedvig.android.design.system.hedvig.tokens.NavigationTokens
import com.hedvig.android.navigation.core.TopLevelGraph
import hedvig.resources.R

@Composable
fun NavigationBar(
  destinations: Set<TopLevelGraph>,
  destinationsWithNotifications: Set<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  getIsCurrentlySelected: (TopLevelGraph) -> Boolean,
  modifier: Modifier = Modifier,
) {
  val borderColor = NavigationTokens.BorderColor.value
  NavigationContainer(modifier) {
    Row(
      modifier = Modifier
        .drawWithContent {
          drawContent()
          drawLine(
            color = borderColor,
            start = Offset.Zero,
            end = Offset(size.width, 0f),
          )
        }
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
    ) {
      for (destination in destinations) {
        val name = stringResource(destination.titleTextId())
        val selected = getIsCurrentlySelected(destination)
        NavigationItem(
          icon = if (selected) {
            destination.selectedIcon()
          } else {
            destination.unselectedIcon()
          },
          text = name,
          withNotification = destination in destinationsWithNotifications,
          selected = selected,
          onClick = { onNavigateToDestination(destination) },
          itemPaddings = PaddingValues(
            start = NavigationBarTokens.ItemHorizontalPadding,
            end = NavigationBarTokens.ItemHorizontalPadding,
            top = NavigationBarTokens.ItemTopPadding,
            bottom = NavigationBarTokens.ItemBottomPadding,
          ),
          modifier = Modifier.weight(1f)
            .semantics {
              role = Role.Tab
              this.selected = selected
            },
        )
      }
    }
  }
}

@Composable
fun NavigationRail(
  destinations: Set<TopLevelGraph>,
  destinationsWithNotifications: Set<TopLevelGraph>,
  onNavigateToDestination: (TopLevelGraph) -> Unit,
  getIsCurrentlySelected: (TopLevelGraph) -> Boolean,
  isExtraTall: Boolean,
  modifier: Modifier = Modifier,
) {
  val borderColor = NavigationTokens.BorderColor.value
  NavigationContainer(modifier.fillMaxHeight()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = if (isExtraTall) {
        Arrangement.Center
      } else {
        Arrangement.Top
      },
      modifier = Modifier
        .drawWithContent {
          drawContent()
          drawLine(
            color = borderColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, size.height),
          )
        }
        .windowInsetsPadding(
          WindowInsets.systemBars
            .union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Start + WindowInsetsSides.Vertical),
        )
        .then(
          if (isExtraTall) {
            Modifier
          } else {
            Modifier.padding(top = NavigationRailTokens.ContainerTopPadding)
          },
        ),
    ) {
      for (destination in destinations) {
        val selected = getIsCurrentlySelected(destination)
        NavigationItem(
          icon = if (selected) {
            destination.selectedIcon()
          } else {
            destination.unselectedIcon()
          },
          text = stringResource(destination.titleTextId()),
          withNotification = destination in destinationsWithNotifications,
          selected = selected,
          onClick = { onNavigateToDestination(destination) },
          itemPaddings = PaddingValues(
            start = NavigationRailTokens.ItemHorizontalPadding,
            end = NavigationRailTokens.ItemHorizontalPadding,
            top = NavigationRailTokens.ItemTopPadding,
            bottom = NavigationRailTokens.ItemBottomPadding,
          ),
          modifier = Modifier.semantics {
            role = Role.Tab
            this.selected = selected
          },
        )
      }
    }
  }
}

@Composable
private fun NavigationContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  val density = LocalDensity.current
  val fontCappedDensity = Density(
    density = density.density,
    fontScale = density.fontScale.coerceAtMost(NavigationTokens.NavigationBarFontScaleCap),
  )
  CompositionLocalProvider(LocalDensity provides fontCappedDensity) {
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = modifier.semantics {
        this.traversalIndex = 0f
      },
    ) {
      content()
    }
  }
}

@Composable
private fun NavigationItem(
  icon: ImageVector,
  text: String,
  withNotification: Boolean,
  selected: Boolean,
  onClick: () -> Unit,
  itemPaddings: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  var itemWidthPx by remember { mutableIntStateOf(0) }
  val deltaOffset: Offset = with(LocalDensity.current) {
    val indicatorWidth = NavigationTokens.IndicatorWidth.toPx()
    Offset((itemWidthPx - indicatorWidth).toFloat() / 2, itemPaddings.calculateTopPadding().toPx())
  }
  val offsetInteractionSource = remember(interactionSource, deltaOffset) {
    MappedInteractionSource(interactionSource, { -deltaOffset })
  }
  val indicatorShape = HedvigTheme.shapes.cornerLarge
  Column(
    modifier = modifier
      .onPlaced { itemWidthPx = it.size.width }
      .clickable(
        onClick = onClick,
        interactionSource = interactionSource,
        indication = null,
      )
      .padding(itemPaddings),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) {
          NavigationTokens.IconColor.value
        } else {
          NavigationTokens.UnselectedIconColor.value
        },
        modifier = Modifier
          .size(NavigationTokens.IconSize)
          .notificationCircle(withNotification),
      )
      Box(
        Modifier
          .matchParentSize()
          .wrapContentWidth(unbounded = true)
          .width(NavigationTokens.IndicatorWidth)
          .clip(indicatorShape)
          .indication(
            offsetInteractionSource,
            ripple(),
          ),
      )
    }
    HedvigText(
      text = text,
      style = NavigationTokens.TextStyle.value,
      color = if (selected) {
        NavigationTokens.TextColor.value
      } else {
        NavigationTokens.UnselectedTextColor.value
      },
      maxLines = 1,
      overflow = TextOverflow.Visible,
    )
  }
}

private fun TopLevelGraph.selectedIcon(): ImageVector {
  return when (this) {
    TopLevelGraph.Home -> HedvigIcons.HelipadFilled
    TopLevelGraph.Insurances -> HedvigIcons.ShieldFilled
    TopLevelGraph.Forever -> HedvigIcons.ForeverFilled
    TopLevelGraph.Payments -> HedvigIcons.PaymentFilled
    TopLevelGraph.Profile -> HedvigIcons.ProfileFilled
  }
}

private fun TopLevelGraph.unselectedIcon(): ImageVector {
  return when (this) {
    TopLevelGraph.Home -> HedvigIcons.HelipadOutline
    TopLevelGraph.Insurances -> HedvigIcons.ShieldOutline
    TopLevelGraph.Forever -> HedvigIcons.ForeverOutline
    TopLevelGraph.Payments -> HedvigIcons.PaymentOutline
    TopLevelGraph.Profile -> HedvigIcons.ProfileOutline
  }
}

private fun TopLevelGraph.titleTextId(): Int {
  return when (this) {
    TopLevelGraph.Home -> R.string.TAB_HOME_TITLE
    TopLevelGraph.Insurances -> R.string.TAB_INSURANCES_TITLE
    TopLevelGraph.Forever -> R.string.TAB_REFERRALS_TITLE
    TopLevelGraph.Payments -> R.string.TAB_PAYMENTS_TITLE
    TopLevelGraph.Profile -> R.string.TAB_PROFILE_TITLE
  }
}

@PreviewFontScale
@Preview(name = "200%", fontScale = 2f, locale = "sv")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewNavigationBar(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) selected: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationBar(
        destinations = TopLevelGraph.entries.toSet(),
        destinationsWithNotifications = TopLevelGraph.entries.take(1).toSet(),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { selected },
      )
    }
  }
}

@PreviewFontScale
@Preview(name = "200%", fontScale = 2f, locale = "sv")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun PreviewNavigationRail(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) selected: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationRail(
        destinations = TopLevelGraph.entries.toSet(),
        destinationsWithNotifications = TopLevelGraph.entries.take(1).toSet(),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { selected },
        isExtraTall = false,
      )
    }
  }
}

@Preview(device = "spec:width=1920dp,height=400dp,dpi=160")
@Composable
private fun PreviewTallNavigationRail() {
  HedvigTheme {
    Surface(Modifier.fillMaxHeight(), color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationRail(
        destinations = TopLevelGraph.entries.toSet(),
        destinationsWithNotifications = TopLevelGraph.entries.take(1).toSet(),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { false },
        isExtraTall = true,
      )
    }
  }
}

@Preview(device = "spec:width=673dp,height=400dp")
@Composable
private fun PreviewShortNavigationRail() {
  HedvigTheme {
    Surface(Modifier.fillMaxHeight(), color = HedvigTheme.colorScheme.backgroundPrimary) {
      NavigationRail(
        destinations = TopLevelGraph.entries.toSet(),
        destinationsWithNotifications = TopLevelGraph.entries.take(1).toSet(),
        onNavigateToDestination = {},
        getIsCurrentlySelected = { false },
        isExtraTall = false,
      )
    }
  }
}
