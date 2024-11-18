package com.hedvig.android.feature.home.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastSumBy
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import kotlin.math.max

@Composable
internal fun HomeLayout(
  fullScreenSize: IntSize,
  welcomeMessage: @Composable @UiComposable () -> Unit,
  claimStatusCards: @Composable @UiComposable () -> Unit,
  veryImportantMessages: @Composable @UiComposable () -> Unit,
  memberReminderCards: @Composable @UiComposable () -> Unit,
  startClaimButton: @Composable @UiComposable () -> Unit,
  helpCenterButton: @Composable @UiComposable () -> Unit,
  topSpacer: @Composable @UiComposable () -> Unit,
  bottomSpacer: @Composable @UiComposable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Layout(
    content = {
      Box(Modifier.layoutId(HomeLayoutContent.WelcomeMessage)) { welcomeMessage() }
      Box(Modifier.layoutId(HomeLayoutContent.ClaimStatusCards)) { claimStatusCards() }
      Box(Modifier.layoutId(HomeLayoutContent.VeryImportantMessages)) { veryImportantMessages() }
      Box(Modifier.layoutId(HomeLayoutContent.MemberReminderCards)) { memberReminderCards() }
      Box(Modifier.layoutId(HomeLayoutContent.StartClaimButton)) { startClaimButton() }
      Box(Modifier.layoutId(HomeLayoutContent.HelpCenterButton)) { helpCenterButton() }
      Box(Modifier.layoutId(HomeLayoutContent.TopSpacer)) { topSpacer() }
      Box(Modifier.layoutId(HomeLayoutContent.BottomSpacer)) { bottomSpacer() }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val topSpacerPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.TopSpacer }!!.measure(constraints)
    val bottomSpacerPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.BottomSpacer }!!.measure(constraints)

    val welcomeMessagePlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.WelcomeMessage }!!.measure(constraints)
    val claimStatusCardsPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.ClaimStatusCards }!!.measure(constraints)
    val veryImportantMessagesPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.VeryImportantMessages }!!.measure(constraints)
    val memberReminderCardsPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.MemberReminderCards }!!.measure(constraints)
    val startClaimButtonPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.StartClaimButton }!!.measure(constraints)
    val helpCenterButtonPlaceable: Placeable =
      measurables.fastFirstOrNull { it.layoutId == HomeLayoutContent.HelpCenterButton }!!.measure(constraints)

    val centerPlaceables = buildList {
      add(welcomeMessagePlaceable)
      if (claimStatusCardsPlaceable.height > 0) {
        add(FixedSizePlaceable(0, 24.dp.roundToPx()))
        add(claimStatusCardsPlaceable)
      }
    }

    val bottomAttachedPlaceables = buildList {
      add(FixedSizePlaceable(0, 16.dp.roundToPx()))
      val doPlaceVeryImportantMessages = veryImportantMessagesPlaceable.height > 0
      val doPlaceMemberReminderCards = memberReminderCardsPlaceable.height > 0

      if (doPlaceVeryImportantMessages) {
        add(veryImportantMessagesPlaceable)
      }
      if (doPlaceVeryImportantMessages && doPlaceMemberReminderCards) {
        add(FixedSizePlaceable(0, 8.dp.roundToPx()))
      }
      if (doPlaceMemberReminderCards) {
        add(memberReminderCardsPlaceable)
      }
      if (doPlaceVeryImportantMessages || doPlaceMemberReminderCards) {
        add(FixedSizePlaceable(0, 16.dp.roundToPx()))
      }
      add(startClaimButtonPlaceable)
      if (!helpCenterButtonPlaceable.isEmpty()) {
        add(FixedSizePlaceable(0, 8.dp.roundToPx()))
        add(helpCenterButtonPlaceable)
      }
      add(bottomSpacerPlaceable)
    }

    val topSpacerHeight = topSpacerPlaceable.height
    val centerPlaceablesSumHeight = centerPlaceables.fastSumBy { it.height }
    val bottomAttachedPlaceablesSumHeight = bottomAttachedPlaceables.fastSumBy { it.height }

    val contentHeight: Int = topSpacerHeight + centerPlaceablesSumHeight + bottomAttachedPlaceablesSumHeight

    val layoutHeight = max(contentHeight, fullScreenSize.height)
    layout(constraints.maxWidth, layoutHeight) {
      topSpacerPlaceable.place(0, 0)
      if (contentHeight >= fullScreenSize.height) {
        // If we're exceeding the available space, simply lay everything out as a column would
        placeAsColumn(
          placeables = centerPlaceables + bottomAttachedPlaceables,
          startYPosition = topSpacerHeight,
        )
      } else {
        // Here we got enough space to not need to scroll
        val layoutCenterYPoint = fullScreenSize.height / 2
        // The end Y position if the center items were to be centered properly, aka where they end
        val centeredPlacablesPreferredBottomYPosition = layoutCenterYPoint + (centerPlaceablesSumHeight / 2)
        // The top Y position for thebottom attached items, aka where they start
        val bottomAttachedPlaceablesTopYPosition = fullScreenSize.height - bottomAttachedPlaceablesSumHeight
        if (centeredPlacablesPreferredBottomYPosition < bottomAttachedPlaceablesTopYPosition) {
          // Happy path, we can center the content and have space for bottom attached too
          placeAsColumn(bottomAttachedPlaceables, bottomAttachedPlaceablesTopYPosition)
          placeAsColumn(centerPlaceables, centeredPlacablesPreferredBottomYPosition - centerPlaceablesSumHeight, true)
        } else {
          // Here we need to move the "centered" content up as much as the bottom attached content needs to push it
          placeAsColumn(
            placeables = centerPlaceables + bottomAttachedPlaceables,
            startYPosition = bottomAttachedPlaceablesTopYPosition - centerPlaceablesSumHeight,
            reverseOrderOfZIndex = true,
          )
        }
      }
    }
  }
}

private fun Placeable.isEmpty(): Boolean = width == 0 && height == 0

/**
 * [reverseOrderOfZIndex] lays out items from top to bottom, so if they overlap the top ones show above the bottom ones
 * This is useful for the centered content, so that the text stays above the card content when they are overlapping
 * during an animation
 */
private fun Placeable.PlacementScope.placeAsColumn(
  placeables: List<Placeable>,
  startYPosition: Int,
  reverseOrderOfZIndex: Boolean = false,
) {
  var yPosition = startYPosition
  placeables.fastForEachIndexed { index, placeable ->
    val zIndex = if (reverseOrderOfZIndex) 1000 - index.toFloat() else 0f
    placeable.place(0, yPosition, zIndex)
    yPosition += placeable.height
  }
}

private enum class HomeLayoutContent {
  WelcomeMessage,
  ClaimStatusCards,
  MemberReminderCards,
  StartClaimButton,
  HelpCenterButton,
  VeryImportantMessages,
  TopSpacer,
  BottomSpacer,
}

/**
 * A [Placeable] to be used inside custom [androidx.compose.ui.layout.Layout] if we need to add some spacing manually.
 */
private class FixedSizePlaceable(width: Int, height: Int) : Placeable() {
  init {
    measuredSize = IntSize(width, height)
  }

  override fun get(alignmentLine: AlignmentLine): Int = AlignmentLine.Unspecified

  override fun placeAt(position: IntOffset, zIndex: Float, layerBlock: (GraphicsLayerScope.() -> Unit)?) = Unit
}

// region previews
@Preview(showSystemUi = true)
@Composable
private fun PreviewHomeLayoutCenteredContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints {
        PreviewHomeLayout(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight,
          claimStatusCards = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              PreviewBox { HedvigText("claim status card") }
            }
          },
        )
      }
    }
  }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewHomeLayoutCenteredContentWithSomeBottomAttachedContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints {
        PreviewHomeLayout(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight,
          memberReminderCards = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              repeat(1) { index ->
                PreviewBox(index)
              }
            }
          },
        )
      }
    }
  }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewHomeLayoutNonCenteredNonScrollableContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints {
        PreviewHomeLayout(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight,
          veryImportantMessages = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              PreviewBox(0) { HedvigText("Important message") }
            }
          },
          memberReminderCards = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              repeat(3) { index ->
                PreviewBox(index)
              }
            }
          },
        )
      }
    }
  }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewHomeLayoutScrollingContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints {
        PreviewHomeLayout(
          maxWidth = constraints.maxWidth,
          maxHeight = constraints.maxHeight,
          claimStatusCards = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              PreviewBox { HedvigText("claim status card") }
            }
          },
          memberReminderCards = {
            Column(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(8.dp)) {
              repeat(3) { index ->
                PreviewBox(index)
              }
            }
          },
        )
      }
    }
  }
}

@Composable
private fun PreviewHomeLayout(
  maxWidth: Int,
  maxHeight: Int,
  modifier: Modifier = Modifier,
  claimStatusCards: @Composable @UiComposable () -> Unit = {},
  veryImportantMessages: @Composable @UiComposable () -> Unit = {},
  memberReminderCards: @Composable @UiComposable () -> Unit = {},
) {
  HomeLayout(
    fullScreenSize = IntSize(maxWidth, maxHeight),
    welcomeMessage = {
      HedvigText(
        "Welcome!",
        Modifier
          .fillMaxWidth()
          .wrapContentWidth(Alignment.CenterHorizontally),
      )
    },
    claimStatusCards = claimStatusCards,
    veryImportantMessages = veryImportantMessages,
    memberReminderCards = memberReminderCards,
    startClaimButton = {
      HedvigButton(
        text = "Start claim",
        onClick = {},
        enabled = true,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      )
    },
    helpCenterButton = {
      HedvigTextButton(
        text = "Other services",
        onClick = {},
        buttonSize = Large,
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
      )
    },
    topSpacer = {
      Spacer(
        Modifier
          .height(64.dp)
          .windowInsetsTopHeight(WindowInsets.safeDrawing),
      )
    },
    bottomSpacer = {
      Spacer(
        Modifier
          .height(16.dp)
          .windowInsetsBottomHeight(WindowInsets.safeDrawing),
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun PreviewBox(index: Int = 0, content: @Composable () -> Unit = {}) {
  Box(
    Modifier
      .fillMaxWidth()
      .height(80.dp)
      .background(Color(0xFF0066FF + (index * 0xFF001100))),
  ) {
    content()
  }
}
// endregion
