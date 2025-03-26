package com.hedvig.android.shared.tier.comparison.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTabRow
import com.hedvig.android.design.system.hedvig.HedvigTabletLandscapePreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigTabRowState
import com.hedvig.android.shared.tier.comparison.ui.ComparisonEvent.Reload
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Failure
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Loading
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel.ComparisonItem.CoveredStatus.Checkmark
import com.hedvig.android.shared.tier.comparison.ui.ComparisonState.Success.CoverageLevel.ComparisonItem.CoveredStatus.Description
import hedvig.resources.R

@Composable
fun ComparisonDestination(viewModel: ComparisonViewModel, navigateUp: () -> Unit) {
  val uiState: ComparisonState by viewModel.uiState.collectAsStateWithLifecycle()
  when (val state = uiState) {
    Loading -> {
      Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        HedvigCircularProgressIndicator()
      }
    }

    is Success -> {
      ComparisonScreen(state, navigateUp)
    }

    Failure -> {
      Box(Modifier.fillMaxSize()) {
        HedvigErrorSection(
          onButtonClick = {
            viewModel.emit(Reload)
          },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun ComparisonScreen(uiState: Success, navigateUp: () -> Unit) {
  val selectedComparisonRowBottomSheetState = rememberHedvigBottomSheetState<CoverageLevel.ComparisonItem>()
  HedvigBottomSheet(
    hedvigBottomSheetState = selectedComparisonRowBottomSheetState,
    contentPadding = PaddingValues(horizontal = 24.dp),
  ) { comparisonRow ->
    ComparisonRowBottomSheetContent(comparisonRow, { selectedComparisonRowBottomSheetState.dismiss() })
  }
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { navigateUp() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = stringResource(R.string.general_close_button),
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    FlowHeading(
      stringResource(R.string.TIER_COMPARISON_TITLE),
      stringResource(R.string.TIER_COMPARISON_SUBTITLE),
      Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    val pagerState = rememberPagerState(initialPage = uiState.initialTabIndex) { uiState.coverageLevels.size }
    val tabRowState = rememberHedvigTabRowState(pagerState)
    HedvigTabRow(
      tabRowState = tabRowState,
      tabTitles = uiState.coverageLevels.map { it.title },
      tabStyle = TabDefaults.TabStyle.Filled,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    ComparisonPager(
      coverageLevels = uiState.coverageLevels,
      pagerState = pagerState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      onCoverageClicked = { selectedComparisonRowBottomSheetState.show(it) },
      modifier = Modifier,
    )
    Spacer(Modifier.height(32.dp))
  }
}

@Composable
private fun ComparisonPager(
  coverageLevels: List<CoverageLevel>,
  pagerState: PagerState,
  contentPadding: PaddingValues,
  onCoverageClicked: (CoverageLevel.ComparisonItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalPager(
    state = pagerState,
    verticalAlignment = Alignment.Top,
    modifier = modifier,
  ) { index ->
    val coverage = coverageLevels[index]
    CoverageLevelRow(coverage, contentPadding, { onCoverageClicked(it) })
  }
}

@Composable
private fun CoverageLevelRow(
  coverage: CoverageLevel,
  contentPadding: PaddingValues,
  onCoverageClicked: (CoverageLevel.ComparisonItem) -> Unit,
) {
  Column {
    coverage.items.forEachIndexed { index, item ->
      if (index != 0) {
        HorizontalDivider(Modifier.padding(contentPadding))
      }
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          val inlinePlusIconId = "plus"
          HedvigText(
            buildAnnotatedString {
              // Add a space so that the + icon does not touch the text too much, while still having the space be taken
              // into consideration for the text layout purposes like when the line break will go
              append(item.title + " ")
              appendInlineContent(inlinePlusIconId, alternateText = "+")
            },
            style = LocalTextStyle.current.copy(
              lineBreak = LineBreak.Heading,
            ),
            inlineContent = mapOf(
              inlinePlusIconId to InlineTextContent(
                placeholder = Placeholder(
                  width = 24.sp,
                  height = 24.sp,
                  placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                ),
                children = {
                  Icon(
                    imageVector = HedvigIcons.Plus,
                    contentDescription = null,
                    tint = HedvigTheme.colorScheme.textSecondary,
                    modifier = Modifier.fillMaxSize(),
                  )
                },
              ),
            ),
          )
        },
        endSlot = {
          when (item.coveredStatus) {
            Checkmark -> {
              Icon(
                imageVector = HedvigIcons.Checkmark,
                contentDescription = null,
                modifier = with(LocalDensity.current) {
                  Modifier
                    .wrapContentSize(Alignment.CenterEnd)
                    .size(24.sp.toDp())
                },
              )
            }

            is Description -> {
              HedvigText(
                text = item.coveredStatus.description,
                color = HedvigTheme.colorScheme.textSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.wrapContentSize(Alignment.TopEnd),
              )
            }
          }
        },
        spaceBetween = 8.dp,
        modifier = Modifier
          .clickable { onCoverageClicked(item) }
          .padding(contentPadding)
          .padding(horizontal = 4.dp, vertical = 16.dp),
      )
    }
  }
}

@Composable
private fun ComparisonRowBottomSheetContent(comparisonItem: CoverageLevel.ComparisonItem, dismissSheet: () -> Unit) {
  Column {
    HedvigText(text = comparisonItem.title)
    Spacer(Modifier.height(2.dp))
    HedvigText(
      text = comparisonItem.description,
      color = HedvigTheme.colorScheme.textSecondary,
    )
    Spacer(Modifier.height(32.dp))
    HedvigButton(
      onClick = dismissSheet,
      text = stringResource(R.string.general_close_button),
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@HedvigPreview
@HedvigTabletLandscapePreview
@PreviewFontScale
@Preview
@Composable
private fun ComparisonScreenPreview() {
  HedvigTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      ComparisonScreen(
        uiState = Success(
          coverageLevels = List(4) { coverageIndex ->
            CoverageLevel(
              title = "Cover #${coverageIndex + 1}",
              items = List(8) { index ->
                CoverageLevel.ComparisonItem(
                  title = " title".repeat((index + 1) * 2).trimStart(),
                  description = "description",
                  coveredStatus = if (index % 2 == 0) Description("description") else Checkmark,
                )
              },
            )
          },
          initialTabIndex = 1,
        ),
        navigateUp = {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewComparisonRowBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ComparisonRowBottomSheetContent(
        comparisonItem = CoverageLevel.ComparisonItem(
          title = "Title",
          description = "Description eh".repeat(15),
          coveredStatus = Checkmark,
        ),
        dismissSheet = {},
      )
    }
  }
}
